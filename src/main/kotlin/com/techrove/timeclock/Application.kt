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

        var base64EncData = Settings.DEFAULT_KEY_ENC // preference(userPref, "DEFAULT_KEY_ENC", "")
        logger.info("BASE64 암호화 된 값(DEFAULT_KEY_ENC from UserPrefs) : " + base64EncData)
        var base64DecData = Base64.decode(base64EncData.toString())
        Settings.DEFAULT_KEY = String(base64DecData)
        logger.info("BASE64 복호화 된 값(DEFAULT_KEY_DEC) : " + Settings.DEFAULT_KEY)
        var base64EncData2 = Settings.ADMIN_KEY_ENC                                              // "WnZr4u7x!A%D*G-K"
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

