package org.udelledo.transmission.client

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.JsonNode

abstract class TransmissionRequest(val method: String = "", val tag: Int?) {
    abstract fun getArguments(): MutableMap<String, Any>
}


data class TransmissionResponse(val result: String, val arguments: JsonNode, val tag: Int? = null) {
    @JsonIgnore
    val success = result == "success"

    @JsonIgnore
    val jsonString = arguments.toString()
}

object Actions {

    const val SESSION_SET = "session-set"
    const val SESSION_GET = "session-get"
    const val TORRENT_REMOVE = "torrent-remove"
    const val SESSION_STATS = "session-stats"
    const val TORRENT_ADD = "torrent-add"
    const val TORRENT_GET = "torrent-get"
    const val TORRENT_START = "torrent-start"
    const val TORRENT_START_NOW = "torrent-start-now"
    const val TORRENT_STOP = "torrent-stop"
    const val TORRENT_VERIFY = "torrent-verify"
    const val TORRENT_REANNOUNCE = "torrent-reannounce"
}