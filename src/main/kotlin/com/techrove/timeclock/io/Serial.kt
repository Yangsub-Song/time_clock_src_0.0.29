package com.techrove.timeclock.io

import com.fazecast.jSerialComm.SerialPort
import com.techrove.timeclock.utils.toHexString
import mu.KotlinLogging
import java.nio.ByteBuffer
import java.util.*

private val logger = KotlinLogging.logger("Serial")

open class Serial(private val portName: String, private val baudRate: Int, private val logPrefix: String = "") {
    private var port: SerialPort? = null
    private var readTimeout = 10000

    val isOpened: Boolean get() = port?.isOpen == true

    fun open(): SerialPort? {
        if (!isOpened) {
            logger.info { "$logPrefix open ${portName}:${baudRate}" }
            port = SerialPort.getCommPorts().find { it.systemPortName.startsWith(portName) }
            port?.let {
                it.openPort()
                it.baudRate = baudRate
                it.setComPortTimeouts(
                    SerialPort.TIMEOUT_READ_BLOCKING + SerialPort.TIMEOUT_WRITE_BLOCKING,
                    readTimeout,
                    3000
                )
            } ?: run {
                logger.error { "$logPrefix error ${portName}:${baudRate}" }
            }
        }
        return port
    }

    fun close() {
        port?.closePort()
    }

    fun setReadTimeout(timeout: Int) {
        if (timeout == readTimeout) return
        readTimeout = timeout
        val port = port ?: return
        port.setComPortTimeouts(
            SerialPort.TIMEOUT_READ_BLOCKING + SerialPort.TIMEOUT_WRITE_BLOCKING,
            readTimeout,
            port.writeTimeout
        )
    }

    suspend fun receive(rxLen: Int): ByteBuffer? {
        val port = port ?: return null
        val rxData = ByteArray(rxLen)
        val rxCount = port.readBytes(rxData, rxLen.toLong())
        if (rxCount == 0) {
            // no rx: timeout
            logger.error { "$logPrefix timeout" }
            return null
        }
        logger.trace { "$logPrefix RX: " + rxData.toHexString() }
        return ByteBuffer.wrap(rxData)
    }

    suspend fun transfer(txData: ByteArray, rxLen: Int) : ByteBuffer? =
        open()?.run {
            val port = port ?: return@run null
            // flush rx buffer
            val left = bytesAvailable()
            if (left > 0) {
                val leftBytes = ByteArray(left)
                readBytes(leftBytes, leftBytes.size.toLong())
            }
            // send
            logger.trace { "$logPrefix TX: " + txData.toHexString() }
            port.writeBytes(txData, txData.size.toLong())

            // receive
            return receive(rxLen)
        }

    suspend fun transfer(txData: ByteBuffer, rxLen: Int) : ByteBuffer? =
        transfer(Arrays.copyOf(txData.array(), txData.position()), rxLen)
}
