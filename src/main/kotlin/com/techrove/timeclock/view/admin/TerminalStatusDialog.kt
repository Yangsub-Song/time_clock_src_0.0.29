package com.techrove.timeclock.view.admin

import com.techrove.timeclock.controller.admin.TerminalStatusController
import com.techrove.timeclock.controller.model.DeviceCheckStatus
import com.techrove.timeclock.io.Audio
import com.techrove.timeclock.view.custom.timeoutDialog
import javafx.beans.property.ObjectProperty
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import tornadofx.*

/**
 * 단말기 상태 정보 dialog
 */
fun AdminCenterViewVbox.terminalStatusDialog() {
    val controller = find(TerminalStatusController::class)

    Audio.play("단말기 상태 정보.wav")
    controller.checkDevice()
    timeoutDialog(
        title = "SETTINGS > 단말기 상태 정보",
        delay = AdminView.defaultTimeout,
        large = true,
        op = {
            fun Fieldset.textFieldCustom(title: String, result: ObjectProperty<DeviceCheckStatus>) {
                field(title) {
                    textfield(result.stringBinding {
                        when (it) {
                            DeviceCheckStatus.DeviceChecking -> {
                                style = "-fx-text-inner-color: gray"
                            }
                            DeviceCheckStatus.DeviceOk -> {
                                style = "-fx-text-inner-color: #208800"
                            }
                            DeviceCheckStatus.DeviceError -> {
                                style = "-fx-text-inner-color: red"
                            }
                        }
                        it?.toString() ?: ""
                    }) {
                        isFocusTraversable = false
                    }
                }
            }
            form {
                fieldset {
                    textFieldCustom("서버통신[공제회]", controller.server1CheckProperty)
                    textFieldCustom("서버통신[관리서버]", controller.server2CheckProperty)
                    textFieldCustom("키패드", controller.keyboardCheckProperty)
                    textFieldCustom("RF 리더기", controller.rfReaderCheckProperty)
                    textFieldCustom("지문인식기", controller.supremaCheckProperty)
                    textFieldCustom("온도센서", controller.tempSensorCheckProperty)
                    textFieldCustom("카메라", controller.cameraCheckProperty)
                    textFieldCustom("출근 버튼", controller.gotoWorkButtonProperty)
                    textFieldCustom("퇴근 버튼", controller.getOffWorkButtonProperty)
                }
            }

            runLater {
                requestFocus()
                addEventFilter(KeyEvent.KEY_PRESSED) {
                    when (it.code) {
                        KeyCode.HOME -> controller.gotoWorkButtonProperty.value =
                            DeviceCheckStatus.DeviceOk
                        KeyCode.END -> controller.getOffWorkButtonProperty.value =
                            DeviceCheckStatus.DeviceOk
                        else -> {}
                    }
                }
            }
        },
        buttons = listOf("", "확인")
    ) {
        if (it == -1) return@timeoutDialog
        settingsDialog()
    }
}
