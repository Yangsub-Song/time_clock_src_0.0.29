package com.techrove.timeclock.server.admin.model.req

import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import com.techrove.timeclock.Settings

data class AdminTerminalStatus(
    @SerializedName("terminalId") val terminalId: String = Settings.terminalId,
    @SerializedName("placeCd") val placeCd: String = Settings.placeCd,
    @SerializedName("version") val version: String = Settings.VERSION,
    /** 함체 도어 열림 여부(0:close, 1:open) */
    @SerializedName("statOpen") val statOpen: Int = 0,
    /** 온도(-255 ~ 255) */
    @SerializedName("temperature") val statTemp: Int,
    @SerializedName("eCode") val eCode: String? = null,
    @SerializedName("memUsage") val memUsage: Long,
    @SerializedName("memTotal") val memTotal: Long,
    @SerializedName("diskUsage") val diskUsage: Long,
    @SerializedName("diskTotal") val diskTotal: Long,
) {
    override fun toString(): String {
        return GsonBuilder().create().toJson(this)
    }
}