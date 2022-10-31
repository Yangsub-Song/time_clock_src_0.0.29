package com.techrove.timeclock.io

import com.techrove.timeclock.isLinux
import kotlinx.coroutines.*
import kotlinx.coroutines.javafx.JavaFx
import mu.KotlinLogging
import kotlin.experimental.xor

private val logger = KotlinLogging.logger("RFReader")

object RfReader: HwStatus() {
    private val portName = if (isLinux) "ttyS1" else "COM12"
    private val serial = Serial(portName, 115200, "RFReader")
    private var job: Job? = null

    var onRfCardTagged: ((cardNumber: String) -> Unit)? = null

    override suspend fun checkHw(): Boolean {
        if (!isLinux) return false
        job?.cancel()
        serial.close()
        serial.setReadTimeout(2000)
        return (serial.transfer(
            byteArrayOf(
                0x01, 0x20, 0xA0.toByte(), 0x00, 0x05, 0x01, 0x90.toByte(), 0x01, 0x00, 0x01, 0x58, 0x03
            ), 8
        ) != null).also {
            serial.close()
            init()
        }
    }

    fun init() {
        job = GlobalScope.launch(Dispatchers.IO) {
            while (isActive) {
                if (!serial.isOpened) {
                    serial.open()
                    serial.setReadTimeout(0)
                }
                serial.receive(24)?.let {
                    isHwOk = true
                    if (it.remaining() != 24) {
                        logger.error { "rx size wrong ${it.remaining()}" }
                        return@let
                    }
                    val bytes = ByteArray(it.remaining())
                    it.get(bytes)
                    val cardNumber = bytes.copyOfRange(6, 6 + 16).let { number ->
                        for (i in arrayOf(0, 1, 4, 5, 8, 9, 12, 13)) {
                            number[i] = number[i] xor 0xff.toByte()
                        }
                        String(number)
                    }
                    logger.info { "Card tagged : $cardNumber" }
                    withContext(Dispatchers.JavaFx) {
                        onRfCardTagged?.invoke(cardNumber)
                    }
                } ?: run {
                    isHwOk = false
                }
            }
        }
    }
}
