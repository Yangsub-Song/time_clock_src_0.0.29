package com.techrove.timeclock.view

import com.techrove.timeclock.Settings
import com.techrove.timeclock.Styles
import com.techrove.timeclock.controller.MainController
import javafx.scene.paint.Color
import tornadofx.*
import java.time.format.DateTimeFormatter

/**
 * 화면 상단 Status view
 */
open class TopView : View("top") {
    private val controller: MainController by inject()

    private val formatter = DateTimeFormatter.ofPattern("H:mm")

    override val root = hbox {
        addClass(Styles.top)

        label(controller.timeProperty.objectBinding {
            formatter.format(it)
        })
        spacer()

        label(Settings.VERSION) {
            style { textFill = Color.GRAY }
        }
        imageview("/main/wifi_icon.png") {
            visibleWhen(controller.networkOnProperty)
        }
        imageview("/main/pc_icon.png") {
            visibleWhen(controller.serverOnProperty)
        }
        imageview("/main/info_icon.png") {
            visibleWhen(controller.infoOnProperty)
        }
    }
}
