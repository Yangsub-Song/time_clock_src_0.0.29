
package com.techrove.timeclock.controller.admin

import com.techrove.timeclock.Settings
import com.techrove.timeclock.controller.BaseController
import com.techrove.timeclock.controller.model.AdminModel
import com.techrove.timeclock.database.Db
import com.techrove.timeclock.extensions.configBooleanProperty
import com.techrove.timeclock.extensions.configIntegerProperty
import com.techrove.timeclock.isLinux
import com.techrove.timeclock.security.Key
import com.techrove.timeclock.security.KeyHelper
import com.techrove.timeclock.security.decrypt
import com.techrove.timeclock.security.encrypt
import kotlinx.coroutines.*
import kotlinx.coroutines.javafx.JavaFx
import mu.KotlinLogging
import tornadofx.getValue
import tornadofx.setValue
import tornadofx.toProperty
import java.io.File
import java.net.Inet4Address
import java.net.NetworkInterface
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlin.system.exitProcess


private val logger = KotlinLogging.logger("SettingsController")


/**
 * 관리자 화면/설정 controller
 */
@Suppress("NonAsciiCharacters")
class SettingsController : BaseController() {

    val model = AdminModel()
    val modelSU = AdminModel()  // Yade1013
    val modelSF = AdminModel()  // Yade1013

    val swVersion = Settings.VERSION

    val cpuTemperatureProperty = 0.0.toProperty()
    var cpuTemperature by cpuTemperatureProperty

    val memFreeProperty = 0L.toProperty()
    var memFree by memFreeProperty

    val memTotalProperty = 0L.toProperty()
    var memTotal by memTotalProperty

    val diskFreeProperty = 0L.toProperty()
    var diskFree by diskFreeProperty

    val diskTotalProperty = 0L.toProperty()
    var diskTotal by diskTotalProperty

    val macAddressProperty = "".toProperty()
    var macAddress by macAddressProperty

    val ipAddressProperty = "".toProperty()
    var ipAddress by ipAddressProperty

    val showTerminalInfoProperty = false.toProperty()
    var showTerminalInfo by showTerminalInfoProperty

    val measureTemperatureOptionProperty = configIntegerProperty("temp_opt", 0)
    val takePictureProperty = configBooleanProperty("take_picture", true)
    val beepOnProperty = configBooleanProperty("beep", true)

    var idleTimeOutEnabled = true

    init {
        launch {
            diskTotal = File("/").totalSpace
            diskFree = File("/").freeSpace

            if (isLinux) {
                for (device in arrayOf("eth0", "wlan0")) {
                    ipAddress = getDeviceIpAddress(device) ?: continue
                    macAddress = try {
                        File("/sys/class/net/${device}/address").readText().trim()
                    } catch (e: Exception) {
                        ""
                    }
                    break
                }
            }

            while (isActive) {
                cpuTemperature = if (isLinux) {
                    File("/sys/class/thermal/thermal_zone1/temp").readLines().let {
                        it[0].toDouble() / 1000
                    }
                } else {
                    40.0
                }
                memTotal = Runtime.getRuntime().totalMemory()
                memFree = Runtime.getRuntime().freeMemory()
                delay(1000)
            }
        }
    }

    private fun getDeviceIpAddress(deviceName: String): String? {
        NetworkInterface.getNetworkInterfaces().toList().filter { deviceName in it.name }.forEach {
            return it.inetAddresses.toList().filterIsInstance<Inet4Address>().firstOrNull()?.hostAddress
        }
        return null
    }

    fun tryChangePassword(): Boolean {
        return if (model.password1.value != model.password2.value) {
            false
        } else {
            Settings.password = model.password1.value.encrypt(Key.pwdKey, "pw")
            Settings.passwordRenewedDate = LocalDateTime.now()
            true
        }.also {
            model.password1.value = ""
            model.password2.value = ""
        }
    }
    // Yade0924
    fun tryChangeSWUpdatePassword(): Boolean {
        return if (modelSU.swUpdatePassword1.value != modelSU.swUpdatePassword2.value) {
//            modelSU.swUpdatePassword1.value = ""    // Yade1014
//            modelSU.swUpdatePassword2.value = ""
            false
        } else {
            logger.info("sw업데이트암호(in plain text before encryption): ${modelSU.swUpdatePassword1.value}")
            Settings.swUpdatePassword = modelSU.swUpdatePassword1.value.encrypt(Key.pwdSUKey, "sUpw")
            logger.info("sw업데이트암호: ${Settings.swUpdatePassword}")
            logger.info("sw업데이트암호(in plain text): ${Settings.swUpdatePassword.decrypt(Key.pwdSUKey, "sUpw")}")
            Settings.swUpdatePasswordRenewedDate = LocalDateTime.now()
            true
        }.also {
            modelSU.swUpdatePassword1.value = ""
            modelSU.swUpdatePassword2.value = ""
            modelSF.swUpdatePassword1.value = ""    // Yade1014
            modelSF.swUpdatePassword2.value = ""
            model.swUpdatePassword1.value = ""
            model.swUpdatePassword2.value = ""
        }
    }
    // Yade0925
    fun tryChangeSFTPPassword(): Boolean {
        return if (modelSF.sFTPPassword1.value != modelSF.sFTPPassword2.value) {
//            modelSF.sFTPPassword1.value = ""   // Yade1014
//            modelSF.sFTPPassword2.value = ""
            false
        } else {
            logger.info("sFTP암호(in plain text before encryption): ${modelSF.sFTPPassword1.value}")
            Settings.sFTPPassword = modelSF.sFTPPassword1.value.encrypt(Key.pwdSFKey, "sFpw")
            logger.info("sFTP암호: ${Settings.sFTPPassword}")
            logger.info("sFTP암호(in plain text): ${Settings.sFTPPassword.decrypt(Key.pwdSFKey, "sFpw")}")
            Settings.sFTPPasswordRenewedDate = LocalDateTime.now()
            true
        }.also {
            modelSF.sFTPPassword1.value = ""
            modelSF.sFTPPassword2.value = ""
            modelSU.sFTPPassword1.value = ""    // Yade1014
            modelSU.sFTPPassword2.value = ""
            model.sFTPPassword1.value = ""
            model.sFTPPassword2.value = ""
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

    fun checkSecurity(done: (swIntegrityOk: Boolean, keyIntegrityOk: Boolean, passwordExpiryDaysRemaining: Long, keyExpiryDaysRemaining: Long) -> Unit) {
        launch(Dispatchers.Default) {
            logger.warn { "# integrity check #" }
            System.gc()
            delay(3000)
            if (KeyHelper.checkSwIntegrity()) {
                KeyHelper.checkKeyIntegrity()
            }
            val now = LocalDateTime.now()
            val passwordExpiryDate =
                Settings.passwordRenewedDate.plusYears(Settings.PASSWORD_VALID_YEAR)
            val passwordExpiryDaysRemaining = ChronoUnit.DAYS.between(now, passwordExpiryDate)
            val keyExpiryDate = Settings.keyRenewedDate.plusYears(Settings.KEY_VALID_YEAR)
            val keyExpiryDaysRemaining = ChronoUnit.DAYS.between(now, keyExpiryDate)
            withContext(Dispatchers.JavaFx) {
                done(
                    KeyHelper.swIntegrityOk,
                    KeyHelper.keyIntegrityOk,
                    passwordExpiryDaysRemaining,
                    keyExpiryDaysRemaining
                )
            }
        }
    }
}

