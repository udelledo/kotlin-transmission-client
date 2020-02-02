package org.transmission.client

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.sql.Timestamp
import java.util.Base64

val mapper = ObjectMapper().registerKotlinModule().apply {
    configure(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, true)
    configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    registerModule(SimpleModule().apply {
        addDeserializer(Timestamp::class.java, UnixTimestampDeserializer())
    })

}

class TransmissionClient(private val host: String, private val username: String = "", password: String = "") {

    private var csrfToken: String = ""
    private val encoded = Base64.getEncoder()
            .encodeToString(("$username:$password").toByteArray(StandardCharsets.UTF_8))

    val targetUrl: String
        get() {
            val url = URL(host)
            return when (host) {
                "${url.protocol}://${url.host}" -> host + DEFAULT_CONTEXT
                "${url.protocol}://${url.host}:${url.port}" -> host + DEFAULT_CONTEXT
                else -> if (host.indexOf(RPC_CONTEXT) > 0) {
                    host
                } else {
                    host + RPC_CONTEXT
                }
            }
        }

    fun connect() {
        sendPostRequest(LoginRequest())
    }

    private fun <T : TransmissionRequest> sendPostRequest(request: T): TransmissionResponse {
        with(URL(targetUrl).openConnection() as HttpURLConnection) {
            requestMethod = "POST"
            doOutput = true
            setHeaders()
            val wr = OutputStreamWriter(outputStream)
            wr.write(mapper.writeValueAsString(request))
            wr.flush()
            if (responseCode == HttpURLConnection.HTTP_CONFLICT) {
                refreshCsrf()
                return sendPostRequest(request)
            }
            val responseString: String = when (responseCode) {
                200 -> parseResponse(inputStream)
                else -> parseResponse(errorStream)
            }
            return mapper.readValue(responseString)
        }
    }

    fun isInit(): Boolean {
        return csrfToken !== ""

    }

    private fun HttpURLConnection.refreshCsrf() {
        csrfToken = headerFields[CSRF_HEADER]?.get(0) ?: ""
    }

    private fun parseResponse(responseStream: InputStream): String {
        BufferedReader(InputStreamReader(responseStream)).use {
            val response = StringBuffer()

            var inputLine = it.readLine()
            while (inputLine != null) {
                response.append(inputLine)
                inputLine = it.readLine()
            }
            it.close()
            return response.toString()
        }
    }

    private fun HttpURLConnection.setHeaders() {
        if (username != "") {
            setRequestProperty("Authorization", "Basic $encoded")
        }
        setRequestProperty("X-Transmission-Session-Id", csrfToken)
        setRequestProperty("Content-Type", "application/json")
        setRequestProperty("Accept", "application/json")

    }

    fun getTorrents(): List<Torrent> {
        return parseAllTorrents(GetTorrentRequest())

    }

    private fun parseAllTorrents(request: TorrentRequest): List<Torrent> {
        val response = sendPostRequest(request)
        return mapper.readValue(response.arguments["torrents"].toString())
    }

    fun getTorrent(
            torrentId: Int,
            fields: List<String> = emptyList()): Torrent {

        return parseAllTorrents(parseTorrentRequest(torrentId, fields))[0]
    }

    private fun parseTorrentRequest(torrentId: Int? = null, fields: List<String>, ids: List<Int>? = null
    ) = when {
        fields.isNotEmpty() -> when (torrentId) {
            null -> when {
                !ids.isNullOrEmpty() -> GetTorrentRequest(fields, ids)
                else -> GetTorrentRequest(fields)
            }
            else -> GetTorrentRequest(fields, id = torrentId)
        }
        torrentId != null -> GetTorrentRequest(id = torrentId)
        !ids.isNullOrEmpty() -> GetTorrentRequest(ids = ids)
        else -> GetTorrentRequest()
    }

    fun getTorrents(fields: List<String> = emptyList(), ids: List<Int> = emptyList()): List<Torrent> {
        return parseAllTorrents(parseTorrentRequest(ids = ids, fields = fields))
    }

    fun stopTorrents(runningIds: List<Int>): Boolean {
        return sendPostRequest(StopTorrentRequest(ids = runningIds)).result == "success"
    }

    fun startTorrent(i: Int) {
        sendPostRequest(StartTorrentRequest(id = i))
    }

    fun stopTorrent(id: Int) {
        sendPostRequest(StopTorrentRequest(id = id))
    }

    fun startTorrents(runningIds: List<Int>) {
        sendPostRequest(StartTorrentRequest(ids = runningIds))
    }

    fun addTorrent(torrentUri: String) {
        println(sendPostRequest(AddTorrentRequest(filename = torrentUri)))
    }

    fun removeTorrents(ids: List<Int>, deleteLocalData: Boolean = false) {
        println(sendPostRequest(RemoveTorrentRequest(ids, deleteLocalData)))
    }

    fun getSessionStatistics(): SessionStat {
        val response = sendPostRequest(SessionRequest(Actions.SESSION_STATS))
        return mapper.readValue(response.arguments.toString())
    }

    fun getSessionInformation(): SessionInfo {
        val response = sendPostRequest(GetSessionInformationRequest())
        println(response)
        return mapper.readValue(response.arguments.toString())
    }

    companion object {
        const val CSRF_HEADER = "X-Transmission-Session-Id"
        const val RPC_CONTEXT = "/rpc"
        const val DEFAULT_CONTEXT = "/transmission$RPC_CONTEXT"
    }

}


class LoginRequest : TransmissionRequest(Actions.SESSION_STATS, null) {
    override fun getArguments() = mutableMapOf<String, Any>()

}
