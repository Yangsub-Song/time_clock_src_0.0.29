package com.techrove.timeclock.extensions

import mu.KotlinLogging
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField
import java.time.temporal.ChronoUnit
import kotlin.math.abs

private val logger = KotlinLogging.logger("DateTime")

private var formatter = DateTimeFormatterBuilder()
    .appendPattern("[yyyy-MM-dd HH:mm:ss][yyyyMMddHHmmss][yyyyMMdd]")
    .appendPattern("[yyMMdd]")
    .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
    .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
    .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
    .toFormatter()

private val formats =
    arrayOf(
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
        DateTimeFormatter.ofPattern("yyyyMMddHHmmss"),
        DateTimeFormatter.ofPattern("yyyyMMdd")
    )

val LocalDateTime.fullString: String get() = format(formats[0])

val LocalDateTime.mediumString: String get() = format(formats[1])

val LocalDateTime.shortString: String get() = format(formats[2])

val String.toLocalDateTime: LocalDateTime?
    get() {
        try {
            return LocalDateTime.parse(this, formatter)
        } catch (e: Throwable) {
            logger.warn { "$this date time parse error" }
        }
        return null
    }

val String.toLocalDate: LocalDate?
    get() {
        try {
            return LocalDate.parse(this, formatter)
        } catch (e: Throwable) {
            logger.warn { "$this date parse error" }
        }
        return null
    }

fun LocalDateTime?.applyToSystem(graceSec: Int = 5): Boolean {
    this?.run {
        if (abs(ChronoUnit.SECONDS.between(this, LocalDateTime.now())) > graceSec) {
            logger.warn { "set system time to $this" }
            try {
                Runtime.getRuntime().exec(arrayOf("sudo", "date", "-s", fullString))
            } catch (e: Exception) {
                logger.error { e }
            }
            return true
        }
    }
    return false
}

