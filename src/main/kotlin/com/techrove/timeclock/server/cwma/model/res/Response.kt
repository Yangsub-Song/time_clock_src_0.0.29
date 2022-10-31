package com.techrove.timeclock.server.cwma.model.res

import com.google.gson.annotations.SerializedName

data class Response(
    @SerializedName("result") val result: Result,
)