package com.techrove.timeclock.server.admin.model.res

import com.google.gson.annotations.SerializedName

data class AdminTerminalStatusResponse(
    @SerializedName("version") val version: String,
    @SerializedName("otaUri") val otaUri: String,
    @SerializedName("port") val port: Int,
)