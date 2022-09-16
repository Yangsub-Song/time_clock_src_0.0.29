package com.techrove.timeclock.io.gpio.impl

import com.techrove.timeclock.io.gpio.GpioOutput
import mu.KotlinLogging
import java.io.File

private val logger = KotlinLogging.logger("IO")

class RealGpioOutput(
    override val name: String, override val portNumber: Int, override val initial: Boolean = false
) : GpioOutput {
    override var on: Boolean = false
        set(value) {
            File("/sys/class/gpio/gpio$portNumber/value").writeText(if (value) "1" else "0")
            field = value
            //logger.warn { "$name ${if (field) "ON" else "OFF"}" }
        }

    init {
        try {
            File("/sys/class/gpio/export").writeText(portNumber.toString())
            File("/sys/class/gpio/gpio$portNumber/direction").writeText("out")
        } catch (_: Exception) {
        }
        on = initial
    }
}
