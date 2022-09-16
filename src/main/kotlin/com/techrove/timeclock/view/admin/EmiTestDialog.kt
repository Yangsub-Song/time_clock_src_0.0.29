package com.techrove.timeclock.view.admin

import com.techrove.timeclock.controller.admin.EmiTestController
import com.techrove.timeclock.controller.admin.SettingsController
import com.techrove.timeclock.extensions.imageViewEncrypted
import com.techrove.timeclock.extensions.jfxSlider
import com.techrove.timeclock.security.Key
import com.techrove.timeclock.view.custom.IconType
import com.techrove.timeclock.view.custom.timeoutDialog
import javafx.geometry.Pos
import javafx.scene.layout.Priority
import tornadofx.*
import tornadofx.Stylesheet.Companion.imageView

/**
 * EMI 테스트 dialog
 */
fun AdminCenterViewVbox.emiTestDialog() {
    val controller = find(EmiTestController::class)
    controller.startTest()
    find(SettingsController::class).idleTimeOutEnabled = false
    timeoutDialog(
        title = "EMI 테스트",
        iconType = IconType.Wait,
        buttons = listOf("종료"),
        op = {
            style { padding = box(32.px, 32.px) }
            vbox(alignment = Pos.CENTER) {
                hbox(alignment = Pos.CENTER, spacing = 32) {
                    label(controller.delayProperty.stringBinding { "동작주기: $it s" })
                    jfxSlider(
                        controller.delayMin.toDouble(),
                        controller.delayMax.toDouble(),
                        controller.delay.toDouble()
                    ) {
                        hgrow = Priority.ALWAYS
                        valueProperty().onChange {
                            if (!isValueChanging) {
                                controller.delay = it.toLong()
                            }
                        }
                        valueChangingProperty().onChange {
                            if (!it) {
                                controller.delay = this.value.toLong()
                            }
                        }
                    }
                }
                imageview(controller.photoProperty) {
                    isPreserveRatio = true
                    fitWidth = 310.0
                    fitHeight = 220.0
                }

            }
        }
    ) {
        controller.stop()
        find(SettingsController::class).idleTimeOutEnabled = true
        settingsDialog()
    }
}
