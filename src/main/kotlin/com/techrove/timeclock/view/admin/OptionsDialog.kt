package com.techrove.timeclock.view.admin

import com.techrove.timeclock.Settings
import com.techrove.timeclock.controller.admin.SettingsController
import com.techrove.timeclock.extensions.jfxSlider
import com.techrove.timeclock.io.Audio
import com.techrove.timeclock.view.custom.timeoutDialog
import javafx.geometry.HPos
import javafx.geometry.Pos
import javafx.scene.layout.GridPane
import tornadofx.*

/**
 * 기기 설정 dialog
 */
fun AdminCenterViewVbox.optionsDialog() {
    val controller = find(SettingsController::class)

    Audio.play("beep1.wav")
    timeoutDialog(
        title = "SETTINGS > 기기 설정",
        delay = AdminView.defaultTimeout,
        op = {
            style { padding = box(32.px, 32.px) }
            gridpane {
                style {
                    alignment = Pos.TOP_CENTER
                    vgap = 24.px
                }
                row {
                    label("온도센서").gridpaneColumnConstraints { percentWidth = 25.0 }
                    togglegroup {
                        radiobutton("미사용", value = 0).gridpaneColumnConstraints { percentWidth = 25.0 }
                        radiobutton("사용1", value = 1).gridpaneColumnConstraints { percentWidth = 25.0 }
                        radiobutton("사용2", value = 2).gridpaneColumnConstraints { percentWidth = 25.0 }
                        bind(controller.measureTemperatureOptionProperty)
                    }
                }
                row {
                    label("카메라")
                    togglegroup {
                        radiobutton("미사용", value = false)
                        radiobutton("사용", value = true)
                        bind(controller.takePictureProperty)
                    }
                    label("")
                }
                row {
                    label("버튼음")
                    togglegroup {
                        radiobutton("미사용", value = false)
                        radiobutton("사용", value = true)
                        bind(controller.beepOnProperty)
                    }
                    label("")
                }
                row {
                    label("음량설정")
                    jfxSlider(0.0, 100.0, Settings.volume.toDouble()) {
                        valueProperty().onChange {
                            if (!isValueChanging) {
                                val percent = it.toInt()
                                Settings.volume = percent
                                Audio.setVolume(percent)
                                Audio.play("beep1.wav")
                            }
                        }
                        valueChangingProperty().onChange {
                            if (!it) {
                                val percent = this.value.toInt()
                                Settings.volume = percent
                                Audio.setVolume(percent)
                                Audio.play("beep1.wav")
                            }
                        }
                        gridpaneConstraints {
                            columnSpan = 3
                        }
                    }
                }
                children.forEach {
                    GridPane.setHalignment(it, HPos.LEFT)
                }
            }
        },
        buttons = listOf("이전")
    ) {
        if (it == -1) return@timeoutDialog
        settingsDialog()
    }
}
