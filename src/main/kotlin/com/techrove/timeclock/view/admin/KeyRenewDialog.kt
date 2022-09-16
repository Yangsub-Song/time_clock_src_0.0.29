package com.techrove.timeclock.view.admin

import com.techrove.timeclock.Styles
import com.techrove.timeclock.controller.admin.SettingsController
import com.techrove.timeclock.io.Audio
import com.techrove.timeclock.security.KeyHelper
import com.techrove.timeclock.view.custom.IconType
import com.techrove.timeclock.view.custom.timeoutDialog
import com.techrove.timeclock.view.custom.timeoutDialogClose
import javafx.scene.paint.Color
import javafx.scene.text.TextAlignment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.javafx.JavaFx
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tornadofx.*

/**
 * 키 갱신 dialog
 */
fun AdminCenterViewVbox.showKeyRenewDialog() {
    Audio.play("beep1.wav")

    timeoutDialog(
        title = "보안키 갱신",
        iconType = IconType.Wait,
        delay = AdminView.defaultTimeout,
        buttons = listOf("취소", "갱신"),
        op = {
            textflow {
                textAlignment = TextAlignment.CENTER
                text("보안키를 갱신합니다.").apply {
                    fill = Color.BLACK
                    font = Styles.customFont
                }
                text(
                    if (KeyHelper.keyIntegrityOk) "\n\n갱신 시 저장된 사진은 모두 무효화 됩니다."
                    else "\n\n현재 저장된 정보(사진,오프라인 출퇴근 정보 등)는 모두 무효화 됩니다."
                ).apply {
                    fill = Color.RED
                    font = Styles.customFont
                }
            }
        }
    ) {
        if (it == -1) return@timeoutDialog
        if (it == 1) {
            confirmDialog(
                title = "보안키 갱신",
                message = "보안키 갱신을 진행하시겠습니까?",
            ) {
                onRenewClicked()
                timeoutDialog(
                    title = "보안키 갱신",
                    message = "보안키 갱신 중 입니다...",
                    iconType = IconType.Wait,
                    op = {
                        spacer()
                        progressindicator()
                        spacer()
                    }
                ) {
                    timeoutDialogClose()
                }
            }
        } else {
            settingsDialog()
        }
    }
}

/**
 * 키 갱신
 */
private fun AdminCenterViewVbox.onRenewClicked() {
    val controller = find(SettingsController::class)
    controller.launch(Dispatchers.Default) {
        val renew = KeyHelper.keyIntegrityOk
        if (renew) {
            KeyHelper.renewKeys()
        } else {
            KeyHelper.resetKeys()
        }
        withContext(Dispatchers.JavaFx) {
            infoDialogCustom(
                title = "보안키 갱신",
                message =
                when {
                    KeyHelper.keyIntegrityOk && renew -> "보안키 갱신이 완료 되었습니다."
                    KeyHelper.keyIntegrityOk -> "보안키 갱신이 완료 되었습니다.\n관리자 암호를 재설정해 주세요."
                    else -> "보안키 갱신에 실패 했습니다."
                },
                iconType = if (KeyHelper.keyIntegrityOk) IconType.Info else IconType.Error
            ) {
                if (KeyHelper.keyIntegrityOk) {
                    if (!renew) {
                        runLater {
                            passwordChangeDialog(false)
                        }
                    }
                } else {
                    settingsDialog()
                }
            }
        }
    }
}
