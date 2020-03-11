package org.udelledo.transmission.client

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
import java.util.*

val mapper = ObjectMapper().registerKotlinModule().apply {
    configure(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, true)
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
        with(getConnection()) {
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
            return mapper.readValue(when (responseCode) {
                200 -> parseResponse(inputStream)
                else -> parseResponse(errorStream)
            })
        }
    }

    fun getConnection() = URL(targetUrl).openConnection() as HttpURLConnection

    fun isInit() = csrfToken !== ""

    private fun HttpURLConnection.refreshCsrf() {
        csrfToken = headerFields[CSRF_HEADER]?.get(0) ?: ""
    }

    private fun parseResponse(responseStream: InputStream): String {
        return BufferedReader(InputStreamReader(responseStream)).readLines().joinToString("")
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
        return parseTorrents(GetTorrentRequest())

    }

    private fun parseTorrents(request: TorrentRequest): List<Torrent> {
        val sendPostRequest = sendPostRequest(request)
        return mapper.readValue(sendPostRequest.arguments["torrents"].toString())
    }

    private fun parseTorrentAdded(request: AddTorrentRequest): Torrent {
        val sendPostRequest = sendPostRequest(request)
        val jsonNode = sendPostRequest.arguments["torrent-added"] ?: sendPostRequest.arguments["torrent-duplicate"]
        return mapper.readValue(jsonNode.toString())
    }

    fun getTorrent(
            torrentId: Long,
            fields: List<String> = emptyList()): Torrent {
        return parseTorrents(parseTorrentRequest(torrentId, fields))[0]
    }

    private fun parseTorrentRequest(torrentId: Long? = null, fields: List<String>, ids: List<Long> = emptyList()
    ) = if (fields.isNotEmpty()) {

        when {
            (ids.isNotEmpty()) -> GetTorrentRequest(fields, ids)
            torrentId != null -> GetTorrentRequest(fields, id = torrentId)
            else -> GetTorrentRequest(fields)
        }
    } else {
        when {
            (ids.isNotEmpty()) -> GetTorrentRequest(ids = ids)
            torrentId != null -> GetTorrentRequest(id = torrentId)
            else -> GetTorrentRequest()
        }

    }

    fun getTorrents(fields: List<String> = emptyList(), ids: List<Long> = emptyList()): List<Torrent> {
        return parseTorrents(parseTorrentRequest(ids = ids, fields = fields))
    }

    fun stopTorrents(runningIds: List<Long>): Boolean {
        return sendPostRequest(StopTorrentRequest(ids = runningIds)).result == "success"
    }

    fun startTorrent(id: Long): Boolean {
        return sendPostRequest(StartTorrentRequest(id = id)).success
    }

    fun stopTorrent(id: Long): Boolean {
        return sendPostRequest(StopTorrentRequest(id = id)).success
    }

    fun startTorrents(runningIds: List<Long>): Boolean {
        return sendPostRequest(StartTorrentRequest(ids = runningIds)).success
    }

    fun addTorrent(torrentUri: String): Torrent {
        return parseTorrentAdded(AddTorrentRequest(filename = torrentUri))
    }

    fun removeTorrents(ids: List<Long>, deleteLocalData: Boolean = false): Boolean {
        return sendPostRequest(RemoveTorrentRequest(ids, deleteLocalData)).success
    }

    fun getSessionStatistics(): SessionStat {
        return mapper.readValue(sendPostRequest(SessionRequest(Actions.SESSION_STATS)).jsonString)
    }

    fun getSessionInformation(fields: List<String>? = null): SessionInfo {
        return mapper.readValue(sendPostRequest(GetSessionInformationRequest(fields)).jsonString)
    }

    fun setSessionInformation(newSessionValue: SessionInfo): Boolean {
        return sendPostRequest(SetSessionInformationRequest(newSessionValue)).success


    }

    fun startTorrentNow(id: Long): Boolean {
        return sendPostRequest(StartNowTorrentRequest(id = id)).success
    }

    fun startTorrentsNow(id: List<Long>): Boolean {
        return sendPostRequest(StartNowTorrentRequest(ids = id)).success
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
