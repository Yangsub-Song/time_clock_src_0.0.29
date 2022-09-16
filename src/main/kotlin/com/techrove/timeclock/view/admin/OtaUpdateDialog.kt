package com.techrove.timeclock.view.admin

import com.techrove.timeclock.Settings
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
 * OTA 업데이트 dialog
 */
fun AdminCenterViewVbox.otaUpdateDialog() {
    Audio.play("beep1.wav")
    var cancelled = false
    timeoutDialog(
        title = "OTA 업데이트",
        message = "서버 정보 확인 중 입니다...",
        iconType = IconType.Wait,
        delay = 1.minutes,
        op = {
            spacer()
            progressindicator()
            spacer()
        },
        buttons = listOf("취소", "")
    ) {
        cancelled = true
        settingsDialog()
    }
    GlobalScope.launch {
        val (otaUrl, otaVersion, port) = SwUpdateUtils.getOtaInfo()
        withContext(Dispatchers.JavaFx) {
            // check cancel flag
            if (cancelled) {
                return@withContext
            }
            // check server response status
            if (otaUrl == null) {
                timeoutDialog(
                    title = "OTA 업데이트",
                    message = "서버 정보를 확인 할 수 없습니다.",
                    iconType = IconType.Error,
                    delay = AdminView.defaultTimeout,
                    buttons = listOf("닫기", "")
                )
            } else {
                timeoutDialog(
                    title = "OTA 업데이트",
                    message = "기기 버전 : ${Settings.VERSION}\nOTA 버전 : $otaVersion",
                    iconType = IconType.Consent,
                    delay = AdminView.defaultTimeout,
                    buttons = listOf("취소", "업데이트")
                ) {
                    if (it == -1) return@timeoutDialog
                    if (it == 1) {
                        confirmDialog(
                            title = "OTA 업데이트",
                            message = "OTA 업데이트를 진행하시겠습니까?"
                        ) {
                            timeoutDialog(
                                title = "OTA 업데이트",
                                message = "OTA 업데이트 중 입니다...",
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
                                val (success, message) = SwUpdateUtils.swUpdateByOta(otaUrl, port)
                                withContext(Dispatchers.JavaFx) {
                                    if (success) {
                                        infoDialogCustom(
                                            title = "OTA 업데이트",
                                            message = "OTA 업데이트가 완료 되었습니다. 재시작 합니다.",
                                            iconType = IconType.Wait
                                        ) {
                                            SwUpdateUtils.restartApp()
                                        }
                                    } else {
                                        infoDialogCustom(
                                            title = "OTA 업데이트",
                                            message = message ?: "OTA 업데이트를 실패 했습니다.",
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
    }
}
