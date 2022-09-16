package com.techrove.timeclock.server.cwma.model.req

import com.google.gson.annotations.SerializedName
import com.techrove.timeclock.Settings
import com.techrove.timeclock.server.cwma.CwmaServer

data class TerminalStatus(
    @SerializedName("terminalId") val terminalId: String = CwmaServer.terminalId,
    @SerializedName("placeCd") val placeCd: String = CwmaServer.placeCd,
    @SerializedName("version") val version: String = Settings.VERSION,

//  optional
//
//    /** 함체 도어 열림 여부(0:close, 1:open) */
//    @SerializedName("statOpen") val statOpen: String,
//    /** 온도(-255 ~ 255) */
//    @SerializedName("statTemp") val statTemp: String,
//    /** 메모리 사용 용량(0 ~255 MByte) */
//    @SerializedName("devMem") val devMem: String,
)
