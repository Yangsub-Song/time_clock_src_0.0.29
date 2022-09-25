package com.techrove.timeclock.view.admin

import com.techrove.timeclock.Settings
import com.techrove.timeclock.controller.admin.SettingsController
import com.techrove.timeclock.io.Audio
import com.techrove.timeclock.view.custom.IconType
import com.techrove.timeclock.view.custom.infoDialog
import com.techrove.timeclock.view.custom.numberTextField
import com.techrove.timeclock.view.custom.timeoutDialog
import tornadofx.*

/**
 * SFTP 암호 변경 dialog     // Yade0925
 */
fun AdminCenterViewVbox.sFTPPasswordChangeDialog(changePassword: Boolean = true) {
    val controller = find(SettingsController::class)

    Audio.play("beep1.wav")
    controller.model.resetSFTPPassword()
    timeoutDialog(
        title = if (changePassword) "sFTP 암호 변경" else "sFTP 암호 설정",
        message = "3종류 이상의 문자(영문 대/소문자, 숫자, 특수문자) 8자리 이상 입력해주세요.",
        iconType = IconType.PassWord,
        keyboard = true,
        delay = if (changePassword) AdminView.defaultTimeout else null,
        lastEnabledWhen = controller.model.valid,
        op = {
            form {
                fieldset {
                    val regexPasswordValid =
                        """^(?=.*[A-Za-z])(?=.*\d)(?=.*[@${'$'}!%*#?&])[A-Za-z\d@${'$'}!%*#?&]{8,}${'$'}"""
                    field(if (changePassword) "변경할 암호" else "설정할 암호") {
                        numberTextField(
                            controller.model.swUpdatePassword1,
                            30,
                            regexPasswordValid,
                            password = true,
                        ).apply {
                            runLater(300.millis) {
                                requestFocus()
                            }
                        }
                    }
                    field("암호 재확인") {
                        numberTextField(
                            controller.model.swUpdatePassword2,
                            30,
                            regexPasswordValid,
                            password = true,
                        )
                    }
                }
            }
        },
        buttons = if (changePassword) listOf("취소", "변경") else  listOf("설정")
    ) {
        if (it == -1) return@timeoutDialog
        if (changePassword) {
            if (it == 1) {
                if (controller.tryChangeSFTPPassword()) {
                    infoDialogCustom("sFTP 암호가 변경되었습니다.", iconType = IconType.Info)
                } else {
                    infoDialog("입력한 암호가 다릅니다. 재입력해 주세요", iconType = IconType.Error) {
                        sFTPPasswordChangeDialog(true)
                    }
                }
            } else {
                settingsDialog()
            }
        } else {
            if (controller.tryChangeSFTPPassword()) {
                infoDialogCustom("sFTP 암호가 설정되었습니다.", iconType = IconType.Info)
            } else {
                infoDialog("입력한 암호가 다릅니다. 재입력해 주세요", iconType = IconType.Error) {
                    sFTPPasswordChangeDialog(false)
                }
            }
        }
    }
}

fun AdminCenterViewVbox.initSFTPPasswordChange() {
    if (Settings.password.isEmpty()) {
        runLater { sFTPPasswordChangeDialog(false) }
    }
}
