import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.junit.jupiter.api.assertThrows
import org.transmission.client.SessionInfo
import org.transmission.client.Torrent
import org.transmission.client.TransmissionClient
import java.lang.System.getenv
import java.util.concurrent.TimeUnit

@TestMethodOrder(OrderAnnotation::class)
class IntegrationTest {

    private val transmissionHost = getenv("TRANSMISSION_HOST")!!
    private val transmissionUser = getenv("TRANSMISSION_USER")
    private val transmissionPassword = getenv("TRANSMISSION_PASSWORD")

    @Test
    @Order(1)
    fun `Test transmission client has configurable host`() {

        assert(TransmissionClient("http://localhost").targetUrl == "http://localhost/transmission/rpc")
        assert(TransmissionClient("http://localhost:9091").targetUrl == "http://localhost:9091/transmission/rpc")
        assert(TransmissionClient("http://localhost:9091/customContext").targetUrl == "http://localhost:9091/customContext/rpc")
        assert(TransmissionClient("http://localhost/customContext").targetUrl == "http://localhost/customContext/rpc")
        assert(TransmissionClient("http://localhost/rpc").targetUrl == "http://localhost/rpc")

    }

    @Test
    @Order(2)
    fun `Test transmission client is initialized`() {
        val testSubject = initTestSubject()
        val beforeConnecting = testSubject.isInit()
        testSubject.connect()
        assert(beforeConnecting != testSubject.isInit()) { "Client is initialized" }

    }

    @Test
    @Order(3)
    fun `Test client can get torrent list`() {
        val testSubject = initTestSubject()
        with(testSubject) {
            val torrents = getTorrents()
            assert(torrents.isNotEmpty())
            assert(getTorrents(fields = listOf("id", "activityDate"))[0].name == null)
            assert(getTorrents(ids = torrents.firstTwo().mapNotNull { it.id }, fields = listOf("all")).size == 2)
            assert(getTorrent(1).toNonNull().id.toInt() == 1)
            assert(getTorrent(1, fields = emptyList()).id?.toInt() == 1)
            assert(getTorrent(1, fields = listOf("id")).activityDate == null)
        }
    }


    @Test
    @Order(4)
    fun `Test client can start and stop torrents`() {
        val testSubject = initTestSubject()
        with(testSubject.getTorrents(listOf("id", "status", "errorString"))) {
            assertThrows<IllegalArgumentException> { this[0].toNonNull() }
            assert(this[0].name == null)
            take(5).groupBy { it.isActive() }.forEach {
                it.value.let { runningTorrents ->
                    testSubject.toggleState(runningTorrents)
                }
            }
        }

    }

    @Test
    @Order(5)
    fun `Test client can add and delete torrents`() {
        val testSubject = initTestSubject()
        val nameToTorrent = mapOf("ubuntu-19.10-desktop-amd64.iso" to "http://releases.ubuntu.com/19.10/ubuntu-19.10-desktop-amd64.iso.torrent",
                "ubuntu-19.10-live-server-amd64.iso" to "http://releases.ubuntu.com/19.10/ubuntu-19.10-live-server-amd64.iso.torrent",
                "bluestar-linux-4.6.4-desktop-2016.07.18-x86_64.iso" to "magnet:?xt=urn:btih:532b91662ce11bc040f4c467d4e0884376f1778a&dn=bluestar-linux-4.6.4-desktop-2016.07.18-x86_64.iso")
        nameToTorrent.forEach { (_, torrent) -> assert(testSubject.addTorrent(torrent)) }
        val expectedNames = listOf("ubuntu-19.10-desktop-amd64.iso", "bluestar-linux-4.6.4-desktop-2016.07.18-x86_64.iso", "ubuntu-19.10-live-server-amd64.iso")
        val torrents = testSubject.getTorrents()
        val newTorrents = torrents.filter { torrent -> expectedNames.firstOrNull { it == torrent.name } != null }

        assert(newTorrents.size == nameToTorrent.size)
        newTorrents.mapNotNull { it.id }.forEachIndexed { index, id ->
            when (index) {
                1 -> assert(testSubject.removeTorrents(listOf(id)))
                else -> assert(testSubject.removeTorrents(listOf(id), index.isEven()))
            }
        }

    }

    @Test
    @Order(6)
    fun `Test client can get session information`() {
        val testSubject = initTestSubject()
        val statistics = testSubject.getSessionStatistics()
        assert(statistics.torrentCount > 0.toBigInteger())

    }

