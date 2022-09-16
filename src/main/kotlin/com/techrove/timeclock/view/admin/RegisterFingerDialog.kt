package com.techrove.timeclock.view.admin

import com.techrove.timeclock.Styles
import com.techrove.timeclock.controller.admin.RegisterFingerController
import com.techrove.timeclock.extensions.onChangeTrue
import com.techrove.timeclock.io.Audio
import com.techrove.timeclock.utils.playAudio
import com.techrove.timeclock.view.custom.IconType
import com.techrove.timeclock.view.custom.numberTextField
import com.techrove.timeclock.view.custom.timeoutDialog
import javafx.scene.paint.Color
import javafx.scene.text.TextAlignment
import tornadofx.*

fun AdminCenterViewVbox.initRegisterFinger() {
    val controller = find(RegisterFingerController::class)

    controller.fingerRegisterProperty.onChangeTrue {
        controller.personModel.reset()
        Audio.play("전화번호와 주민등록(외국인등록)번호 열세자리를 입력 후 [확인]버튼을 눌러주세요.wav")
        timeoutDialog(
            title = "휴대전화/주민등록(외국인등록)번호",
            message = "전화번호와 주민등록(외국인등록)번호\n13자리를 입력 후 [확인]버튼을 눌러주세요.",
            iconType = IconType.PassWord,
            keyboard = true,
            delay = AdminView.defaultTimeout,
            closable = true,
            lastEnabledWhen = controller.personModel.valid,
            buttons = listOf("취소", "확인"),
            op = {
                form {
                    fieldset {
                        field("휴대전화번호") {
                            numberTextField(
                                controller.personModel.phoneNumber,
                                11,
                                """\d{9,11}"""
                            )
                        }
                        field("주민등록번호") {
                            numberTextField(
                                controller.personModel.residentNumber,
                                13,
                                """\d{13}""",
                                password = true,
                                numberOnly = true
                            )
                        }
                    }
                }
            }) {
            controller.fingerRegister = false
            if (it == 1) {
                controller.fingerScanning = true
            }
        }
    }

    controller.fingerScanningProperty.onChangeTrue {
        Audio.play("아래의 그림과 같이 지문 센서에 지문을 대 주세요.wav")
        timeoutDialog(
            title = "지문 등록",
            iconType = IconType.FingerPrint,
            large = true,
            delay = AdminView.defaultTimeout,
            buttons = listOf("취소"),
            op = {
                textflow {
                    textAlignment = TextAlignment.CENTER
                    text("아래의 그림과 같이 지문 센서에 지문을 대 주세요.\n" +
                            "(3회 실패시 처음 화면으로 이동합니다. ").apply {
                        fill = Color.rgb(0x33, 0x33, 0x33)
                        font = Styles.customFont
                    }
                    text(controller.retryCountProperty.stringBinding { "$it/3" }).apply {
                        fill = Color.RED
                        font = Styles.customFont
                    }
                    text(")").apply {
                        fill = Color.rgb(0x33, 0x33, 0x33)
                        font = Styles.customFont
                    }
                }
                spacer()
                imageview("/main/MEDIA_Scan.png")
            }
        ) {
            controller.fingerScanning = false
            if (it == 0) {
                // cancel 지문 등록
                controller.retryCount = -1
            }
        }
    }

    controller.fingerRegistrationResponseProperty.onChange {
        val response = it ?: return@onChange
        response.playAudio("지문등록이 완료 되었습니다.wav")
        when {
            it.result.isOk -> {
                timeoutDialog(
                    title = "지문등록",
                    message = "지문등록이 완료 되었습니다.",
                    iconType = IconType.FingerPrintOk,
                    delay = 3.seconds,
                    buttons = listOf("닫기")
                )
            }
            else -> {
                timeoutDialog(
                    title = "지문등록",
                    message = it.result.details,
                    iconType = IconType.Error,
                    delay = 3.seconds,
                    buttons = listOf("닫기")
                ) {
                    if (it == 0) {
                        // cancel 지문 등록
                        controller.retryCount = -1
                    }
                }
            }
        }
    }
}
