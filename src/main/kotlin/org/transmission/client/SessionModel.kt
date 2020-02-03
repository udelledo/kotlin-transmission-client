package org.transmission.client

import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal
import java.math.BigInteger

data class SessionStat(val activeTorrentCount: BigDecimal, @JsonProperty("cumulative-stats") val cumulativeStats: TRStat, @JsonProperty("current-stats") val currentStats: TRStat,
                       val downloadSpeed: BigInteger, val pausedTorrentCount: BigInteger, val torrentCount: BigInteger, val uploadSpeed: BigInteger)

data class TRStat(val downloadedBytes: BigInteger, val filesAdded: BigInteger, val secondsActive: BigInteger, val sessionCount: BigInteger, val uploadedBytes: BigInteger)

data class SessionInfo(
        @JsonProperty("alt-speed-down") val altSpeedDown: Long? = null,
        @JsonProperty("alt-speed-enabled") val altSpeedEnabled: Boolean? = null,
        @JsonProperty("alt-speed-time-begin") val altSpeedTimeBegin: Long? = null,
        @JsonProperty("alt-speed-time-day") val altSpeedTimeDay: Long? = null,
        @JsonProperty("alt-speed-time-enabled") val altSpeedTimeEnabled: Boolean? = null,
        @JsonProperty("alt-speed-time-end") val altSpeedTimeEnd: Long? = null,
        @JsonProperty("alt-speed-up") val altSpeedUp: Long? = null,
        @JsonProperty("blocklist-enabled") val blockListEnabled: Boolean? = null,
        @JsonProperty("blocklist-size") val blockListSize: Long? = null,
        @JsonProperty("seed-queue-size") val seedQueueSize: Long? = null,
        @JsonProperty("blocklist-url") val blocklistUrl: String? = null,
        @JsonProperty("cache-size-mb") val cacheSizeMb: Long? = null,
        @JsonProperty("config-dir") val configDir: String? = null,
        val seedRatioLimited: Boolean? = null,
        val seedRatioLimit: Long? = null,
        val encryption: String? = null,
        @JsonProperty("download-queue-size") val downloadQueueSize: Long? = null,
        @JsonProperty("dht-enabled") val dhtEnabled: Boolean? = null,
        @JsonProperty("peer-limit-global") val peerLimitGlobal: Long? = null,
        @JsonProperty("peer-limit-per-torrent") val peerLimitPerTorrent: Long? = null,
        @JsonProperty("peer-port") val peerPort: Int? = null,
        @JsonProperty("peer-port-random-on-start") val peerPortRandomOnStart: Boolean? = null,
        @JsonProperty("idle-seeding-limit") val idleSeedingLimit: Long? = null,
        @JsonProperty("idle-seeding-limit-enabled") val idleSeedingLimitEnabled: Boolean? = null,
        @JsonProperty("download-dir") val downloadDir: String? = null,
        @JsonProperty("download-dir-free-space") val downloadDirFreeSpace: Long? = null,
        @JsonProperty("rpc-version") val rpcVersion: Long? = null,
        @JsonProperty("rpc-version-minimum") val rpcVersionMinimum: Long? = null,
        @JsonProperty("download-queue-enabled") val downloadQueueEnabled: Boolean? = null,
        @JsonProperty("incomplete-dir") val incompleteDir: String? = null,
        @JsonProperty("incomplete-dir-enabled") val incompleteDirEnabled: Boolean? = null,
        @JsonProperty("queue-stalled-enabled") val queueStalledEnabled: Boolean? = null,
        @JsonProperty("queue-stalled-minutes") val queueStalledMinutes: Long? = null,
        @JsonProperty("speed-limit-down") val speedLimitDown: Long? = null,
        @JsonProperty("speed-limit-down-enabled") val speedLimitDownEnabled: Boolean? = null,
        @JsonProperty("speed-limit-up") val speedLimitUp: Long? = null,
        @JsonProperty("speed-limit-up-enabled") val speedLimitUpEnabled: Boolean? = null,
        @JsonProperty("start-added-torrents") val startAddedTorrents: Boolean? = null,
        @JsonProperty("trash-original-torrent-files") val trashOriginalTorrentFiles: Boolean? = null,
        @JsonProperty("seed-queue-enabled") val seedQueueEnabled: Boolean? = null,
        val units: Units? = null,
        val version: String? = null,
        @JsonProperty("script-torrent-done-enabled") val scriptTorrentDoneEnabled: Boolean? = null,
        @JsonProperty("script-torrent-done-filename") val scriptTorrentDoneFilename: String? = null,
        @JsonProperty("rename-partial-files") val renamePartialFile: Boolean? = null,
        @JsonProperty("lpd-enabled") val ldpEnabled: Boolean? = null,
        @JsonProperty("pex-enabled") val pexEnabled: Boolean? = null,
        @JsonProperty("utp-enabled") val utpEnabled: Boolean? = null,
        @JsonProperty("port-forwarding-enabled") val portForwardingEnabled: Boolean? = null)

data class Units(
        @JsonProperty("memory-bytes") val memoryBytes: BigInteger,
        @JsonProperty("memory-units") val memoryUnits: List<String>,
        @JsonProperty("size-bytes") val sizeBytes: BigInteger,
        @JsonProperty("size-units") val sizeUnits: List<String>,
        @JsonProperty("speed-units") val speedUnits: List<String>,
        @JsonProperty("speed-bytes") val speedBytes: BigInteger

)
