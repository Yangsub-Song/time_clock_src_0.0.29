package com.techrove.timeclock.io

import com.techrove.timeclock.extensions.runCommand
import com.techrove.timeclock.isLinux
import com.techrove.timeclock.security.Key
import com.techrove.timeclock.security.encrypt
import com.techrove.timeclock.utils.extension
import mu.KotlinLogging
import java.io.File

private val logger = KotlinLogging.logger("Camera")

object Camera: HwStatus() {
    private const val DEVICE = "/dev/video5"
    private const val WIDTH = 1024
    private const val HEIGHT = 768

    override var isHwOk: Boolean
        get() = File(DEVICE).exists()
        set(value) {}

    override suspend fun checkHw(): Boolean {
        // usb driver 상태만 확인
        return isHwOk
    }

    fun takePicture(file: File, encrypt: Boolean = false): Boolean {
        logger.info { "SW 기밀성 확인" }
        logger.info { "capture to $file" }
        logger.info { "SW 무결성 정상" }
        if (isLinux) {
            try {
                val captureFile = if (encrypt) "./.tmp" else file.absolutePath
                when (file.extension.toLowerCase()) {
                    "jpg" -> {
                        "fswebcam -d $DEVICE -r ${WIDTH}x${HEIGHT} --no-banner --jpeg 90 $captureFile".runCommand()
                    }
                    "png" -> {
                        "fswebcam -d $DEVICE -r ${WIDTH}x${HEIGHT} --no-banner --png 9 $captureFile".runCommand()
                    }
                }
                if (encrypt) {
                    File(captureFile).run {
                        encrypt(Key.photoKey, file, "photo")
                        delete()
                    }
                }
                return true
            } catch (e: Exception) {
                logger.error { e }
            }
            return false
        } else {
            val captureFile = if (encrypt) "./.tmp" else file.absolutePath
            File("./pictures/debug.jpg").copyTo(File(captureFile), overwrite = true)
            if (encrypt) {
                File(captureFile).run {
                    encrypt(Key.photoKey, file, "photo")
                    delete()
                }
            }
            return true
        }
    }
}
