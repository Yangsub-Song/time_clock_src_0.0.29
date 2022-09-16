package com.techrove.timeclock.view.admin

import com.techrove.timeclock.controller.admin.SettingsController
import com.techrove.timeclock.io.Audio
import com.techrove.timeclock.view.custom.IconType
import com.techrove.timeclock.view.custom.numberTextField
import com.techrove.timeclock.view.custom.timeoutDialog
import tornadofx.*

/**
 * 단말기 정보 dialog
 */
fun AdminCenterViewVbox.terminalInfoDialog() {
    val controller = find(SettingsController::class)
    Audio.play("beep1.wav")
    timeoutDialog(
        title = "SETTINGS > 단말기 정보",
        delay = AdminView.defaultTimeout,
        keyboard = true,
        op = {
            form {
                fieldset {
                    field("터미널 ID") {
                        numberTextField(controller.model.terminalId, 21, """\d{21}""") {
                            positionCaret(Int.MAX_VALUE)
                        }
                    }
                    field("현장 코드") {
                        numberTextField(controller.model.placeCd, 6, """\d{6}""")
                    }
                }
            }
        },
        buttons = listOf("취소", "변경")
    ) {
        if (it == -1) return@timeoutDialog
        if (it == 1) {
            if (controller.model.isDeviceInfoChanged()) {
                confirmDialog(
                    title = "단말기 정보",
                    message = "단말기 정보를 변경하시겠습니까?",
                    cancelled = {
                        controller.model.resetDeviceInfo()
                    }
                ) {
                    controller.model.updateDeviceInfo()
                    timeoutDialog(
                        title = "알림",
                        message = "설정이 변경되어 재실행합니다.",
                        iconType = IconType.Wait,
                        delay = 10.seconds,
                        buttons = listOf("", "확인")
                    ) {
                        controller.restartApp(true)
                    }
                }
            } else {
                settingsDialog()
            }
        } else {
            controller.model.resetDeviceInfo()
            settingsDialog()
        }
    }
}
