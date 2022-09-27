package com.techrove.timeclock.controller.admin

import com.techrove.timeclock.Settings
import com.techrove.timeclock.controller.BaseController
import com.techrove.timeclock.io.*
import com.techrove.timeclock.server.cwma.CwmaServer
import javafx.beans.property.SimpleStringProperty
import kotlinx.coroutines.*
import mu.KotlinLogging
import tornadofx.getValue
import tornadofx.setValue
import tornadofx.toProperty
import java.io.File

private val logger = KotlinLogging.logger("EmiTestController")

/**
 * 관리자 설정 / EMI 테스트  controller
 */
class EmiTestController: BaseController() {
    private var job: Job? = null

    val delayMin = 0L
    val delayMax = 60L
    val delayProperty = 0L.toProperty()
    var delay by delayProperty

    val photoProperty = SimpleStringProperty(null)

    override fun stop() {
        job?.cancel()
        logger.info { "#### EMI 테스트 종료 ####" }
        launch(Dispatchers.IO) {
            Suprema.cancel(2000, true)
        }
    }

    private suspend fun wait() {
        delay(if (delay == 0L) 300 else (delay * 1000))
    }

    fun startTest() {
        logger.info { "#### EMI 테스트 시작 ####" }
        job?.cancel()
        job = launch(Dispatchers.IO) {
            while (isActive) {
                try {
                    logger.info { "1. LTE 라우터" }
//                    CwmaServer.key = Settings.DEFAULT_KEY   // Yade0927
                    CwmaServer.service.보안키등록확인().let { res ->
                        logger.trace { res }
                        if (res.result.isOk) {
                            CwmaServer.key = res.data.kmsKey
                            CwmaServer.version = res.data.kmsVersion
                        }
                    }
                    wait()

                    logger.info { "2. 키패드" }
                    KeyPad.checkHw()
                    wait()

                    logger.info { "3. RF 카드리더기" }
                    RfReader.checkHw()
                    wait()

                    logger.info { "4. 지문 모듈" }
                    launch(Dispatchers.IO) {
                        delay(3000)
                        Suprema.cancel(2000, true)
                    }
                    Suprema.scanTemplate()
                    wait()

                    logger.info { "5. 온도 센서" }
                    TempSensor.checkHw()
                    wait()

                    logger.info { "6. 카메라" }
                    photoProperty.value = null
                    Camera.takePicture(File("./temp.jpg")).let {
                        if (it) {
                            photoProperty.value = File("./temp.jpg").toURI().toString()
                            logger.info { "이미지가 저장 되었습니다." }
                        }
                    }
                    wait()
                } catch (e: Exception) {
                    logger.error { e }
                }
            }
        }
    }
}
