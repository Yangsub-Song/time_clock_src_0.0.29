package com.techrove.timeclock.server.cwma.model.res

import com.google.gson.annotations.SerializedName

data class Worker (
        @SerializedName("name") val name: String,
        @SerializedName("resident_no") val residentNumber: String,
)

data class WorkerInfo(
        @SerializedName("worker") val info: Worker,
)

data class WorkerInfoResponse(
        @SerializedName("result") val result: Result,
        @SerializedName("body") val workerInfo: WorkerInfo
)

