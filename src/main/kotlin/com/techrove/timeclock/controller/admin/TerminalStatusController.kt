package com.techrove.timeclock.controller.admin

import com.techrove.timeclock.Settings
import com.techrove.timeclock.controller.BaseController
import com.techrove.timeclock.controller.model.DeviceCheckStatus
import com.techrove.timeclock.io.*
import com.techrove.timeclock.server.admin.AdminServer
import com.techrove.timeclock.server.cwma.CwmaServer
import javafx.beans.property.SimpleObjectProperty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.javafx.JavaFx
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mu.KotlinLogging

private val logger = KotlinLogging.logger("TerminalStatusController")

/**
 * 관리자 설정 / 단말기 상태 controller
 */
class TerminalStatusController: BaseController() {
    private var job: Job? = null

    val server1CheckProperty = SimpleObjectProperty<DeviceCheckStatus>()
    val server2CheckProperty = SimpleObjectProperty<DeviceCheckStatus>()
    val keyboardCheckProperty = SimpleObjectProperty<DeviceCheckStatus>()
    val supremaCheckProperty = SimpleObjectProperty<DeviceCheckStatus>()
    val tempSensorCheckProperty = SimpleObjectProperty<DeviceCheckStatus>()
    val cameraCheckProperty = SimpleObjectProperty<DeviceCheckStatus>()
    val rfReaderCheckProperty = SimpleObjectProperty<DeviceCheckStatus>()
    val gotoWorkButtonProperty = SimpleObjectProperty<DeviceCheckStatus>()
    val getOffWorkButtonProperty = SimpleObjectProperty<DeviceCheckStatus>()

    fun checkDevice() {
        server1CheckProperty.value = DeviceCheckStatus.DeviceChecking
        server2CheckProperty.value = DeviceCheckStatus.DeviceChecking
        keyboardCheckProperty.value = DeviceCheckStatus.DeviceChecking
        supremaCheckProperty.value = DeviceCheckStatus.DeviceChecking
        tempSensorCheckProperty.value = DeviceCheckStatus.DeviceChecking
        cameraCheckProperty.value = DeviceCheckStatus.DeviceChecking
        rfReaderCheckProperty.value = DeviceCheckStatus.DeviceChecking
        gotoWorkButtonProperty.value = DeviceCheckStatus.DeviceChecking
        getOffWorkButtonProperty.value = DeviceCheckStatus.DeviceChecking

        job?.cancel()
        job = launch(Dispatchers.IO) {
            var ok = false
            try {
                logger.info { "보안키등록확인" }
                CwmaServer.key = Settings.DEFAULT_KEY
                CwmaServer.service.보안키등록확인().let { res ->
                    logger.trace { res }
                    if (res.result.isOk) {
                        CwmaServer.key = res.data.kmsKey
                        CwmaServer.version = res.data.kmsVersion
                        ok = true
                    }
                }
            } catch (e: Exception) {
                logger.error { e }
            }
            server1CheckProperty.value =
                if (ok) DeviceCheckStatus.DeviceOk else DeviceCheckStatus.DeviceError
            server2CheckProperty.value =
                if (AdminServer.단말기정보().first != null) DeviceCheckStatus.DeviceOk else DeviceCheckStatus.DeviceError

            KeyPad.checkHw().let {
                withContext(Dispatchers.JavaFx) {
                    keyboardCheckProperty.value =
                        if (it) DeviceCheckStatus.DeviceOk else DeviceCheckStatus.DeviceError
                }
            }
            RfReader.checkHw().let {
                withContext(Dispatchers.JavaFx) {
                    rfReaderCheckProperty.value =
                        if (it) DeviceCheckStatus.DeviceOk else DeviceCheckStatus.DeviceError
                }
            }
            Suprema.checkHw().let {
                withContext(Dispatchers.JavaFx) {
                    supremaCheckProperty.value =
                        if (it) DeviceCheckStatus.DeviceOk else DeviceCheckStatus.DeviceError
                }
            }
            TempSensor.checkHw().let {
                withContext(Dispatchers.JavaFx) {
                    tempSensorCheckProperty.value =
                        if (it) DeviceCheckStatus.DeviceOk else DeviceCheckStatus.DeviceError
                }
            }
            Camera.checkHw().let {
                withContext(Dispatchers.JavaFx) {
                    cameraCheckProperty.value =
                        if (it) DeviceCheckStatus.DeviceOk else DeviceCheckStatus.DeviceError
                }
            }
        }
    }
}
