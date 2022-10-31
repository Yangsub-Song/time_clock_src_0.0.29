package com.techrove.timeclock

import com.techrove.timeclock.database.Db
import com.techrove.timeclock.io.Audio
import com.techrove.timeclock.io.Video
import com.techrove.timeclock.security.*
import com.techrove.timeclock.utils.IoUtils
import com.techrove.timeclock.utils.toHexString
import com.techrove.timeclock.view.MainView
import javafx.scene.Cursor
import javafx.stage.Stage
import javafx.stage.StageStyle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import ksign.jce.util.Base64
import mu.KotlinLogging
import tornadofx.App
import tornadofx.runLater
import java.io.File


private val logger = KotlinLogging.logger("App")

val isLinux = System.getProperty("os.name") == "Linux"

class Application : App(MainView::class, Styles::class) {
    private fun checkIntegrity() {
        // SW integrity check. SW 유효성 오류시 모든 동작 중지
        if (KeyHelper.checkSwIntegrity()) {
            // 초기키 생성. 암호 설정 여부로 처음 실행 판단
            if (Settings.password.isEmpty()) {
                KeyHelper.resetKeys()
            } else {
                // 키 유효성 체크. UI 처리는 MainView 에서 함.
                KeyHelper.checkKeyIntegrity()
                KeyHelper.checkKeyIntegrity2()
                KeyHelper.verifyKeyFile(KeyHelper.keyDir2, "adminKey", Settings.ADMIN_KEY_AES_ENC)
                KeyHelper.verifyKeyFile(KeyHelper.keyDir2, "defaultKey", Settings.DEFAULT_KEY_AES_ENC) // Yade1020 ) { // Yade0916, Yade0926, Yade1020
            }
        }
    }

    override fun start(stage: Stage) {
        logger.info { "===== APP started ${Settings.VERSION} =====" }

        checkIntegrity()

        stage.initStyle(StageStyle.UNDECORATED)
        stage.width = 768.0
        stage.height = 1024.0

        var passwordEnc = Settings.password
        var password = Settings.password.decrypt(Key.pwdKey, "pw")  // Yade0924
        logger.info { "관리자 암호(in plain text/Encoded): $password($passwordEnc)" }     // Yade0924
        passwordEnc = Settings.swUpdatePassword
        password = Settings.swUpdatePassword.decrypt(Key.pwdSUKey, "sUpw")  // Yade0924
        logger.info { "SW업데이트 암호(in plain tex/Encoded): $password($passwordEnc)" }     // Yade0924
        passwordEnc = Settings.sFTPPassword
        password = Settings.sFTPPassword.decrypt(Key.pwdSFKey, "sFpw")  // Yade0924
        logger.info { "sFTP 암호(in plain text/Encoded): $password($passwordEnc)" }     // Yade0924

        // AES-256으로 암호화된 디폴트키를 복호화
        var dir = KeyHelper.keyDir2 // "./serverKeys"
        var keyName = "defaultKey"

        // 키 유효성 체크(관리자/디폴트키). UI 처리는 MainView 에서 함.
        if (KeyHelper.checkKeyIntegrity2()) { // Yade1020
            logger.info { "(관리자/디폴트) 키 무결성 체크 OK" }// [인증용로그]
        } else {
            logger.error { "(관리자/디폴트) 키 무결성 체크 Error" }// [인증용로그]
            // find(MainView::class).showIntegrityErrorDialog()   // gui플랫폼이 올라오기 전임
            return@start
        }

        var defaultKeyEnc256 = Settings.DEFAULT_KEY_AES_ENC
        logger.info("${keyName}-2 by AES-256(Enc)-${defaultKeyEnc256}")
        // 키 유효성 체크(prefs.xml에 담긴 AES_ENC 키). UI 처리는 MainView 에서 함.
        if (KeyHelper.verifyKeyFile(KeyHelper.keyDir2, keyName, defaultKeyEnc256)) { // Yade1020
            logger.info { "(디폴트 AES_ENC) 키 무결성 체크 OK" }
        } else {
            logger.error { "(디폴트 AES_ENC) 키 무결성 체크 Error" }
            // find(MainView::class).showIntegrityErrorDialog()   // gui플랫폼이 올라오기 전임
            return@start
        }
        var defaultKeyDec256 = defaultKeyEnc256.decrypt(Key.defaultKey, "default")
        logger.info("${keyName}-2 by AES-256(Enc/Dec)-${defaultKeyEnc256}/${defaultKeyDec256}")
        Settings.DEFAULT_KEY = defaultKeyDec256
        logger.info("${keyName}-3 Original-${Settings.DEFAULT_KEY}")

        keyName = "adminKey"
        var adminKeyEnc256 = Settings.ADMIN_KEY_AES_ENC
        logger.info("${keyName}-2 by AES-256(Enc)-${adminKeyEnc256}")
        if (KeyHelper.verifyKeyFile(KeyHelper.keyDir2, keyName, adminKeyEnc256)) { // Yade1020
            logger.info { "(관리자 AES_ENC) 키 무결성 체크 OK" }// [인증용로그]
        } else {
            logger.error { "(관리자 AES_ENC) 키 무결성 체크 Error" }// [인증용로그]
            // find(MainView::class).showIntegrityErrorDialog()   // gui플랫폼이 올라오기 전임
            return@start
        }
        var adminKeyDec256 = adminKeyEnc256.decrypt(Key.adminKey, "admin")
        logger.info("${keyName}-2 by AES-256(Enc/Dec)-${adminKeyEnc256}/${adminKeyDec256}")
        Settings.ADMIN_KEY = adminKeyDec256
        logger.info("${keyName}-3 Original-${Settings.ADMIN_KEY}")

        super.start(stage)
        if (isLinux) {
            stage.scene.cursor = Cursor.NONE
            IoUtils.initialize()
        }
        GlobalScope.launch(Dispatchers.Default) {
            Db.initialize()
        }

        Audio.setVolume(Settings.volume)
        Video.play("ad.mp4")

        runLater {
            Thread.setDefaultUncaughtExceptionHandler(DefaultErrorHandler())
        }
    }

    // Yade1020 - 서버키(관리서버 통신용(adminKey)/디폴트(defaultKey)) 생성
    fun createEncryptedServerKey(
        keyName: String,
        encodedKey64: String,
        key: ByteArray,
        tag: String = ""
    ) {
        KeyHelper.keyIntegrityOk2 = true    // Yade1017
        KeyHelper.renewKeys2()              // Yade1017
        logger.info("${keyName} base64(Enc) : " + encodedKey64)
        var serverKeyDec64 = String(Base64.decode(encodedKey64))
        logger.info("${keyName}0 by based64(Dec)-${serverKeyDec64}")
        var serverKeyEnc256 = serverKeyDec64.encrypt(key, tag)
        Settings.ADMIN_KEY_AES_ENC = serverKeyEnc256
        logger.info("${keyName}0 by AES-256(Enc)-${serverKeyEnc256}")
        KeyHelper.writeKeyFile3(keyName, serverKeyEnc256)
        logger.info("${keyName}1 by AES-256(Enc/Dec)-${serverKeyEnc256}/${serverKeyDec64}")
    }
}