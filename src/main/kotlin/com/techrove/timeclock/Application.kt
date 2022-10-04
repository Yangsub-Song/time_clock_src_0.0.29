package com.techrove.timeclock

import com.techrove.timeclock.database.Db
import com.techrove.timeclock.io.Audio
import com.techrove.timeclock.io.Video
import com.techrove.timeclock.security.KeyHelper
import com.techrove.timeclock.utils.IoUtils
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

        // 디폴트키(DEFAULT_KEY) 무결성 체크
        if (!KeyHelper.checkFileSha("./serverKey/defaultKey.bin")) {
            logger.info("디폴트키에서 무결성 오류가 발생했습니다.")
        } else
            logger.info("디폴트키 무결성 체크 OK.")

        var dir = "./serverKey"
        var name = "defaultKey"
        var temp = File(dir, "${name}.bin").readText()
        logger.info("디폴트키-${temp}")
        var base64EncData = Settings.DEFAULT_KEY_ENC // preference(userPref, "DEFAULT_KEY_ENC", "")
//        temp = Settings.DEFAULT_KEY_ENC
        logger.info("Yade1004-${base64EncData}")
//        if (KeyHelper.verifyKeyFile(dir, name, base64EncData.toByteArray()) == null)
//            logger.info("Integrity Error!")
//        else
//            logger.info("OK, No Integrity Error!")
//        logger.info("BASE64 암호화 된 값(DEFAULT_KEY_ENC from UserPrefs) : " + base64EncData)
        var base64DecData = Base64.decode(base64EncData)
        Settings.DEFAULT_KEY = String(base64DecData)
        logger.info("BASE64 복호화 된 값(DEFAULT_KEY_DEC) : " + Settings.DEFAULT_KEY)

        // 관리자키(ADMIN_KEY) 무결성 체크
        if (!KeyHelper.checkFileSha("./serverKey/adminKey.bin")) {
            logger.info("관리자키에서 무결성 오류가 발생했습니다.")
        } else
            logger.info("관리자키 무결성 OK.")
        name = "admintKey"
//        temp = File(dir, "${name}.bin").readText()
        var base64EncData2 = Settings.ADMIN_KEY_ENC
        logger.info("BASE64 암호화 된 값(ADMIN_KEY_ENC) : " + base64EncData2)
        var base64DecData2 = Base64.decode(base64EncData2)
        Settings.ADMIN_KEY = String(base64DecData2)
        logger.info("BASE64 복호화 된 값(ADMIN_KEY) : " + Settings.ADMIN_KEY)

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

