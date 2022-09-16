package com.techrove.timeclock.io

import com.techrove.timeclock.isLinux
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull
import mu.KotlinLogging
import java.nio.ByteBuffer
import java.nio.ByteOrder

private val logger = KotlinLogging.logger("Suprema")

object Suprema: HwStatus() {
    private val timeoutDefault = 15000
    private val portName = if (isLinux) "ttyACM" else "COM13"
    private val serial = Serial(portName, 115200, "Suprema").apply {
        setReadTimeout(timeoutDefault)
    }
    private var scanning = false

    private fun createTx(command: Int, parameter: Int = 0, size: Int = 0, flag: Int = 0) =
        ByteBuffer.allocate(13).apply {
            order(ByteOrder.LITTLE_ENDIAN)
            put(0x40)
            put(command.toByte())
            putInt(parameter)
            putInt(size)
            put(flag.toByte())
            val length = position()
            rewind()
            var sum = 0.toByte()
            repeat(length) {
                sum = (sum + get()).toByte()
            }
            put(sum)
            put(0x0A)
        }

    ///////////////////////////////////////////////////////////////////////////
    //    UF_PROTO_RET_SUCCESS					= 0x61,
    //    UF_PROTO_RET_SCAN_SUCCESS			= 0x62,
    //    UF_PROTO_RET_SCAN_FAIL				= 0x63,
    //    UF_PROTO_RET_NOT_FOUND				= 0x69,
    //    UF_PROTO_RET_NOT_MATCH				= 0x6a,
    //    UF_PROTO_RET_TRY_AGAIN				= 0x6b,
    //    UF_PROTO_RET_TIME_OUT				= 0x6c,
    //    UF_PROTO_RET_MEM_FULL				= 0x6d,
    //    UF_PROTO_RET_EXIST_ID				= 0x6e,
    //    UF_PROTO_RET_FINGER_LIMIT			= 0x72,
    //    UF_PROTO_RET_CONTINUE				= 0x74,
    //    UF_PROTO_RET_UNSUPPORTED				= 0x75,
    //    UF_PROTO_RET_INVALID_ID				= 0x76,
    //    UF_PROTO_RET_TIMEOUT_MATCH			= 0x7a,
    //    UF_PROTO_RET_BUSY						= 0x80,
    //    UF_PROTO_RET_CANCELED				= 0x81,
    //    UF_PROTO_RET_DATA_ERROR				= 0x82,
    //    UF_PROTO_RET_DATA_OK					= 0x83,
    //    UF_PROTO_RET_EXIST_FINGER 			= 0x86,
    //    UF_PROTO_RET_DURESS_FINGER 			= 0x91,
    //    UF_PROTO_RET_ACCESS_NOT_GRANTED 	= 0x93,
    //    UF_PROTO_RET_CARD_ERROR				= 0xa0,
    //    UF_PROTO_RET_LOCKED					= 0xa1,
    //    UF_PROTO_RET_REJECTED_ID				= 0x90,
    //    UF_PROTO_RET_EXCEED_ENTRANCE_LIMIT = 0x94,
    //    UF_PROTO_FAKE_DETECTED				= 0xB0
    ///////////////////////////////////////////////////////////////////////////
    private suspend fun parseRx(command: Int, rxData: ByteBuffer, getBinaryData: Boolean): RxPacket? {
        with(rxData) {
            order(ByteOrder.LITTLE_ENDIAN)
            if (get() != 0x40.toByte()) {
                logger.error { "invalid start code" }
                return null
            }
            if (get() != command.toByte()) {
                logger.error { "invalid command" }
                return null
            }
            val packet = RxPacket(int, int, get().toInt())
            get()
            if (get() != 0x0a.toByte()) {
                logger.error { "invalid end code" }
                return null
            }
            logger.debug { packet }

            if (getBinaryData) {
                when (packet.flag) {
                    0x61 -> {
                        // success
                        packet.binData = ByteArray(packet.size)
                        serial.receive(packet.size)?.let {
                            it.get(packet.binData, 0, packet.size)
                            // read end code 0x0a
                            serial.receive(1)
                            //log.info { packet.binData?.toHexString() }
                        } ?: run {
                            logger.error { "timeout" }
                            return null
                        }
                    }
                    0x62 -> {
                        // scan success
                        return serial.receive(13)?.let {
                            parseRx(command, it, true)
                        }
                    }
                    0x6c -> {
                        // timeout
                        logger.error { "scan timeout" }
                        return null
                    }
                }
            }
            return packet
        }
    }

    suspend fun scanTemplate(): ByteArray? {
        if (scanning) {
            withTimeoutOrNull(3000) {
                while (scanning) {
                    delay(250)
                }
            }
        }
        serial.setReadTimeout(timeoutDefault)
        scanning = true
        val command = 0x21
        return serial.transfer(createTx(command), 13)?.let {
            parseRx(command, it, true)?.binData
        }.also {
            scanning = false
            isHwOk = it != null
        }
    }

    suspend fun cancel(timeout: Int = 15000, cancelAlways: Boolean = false): Boolean {
        if (!scanning && !cancelAlways) return true
        serial.setReadTimeout(timeout)
        val response = serial.transfer(createTx(0x60), 13)
        scanning = false
        return response != null && response.get() != 0x00.toByte()
    }

    override suspend fun checkHw(): Boolean {
        // cancel command 로 HW 상태 확인
        return cancel(2000, true)
    }

    @Suppress("ArrayInDataClass")
    private data class RxPacket(
        val parameter: Int,
        val size: Int,
        val flag: Int,
        var binData: ByteArray? = null
    )
}
