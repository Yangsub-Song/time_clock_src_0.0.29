package com.techrove.timeclock.io.gpio.mock

import com.techrove.timeclock.io.gpio.GpioInput

class MockGpioInput(
    override val name: String,
    override val portNumber: Int, override val onTriggered: (GpioInput, Boolean) -> Unit = { _, _ -> }
) : GpioInput {
    override val on = false
}