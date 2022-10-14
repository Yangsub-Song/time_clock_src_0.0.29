package com.techrove.timeclock.view.admin

import com.techrove.timeclock.controller.admin.RegisterFingerController
import com.techrove.timeclock.controller.admin.SettingsController
import com.techrove.timeclock.io.Audio
import com.techrove.timeclock.view.MainView
import com.techrove.timeclock.view.custom.agreementDialog
import javafx.geometry.Pos
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import tornadofx.*

///////////////////////////////////////////////////////////////////////////
// Admin Center
///////////////////////////////////////////////////////////////////////////
class AdminCenterViewVbox: VBox()

/**
 * 관리자 화면 중앙 view
 */
class AdminCenterView : View("Admin") {
    private val controller: SettingsController by inject()

    override val root = adminCenterVbox {
        style { padding = box(34.px) }
        imageButton("/main/BTN_GoToMain.png") {
            prefWidth = 160.0
            prefHeight = 70.0
            action {
                primaryStage.uiComponent<View>()?.replaceWith(find<MainView>())
            }
        }
        spacer { maxHeight = 160.0 }
        hbox(alignment = Pos.CENTER) {
            spacer()
            imageButton("/main/BTN_AddFingerprint.png") {
                prefWidth = 220.0
                prefHeight = 250.0
                action {
                    agreementDialog {
                        val controller = find(RegisterFingerController::class)
                        controller.retryCount = 1
                        controller.fingerRegister = true
                    }
                }
            }
            spacer()
            imageButton("/main/BTN_Setting.png") {
                prefWidth = 220.0
                prefHeight = 250.0
                action {
                    Audio.play("beep1.wav")
                    settingsDialog()
                }
            }
            spacer()
        }
        spacer()
        vbox(spacing = 5) {
            style { padding = box(23.px) }
            label(stringBinding(controller.ipAddressProperty, controller.macAddressProperty) {
                "SW 버전: ${controller.swVersion},  IP: ${controller.ipAddress},  MAC: ${controller.macAddress}"
            })
            label(controller.memFreeProperty.stringBinding(controller.memTotalProperty) {
                val free = controller.memFree
                val total = controller.memTotal
                "메모리 사용량: Free-${free / 1024 / 1024}MB, Total-${total / 1024 / 1024}MB"
            })
            label(controller.diskFreeProperty.stringBinding(controller.diskTotalProperty) {
                val free = controller.diskFree
                val total = controller.diskTotal
                "디스크 사용량: Free-${free / 1024 / 1024 / 1024}GB, Total-${total / 1024 / 1024 / 1024}GB"
            })
            label(controller.cpuTemperatureProperty.stringBinding {
                "CPU 온도 : ${
                    String.format(
                        "%.1f °C",
                        it
                    )
                }"
            })
            label("터미널 ID: ${controller.model.terminalId.value}") {
                style { textFill = Color.rgb(0x00, 0x97, 0x6e) }
                //visibleWhen(controller.showTerminalInfoProperty)
            }
            label("현장 코드: ${controller.model.placeCd.value}") {
                style { textFill = Color.rgb(0x00, 0x97, 0x6e) }
                //visibleWhen(controller.showTerminalInfoProperty)
            }
        }

        initRegisterFinger()
        initSFTPPasswordChange()        // Yade1011
        initUpdateSWPasswordChange()    // Yade0922
        initPasswordChange()
    }
}