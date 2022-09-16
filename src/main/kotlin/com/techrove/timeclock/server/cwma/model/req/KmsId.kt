package com.techrove.timeclock.server.cwma.model.req

import com.google.gson.annotations.SerializedName
import com.techrove.timeclock.server.cwma.CwmaServer

data class KmsId(
    @SerializedName("terminalId") val terminalId: String = CwmaServer.terminalId,
    @SerializedName("placeCd") val placeCd: String = CwmaServer.placeCd,
)