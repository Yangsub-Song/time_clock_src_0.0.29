package com.techrove.timeclock.view.admin

import com.techrove.timeclock.Settings
import com.techrove.timeclock.controller.admin.SettingsController
import com.techrove.timeclock.io.Audio
import com.techrove.timeclock.view.custom.IconType
import com.techrove.timeclock.view.custom.timeoutDialog
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import tornadofx.find

/**
 * 공장 초기화 dialog
 */
fun AdminCenterViewVbox.factoryResetDialog() {
    Audio.play("beep1.wav")
    timeoutDialog(
        title = "공장 초기화",
        message = "공장 초기화를 진행하고 재실행합니다.",
        iconType = IconType.Wait,
        delay = AdminView.defaultTimeout,
        buttons = listOf("취소", "초기화")
    ) {
        if (it == -1) return@timeoutDialog
        if (it == 1) {
            confirmDialog(
                title = "공장 초기화",
                message = "공장초기화를 진행하시겠습니까?"
            ) {
                Settings.clear()
                GlobalScope.launch {
                    delay(3000)
                    val controller = find(SettingsController::class)
                    controller.restartApp(true)
                }
            }
        } else {
            settingsDialog()
        }
    }
}
