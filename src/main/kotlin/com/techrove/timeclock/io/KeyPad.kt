package com.techrove.timeclock.io

import com.techrove.timeclock.isLinux
import com.techrove.timeclock.utils.getString
import kotlinx.coroutines.*
import kotlinx.coroutines.javafx.JavaFx
import mu.KotlinLogging
import java.awt.Robot
import java.awt.event.KeyEvent
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.experimental.xor

private val logger = KotlinLogging.logger("KeyPad")

object KeyPad: HwStatus() {
    private val portName = if (isLinux) "ttyS2" else "COM11"
    private val serial = Serial(portName, 38400, "KeyPad")

    private const val STX = 0x02.toByte()
    private const val ETX = 0x03.toByte()
    private const val ACK = 0x06.toByte()
    private const val NAK = 0x15.toByte()

    private const val UNIT_CODE = "@EPV"

    private var job: Job? = null

    private fun ByteBuffer.calcBcc(): Byte {
        val length = remaining()
        var bcc = 0.toByte()
        repeat(length - 1) {
            bcc = (bcc xor get())
        }
        return bcc
    }

    private fun createTx(command: String, data: ByteArray) =
        ByteBuffer.allocate(17 + data.size).apply {
            order(ByteOrder.LITTLE_ENDIAN)
            put(STX)
            put(UNIT_CODE.toByteArray())
            // length
            put("%04d".format(capacity() - 1).toByteArray())
            // head
            put("00".toByteArray())
            // command
            put(command.toByteArray())
            // mode
            put("00".toByteArray())
            // data
            put(data)
            put(ETX)
            // checksum
            put(0)
            rewind()
            get()
            put(calcBcc())
        }

    private fun parseRx(command: String, rxData: ByteBuffer): ByteArray? {
        with(rxData) {
            // check STX
            if (get() != STX) {
                //logger.warn { "invalid frame" }
                return null
            }
            mark()
            // check BCC
            if (calcBcc() != get()) {
                logger.warn { "BCC error" }
                return null
            }
            reset()
            // unit code
            if (getString(4) == UNIT_CODE) {
                // length
                val dataLen = getString(4).toInt() - 18
                // head
                when (getString(2)) {
                    "41" -> {
                        // command
                        if (getString(2) == command) {
                            // skip mode
                            position(position() + 2)
                            // skip error
                            position(position() + 2)
                            return ByteArray(dataLen).apply {
                                get(this, 0, dataLen)
                            }
                        } else {
                            logger.warn { "invalid command response" }
                        }
                    }
                    else -> {
                        logger.warn { "error response" }
                    }
                }
            } else {
                logger.warn { "invalid unit code" }
            }
        }
        return null
    }

    private suspend fun transfer(command: String, rxLen : Int, timeout: Int = 10000, data: ByteArray = ByteArray(0)): ByteArray? {
        serial.setReadTimeout(timeout)
        serial.transfer(createTx(command, data), rxLen)?.run {
            when (get()) {
                ACK -> {
                    return parseRx(command, this)
                }
                NAK -> {
                    logger.warn { "NAK received" }
                }
                else -> {
                    logger.warn { "invalid response" }
                }
            }
        }
        return null
    }

    override suspend fun checkHw(): Boolean {
        close()
        return checkConnection().also {
            init()
        }
    }

    private suspend fun checkConnection(): Boolean {
        transfer("BI", 20, timeout = 2000)?.let {
            isHwOk = true
            return true
        }
        isHwOk = false
        return false
    }

    private fun close() {
        job?.cancel()
        serial.close()
    }

    suspend fun scan(on: Boolean, block: (Key)->Unit = {}): Boolean {
        if (on) {
            transfer("OS", 22, data = byteArrayOf(0x30, 0x00, 0x00, 0x00, 0x00))?.let {
                job?.cancel()
                job = GlobalScope.launch(Dispatchers.IO) {
                    serial.setReadTimeout(0)
                    while (isActive) {
                        serial.receive(21)?.let {
                            parseRx("OS", it)?.let {
                                withContext(Dispatchers.JavaFx) {
                                    block(Key.fromString(String(it)))
                                }
                            }
                        }
                    }
                }
                return true
            }
            return false
        } else {
            job?.cancel()
            transfer("OE", 20)?.let {
                return true
            }
            return false
        }
    }

    fun init() {
        GlobalScope.launch(Dispatchers.IO) {
            while (!checkConnection()) {
                logger.warn { "keypad not found. retry in 10sec" }
                delay(10000)
            }
            val r = Robot()
            scan(true) {
                logger.info { it }
                val key = when (it) {
                    Key.Number0 -> KeyEvent.VK_0
                    Key.Number1 -> KeyEvent.VK_1
                    Key.Number2 -> KeyEvent.VK_2
                    Key.Number3 -> KeyEvent.VK_3
                    Key.Number4 -> KeyEvent.VK_4
                    Key.Number5 -> KeyEvent.VK_5
                    Key.Number6 -> KeyEvent.VK_6
                    Key.Number7 -> KeyEvent.VK_7
                    Key.Number8 -> KeyEvent.VK_8
                    Key.Number9 -> KeyEvent.VK_9
                    Key.Cancel -> KeyEvent.VK_ESCAPE
                    Key.Left -> KeyEvent.VK_LEFT
                    Key.Right -> KeyEvent.VK_RIGHT
                    Key.Store,
                    Key.Ok -> KeyEvent.VK_ENTER
                    Key.Register -> KeyEvent.VK_INSERT
                    Key.None -> {
                        return@scan
                    }
                }
                r.keyPress(key)
                r.keyRelease(key)
                Audio.play("beep1.wav")
            }
        }
    }
}
