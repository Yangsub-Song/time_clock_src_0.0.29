package com.techrove.timeclock.extensions

import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

val String.dob get() = this.substring(0..5)

val String.isRegistrationNumber: Boolean
    get() {
        return if (length == 13) {
            var sum = 0
            intArrayOf(2, 3, 4, 5, 6, 7, 8, 9, 2, 3, 4, 5).forEachIndexed { i, v ->
                sum += Character.getNumericValue(get(i)) * v
            }
            Character.getNumericValue(last()) == ((11 - (sum % 11)) % 10)
        } else {
            false
        }
    }

val String.numberMasked: String
    get() = if (this.all { it.isDigit() }) {
        "*".repeat(this.length)
    } else {
        this
    }

fun String.runCommand(workingDir: File = File("."), argument: String = ""): String? {
    return try {
        val parts = this.split("\\s".toRegex())
        val process = ProcessBuilder(*parts.toTypedArray(), argument)
            .directory(workingDir)
            .redirectOutput(ProcessBuilder.Redirect.DISCARD)
            .redirectError(ProcessBuilder.Redirect.DISCARD)
            .start()
//        GlobalScope.launch {
//            val output = process.inputStream.bufferedReader()
//            var line: String? = output.readLine()
//            while (line != null) {
//                logger.info { line }
//                line = output.readLine()
//            }
//        }
//        GlobalScope.launch {
//            val output = process.errorStream.bufferedReader()
//            var line: String? = output.readLine()
//            while (line != null) {
//                logger.info { line }
//                line = output.readLine()
//            }
//        }
        process.waitFor(60, TimeUnit.SECONDS)
        "" //process.inputStream.bufferedReader().readText()
    } catch(e: IOException) {
        e.printStackTrace()
        null
    }
}

fun String.exec(): String? {
    val process = Runtime.getRuntime().exec(this)
    return process.waitFor(60, TimeUnit.SECONDS).let {
        if (process.exitValue() == 0) process.inputStream.bufferedReader().readText()
        else null
    }
}

fun String.decodeHex(): ByteArray {
    return chunked(2)
        .map { it.toInt(16).toByte() }
        .toByteArray()
}

