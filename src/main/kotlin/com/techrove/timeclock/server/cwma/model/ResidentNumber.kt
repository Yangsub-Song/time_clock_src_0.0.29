package com.techrove.timeclock.server.cwma.model

data class ResidentNumber(val number: String) {
    val dob get() = number.substring(0..5)
}