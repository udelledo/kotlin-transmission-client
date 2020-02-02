import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.transmission.client.Torrent
import org.transmission.client.TransmissionClient
import java.lang.System.getenv
import java.util.concurrent.TimeUnit

@TestMethodOrder(OrderAnnotation::class)
class IntegrationTest {
    private val DELAY: Long = 500

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
        assert(testSubject.getTorrents(ids = listOf(1, 2)).size == 2)
        assert(testSubject.getTorrent(1).id?.toInt() == 1)
        assert(testSubject.getTorrents(fields = listOf("id", "activityDate"))[0].name == null)
    }


    @Test
    @Order(4)
    fun `Test client can start and stop torrents`() {
        val testSubject = initTestSubject()
        with(testSubject.getTorrents(listOf("id", "status", "errorString"))) {
            assert(this[0].name == null)
            groupBy { it.isActive() }.forEach {
                it.value.let { runningTorrents ->
                    testSubject.toggleState(runningTorrents[0])
                    testSubject.toggleState(runningTorrents)
                }
            }
        }

    }

    @Test
    @Order(5)
    fun `Test client can add and delete torrents`() {
        val testSubject = initTestSubject()
        testSubject.addTorrent(torrentUri = "http://releases.ubuntu.com/19.10/ubuntu-19.10-desktop-amd64.iso.torrent")
        testSubject.addTorrent(torrentUri = "magnet:?xt=urn:btih:532b91662ce11bc040f4c467d4e0884376f1778a&dn=bluestar-linux-4.6.4-desktop-2016.07.18-x86_64.iso")
        val torrents = testSubject.getTorrents()
        val newTorrents = torrents.filter {
            it.name == "ubuntu-19.10-desktop-amd64.iso"
                    || it.name == "bluestar-linux-4.6.4-desktop-2016.07.18-x86_64.iso"
        }
        testSubject.removeTorrents(newTorrents.mapNotNull { it.id?.toInt() }, true)
        assert(newTorrents.size == 2)

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
        val result = testSubject.getSessionInformation()
        assert(result.altSpeedEnabled != null)
    }

    private fun initTestSubject() = TransmissionClient(transmissionHost, transmissionUser, transmissionPassword)


    private fun TransmissionClient.toggleState(torrents: Torrent) {
        if (torrents.isActive()) {
            torrents.id?.toInt()?.let {
                stopTorrent(it)
                TimeUnit.MILLISECONDS.sleep(DELAY)
                assert(areAllStopped(listOf(it)))
                startTorrent(it)
                TimeUnit.MILLISECONDS.sleep(DELAY)
                assert(areAllRunning(listOf(it)))
            }
        }
        else{
            torrents.id?.toInt()?.let {
                startTorrent(it)
                TimeUnit.MILLISECONDS.sleep(DELAY)
                assert(areAllRunning(listOf(it)))
                stopTorrent(it)
                TimeUnit.MILLISECONDS.sleep(DELAY)
                assert(areAllStopped(listOf(it)))
            }
        }
    }


    private fun TransmissionClient.toggleState(torrents: List<Torrent>) {
        torrents.groupBy { it.isActive() }.forEach { entry ->
            val torrentIds = entry.value.mapNotNull { it.id?.toInt() }
            val delay = DELAY * (torrentIds.size / 100 + 1)
            if (entry.key) {
                println("Stopping ${torrentIds.size} torrent")
                stopTorrents(runningIds = torrentIds)
                TimeUnit.MILLISECONDS.sleep(delay)
                assert(areAllStopped(torrentIds))
                startTorrents(torrentIds)
                TimeUnit.MILLISECONDS.sleep(delay)
                assert(areAllRunning(torrentIds))

            } else {
                println("Starting ${torrentIds.size} torrent")
                startTorrents(torrentIds)
                TimeUnit.MILLISECONDS.sleep(delay)
                assert(areAllRunning(torrentIds))
                stopTorrents(runningIds = torrentIds)
                TimeUnit.MILLISECONDS.sleep(delay)
                assert(areAllStopped(torrentIds))

            }

        }

    }

    private fun TransmissionClient.areAllStopped(torrentIds: List<Int>) =
            getTorrents(ids = torrentIds).firstOrNull { it.isActive() } == null

    private fun TransmissionClient.areAllRunning(torrentIds: List<Int>) =
            getTorrents(ids = torrentIds).firstOrNull { !it.isActive() && !it.isError() } == null
}




