package org.transmission.client

internal open class TorrentRequest(method: String,
                                   private val fields: List<String> = RPC_FIELDS,
                                   private val ids: List<Int>? = null,
                                   private val id: Int? = null,
                                   tag: Int? = null) : TransmissionRequest(method, tag) {


    companion object {
        val RPC_FIELDS = listOf(
                "id",
                "activityDate",
                "addedDate",
                "bandwidthPriority",
                "comment",
                "corruptEver",
                "dateCreated",
                "doneDate",
                "downloadDir",
                "downloadedEver",
                "downloadLimit",
                "downloadLimited",
                "error",
                "errorString",
                "eta",
                "etaIdle",
                "files",
                "fileStats",
                "hashString",
                "haveUnchecked",
                "haveValid",
                "isFinished",
                "isPrivate",
                "isStalled",
                "leftUntilDone",
                "magnetLink",
                "name",
                "percentDone",
                "rateDownload",
                "rateUpload",
                "secondsDownloading",
                "secondsSeeding",
                "sizeWhenDone",
                "startDate",
                "status",
                "totalSize",
                "torrentFile",
                "uploadedEver",
                "uploadRatio",
                "files",
                "fileStats"
        )

    }

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

internal class GetTorrentRequest(fields: List<String> = RPC_FIELDS,
                                 ids: List<Int>? = null,
                                 id: Int? = null,
                                 tag: Int? = null) : TorrentRequest(Actions.TORRENT_GET, fields, ids, id, tag)

internal class StartTorrentRequest(fields: List<String> = RPC_FIELDS,
                                   ids: List<Int>? = null,
                                   id: Int? = null,
                                   tag: Int? = null) : TorrentRequest(Actions.TORRENT_START, fields, ids, id, tag)

internal class StopTorrentRequest(fields: List<String> = RPC_FIELDS,
                                  ids: List<Int>? = null,
                                  id: Int? = null,
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

internal class RemoveTorrentRequest(private val ids: List<Int>, private val deleteLocalData: Boolean = false) : TransmissionRequest(Actions.TORRENT_REMOVE, null) {
    override fun getArguments() = mutableMapOf<String, Any>().apply {
        put("delete-local-data",deleteLocalData)
        if (ids.isNotEmpty()) {
            put("ids", ids)
        }
    }

}