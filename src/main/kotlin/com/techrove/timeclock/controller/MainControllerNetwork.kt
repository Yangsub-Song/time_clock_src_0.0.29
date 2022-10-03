package com.techrove.timeclock.controller

import com.techrove.timeclock.Settings
import com.techrove.timeclock.database.Db
import com.techrove.timeclock.database.TimeSheet
import com.techrove.timeclock.database.TimeSheetType
import com.techrove.timeclock.database.transactionWithLock
import com.techrove.timeclock.extensions.applyToSystem
import com.techrove.timeclock.extensions.safeLet
import com.techrove.timeclock.security.Key
import com.techrove.timeclock.security.KeyHelper
import com.techrove.timeclock.security.decrypt
import com.techrove.timeclock.server.cwma.CwmaServer
import com.techrove.timeclock.server.cwma.model.req.CardAuthRequest
import com.techrove.timeclock.server.cwma.model.req.FingerAuthRequest
import com.techrove.timeclock.view.MainView
import kotlinx.coroutines.*
import mu.KotlinLogging
import tornadofx.onChange
import java.net.Inet4Address

private val logger = KotlinLogging.logger("MainController")

private var serverMonitorJob: Job? = null

/**
 * network 관려 처리
 *
 * - network 상태 모니터링
 * - 서버 상태 모니터링
 * - 서버 on 시 미전송 전문 전송
 */
