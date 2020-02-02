package org.transmission.client

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonValue
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import java.math.BigDecimal
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
        val bandwidthPriority: Int?,
        val comment: String?,
        val corruptEver: Int?,
        val dateCreated: Timestamp?,
        val doneDate: Timestamp?,
        val downloadDir: String?,
        val downloadLimit: Int?,
        val downloadLimited: Boolean?,
        val error: Int?,
        val errorString: String?,
        val eta: Int?,
        val etaIdle: Int?,
        val files: Array<File>?,
        val fileStats: Array<FileStat>?,
        val hashString: String?,
        val haveUnchecked: Long?,
        val haveValid: Long?,
        val id: Long?,
        val isFinished: Boolean?,
        val isPrivate: Boolean?,
        val isStalled: Boolean?,
        val lefUntilDone: Long?,
        val magnetLink: String?,
        val name: String?,
        val percentDone: Double?,
        val rateDownload: Long?,
        val rateUpload: Long?,
        val secondsDownloading: Long?,
        val status: TorrentStatus?,
        val startDate: Timestamp?,
        val uploadRatio: Long?
) {

    companion object {
        val RPC_FIELDS = arrayOf(
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
data class TRStat(val downloadedBytes: BigInteger, val filesAdded: BigInteger, val secondsActive: BigInteger, val sessionCount: BigInteger, val uploadedBytes: BigInteger)

data class SessionStat(val activeTorrentCount: BigDecimal, @JsonProperty("cumulative-stats") val cumulativeStats: TRStat, @JsonProperty("current-stats") val currentStats: TRStat,
                       val downloadSpeed: BigInteger, val pausedTorrentCount: BigInteger, val torrentCount: BigInteger, val uploadSpeed: BigInteger)

@JsonIgnoreProperties(ignoreUnknown = true)
data class SessionInfo(
        @JsonProperty("alt-speed-down")
        val altSpeedDown: BigInteger?,
        @JsonProperty("alt-speed-enabled")
        val altSpeedEnabled: Boolean?,
        @JsonProperty("alt-speed-time-begin")
        val altSpeedTimeBegin: BigInteger?,
        @JsonProperty("alt-speed-time-day")
        val altSpeedTimeDay: BigInteger?,
        @JsonProperty("alt-speed-time-enabled")
        val altSpeedTimeEnabled: Boolean?,
        @JsonProperty("alt-speed-time-end")
        val altSpeedTimeEnd: BigInteger?,
        @JsonProperty("alt-speed-up")
        val altSpeedUp: BigInteger?,
        @JsonProperty("blocklist-enabled")
        val blockListEnabled: Boolean?,
        @JsonProperty("blocklist-size")
        val blockListSize: BigInteger?,
        @JsonProperty("blocklist-url")
        val blocklistUrl: String?,
        @JsonProperty("cache-size-mb")
        val cacheSizeMb: BigInteger,
        @JsonProperty("config-dir")
        val configDir: String?)