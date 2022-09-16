package com.techrove.timeclock.server.cwma.model.req

import com.google.gson.annotations.SerializedName
import com.techrove.timeclock.server.cwma.CwmaServer

data class RegisterFingerRequest(
    @SerializedName("terminalId") val terminalId: String = CwmaServer.terminalId,
    @SerializedName("placeCd") val placeCd: String = CwmaServer.placeCd,
    @SerializedName("residentNo") val residentNo: String,
    @SerializedName("phone") val phoneNumber: String,
    @SerializedName("fingerIdx") val fingerIndex: String = "1",
    @SerializedName("finger") val finger: String,
    @SerializedName("fingerTypeCd") val fingerType: String = "0003" //FingerType = FingerType.Suprema,
)