@Suppress("BlockingMethodInNonBlockingContext")
fun MainController.initNetwork() {
    ///////////////////////////////////////////////////////////////////////////
    // NETWORK 상태 MONITORING
    ///////////////////////////////////////////////////////////////////////////
    
    launch {
        while (isActive) {
            networkOn = try {
                Inet4Address.getByName("www.google.com").isReachable(5000)
            } catch (e: Exception) {
                false
            }
            delay(10000)
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // SERVER 상태 MONITORING
    //  보안키 등록
    ///////////////////////////////////////////////////////////////////////////
    
    networkOnProperty.onChange {
        logger.info { "NETWORK ${if (it) "ON" else "OFF"}" }
        if (it) {
            serverMonitorJob?.cancel()
            serverMonitorJob = launch {
                while (!serverOn) {
                    try {
                        logger.info { "보안키등록확인(서버 상태 모니터링...)" }
                        CwmaServer.key = Settings.DEFAULT_KEY // _DEC
                        CwmaServer.service.보안키등록확인().let { res ->
                            logger.trace { res }
                            if (res.result.isOk) {
                                logger.info { "보안키등록확인 성공" }
                                CwmaServer.key = res.data.kmsKey
                                CwmaServer.version = res.data.kmsVersion
                                serverOn = true
                                logger.warn { "서버 ON" }
                                while (serverOn) {
                                    try {
                                        logger.info { "단말기상태" }
                                        CwmaServer.service.단말기상태().let { response ->
                                            logger.trace { response }
                                            if (!response.result.isOk) {
                                                logger.warn { "단말기상태 오류 ${response.result}" }
                                                logger.warn { "서버 OFF" }
                                                serverOn = false
                                            }
                                        }
                                    } catch (e: Exception) {
                                        logger.warn { "단말기상태 오류 $e" }
                                        logger.warn { "서버 OFF" }
                                        serverOn = false
                                    }

                                    try {
                                        logger.info { "시간조회" }
                                        CwmaServer.service.시간조회().let { response ->
                                            logger.trace { response }
                                            if (response.result.isOk) {
                                                response.data.dateTime.applyToSystem(1)
                                            }
                                        }
                                    } catch (e: Exception) {
                                        logger.warn { "시간조회 오류 $e" }
                                        logger.warn { "서버 OFF" }
                                        serverOn = false
                                    }
                                    if (serverOn) {
                                        // 1 hour : online 상태
                                        delay(60 * 60 * 1000)
                                        //delay(10 * 1000)
                                    }
                                }
                            }
                        }
                    } catch (e: Exception) {
                        logger.error { e }
                    }
                    // 1 minute : offline 상태
                    delay(1 * 60 * 1000)
                }
            }
        } else {
            logger.warn { "서버 OFF" }
            serverOn = false
            serverMonitorJob?.cancel()
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // 미전송 전문 처리
    ///////////////////////////////////////////////////////////////////////////
    
    serverOnProperty.onChange { on ->
        if (!on) return@onChange

        // 키 유효성 체크. UI 처리는 MainView 에서 함.
        if (KeyHelper.checkKeyIntegrity()) { // Yade0916
            logger.info { "무결성 체크 OK(오프라인카드/지문 인증 출근)" }
        } else {
            logger.info { "무결성 체크 Error(오프라인카드/지문 인증 출근)" }
            find(MainView::class).showIntegrityErrorDialog()   // Yade0926
            return@onChange
        }
        launch {
            withTimeoutOrNull(30000) {
                while (!Db.isReady) {
                    delay(1000)
                }
            } ?: return@launch
            val records = transactionWithLock { TimeSheet.all().sortedBy { it.date } }
            records.forEachIndexed { i, record ->
                delay(1000)

                // server off 면 전송 중지
                if (!serverOn) return@launch
                logger.info { "[${i + 1}/${records.size}] $record" }

                when (record.type) {
                    TimeSheetType.GoingToWork -> {
                        runApi {
                            record.cardNumber?.let { cardNumber ->
                                logger.info { "오프라인카드인증출근" }
                                val request =
                                    CardAuthRequest(
                                        date = record.date,
                                        cardNumber = cardNumber.decrypt(Key.cardKey, "card")
                                    )
                                CwmaServer.service.오프라인카드인증출근(request).let { res ->
                                    logger.trace { res }
                                    if (res.result.isOk) {
                                        logger.info { "오프라인카드인증출근 성공" }
                                    } else {
                                        logger.warn { "오프라인카드인증출근 오류 ${res.result}" }
                                    }
                                }
                            }
                            safeLet(record.fingerPrint, record.dob) { fingerPrint, dob ->
                                logger.info { "오프라인지문인증출근" }
                                val request = FingerAuthRequest(
                                    date = record.date,
                                    dob = dob.decrypt(Key.idsnKey, "idsn"),
                                    finger = fingerPrint.decrypt(Key.fingerKey, "finger")
                                )
                                CwmaServer.service.오프라인지문인증출근(request).let { res ->
                                    logger.trace { res }
                                    if (res.result.isOk) {
                                        logger.info { "오프라인지문인증출근 성공" }
                                    } else {
                                        logger.warn { "오프라인지문인증출근 오류 ${res.result}" }
                                    }
                                }
                            }
                            transactionWithLock { record.delete() }
                        }
                    }
                    TimeSheetType.GetOffWork -> {
                        runApi {
                            record.cardNumber?.let { cardNumber ->
                                logger.info { "오프라인카드인증퇴근" }
                                val request =
                                    CardAuthRequest(
                                        date = record.date,
                                        cardNumber = cardNumber.decrypt(Key.cardKey, "card")
                                    )
                                CwmaServer.service.오프라인카드인증퇴근(request).let { res ->
                                    logger.trace { res }
                                    if (res.result.isOk) {
                                        logger.info { "오프라인카드인증퇴근 성공" }
                                    } else {
                                        logger.warn { "오프라인카드인증퇴근 오류 ${res.result}" }
                                    }
                                }
                            }
                            safeLet(record.fingerPrint, record.dob) { fingerPrint, dob ->
                                logger.info { "오프라인지문인증퇴근" }
                                val request = FingerAuthRequest(
                                    date = record.date,
                                    dob = dob.decrypt(Key.idsnKey, "idsn"),
                                    finger = fingerPrint.decrypt(Key.fingerKey, "finger")
                                )
                                CwmaServer.service.오프라인지문인증퇴근(request).let { res ->
                                    logger.trace { res }
                                    if (res.result.isOk) {
                                        logger.info { "오프라인지문인증퇴근 성공" }
                                    } else {
                                        logger.warn { "오프라인지문인증퇴근 오류 ${res.result}" }
                                    }
                                }
                            }
                            transactionWithLock { record.delete() }
                        }
                    }
                }
            }
        }
    }
}