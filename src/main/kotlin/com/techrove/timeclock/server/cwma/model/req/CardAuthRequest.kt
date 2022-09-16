package com.techrove.timeclock.server.cwma.model.req

import com.google.gson.annotations.SerializedName
import com.techrove.timeclock.server.cwma.CwmaServer
import java.time.LocalDateTime

data class CardAuthRequest (
    @SerializedName("terminalId") val terminalId: String = CwmaServer.terminalId,
    @SerializedName("placeCd") val placeCd: String = CwmaServer.placeCd,
    @SerializedName("useXOR") val useXor: Int = 0,
    //@SerializedName("fileNm") val fileName: String,
    @SerializedName("regDate") val date: LocalDateTime,
    @SerializedName("cardPinNo") val cardNumber: String,
)
