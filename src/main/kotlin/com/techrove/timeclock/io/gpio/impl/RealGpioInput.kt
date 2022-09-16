package com.techrove.timeclock.io.gpio.impl

import com.techrove.timeclock.io.gpio.GpioInput
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mu.KotlinLogging
import java.io.File

private val logger = KotlinLogging.logger("IO")

class RealGpioInput(
    override val name: String, override val portNumber: Int,
    override val onTriggered: (GpioInput, Boolean) -> Unit = { _, _ -> }
) : GpioInput {
    override val on: Boolean
        get() = File("/sys/class/gpio/gpio$portNumber/value").readText().first() == '0'

    init {
        try {
            File("/sys/class/gpio/export").writeText(portNumber.toString())
            File("/sys/class/gpio/gpio$portNumber/direction").writeText("in")
        } catch (_: Exception) {
        }

        GlobalScope.launch(Dispatchers.Default) {
            var prevOn = on
            while (true) {
                try {
                    delay(200)
                    val currentOn = on
                    if (prevOn != currentOn) {
                        onTriggered(this@RealGpioInput, currentOn)
                        logger.warn { "$name ${if (currentOn) "H" else "L"}" }
                        prevOn = currentOn
                    }
                } catch (e: Exception) {
                    logger.error { e }
                }
            }
        }
    }
}
