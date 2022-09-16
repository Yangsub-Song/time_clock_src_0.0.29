package com.techrove.timeclock.utils

import com.techrove.timeclock.io.KeyPad
import com.techrove.timeclock.io.RfReader
import com.techrove.timeclock.io.Suprema
import com.techrove.timeclock.io.TempSensor
import com.techrove.timeclock.io.gpio.GpioHelper
import com.techrove.timeclock.io.gpio.GpioInput
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mu.KotlinLogging
import java.awt.Robot
import java.awt.event.KeyEvent

private val logger = KotlinLogging.logger("IO")

object IoUtils {
    var gpioInputChanged: ((input: GpioInput, level: Boolean) -> Unit) = {_, _ ->}

    private val inputCallback: ((GpioInput, Boolean) -> Unit) = { input, level ->
        when (input.name) {
            "DX1" -> KeyEvent.VK_HOME
            "DX2" -> KeyEvent.VK_END
            else -> null
        }?.let {
            if (level) {
                Robot().keyPress(it)
            } else {
                Robot().keyRelease(it)
            }
        } ?: run {
            gpioInputChanged(input, level)
        }
    }

    val gpioMute = GpioHelper.output("MUTE", 171)
    val gpioSysLed = GpioHelper.output("SYS", 162)
    val gpioStatusLed = GpioHelper.output("STS", 163)

    val gpioSw1 = GpioHelper.input("SW1", 165, inputCallback)
    val gpioSw2 = GpioHelper.input("SW2", 168, inputCallback)
    val gpioDx1 = GpioHelper.input("DX1", 188, inputCallback)
    val gpioDx2 = GpioHelper.input("DX2", 187, inputCallback)
    val gpioDx3 = GpioHelper.input("DX3", 234, inputCallback)
    val gpioDx4 = GpioHelper.input("DX4", 251, inputCallback)

    val gpioDip0 = GpioHelper.input("DIP0", 164, inputCallback)
    val gpioDip1 = GpioHelper.input("DIP1", 257, inputCallback)
    val gpioDip2 = GpioHelper.input("DIP2", 256, inputCallback)
    val gpioDip3 = GpioHelper.input("DIP3", 254, inputCallback)

    fun initialize() {
        KeyPad.init()
        RfReader.init()

        gpioMute.on = true

        gpioSysLed.on = true
        gpioStatusLed.on = true
    }
}
