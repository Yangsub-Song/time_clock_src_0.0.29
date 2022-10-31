package com.techrove.timeclock.utils

import com.techrove.timeclock.Settings
import com.techrove.timeclock.controller.model.Version
import com.techrove.timeclock.database.Db
import com.techrove.timeclock.extensions.exec
import com.techrove.timeclock.isLinux
import com.techrove.timeclock.security.Key
import com.techrove.timeclock.security.KeyHelper
import com.techrove.timeclock.security.decrypt
import com.techrove.timeclock.server.admin.AdminServer
import com.techrove.timeclock.utils.UnzipUtils.unzipEncrypted
import mu.KotlinLogging
import java.io.File
import kotlin.system.exitProcess

private val logger = KotlinLogging.logger("SwUpdateController")

object SwUpdateUtils {

    private var usbDevice = "/dev/sda1"

    fun isUsbDriveMounted(): Boolean {
        return if (isLinux) {
            return listOf("/dev/sda1", "/dev/sdb1", "/dev/sdc1").any { device ->
                "ls $device".exec().let {
                    val success = it != null
                    logger.info { "check $device ${if (success) "success" else "failed"}"}
                    usbDevice = device
                    success
                }
            }
        } else {
            true
        }
    }

    private fun checkUsbDriveLabel(): Boolean {
        return if (isLinux) {
            "blkid $usbDevice".exec().let {
                it?.let {
                    //LABEL="uidf" UUID="5E80-837F" TYPE="exfat" PARTUUID="052bfbd1-01"
                    val find = """LABEL="([^"]+)"""".toRegex().find(it)
                    val label = find?.groupValues?.get(1)
                    logger.debug { "LABEL: $label" }
                    return label == "uidf"
                } ?: return false
            }
        } else {
            true
        }
    }

    fun mountUsbDrive(): Boolean {
        return if (isLinux) {
            File("./usb").mkdirs()
            "mount $usbDevice ./usb".exec().let {
                val success = it != null
                logger.info { "mount usb drive ${if (success) "success" else "failed"}"}
                success
            }
        } else {
            true
        }
    }

    fun unmountUsbDrive(): Boolean {
        return if (isLinux) {
            "umount ./usb".exec().let {
                val success = it != null
                logger.info { "umount usb drive ${if (success) "success" else "failed"}"}
                success
            }
        } else {
            true
        }
    }

    private fun swUpdate(src: File): Pair<Boolean, String?> {
        try {
            if (src.name.endsWith(".zip")) {
                UnzipUtils.unzip(src, "./update")
            } else {
                logger.info { "SW 기밀성 확인..." }
                val password = Settings.swUpdatePassword.decrypt(Key.pwdSUKey, "sUpw")  // Yade0924
                logger.info { "SW업데이트 암호(in plain text): $password" }     // Yade0924
                if (!src.unzipEncrypted("./update", password)) {
                    return false to "SW 기밀성 오류가 발생했습니다."
                }
                logger.info { "SW 기밀성 정상" }
            }
            logger.info { "SW 무결성 오류 확인..." }
            if (!KeyHelper.checkFileSha("./update/time_clock.jar")) {
                return false to "SW 무결성 오류가 발생했습니다."
            }
            logger.info { "SW 무결성 정상"}

            return true to null
        } catch (e: Exception) {
            logger.error { e }
            return false to null
        }
    }

    fun swUpdateByUsb(): Pair<Boolean, String?> {
        try {
            if (!checkUsbDriveLabel()) {
                return false to "지원하지 않는 USB 드라이브입니다."
            }
            val src = File("./usb")
                .listFiles { _, name -> name.startsWith("time_clock") && (name.endsWith(".zip") || name.endsWith(".7z")) }
                ?.toList()?.maxOrNull() ?: return false to "업데이트 파일이 없습니다."
            logger.info { "SW UPDATE: $src" }

            return swUpdate(src).also {
                if (it.first) {
                    logger.info { "SW UPDATE success" }
                }
            }
        } catch (e: Exception) {
            logger.error { "SW UPDATE failed $e" }
        }
        return false to null
    }

    suspend fun swUpdateByOta(uri: String, port: Int): Pair<Boolean, String?> {
        val src = File("./ota.7z")
        try {
            var password = Settings.sFTPPassword.decrypt(Key.pwdSFKey, "sFpw")  // Yade0925-> 1014
            logger.info { "SFTP 암호(in plain text): $password" }               // Yade0925
            SshUtil.
            getFile(uri, src, defaultPassword = password, defaultPort = port).let { success ->
                if (success) {
                    return swUpdate(src).also {
                        if (it.first) {
                            logger.info { "OTA update success" }
                        }
                    }
                } else {
                    logger.error { "OTA download failed" }
                    return false to "SW를 다운로드 할 수 없습니다."
                }
            }
        } finally {
            try {
                src.delete()
            } catch (_: Exception) {
            }
        }
    }

    fun restartApp(deleteDb: Boolean = true) {
        if (isLinux) {
            //"/home/linaro/run.sh".runCommand()
            exitProcess(0)
        }
        if (deleteDb) {
            Db.delete()
        }
    }

    suspend fun getOtaInfo(): Triple<String?, Version, Int> {
        return AdminServer.단말기정보()
    }
}
