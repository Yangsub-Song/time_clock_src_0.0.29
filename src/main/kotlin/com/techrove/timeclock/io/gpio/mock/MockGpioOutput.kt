package com.techrove.timeclock.io.gpio.mock

import com.techrove.timeclock.io.gpio.GpioOutput
import mu.KotlinLogging

private val logger = KotlinLogging.logger("IO-MOCK")


class MockGpioOutput(
    override val name: String,
    override val portNumber: Int,
    override val initial: Boolean = false
) : GpioOutput {
    override var on: Boolean = initial
        set(value) {
            field = value
            //logger.warn { "$name ${if (field) "ON" else "OFF"}" }
        }

    init {
        on = initial
    }
}
