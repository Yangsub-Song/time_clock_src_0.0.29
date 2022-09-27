package com.techrove.timeclock.controller

import com.techrove.timeclock.view.MainView             // Yade0926
import com.techrove.timeclock.Settings
import com.techrove.timeclock.controller.model.InfoMessage
import com.techrove.timeclock.database.TimeSheet
import com.techrove.timeclock.database.TimeSheetType
import com.techrove.timeclock.database.transactionWithLock
import com.techrove.timeclock.extensions.dob
import com.techrove.timeclock.extensions.numberMasked
import com.techrove.timeclock.extensions.onChangeTrue
import com.techrove.timeclock.io.Audio
import com.techrove.timeclock.io.RfReader
import com.techrove.timeclock.io.TempSensor
import com.techrove.timeclock.security.Key
import com.techrove.timeclock.security.KeyHelper
import com.techrove.timeclock.security.encrypt
import com.techrove.timeclock.server.cwma.CwmaServer
import com.techrove.timeclock.server.cwma.model.req.CardAuthRequest
import com.techrove.timeclock.server.cwma.model.req.FingerAuthRequest
import com.techrove.timeclock.utils.playAudio
import com.techrove.timeclock.view.custom.IconType
import kotlinx.coroutines.*
import kotlinx.coroutines.javafx.JavaFx
import mu.KotlinLogging
import tornadofx.millis
import tornadofx.onChange
import tornadofx.runLater
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private val logger = KotlinLogging.logger("MainController")

private val formatter = DateTimeFormatter.ofPattern("yyyy년 M월 d일 HH시 mm분 s초")

/**
 * 출근시 재시도 로직 처리
 *
 * TODO: 재시도 로직 보완
 */
private suspend fun MainController.retryGotoWork(delay: Long? = null) {
    delay?.let { delay(delay) }
    logger.info { "출근 재시도: $retryCount"}

    deleteLastPhoto()

    if (retryCount < 0) {
        retryCount = 1
        return
    }
    if (retryCount < 3) {
        withContext(Dispatchers.JavaFx) {
            retryCount++
            gotoWork = true
        }
    } else {
        retryCount = 1
        on출퇴근Error()
    }
}

/**
 * 출근 event 처리
 */
