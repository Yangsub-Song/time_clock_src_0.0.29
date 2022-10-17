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
            }
        }
    }
    override fun start(stage: Stage) {
        logger.info { "===== APP started ${Settings.VERSION} =====" }

        checkIntegrity()

        stage.initStyle(StageStyle.UNDECORATED)
        stage.width = 768.0
        stage.height = 1024.0

//
//        keyName = "adminKey"
//        var adminKeyDec = File(dir, "${keyName}.dec").readText()
//        var adminKeyEnc = adminKeyDec.encrypt(Key.tmsKey, "tms")
//        File(dir, "${keyName}.enc").writeText(adminKeyEnc)
//        logger.info("관리자키1(Enc/Dec)-${adminKeyEnc}/${adminKeyDec}")
//        adminKeyEnc = File(dir, "${keyName}.enc").readText()
//        adminKeyDec = adminKeyEnc.decrypt(Key.tmsKey, "tms")
//        logger.info("관리자키2(Enc/Dec)-${adminKeyEnc}/${adminKeyDec}")


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
        var dir = "./keys"
        var keyName = "defaultKey"
        var defaultKeyEnc = Settings.DEFAULT_KEY_AES_ENC
        var defaultKeyDec = defaultKeyEnc.decrypt(Key.defaultKey)
        logger.info("${keyName}-2 by AES-256(Enc/Dec)-${defaultKeyEnc}/${defaultKeyDec}")
        Settings.DEFAULT_KEY = defaultKeyDec
        logger.info("${keyName}-3 Original-${Settings.DEFAULT_KEY}")

        // AES-256으로 암호화된 관리자키를 복호화
        keyName = "adminKey"
        var adminKeyEnc = Settings.ADMIN_KEY_AES_ENC
        var adminKeyDec = adminKeyEnc.decrypt(Key.adminKey)
        logger.info("${keyName}-2 by AES-256(Enc/Dec)-${adminKeyEnc}/${adminKeyDec}")
        Settings.ADMIN_KEY = adminKeyDec
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
}

