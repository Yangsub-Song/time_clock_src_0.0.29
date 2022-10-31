package com.techrove.timeclock.view.admin

import com.techrove.timeclock.controller.admin.SettingsController
import com.techrove.timeclock.io.Audio
import com.techrove.timeclock.view.custom.IconType
import com.techrove.timeclock.view.custom.timeoutDialog
import tornadofx.find

/**
 * 앱 재시작 dialog
 */
fun AdminCenterViewVbox.restartDialog() {
    timeoutDialog(
        title = "재시작",
        message = "앱을 재실행합니다.",
        iconType = IconType.Wait,
        delay = AdminView.defaultTimeout,
        buttons = listOf("취소", "재시작")
    ) {
        Audio.play("beep1.wav")
        if (it == -1) return@timeoutDialog
        if (it == 1) {
            val controller = find(SettingsController::class)
            controller.restartApp(false)
        } else {
            settingsDialog()
        }
    }
}
