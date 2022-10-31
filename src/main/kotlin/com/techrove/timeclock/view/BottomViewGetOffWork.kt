package com.techrove.timeclock.view

import com.techrove.timeclock.Settings
import com.techrove.timeclock.Styles
import com.techrove.timeclock.controller.MainController
import com.techrove.timeclock.extensions.imageViewEncrypted
import com.techrove.timeclock.extensions.onChangeTrue
import com.techrove.timeclock.io.Audio
import com.techrove.timeclock.view.custom.*
import javafx.geometry.Pos
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import javafx.scene.text.TextAlignment
import tornadofx.*

/**
 * 카드/지문 대기 dialog
 */
private fun Pane.cardOrFingerPrint(controller: MainController) {
    controller.getOffWorkProperty.onChangeTrue {
        timeoutDialog(
            title = "카드 또는 지문",
            large = true,
            delay = 2.minutes,
            closable = true,
            showCameraIcon = Settings.takePicture,
            buttons = listOf("취소"),
            op = {
                vbox(alignment = Pos.CENTER) {
                    prefHeight = 250.0
                    imageview("/main/ICO_Guide.png")
                    textflow {
                        textAlignment = TextAlignment.CENTER
                        text("카드를 태그하시거나,\n지문을 스캔해 주세요.\n\n" +
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
                }
                spacer()
                hbox {
                    imageview("/main/MEDIA_TagScan.png")
                    spacer()
                    stackpane {
                        imageview("/main/MEDIA_Cam.png")
                        imageViewEncrypted(controller.photoProperty) {
                            isPreserveRatio = false
                            fitWidth = 310.0
                            fitHeight = 220.0
                            //fitWidthProperty().bind(widthProperty())
                            //fitHeightProperty().bind(heightProperty())
                        }
                    }
                }
            }
        ) {
            controller.getOffWork = false
            controller.retryCount = 1
            controller.deleteLastPhoto()
        }
    }
}

/**
 * 생년월일 입력 dialog
 */
private fun Pane.dobDialog(controller: MainController) {
    controller.getOffWorkFingerTemplateProperty.onChange {
        if (it != null) {
            Audio.play("주민등록(외국인등록)번호 앞 여섯자리를 입력하시고 [확인]버튼을 눌러주세요.wav")
            controller.dobModel.reset()
            timeoutDialog(
                title = "주민등록(외국인등록)번호",
                message = "주민등록(외국인등록)번호 앞 6자리를 입력하시고\n[확인]버튼을 눌러주세요.",
                iconType = IconType.PassWord,
                keyboard = true,
                delay = 2.minutes,
                closable = true,
                lastEnabledWhen = controller.dobModel.valid,
                buttons = listOf("취소", "확인"),
                op = {
                    form {
                        fieldset {
                            field("주민등록번호") {
                                numberTextField(
                                    controller.dobModel.residentNumber,
                                    6,
                                    """\d{6}""",
                                    password = true,
                                    numberOnly = true,
                                )
                            }
                        }
                    }
                }) {
                if (it == 1) {
                    controller.getOffWorkByFinger = true
                } else {
                    controller.retryCount = 1
                    controller.deleteLastPhoto()
                }
            }
        }
    }
}

/**
 * 퇴근 버튼 view
 */
fun Pane.getOffWork(controller: MainController, root: View) {
    cardOrFingerPrint(controller)
    dobDialog(controller)

    bottomButton(
        "/main/BTN_GetOffWork.png"
    ) { button ->
        button.apply {
            action {
                root.agreementDialog(forFingerRegistration = false) {
                    controller.retryCount = 1
                    controller.getOffWork = true
                }
            }
        }
    }
}