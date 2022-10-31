package com.techrove.timeclock.server.cwma.model.res

import com.google.gson.annotations.SerializedName
import java.time.LocalDateTime

data class ServerTime(
    @SerializedName("datetime") val dateTime: LocalDateTime,
)

data class ServerTimeResponse(
    @SerializedName("result") val result: Result,
    @SerializedName("body") val data: ServerTime
)
