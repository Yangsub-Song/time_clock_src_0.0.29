package com.techrove.timeclock.view.admin

import com.techrove.timeclock.io.Audio
import com.techrove.timeclock.utils.SwUpdateUtils
import com.techrove.timeclock.view.custom.IconType
import com.techrove.timeclock.view.custom.timeoutDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.javafx.JavaFx
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tornadofx.minutes
import tornadofx.progressindicator
import tornadofx.spacer

/**
 * SW 수동 업데이트 dialog
 */
fun AdminCenterViewVbox.swUpdateDialog() {
    Audio.play("beep1.wav")
    if (!SwUpdateUtils.isUsbDriveMounted()) {
        infoDialogCustom(
            title = "SW 업데이트",
            message = "USB 드라이브를 찾을 수 없습니다.",
            iconType = IconType.Error
        )
    } else {
        timeoutDialog(
            title = "SW 업데이트",
            message = "SW 업데이트를 진행합니다.",
            iconType = IconType.Info,
            delay = AdminView.defaultTimeout,
            buttons = listOf("취소", "업데이트")
        ) {
            if (it == -1) return@timeoutDialog
            if (it == 1) {
                confirmDialog(
                    title = "SW 업데이트",
                    message = "SW 업데이트를 진행하시겠습니까?"
                ) {
                    timeoutDialog(
                        title = "SW 업데이트",
                        message = "SW 업데이트 중 입니다...",
                        iconType = IconType.Wait,
                        delay = 10.minutes,
                        op = {
                            spacer()
                            progressindicator()
                            spacer()
                        },
                        buttons = emptyList()
                    )
                    GlobalScope.launch {
                        SwUpdateUtils.mountUsbDrive()
                        val (success, message) = SwUpdateUtils.swUpdateByUsb()
                        SwUpdateUtils.unmountUsbDrive()
                        withContext(Dispatchers.JavaFx) {
                            if (success) {
                                infoDialogCustom(
                                    title = "SW 업데이트",
                                    message = "SW 업데이트가 완료 되었습니다. 재시작 합니다.",
                                    iconType = IconType.Wait
                                ) {
                                    SwUpdateUtils.restartApp()
                                }
                            } else {
                                infoDialogCustom(
                                    title = "SW 업데이트",
                                    message = message ?: "SW 업데이트를 실패 했습니다.",
                                    iconType = IconType.Error
                                )
                            }
                        }
                    }
                }
            } else {
                settingsDialog()
            }
        }
    }
}
