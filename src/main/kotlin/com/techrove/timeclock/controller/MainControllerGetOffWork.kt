package com.techrove.timeclock.controller

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
import com.techrove.timeclock.security.Key
import com.techrove.timeclock.security.KeyHelper
import com.techrove.timeclock.security.encrypt
import com.techrove.timeclock.server.cwma.CwmaServer
import com.techrove.timeclock.server.cwma.model.req.CardAuthRequest
import com.techrove.timeclock.server.cwma.model.req.FingerAuthRequest
import com.techrove.timeclock.utils.playAudio
import com.techrove.timeclock.view.MainView
import com.techrove.timeclock.view.custom.IconType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.javafx.JavaFx
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import tornadofx.onChange
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private val logger = KotlinLogging.logger("MainController")

private val formatter = DateTimeFormatter.ofPattern("yyyy년 M월 d일 HH시 mm분 s초")

/**
 * 퇴근시 재시도 로직 처리
 *
 * TODO: 재시도 로직 보완
 */
private suspend fun MainController.retryGetOffWork(delay: Long? = null) {
    delay?.let { delay(delay) }
    logger.info { "퇴근 재시도: $retryCount"}

    deleteLastPhoto()

    if (retryCount < 0) {
        retryCount = 1
        return
    }
    if (retryCount < 3) {
        withContext(Dispatchers.JavaFx) {
            retryCount++
            getOffWork = true
        }
    } else {
        retryCount = 1
        on출퇴근Error()
    }
}

/**
 * 퇴근 event 처리
 */
