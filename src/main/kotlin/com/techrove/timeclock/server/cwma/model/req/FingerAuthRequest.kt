package com.techrove.timeclock.server.cwma.model.req

import com.google.gson.annotations.SerializedName
import com.techrove.timeclock.server.cwma.CwmaServer
import java.time.LocalDateTime

data class FingerAuthRequest(
    @SerializedName("terminalId") val terminalId: String = CwmaServer.terminalId,
    @SerializedName("placeCd") val placeCd: String = CwmaServer.placeCd,
    @SerializedName("residentNo") val dob: String,
    //@SerializedName("fileNm") val fileName: String,
    @SerializedName("regDate") val date: LocalDateTime,
    @SerializedName("finger") val finger: String,
    @SerializedName("fingerTypeCd") val fingerType: String = "0003" //FingerType = FingerType.Suprema,
)
