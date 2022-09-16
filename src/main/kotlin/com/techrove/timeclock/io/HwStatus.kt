package com.techrove.timeclock.io

abstract class HwStatus {
    open var isHwOk: Boolean = true
        set(value) {
            if (isHwOkPrev != value) {
                isHwStatusTriggered = true
            }
            isHwOkPrev = field
            field = value
        }
    private var isHwOkPrev = true

    var isHwStatusTriggered = false
        get() {
            return field.also {
                isHwStatusTriggered = false
            }
        }

    abstract suspend fun checkHw(): Boolean
}