fun MainController.initGotoWork() {
    ///////////////////////////////////////////////////////////////////////////
    // 체온 측정
    ///////////////////////////////////////////////////////////////////////////

    var job: Job? = null
    measureTemperatureProperty.onChange {
        job?.cancel()
        if (it) {
            job = launch(Dispatchers.JavaFx) {
                while (isActive) {
                    val temp = withContext(Dispatchers.Default) { TempSensor.getTemperature() }
                    if (temp == null) {
                        // 온도 센서 timeout
                        temperature = 0f
                        temperatureCount++
                        Audio.play("beep3.wav", interruptPlay = false)
                        on출퇴근Error()
                        break
                    } else {
                        val valid = temp >= 35.0
                        val overTemperature = temp >= 38.0
                        if (valid) {
                            temperature = temp
                            temperatureCount++
                            if (overTemperature) {
                                temperatureOver = true
                                Audio.play("beep3.wav", interruptPlay = false)
                                delay(3000)
                                if (Settings.measureTemperatureOption == 2) {
                                    runLater(500.millis) { gotoWork = true }
                                    break
                                }
                            } else {
                                temperatureOver = false
                                delay(3000)
                                gotoWork = true
                                break
                            }
                        }
                    }
                    if (temperatureCount == 3) {
                        delay(1000)
                        on출퇴근Error()
                        break
                    }
                    delay(200)
                }
                temperatureCount = 0
                temperatureOver = false
                measureTemperature = false
            }
        } else {
            temperatureCount = 0
            temperatureOver = false
            measureTemperature = false
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // 출근
    ///////////////////////////////////////////////////////////////////////////
    gotoWorkProperty.onChange {
        // 키 유효성 체크. UI 처리는 MainView 에서 함.
        if (KeyHelper.checkKeyIntegrity()) { // Yade0916
            logger.info { "무결성 체크 OK(온라인카드 인증 출근)" }
        } else {
            logger.info { "무결성 체크 Error(온라인카드 인증 출근)" }
            find(MainView::class).showIntegrityErrorDialog()   // Yade0926
//            infoMessage = InfoMessage(              // Yade0922
//                "무결성 체크 결과",
//                "무결성 체크 Error(온라인카드 인증 출근)",
//                IconType.Error,
//                imageFile = photoProperty.value
//            )
            return@onChange
        }
        if (it) {
            photoProperty.value = null
            Audio.play("출근 카드를 태그하시거나 지문을 스캔해 주세요.wav")
            RfReader.onRfCardTagged = { cardNumber ->
                RfReader.onRfCardTagged = null
                gotoWork = false
                Audio.play("beep2.wav", interruptPlay = false)
                val request = CardAuthRequest(date = LocalDateTime.now(), cardNumber = cardNumber)
                launch {
                    runApi {
                        logger.info { "SW 기밀성 확인" }
                        logger.info { "온라인카드인증출근" }
                        CwmaServer.service.온라인카드인증출근(request).let { res ->
                            logger.trace { res }
                            res.playAudio("출근처리가 완료되었습니다.wav")
                            if (res.result.isOk) {
                                logger.info { "온라인카드인증출근 성공" }
                                logger.info { "SW 무결성 정상" }
                                infoMessage = InfoMessage(
                                    "근태 체크 결과",
                                    "출근 처리가 완료 되었습니다.",
                                    IconType.Info,
                                    mapOf("출근시각" to request.date.format(formatter), "성명" to res.result.message.numberMasked),
                                    imageFile = photoProperty.value
                                )
                                retryCount = 1
                            } else {
                                logger.warn { "온라인카드인증출근 오류 ${res.result}" }
                                infoMessage = InfoMessage(
                                    "근태 체크 결과",
                                    res.result.details,
                                    IconType.Error,
                                    imageFile = photoProperty.value
                                )
                                retryGotoWork(3100)
                            }
                        }
                    } ?: run {
                        ///////////////////////////////////////////////////////////////////////////
                        // network 오류: DB 저장
                        ///////////////////////////////////////////////////////////////////////////
                        logger.warn { "온라인카드인증출근 저장" }
                        logger.info { "SW 무결성 정상" }

                        infoMessage = InfoMessage(
                            "근태 체크 결과",
                            "출근 처리가 완료 되었습니다.",
                            IconType.Info,
                            mapOf("출근시각" to request.date.format(formatter)),
                            errorMessage = "현재 오프라인 상태입니다. 관리자에게 문의바랍니다.",
                            imageFile = photoProperty.value
                        )
                        Audio.play("출근처리가 완료되었습니다.wav")
                        transactionWithLock {
                            TimeSheet.new {
                                terminalId = Settings.terminalId
                                placeCd = Settings.placeCd
                                date = request.date
                                type = TimeSheetType.GoingToWork
                                this.cardNumber = cardNumber.encrypt(Key.cardKey, "card")
                            }
                        }
                        retryCount = 1
                    }
                }
            }

            launch {
                gotoWorkFingerTemplate = null
                scanFingerTemplate()?.also { ft ->
                    logger.trace { ft }
                    if (gotoWork) {
                        gotoWork = false
                        withContext(Dispatchers.JavaFx) {
                            gotoWorkFingerTemplate = ft
                        }
                    }
                } ?: run {
                    if (gotoWork && RfReader.onRfCardTagged != null) {
                        gotoWork = false
                        retryGotoWork()
                    }
                }
            }
        } else {
            launch { scanFingerTemplate(false) }
            RfReader.onRfCardTagged = null
        }
    }

    gotoWorkByFingerProperty.onChangeTrue {
        // 키 유효성 체크. UI 처리는 MainView 에서 함.
        if (KeyHelper.checkKeyIntegrity()) { // Yade0916
            logger.info { "무결성 체크 OK(온라인지문 인증 출근)" }
        } else {
            logger.info { "무결성 체크 Error(온라인지문 인증 출근)" }
            find(MainView::class).showIntegrityErrorDialog()   // Yade0926
            return@onChangeTrue
        }
        launch {
            gotoWorkByFinger = false
            var request: FingerAuthRequest? = null
            runApi {
                logger.info { "SW 기밀성 확인" }
                logger.info { "온라인지문인증출근" }
                val authRequest = FingerAuthRequest(
                    date = LocalDateTime.now(),
                    dob = dobModel.residentNumber.value.dob,
                    finger = gotoWorkFingerTemplate
                )
                request = authRequest
                CwmaServer.service.온라인지문인증출근(authRequest).let { res ->
                    logger.trace { res }
                    res.playAudio("출근처리가 완료되었습니다.wav")
                    if (res.result.isOk) {
                        logger.info { "온라인지문인증출근 성공" }
                        logger.info { "SW 무결성 정상" }
                        infoMessage = InfoMessage(
                            "근태 체크 결과",
                            "출근 처리가 완료 되었습니다.",
                            IconType.Info,
                            mapOf("출근시각" to authRequest.date.format(formatter), "성명" to res.result.message.numberMasked),
                            imageFile = photoProperty.value
                        )
                        retryCount = 1
                    } else {
                        logger.warn { "온라인지문인증출근 오류 ${res.result}" }
                        infoMessage = InfoMessage(
                            "근태 체크 결과",
                            res.result.details,
                            IconType.Error,
                            imageFile = photoProperty.value
                        )
                        retryGotoWork(3100)
                    }
                }
            } ?: run {
                ///////////////////////////////////////////////////////////////////////////
                // network 오류: DB 저장
                ///////////////////////////////////////////////////////////////////////////

                val authRequest = request ?: return@launch
                logger.warn { "온라인지문인증출근 저장" }
                logger.info { "SW 무결성 정상" }
                infoMessage = InfoMessage(
                    "근태 체크 결과",
                    "출근 처리가 완료 되었습니다.",
                    IconType.Info,
                    mapOf("출근시각" to authRequest.date.format(formatter)),
                    errorMessage = "현재 오프라인 상태입니다. 관리자에게 문의바랍니다.",
                    imageFile = photoProperty.value
                )
                Audio.play("출근처리가 완료되었습니다.wav")
                transactionWithLock {
                    TimeSheet.new {
                        terminalId = Settings.terminalId
                        placeCd = Settings.placeCd
                        date = authRequest.date
                        type = TimeSheetType.GoingToWork
                        fingerPrint = authRequest.finger.encrypt(Key.fingerKey, "finger")
                        dob = authRequest.dob.encrypt(Key.idsnKey, "idsn")
                    }
                }
                retryCount = 1
            }
        }
    }
}