    @Test
    @Order(7)
    fun `Test client can read and change session values`() {
        val testSubject = initTestSubject()
        val currentSessionInformation = testSubject.getSessionInformation()
        val newSessionInformation = getUpdatedSessionInformation(currentSessionInformation)
        assert(currentSessionInformation.altSpeedEnabled != null)
        testSubject.setSessionInformation(newSessionInformation)
        waitRequest()
        assert(testSubject.getSessionInformation().altSpeedEnabled == newSessionInformation.altSpeedEnabled)
        testSubject.setSessionInformation(currentSessionInformation)
    }

    @Test
    fun `Test client parse session`() {
        val testSubject = initTestSubject()
        val currentSessionInformation = testSubject.getSessionInformation(emptyList())
        assertThrows<java.lang.IllegalArgumentException> {
            currentSessionInformation.toNonNull()
        }
    }

    @Test
    fun `Test client can startNow`() {
        val testSubject = initTestSubject()
        val torrents = testSubject.getTorrents()
        val torrent = torrents.first { !it.isActive() }
        assert(testSubject.startTorrentNow(torrent.id!!))
        testSubject.stopTorrent(torrent.id!!)
        torrents.takeIf { !torrent.isActive() }?.take(5)?.map{it.id!!}.let {
            assert(testSubject.startTorrentsNow(it!!))
            assert(testSubject.stopTorrents(it!!))
        }
    }

    private fun getUpdatedSessionInformation(currentSessionInformation: SessionInfo): SessionInfo {
        return when {
            currentSessionInformation.altSpeedEnabled != null -> SessionInfo(altSpeedEnabled = currentSessionInformation.altSpeedEnabled!!.not())
            else -> SessionInfo(altSpeedEnabled = true)
        }
    }

    private fun initTestSubject() = TransmissionClient(transmissionHost, transmissionUser, transmissionPassword)


    private fun TransmissionClient.toggleState(torrents: Torrent) {
        if (torrents.isActive()) {
            torrents.id?.let {
                stopTorrent(it)
                waitRequest()
                assert(areAllStopped(listOf(it)))
                startTorrent(it)
                waitRequest()
                assert(areAllRunning(listOf(it)))
            }
        } else {
            torrents.id?.let {
                startTorrent(it)
                TimeUnit.MILLISECONDS.sleep(DELAY)
                assert(areAllRunning(listOf(it)))
                stopTorrent(it)
                waitRequest()
                assert(areAllStopped(listOf(it)))
            }
        }
    }

    private fun waitRequest() {
        TimeUnit.MILLISECONDS.sleep(DELAY)
    }


    private fun TransmissionClient.toggleState(torrents: List<Torrent>) {
        torrents.groupBy { it.isActive() }.forEach { entry ->
            val torrentIds = entry.value.mapNotNull { it.id }
            val adequateDelay = DELAY * (torrentIds.size / 100 + 1)
            if (entry.key) {
                println("Stopping ${torrentIds.size} torrent")
                stopTorrents(runningIds = torrentIds)
                TimeUnit.MILLISECONDS.sleep(adequateDelay)
                assert(areAllStopped(torrentIds))
                startTorrents(torrentIds)
                TimeUnit.MILLISECONDS.sleep(adequateDelay)
                assert(areAllRunning(torrentIds))

            } else {
                println("Starting ${torrentIds.size} torrent")
                startTorrents(torrentIds)
                TimeUnit.MILLISECONDS.sleep(adequateDelay)
                assert(areAllRunning(torrentIds))
                stopTorrents(runningIds = torrentIds)
                TimeUnit.MILLISECONDS.sleep(adequateDelay)
                assert(areAllStopped(torrentIds))

            }

        }

    }

    private fun TransmissionClient.areAllStopped(torrentIds: List<Long>) =
            getTorrents(ids = torrentIds).firstOrNull { it.isActive() } == null

    private fun TransmissionClient.areAllRunning(torrentIds: List<Long>) =
            getTorrents(ids = torrentIds).firstOrNull { !it.isActive() && !it.isError() } == null

    companion object {
        private const val DELAY: Long = 500
    }
}

private fun <E> List<E>.firstTwo(): List<E> {
    if (size < 2) throw RuntimeException("Can't get first to with $size values")
    return listOf(this[0], this[1])
}

private fun Int.isEven() = this % 2 == 0




