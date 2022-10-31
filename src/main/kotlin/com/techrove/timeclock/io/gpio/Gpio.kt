package com.techrove.timeclock.io.gpio

import com.techrove.timeclock.io.gpio.impl.RealGpioInput
import com.techrove.timeclock.io.gpio.impl.RealGpioOutput
import com.techrove.timeclock.io.gpio.mock.MockGpioInput
import com.techrove.timeclock.io.gpio.mock.MockGpioOutput
import com.techrove.timeclock.isLinux

interface Gpio {
    val name: String
    val portNumber: Int
    val on: Boolean
}

interface GpioInput: Gpio {
    val onTriggered: (GpioInput, Boolean) -> Unit
}

interface GpioOutput: Gpio {
    override var on: Boolean
    val initial: Boolean
}

object GpioHelper {
    fun output(name: String, portNumber: Int, initial: Boolean = false) = if (isLinux) {
        RealGpioOutput(name, portNumber, initial)
    } else {
        MockGpioOutput(name, portNumber, initial)
    }

    fun input(name: String, portNumber: Int, onTriggered: (GpioInput, Boolean) -> Unit = { _, _ -> }) = if (isLinux) {
        RealGpioInput(name, portNumber, onTriggered)
    } else {
        MockGpioInput(name, portNumber, onTriggered)
    }
}