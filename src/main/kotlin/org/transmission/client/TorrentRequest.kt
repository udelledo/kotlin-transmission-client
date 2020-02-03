package org.transmission.client

import org.transmission.client.Torrent.Companion.ALL_TORRENT_FIELDS

internal open class TorrentRequest(method: String,
                                   private val fields: List<String>,
                                   private val ids: List<Long>?,
                                   private val id: Long?,
                                   tag: Int?) : TransmissionRequest(method, tag) {

    override fun getArguments() = mutableMapOf<String, Any>("fields" to fields).apply {
        ids?.let {
            if (it.isNotEmpty())
                put("ids", it)
        }
        id?.let {
            put("ids", it)
        }
    }


}

internal class GetTorrentRequest(fields: List<String> = ALL_TORRENT_FIELDS,
                                 ids: List<Long>? = null,
                                 id: Long? = null,
                                 tag: Int? = null) : TorrentRequest(Actions.TORRENT_GET, fields, ids, id, tag)

internal class StartTorrentRequest(fields: List<String> = ALL_TORRENT_FIELDS,
                                   ids: List<Long>? = null,
                                   id: Long? = null,
                                   tag: Int? = null) : TorrentRequest(Actions.TORRENT_START, fields, ids, id, tag)

internal class StopTorrentRequest(fields: List<String> = ALL_TORRENT_FIELDS,
                                  ids: List<Long>? = null,
                                  id: Long? = null,
                                  tag: Int? = null) : TorrentRequest(Actions.TORRENT_STOP, fields, ids, id, tag)

internal class AddTorrentRequest(private val filename: String? = null, private val metainfo: String? = null) : TransmissionRequest(Actions.TORRENT_ADD, null) {
    override fun getArguments() = mutableMapOf<String, Any>().apply {
        filename?.let {
            put("filename", it)
        }
        metainfo?.let {
            put("metainfo", it)
        }

    }

}

internal class RemoveTorrentRequest(private val ids: List<Long>, private val deleteLocalData: Boolean = false) : TransmissionRequest(Actions.TORRENT_REMOVE, null) {
    override fun getArguments() = mutableMapOf<String, Any>().apply {
        put("delete-local-data", deleteLocalData)
        if (ids.isNotEmpty()) {
            put("ids", ids)
        }
    }

}