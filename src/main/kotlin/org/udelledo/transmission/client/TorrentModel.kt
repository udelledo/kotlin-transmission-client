package org.udelledo.transmission.client

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonValue
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import java.math.BigInteger
import java.sql.Timestamp

class UnixTimestampDeserializer : JsonDeserializer<Timestamp>() {
    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): Timestamp {
        val timestamp = p?.text?.trim() ?: ""
        return Timestamp((timestamp + "000").toLong())
    }

}

data class Torrent(
        val activityDate: Timestamp?,
        val addedDate: Timestamp?,
        val bandwidthPriority: Long?,
        val comment: String?,
        val corruptEver: Long?,
        val dateCreated: Timestamp?,
        val doneDate: Timestamp?,
        val downloadDir: String?,
        val downloadLimit: Long?,
        val downloadLimited: Boolean?,
        val error: Long?,
        val errorString: String?,
        val eta: Long?,
        val etaIdle: Int?,
        val files: List<File>?,
        val fileStats: List<FileStat>?,
        val hashString: String?,
        val haveUnchecked: Long?,
        val haveValid: Long?,
        val id: Long?,
        val isFinished: Boolean?,
        val isPrivate: Boolean?,
        val isStalled: Boolean?,
        val leftUntilDone: Long?,
        val magnetLink: String?,
        val name: String?,
        val percentDone: Long?,
        val rateDownload: Long?,
        val rateUpload: Long?,
        val secondsDownloading: Long?,
        val status: TorrentStatus?,
        val startDate: Timestamp?,
        val uploadRatio: Long?,
        val downloadedEver: BigInteger?,
        val secondsSeeding: BigInteger?,
        val desiredAvailable: BigInteger?,
        val sizeWhenDone: BigInteger?,
        val maxConnectedPeers: BigInteger?,
        val torrentFile: String?,
        val totalSize: Long?,
        @JsonProperty("peer-limit") val peerLimit: Long?,
        val metadataPercentComplete: Long?,
        val peers: List<Peer>?,
        val peersFrom: PeersFrom?,
        val manualAnnounceTime: Long?,
        val peersConnected: Long?,
        val peersGettingFromUs: Long?,
        val pieceCount: Long?,
        val pieceSize: Long?,
        val pieces: String?,
        val priorities: List<Long>?,
        val trackerStats: List<TrackerStat>?,
        val trackers: List<Tracker>?,
        val peersSendingToUs: Long?,
        val seedIdleLimit: Long?,
        val queuePosition: Long?,
        val seedIdleMode: Long?,
        val seedRatioLimit: Long?,
        val seedRatioMode: Long?,
        val recheckProgress: Long?,
        val uploadedEver: Long?,
        val uploadLimit: Long?,
        val webseeds: List<String>?,
        val webseedsSendingToUs: Long?,
        val uploadLimited: Boolean?,
        val wanted: List<Long>? = null
) {
    companion object {
        val ALL_TORRENT_FIELDS = listOf("activityDate",
                "addedDate",
                "bandwidthPriority",
                "comment",
                "corruptEver",
                "dateCreated",
                "desiredAvailable",
                "doneDate",
                "downloadDir",
                "downloadLimit",
                "downloadLimited",
                "downloadedEver",
                "editDate",
                "error",
                "errorString",
                "eta",
                "etaIdle",
                "fileStats",
                "files",
                "hashString",
                "haveUnchecked",
                "haveValid",
                "honorSessionLimits",
                "id",
                "isFinished",
                "isPrivate",
                "isStalled",
                "labels",
                "leftUntilDone",
                "magnetLink",
                "manualAnnounceTime",
                "maxConnectedPeers",
                "metadataPercentComplete",
                "name",
                "peer-limit",
                "peers",
                "peersConnected",
                "peersFrom",
                "peersGettingFromUs",
                "peersSendingToUs",
                "percentDone",
                "pieces",
                "pieceCount",
                "pieceSize",
                "priorities",
                "queuePosition",
                "rateDownload",
                "rateUpload",
                "recheckProgress",
                "secondsDownloading",
                "secondsSeeding",
                "seedIdleLimit",
                "seedIdleMode",
                "seedRatioLimit",
                "seedRatioMode",
                "sizeWhenDone",
                "startDate",
                "status",
                "trackers",
                "trackerStats",
                "torrentFile",
                "totalSize",
                "uploadRatio",
                "uploadLimit",
                "uploadLimited",
                "wanted",
                "webseeds",
                "webseedsSendingToUs",
                "uploadedEver")

    }

    enum class TorrentStatus(@get:JsonValue val status: Int) {
        STOPPED(0),
        CHECK_WAIT(1),
        CHECK(2),
        DOWNLOAD_WAIT(3),
        DOWNLOADING(4),
        SEED_WAIT(5),
        SEEDING(6);

        companion object {
            @JvmStatic
            @JsonCreator
            fun fromInt(status: Int): TorrentStatus {
                return values().first { it.status == status }

            }

        }
    }

    @JsonIgnore
    fun isActive() = status != TorrentStatus.STOPPED

    fun isError() = errorString != ""

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Torrent

        if (name != other.name) return false
        if (magnetLink != other.magnetLink) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + magnetLink.hashCode()
        return result
    }
}

data class File(val name: String, val length: Long, val bytesCompleted: Long)
data class FileStat(val bytesCompleted: Long, val priority: Int, val wanted: Boolean)
data class Peer(
        val address: String,
        val clientName: String,
        val clientIsChoked: Boolean,
        val clientIsInterested: Boolean,
        val flagStr: String,
        @JsonProperty("isDownloadingFrom") val downloadingFrom: Boolean,
        @JsonProperty("isEncrypted") val encrypted: Boolean,
        @JsonProperty("isIncoming") val incoming: Boolean,
        @JsonProperty("isUTP") val utp: Boolean,
        @JsonProperty("isUploadingTo") val uploadingTo: Boolean,
        val peerIsChoked: Boolean,
        val peerIsInterested: Boolean,
        val port: Long,
        val progress: Long,
        val rateToClient: Long,
        val rateToPeer: Long)

data class PeersFrom(val fromCache: Long,
                     val fromDht: Long,
                     val fromIncoming: Long,
                     val fromLpd: Long,
                     val fromLtep: Long,
                     val fromPex: Long,
                     val fromTracker: Long)

data class Tracker(val announce: String, val id: Long, val scrape: String, val tier: Long)
data class TrackerStat(val announce: String,
                       val announceState: Long,
                       val downloadCount: Long,
                       val hasAnnounced: Boolean,
                       val hasScraped: Boolean,
                       @JsonProperty("isBackup") val backup: Boolean,
                       val lastAnnounceSucceeded: Boolean,
                       val lastAnnounceTimedOut: Boolean,
                       val lastScrapeSucceeded: Boolean,
                       val host: String,
                       val scrape: String,
                       val lastAnnounceResult: String,
                       val lastScrapeResult: String,
                       val id: Long,
                       val scrapeState: Long,
                       val seederCount: Long,
                       val tier: Long,
                       val lastAnnouncePeerCount: Long,
                       val lastAnnounceStartTime: Long,
                       val lastScrapeStartTime: Long,
                       val lastScrapeTimedOut: Long,
                       val leecherCount: Long,
                       val lastAnnounceTime: Timestamp,
                       val lastScrapeTime: Timestamp,
                       val nextAnnounceTime: Timestamp,
                       val nextScrapeTime: Timestamp
)
