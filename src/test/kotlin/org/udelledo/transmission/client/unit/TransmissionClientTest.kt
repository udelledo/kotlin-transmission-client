package org.udelledo.transmission.client.unit

import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import org.junit.jupiter.api.Test
import org.udelledo.transmission.client.TransmissionClient
import org.udelledo.transmission.client.toNonNull
import java.net.HttpURLConnection

internal class TransmissionClientTest {

    private val connection = mockk<HttpURLConnection>(relaxed = true)
    private val subject = spyk(TransmissionClient("http://localhost")).apply {
        every { this@apply.getConnection() } returns connection
    }

    @Test
    fun `Test transmission client has configurable host`() {
        assert(TransmissionClient("http://localhost").targetUrl == "http://localhost/transmission/rpc")
        assert(TransmissionClient("http://localhost:9091").targetUrl == "http://localhost:9091/transmission/rpc")
        assert(TransmissionClient("http://localhost:9091/customContext").targetUrl == "http://localhost:9091/customContext/rpc")
        assert(TransmissionClient("http://localhost/customContext").targetUrl == "http://localhost/customContext/rpc")
        assert(TransmissionClient("http://localhost/rpc").targetUrl == "http://localhost/rpc")

    }

    @Test
    fun `Client refresh the CSRF token on 409 status`() {
        every { connection.responseCode } returns 409 andThen 200
        every { connection.headerFields[TransmissionClient.CSRF_HEADER] } returns listOf("testcsrfheader")
        every { connection.inputStream } returns getInputStreamFromFile("LoginRequest.json")
        every { subject.getConnection() } returns connection
        assert(!subject.isInit())
        subject.connect()
        assert(subject.isInit())
    }

    private fun getInputStreamFromFile(filename: String) =
            TransmissionClientTest::class.java.getResourceAsStream("/$filename")

    @Test
    fun `Client get all torrent with all fields`() {
        every { connection.responseCode } returns 200
        every { connection.inputStream } returns getInputStreamFromFile("GetTorrentRequest_alltorrents_allfields.json")
        assert(!subject.isInit())
        val torrents = subject.getTorrents()
        assert(torrents.isNotEmpty())
        torrents.first().toNonNull()
    }
}
