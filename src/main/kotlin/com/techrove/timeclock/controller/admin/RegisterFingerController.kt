package com.techrove.timeclock.controller.admin

import com.techrove.timeclock.controller.BaseController
import com.techrove.timeclock.controller.model.Person
import com.techrove.timeclock.controller.model.PersonModel
import com.techrove.timeclock.extensions.decodeHex
import com.techrove.timeclock.io.Suprema
import com.techrove.timeclock.isLinux
import com.techrove.timeclock.server.cwma.CwmaServer
import com.techrove.timeclock.server.cwma.model.req.RegisterFingerRequest
import com.techrove.timeclock.server.cwma.model.res.Response
import javafx.beans.property.SimpleObjectProperty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.javafx.JavaFx
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import tornadofx.getValue
import tornadofx.onChange
import tornadofx.setValue
import tornadofx.toProperty
import java.util.*

private val logger = KotlinLogging.logger("RegisterFingerController")

/**
 * 지문 등록 controller
 */
class RegisterFingerController: BaseController() {

    /** 지문 등록 flag property */
    val fingerRegisterProperty = false.toProperty()
    var fingerRegister by fingerRegisterProperty

    /** 지문 인식 flag property */
    val fingerScanningProperty = false.toProperty()
    var fingerScanning by fingerScanningProperty

    /** 인식한 지문 template 정보 property */
    val fingerTemplateProperty = "".toProperty()
    var fingerTemplate by fingerTemplateProperty

    /** 재시도 count */
    val retryCountProperty = 1.toProperty()
    var retryCount by retryCountProperty

    /** 지문 등록할 사용자 정보 model */
    val personModel = PersonModel(Person())

    enum class FingerRegistrationStatus {
        Success,
        Fail
    }

    /** 지문 등록 결과 property */
    val fingerRegistrationResponseProperty = SimpleObjectProperty<Response>(null)
    var fingerRegistrationResponse by fingerRegistrationResponseProperty

    init {
        // 지문 scan
        fingerScanningProperty.onChange {
            if (it) {
                launch(Dispatchers.IO) {
                    var response: Response? = null
                    runApi {
                        scanFingerTemplate()?.also {
                            logger.trace { "finger template: $it" }
                            if (!fingerScanning) return@also
                            fingerTemplate = it

                            logger.info { "지문 등록" }
                            val request = RegisterFingerRequest(
                                residentNo = personModel.residentNumber.value,
                                phoneNumber = personModel.phoneNumber.value,
                                finger = fingerTemplate
                            )
                            CwmaServer.service.지문등록(
                                request
                            ).let { res ->
                                logger.trace { res }
                                response = res
                                if (!fingerScanning) return@let
                                if (res.result.isOk) {
                                    logger.info { "지문 등록 성공" }
                                } else {
                                    logger.warn { "지문 등록 오류 ${res.result}" }
                                }
                            }
                        }
                    }
                    fingerScanning = false
                    withContext(Dispatchers.JavaFx) {
                        if (response?.result?.isOk == true) {
                            retryCount = 1
                            fingerRegistrationResponse = response
                        } else {
                            if (retryCount < 0) {
                                retryCount = 1
                                return@withContext
                            }
                            fingerRegistrationResponse = response
                            delay(3000)
                            if (retryCount in 1..2) {
                                retryCount++
                                fingerRegister = true
                            } else {
                                retryCount = 1
                            }
                        }
                    }
                }
            } else {
                fingerRegistrationResponse = null
                launch(Dispatchers.IO) { scanFingerTemplate(false) }
            }
        }
    }

    private suspend fun scanFingerTemplate(scan: Boolean = true): String? {
        if (!isLinux) {
            delay(3000)
            return if (scan) {
                Base64.getEncoder()
                    .encodeToString("45171219910055463743f0198e3d44b0140c2505617a8737057070873d45d068881285e179872686412084110740260b1c07611d8427c89078890d8920258828c9b125092c0b008189344b612f8e2ccb70328a414b8054bc0d4ba081853a4cf1460e0e8d012205098d302e872b0e204186318e803d8d2dcef1960affffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffbbbcfffffffffbaaaabbbbcfffffff9aaaaabbbbccfffffa99aaaaaaabcccdfffa9999aaaaaabccdfffa9999999999abbffff9999998888889bfff89988888877764ffff88888887776643ffff88877777766533fffff887777666553ffffff77766666654ffffffff7fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff0eefff000000000000000000000000000000000000000000000000000000000000000000000000".decodeHex())
            } else {
                null
            }
        } else {
            return if (scan) {
                Suprema.scanTemplate()?.let { Base64.getEncoder().encodeToString(it) }
            } else {
                Suprema.cancel()
                null
            }
        }
    }
}