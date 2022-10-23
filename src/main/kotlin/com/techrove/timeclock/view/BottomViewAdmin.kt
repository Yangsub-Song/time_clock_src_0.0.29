package com.techrove.timeclock.view

import com.techrove.timeclock.Settings
import com.techrove.timeclock.Styles
import com.techrove.timeclock.controller.MainController
import com.techrove.timeclock.controller.model.InfoMessage
import com.techrove.timeclock.io.Audio
import com.techrove.timeclock.isLinux
import com.techrove.timeclock.security.Key
import com.techrove.timeclock.security.KeyHelper
import com.techrove.timeclock.security.decrypt
import com.techrove.timeclock.view.admin.AdminView
//import com.techrove.timeclock.view.admin.logger
import com.techrove.timeclock.view.custom.IconType
import com.techrove.timeclock.view.custom.bottomButton
import com.techrove.timeclock.view.custom.numberTextField
import com.techrove.timeclock.view.custom.timeoutDialog
import javafx.scene.layout.Pane
import javafx.scene.layout.VBox
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.javafx.JavaFx
import kotlinx.coroutines.launch
import mu.KotlinLogging
import tornadofx.*
import tornadofx.FX.Companion.primaryStage

private val logger = KotlinLogging.logger("BottomViewAdmin")    // Yade1021

// 개발용
private val useAdminPassword = isLinux

private fun VBox.formPassword(controller: MainController) {
    spacer { maxHeight = 32.0 }
    form {
        fieldset {
            field("비밀번호") {
                numberTextField(
                    controller.passwordModel.password,
                    30,
                    """.{1,}""",
                    password = true
                )
            }
        }
    }
}

private fun openAdminView(registerFinger: Boolean = false, changePassword: Boolean = false) {
    primaryStage.uiComponent<View>()?.replaceWith(
        find(AdminView::class, FX.defaultScope,
            AdminView::registerFingerShortCut to registerFinger,
            AdminView::changePasswordShortCut to changePassword
        )
        //find<AdminView>()
        //transition = ViewTransition.Explode(0.3.seconds)
    )
}

fun Pane.tryAdminView(controller: MainController, registerFinger: Boolean = false, changePassword: Boolean = false) {
    // 키 유효성 체크. UI 처리는 MainView 에서 함.
    if (KeyHelper.checkKeyIntegrity()       // Yade1021
        && KeyHelper.checkKeyIntegrity2()
        && KeyHelper.verifyKeyFile(KeyHelper.keyDir2, "adminKey", Settings.ADMIN_KEY_AES_ENC)
        && KeyHelper.verifyKeyFile(KeyHelper.keyDir2, "defaultKey", Settings.DEFAULT_KEY_AES_ENC)) { // Yade1020 ) { // Yade0916, Yade0926, Yade1020
        logger.info { "무결성 체크 OK" }
    } else {
        logger.info { "무결성 체크 Error" }
        find(MainView::class).showIntegrityErrorDialog()   // Yade0926
        return@tryAdminView
    }
    controller.password = ""
    // 암호 변경 요청시에는 non-closable. 취소 버튼 없음
    timeoutDialog(
        title = "관리자 비밀번호 입력",
        message = if (changePassword) "관리자 비빌번호 갱신이 필요합니다.\n\n관리자 비밀번호를 입력 후 [확인]버튼을 눌러주세요." else "관리자 비밀번호를 입력 후 [확인]버튼을 눌러주세요.",
        keyboard = true,
        iconType = IconType.PassWord,
        delay = if (changePassword) null else 30.seconds,
        closable = !changePassword,
        lastEnabledWhen = controller.passwordModel.valid,
        buttons = listOf(if (changePassword) "" else "취소", "확인"),
        op = { formPassword(controller) }) {
        if (it == 1) {
            if (controller.password == Settings.password.decrypt(Key.pwdKey, "pw")) {  // Yade1011 pw -> pwd 1021다시 원복
                openAdminView(registerFinger, changePassword)
            } else {
                controller.infoMessage =
                    InfoMessage(
                        "관리자 비밀번호 입력",
                        "잘못된 비밀번호입니다. 비밀번호를 확인해 주세요",
                        IconType.Error
                    )
                // 암호 만료시 다시 암호 입력 띄움
                if (changePassword) {
                    controller.launch(Dispatchers.JavaFx) {
                        delay(3000)
                        tryAdminView(controller, changePassword = true)
                    }
                }
            }
        }
    }
}

/**
 * 관리자 버튼 view
 */
fun Pane.admin(controller: MainController) {
    bottomButton("/main/BTN_AdminMenu.png") { button ->
        button.apply {
            addClass(Styles.bigButton)
            action {
                Audio.play("beep1.wav")
                if (useAdminPassword) {
                    tryAdminView(controller)
                } else {
                    openAdminView()
                }
            }
        }
    }
}