fun MainController.initGetOffWork() {
    ///////////////////////////////////////////////////////////////////////////
    // 퇴근
    ///////////////////////////////////////////////////////////////////////////
    getOffWorkProperty.onChange {
        // 키 유효성 체크. UI 처리는 MainView 에서 함.
        if (KeyHelper.checkKeyIntegrity()
            && KeyHelper.checkKeyIntegrity2()
            && KeyHelper.verifyKeyFile(KeyHelper.keyDir2, "adminKey", Settings.ADMIN_KEY_AES_ENC)
            && KeyHelper.verifyKeyFile(KeyHelper.keyDir2, "defaultKey", Settings.DEFAULT_KEY_AES_ENC)) { // Yade1020 ) { // Yade0916, Yade0926, Yade1020
            logger.info { "무결성 체크 OK" }
        } else {
            logger.info { "무결성 체크 Error" }
            KeyHelper.keyIntegrityOk = false                    // Yade1024
            find(MainView::class).showIntegrityErrorDialog()   // Yade0926
            return@onChange
        }
        if (it) {
            photoProperty.value = null
            Audio.play("퇴근 카드를 태그하시거나 지문을 스캔해 주세요.wav")
            RfReader.onRfCardTagged = { cardNumber ->
                RfReader.onRfCardTagged = null
                getOffWork = false
                Audio.play("beep2.wav", interruptPlay = false)
                val request = CardAuthRequest(date = LocalDateTime.now(), cardNumber = cardNumber)
                launch {
                    runApi {
                        logger.info { "SW 기밀성 확인" }
                        logger.info { "온라인카드인증퇴근" }
                        CwmaServer.service.온라인카드인증퇴근(request).let { res ->
                            logger.trace { res }
                            res.playAudio("퇴근처리가 완료되었습니다.wav")
                            if (res.result.isOk) {
                                logger.info { "온라인카드인증퇴근 성공" }
                                logger.info { "SW 무결성 정상" }
                                infoMessage = InfoMessage(
                                    "근태 체크 결과",
                                    "퇴근 처리가 완료 되었습니다.",
                                    IconType.Info,
                                    mapOf("퇴근시각" to request.date.format(formatter), "성명" to res.result.message.numberMasked),
                                    imageFile = photoProperty.value
                                )
                                retryCount = 1
                            } else {
                                logger.warn { "온라인카드인증퇴근 오류 ${res.result}" }
                                infoMessage = InfoMessage(
                                    "근태 체크 결과",
                                    res.result.details,
                                    IconType.Error,
                                    imageFile = photoProperty.value
                                )
                                retryGetOffWork(3100)
                            }
                        }
                    } ?: run {
                        ///////////////////////////////////////////////////////////////////////////
                        // network 오류: DB 저장
                        ///////////////////////////////////////////////////////////////////////////

                        logger.warn { "온라인카드인증퇴근 저장" }
                        logger.info { "SW 무결성 정상" }
                        infoMessage = InfoMessage(
                            "근태 체크 결과",
                            "퇴근 처리가 완료 되었습니다.",
                            IconType.Info,
                            mapOf("퇴근시각" to request.date.format(formatter)),
                            errorMessage = "현재 오프라인 상태입니다. 관리자에게 문의바랍니다.",
                            imageFile = photoProperty.value
                        )
                        Audio.play("퇴근처리가 완료되었습니다.wav")
                        transactionWithLock {
                            TimeSheet.new {
                                terminalId = Settings.terminalId
                                placeCd = Settings.placeCd
                                date = request.date
                                type = TimeSheetType.GetOffWork
                                this.cardNumber = cardNumber.encrypt(Key.cardKey, "card")
                            }
                        }
                        retryCount = 1
                    }
                }
            }

            launch {
                getOffWorkFingerTemplate = null
                scanFingerTemplate()?.also { ft ->
                    logger.trace { ft }
                    if (getOffWork) {
                        getOffWork = false
                        withContext(Dispatchers.JavaFx) {
                            getOffWorkFingerTemplate = ft
                        }
                    }
                } ?: run {
                    if (getOffWork && RfReader.onRfCardTagged != null) {
                        getOffWork = false
                        retryGetOffWork()
                    }
                }
            }
        } else {
            launch { scanFingerTemplate(false) }
            RfReader.onRfCardTagged = null
        }
    }

    getOffWorkByFingerProperty.onChangeTrue {
        // 키 유효성 체크. UI 처리는 MainView 에서 함.
        if (KeyHelper.checkKeyIntegrity()
            && KeyHelper.checkKeyIntegrity2()
            && KeyHelper.verifyKeyFile(KeyHelper.keyDir2, "adminKey", Settings.ADMIN_KEY_AES_ENC)
            && KeyHelper.verifyKeyFile(KeyHelper.keyDir2, "defaultKey", Settings.DEFAULT_KEY_AES_ENC)) { // Yade1020 ) { // Yade0916, Yade0926, Yade1020
            logger.info { "무결성 체크 OK" }
        } else {
            logger.info { "무결성 체크 Error" }
            KeyHelper.keyIntegrityOk = false                    // Yade1024
            find(MainView::class).showIntegrityErrorDialog()   // Yade0926
            return@onChangeTrue
        }
        launch {
            getOffWorkByFinger = false
            var request: FingerAuthRequest? = null
            runApi {
                logger.info { "SW 기밀성 확인" }
                logger.info { "온라인지문인증퇴근" }
                val authRequest = FingerAuthRequest(
                    date = LocalDateTime.now(),
                    dob = dobModel.residentNumber.value.dob,
                    finger = getOffWorkFingerTemplate
                )
                request = authRequest
                CwmaServer.service.온라인지문인증퇴근(authRequest).let { res ->
                    logger.trace { res }
                    res.playAudio("퇴근처리가 완료되었습니다.wav")
                    if (res.result.isOk) {
                        logger.info { "온라인지문인증퇴근 성공" }
                        logger.info { "SW 무결성 정상" }
                        infoMessage = InfoMessage(
                            "근태 체크 결과",
                            "퇴근 처리가 완료 되었습니다.",
                            IconType.Info,
                            mapOf("퇴근시각" to authRequest.date.format(formatter), "성명" to res.result.message.numberMasked),
                            imageFile = photoProperty.value
                        )
                        retryCount = 1
                    } else {
                        logger.warn { "온라인지문인증퇴근 오류 ${res.result}" }
                        infoMessage = InfoMessage(
                            "근태 체크 결과",
                            res.result.details,
                            IconType.Error,
                            imageFile = photoProperty.value
                        )
                        retryGetOffWork(3100)
                    }
                }
            } ?: run {
                ///////////////////////////////////////////////////////////////////////////
                // network 오류: DB 저장
                ///////////////////////////////////////////////////////////////////////////

                val authRequest = request ?: return@launch
                logger.warn { "온라인지문인증퇴근 저장" }
                logger.info { "SW 무결성 정상" }
                infoMessage = InfoMessage(
                    "근태 체크 결과",
                    "퇴근 처리가 완료 되었습니다.",
                    IconType.Info,
                    mapOf("퇴근시각" to authRequest.date.format(formatter)),
                    errorMessage = "현재 오프라인 상태입니다. 관리자에게 문의바랍니다.",
                    imageFile = photoProperty.value
                )
                Audio.play("퇴근처리가 완료되었습니다.wav")
                transactionWithLock {
                    TimeSheet.new {
                        terminalId = Settings.terminalId
                        placeCd = Settings.placeCd
                        date = authRequest.date
                        type = TimeSheetType.GetOffWork
                        fingerPrint = authRequest.finger.encrypt(Key.fingerKey, "finger")
                        dob = authRequest.dob.encrypt(Key.idsnKey, "idsn")
                    }
                }
                retryCount = 1
            }
        }
    }
}
