package com.techrove.timeclock.view

import com.techrove.timeclock.Styles
import com.techrove.timeclock.controller.MainController
import javafx.geometry.Pos
import tornadofx.*
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * 메인 화면 중앙 View
 */
class CenterView : View("center") {
    private val controller: MainController by inject()
    private val formatter1 = DateTimeFormatter.ofPattern("yyyy.MM.dd", Locale.KOREA)
    private val formatter2 = DateTimeFormatter.ofPattern("(E)", Locale.KOREA)
    private val formatter3 = DateTimeFormatter.ofPattern("a", Locale.KOREA)
    // 12:00
    private val formatter4 = DateTimeFormatter.ofPattern("hh:mm", Locale.KOREA)
    // 00:00
    //private val formatter4 = DateTimeFormatter.ofPattern("KK:mm", Locale.KOREA)

    override val root = vbox {
        addClass(Styles.center)

        imageview("/main/광고영역.png")

        stackpane {
            vbox(alignment = Pos.CENTER, spacing = 0) {
                prefHeight = 410.0
                //imageview("/main/정보영역.png")

                ///////////////////////////////////////////////////////////////////////////
                // DATE
                ///////////////////////////////////////////////////////////////////////////

                hbox(alignment = Pos.BASELINE_CENTER, spacing = 32) {
                    label(controller.timeProperty.objectBinding {
                        formatter1.format(it)
                    })
                    label(controller.timeProperty.objectBinding {
                        formatter2.format(it)
                    })
                }

                ///////////////////////////////////////////////////////////////////////////
                // TIME
                ///////////////////////////////////////////////////////////////////////////

                hbox(alignment = Pos.BASELINE_CENTER, spacing = 32) {
                    label(controller.timeProperty.objectBinding {
                        formatter3.format(it).toLowerCase()
                    }) {
                        addClass(Styles.centerTimeAmPm)
                    }
                    label(controller.timeProperty.objectBinding {
                        formatter4.format(it).toLowerCase()
                    }) {
                        addClass(Styles.centerTime)
                    }
                }
            }

            vbox(alignment = Pos.BOTTOM_CENTER) {
                label(controller.notificationMessageProperty) {
                    addClass(Styles.centerNotification)
                }
                spacer { maxHeight = 32.0 }
             }
        }
    }
}

