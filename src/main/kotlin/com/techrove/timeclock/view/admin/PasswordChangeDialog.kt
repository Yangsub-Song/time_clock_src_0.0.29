package com.techrove.timeclock.view.admin

import com.techrove.timeclock.Settings
import com.techrove.timeclock.controller.admin.SettingsController
import com.techrove.timeclock.io.Audio
import com.techrove.timeclock.view.custom.IconType
import com.techrove.timeclock.view.custom.infoDialog
import com.techrove.timeclock.view.custom.numberTextField
import com.techrove.timeclock.view.custom.timeoutDialog
import tornadofx.*

import com.techrove.timeclock.security.KeyHelper
import com.techrove.timeclock.view.MainView
import mu.KotlinLogging

private val logger = KotlinLogging.logger("PasswordChangeDialog")    // Yade1021

/**
 * 암호 변경 dialog
 */
fun AdminCenterViewVbox.passwordChangeDialog(changePassword: Boolean = true) {
    val controller = find(SettingsController::class)

    // 키 유효성 체크. UI 처리는 MainView 에서 함.
    if (KeyHelper.checkKeyIntegrity()       // Yade1024
        && KeyHelper.checkKeyIntegrity2()
        && KeyHelper.verifyKeyFile(KeyHelper.keyDir2, "adminKey", Settings.ADMIN_KEY_AES_ENC)
        && KeyHelper.verifyKeyFile(KeyHelper.keyDir2, "defaultKey", Settings.DEFAULT_KEY_AES_ENC)) { // Yade1020 ) { // Yade0916, Yade0926, Yade1020
        logger.info { "무결성 체크 OK" }
    } else {
        logger.info { "무결성 체크 Error" }
        KeyHelper.keyIntegrityOk = false                    // Yade1024
        find(MainView::class).showIntegrityErrorDialog()   // Yade0926
        return@passwordChangeDialog
    }
    Audio.play("beep1.wav")
    controller.model.resetPassword()
    timeoutDialog(
        title = if (changePassword) "관리자 암호 변경" else "관리자 암호 설정",
        message = "3종류 이상의 문자(영문 대/소문자, 숫자, 특수문자) 8자리 이상 입력해주세요.",
        iconType = IconType.PassWord,
        keyboard = true,
//        delay = if (changePassword) AdminView.defaultTimeout else null,   // Yade1017
        delay = if (changePassword) null else 30.seconds,                   // Yade1017
        closable = !changePassword,                                         // Yade1017
        lastEnabledWhen = controller.model.valid,
        op = {
            form {
                fieldset {
                    val regexPasswordValid =
                        """^(?=.*[A-Za-z])(?=.*\d)(?=.*[@${'$'}!%*#?&])[A-Za-z\d@${'$'}!%*#?&]{8,}${'$'}"""
                    field(if (changePassword) "변경할 암호" else "설정할 암호") {
                        numberTextField(
                            controller.model.password1,
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
                            controller.model.password2,
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
                if (controller.tryChangePassword()) {
                    infoDialogCustom("관리자 암호가 변경되었습니다.", iconType = IconType.Info)
                } else {
                    infoDialog("입력한 암호가 다릅니다. 재입력해 주세요", iconType = IconType.Error) {
                        passwordChangeDialog(true)
                    }
                }
            } else {
                settingsDialog()
            }
        } else {
            if (controller.tryChangePassword()) {
                infoDialogCustom("관리자 암호가 설정되었습니다.", iconType = IconType.Info)
                initSFTPPasswordChange()        // Yade1011, 1017
            } else {
                infoDialog("입력한 암호가 다릅니다. 재입력해 주세요", iconType = IconType.Error) {
                    passwordChangeDialog(false)
                }
            }
        }
    }
}

fun AdminCenterViewVbox.initPasswordChange() {
    if (Settings.password.isEmpty()) {
        runLater { passwordChangeDialog(false) }      // Yade1017
//        passwordChangeDialog(false)     // Yade1017
    }
}
