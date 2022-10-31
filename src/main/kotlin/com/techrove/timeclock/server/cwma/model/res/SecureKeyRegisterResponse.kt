package com.techrove.timeclock.server.cwma.model.res

import com.google.gson.annotations.SerializedName

data class SecureKeyRegisterData(
    @SerializedName("kmsKey") val kmsKey: String,
    @SerializedName("kmsVer") val kmsVersion: String
)

data class SecureKeyRegisterConfirmResponse(
    @SerializedName("result") val result: Result,
    @SerializedName("body") val data: SecureKeyRegisterData
)
