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
fun AdminCenterViewVbox.settingHostURLDialog() {
    val controller = find(SettingsController::class)
    Audio.play("beep1.wav")
    timeoutDialog(
        title = "SETTINGS > 호스트 URL",
        delay = AdminView.defaultTimeout,
        keyboard = true,
        op = {
            form {
                fieldset {
//                    var regexPasswordValid = """^https?://"""
                    val regexPasswordValid =
                        """^(?=.*[A-Za-z])(?=.*\d)(?=.*[@${'$'}!%*#?&])[A-Za-z\d@${'$'}!%*#?&]{8,}${'$'}"""
                    // https?:\/\/(www\.)?[-a-zA-Z0-9@:%._\+~#=]{1,256}\.[a-zA-Z0-9()]{1,6}\b([-a-zA-Z0-9()@:%_\+.~#?&//=]*)"""
                    field("Admin 호스트") {
//                        numberTextField(controller.model.adminHost, 256, "") {
//                            positionCaret(Int.MAX_VALUE)
//                        }
                        numberTextField(
                            controller.model.adminHost,
                            256,
                            regexPasswordValid,
                        )
                    }
                    field("CWMA 호스트") {
                        numberTextField(
                            controller.model.cwmaHost,
                            256,
                            regexPasswordValid,
                        )
                    }
                }
            }
        },
        buttons = listOf("취소", "변경")
    ) {
        if (it == -1) return@timeoutDialog
        if (it == 1) {
            if (controller.model.isHostURLChanged()) {
                confirmDialog(
                    title = "URL 주소",
                    message = "URL 주소를 변경하시겠습니까?",
                    cancelled = {
                        controller.model.resetHostURL()
                    }
                ) {
                    controller.model.updateHostURL()
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
            controller.model.resetHostURL()
            settingsDialog()
        }
    }
}
