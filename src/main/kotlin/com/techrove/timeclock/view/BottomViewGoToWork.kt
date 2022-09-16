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
 * 체온 체크 dialog
 */
private fun Pane.measureBodyTemperature(controller: MainController) {
    controller.measureTemperatureProperty.onChangeTrue {
        Audio.play("출근 아래의 그림과 같이 체온을 측정해 주세요.wav")
        timeoutDialog(
            title = "체온측정",
            large = true,
            delay = 2.minutes,
            closable = true,
            buttons = if (Settings.measureTemperatureOption == 1) listOf("취소") else listOf("취소", "건너뛰기"),
            op = {
                ///////////////////////////////////////////////////////////////////////////
                // 체온 data 가 아직 없을 떄
                ///////////////////////////////////////////////////////////////////////////

                vbox(alignment = Pos.CENTER) {
                    removeWhen(controller.hasTemperatureProperty)
                    prefHeight = 250.0
                    imageview("/main/ICO_Temperature.png")
                    label("아래의 그림과 같이 체온을 측정해 주세요")
                }
                ///////////////////////////////////////////////////////////////////////////
                // 체온 data 가 있을 때
                ///////////////////////////////////////////////////////////////////////////

                vbox(alignment = Pos.CENTER) {
                    removeWhen(controller.hasTemperatureProperty.not())
                    prefHeight = 250.0
                    spacer()
                    ///////////////////////////////////////////////////////////////////////////
                    // 체온
                    ///////////////////////////////////////////////////////////////////////////

                    label(controller.temperatureProperty.stringBinding { String.format("%.1f °C", it) }) {
                        style { fontSize = 50.px }
                        textFillProperty().bind(controller.temperatureOverProperty.objectBinding {
                            if (it == true) Color.rgb(0xd6, 0x00, 0x00) else Color.rgb(0x20, 0x88, 0x00)
                        })
                    }
                    spacer()

                    ///////////////////////////////////////////////////////////////////////////
                    // 상태
                    ///////////////////////////////////////////////////////////////////////////
                    val statusHeight = 80.0
                    textflow {
                        prefHeight = statusHeight
                        maxHeight = statusHeight
                        textAlignment = TextAlignment.CENTER

                        ///////////////////////////////////////////////////////////////////////////
                        // 정상
                        ///////////////////////////////////////////////////////////////////////////
                        text("\n정상 체온입니다.").apply {
                            removeWhen(controller.temperatureOverProperty)
                            fill = Color.rgb(0x33, 0x33, 0x33)
                            font = Styles.customFont
                        }

                        ///////////////////////////////////////////////////////////////////////////
                        // 고온
                        ///////////////////////////////////////////////////////////////////////////
                        if (Settings.measureTemperatureOption == 1) {
                            text("체온이 높습니다.\n다시 한번 측정해 주세요. ").apply {
                                removeWhen(controller.temperatureOverProperty.not())
                                fill = Color.rgb(0x33, 0x33, 0x33)
                                font = Styles.customFont
                            }
                            text(controller.temperatureCountProperty.stringBinding { "($it/3)" }).apply {
                                removeWhen(controller.temperatureOverProperty.not())
                                fill = Color.RED
                                font = Styles.customFont
                            }
                        } else {
                            text("체온이 높습니다.").apply {
                                removeWhen(controller.temperatureOverProperty.not())
                                fill = Color.rgb(0x33, 0x33, 0x33)
                                font = Styles.customFont
                            }
                        }
                    }
                }
                spacer()
                imageview("/main/MEDIA_Temperature.png")
            }
        ) {
            controller.measureTemperature = false
            if (it == 1) {
                controller.gotoWork = true
            }
        }
    }
}

/**
 * 카드/지문 대기 dialog
 */
private fun Pane.cardOrFingerPrint(controller: MainController) {
    controller.gotoWorkProperty.onChangeTrue {
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
/*
                        text(controller.temperatureCountProperty.stringBinding { "$it/3" }).apply {
                            fill = Color.RED
                            font = Styles.customFont
                        }
*/
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
            controller.gotoWork = false
            controller.retryCount = 1
            controller.deleteLastPhoto()
        }
    }
}

/**
 * 생년월일 입력 dialog
 */
private fun Pane.dobDialog(controller: MainController) {
    controller.gotoWorkFingerTemplateProperty.onChange {
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
                    controller.gotoWorkByFinger = true
                } else {
                    controller.retryCount = 1
                    controller.deleteLastPhoto()
                }
            }
        }
    }
}

/**
 * 출근 버튼 view
 */
fun Pane.goToWork(controller: MainController, root: View) {
    measureBodyTemperature(controller)
    cardOrFingerPrint(controller)
    dobDialog(controller)

    bottomButton(
        "/main/BTN_GoToWork.png"
    ) { button ->
        button.apply {
            action {
                root.agreementDialog(forFingerRegistration = false) {
                    controller.retryCount = 1
                    if (Settings.measureTemperatureOption != 0) {
                        controller.measureTemperature = true
                    } else {
                        controller.gotoWork = true
                    }
                }
            }
        }
    }
}
