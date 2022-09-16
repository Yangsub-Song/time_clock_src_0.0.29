package com.techrove.timeclock.view.admin

import com.techrove.timeclock.Styles
import com.techrove.timeclock.controller.admin.SettingsController
import com.techrove.timeclock.extensions.jfxButton
import com.techrove.timeclock.extensions.jfxToggleButton
import com.techrove.timeclock.isLinux
import com.techrove.timeclock.view.custom.IconType
import com.techrove.timeclock.view.custom.infoDialog
import com.techrove.timeclock.view.custom.timeoutDialog
import javafx.beans.property.Property
import javafx.event.EventTarget
import javafx.scene.Cursor
import javafx.scene.control.Button
import javafx.scene.control.Labeled
import javafx.util.Duration
import tornadofx.*
import java.net.URI


fun EventTarget.adminCenterVbox(op: AdminCenterViewVbox.() -> Unit = {}): AdminCenterViewVbox {
    val vbox = AdminCenterViewVbox()
    return opcr(this, vbox, op)
}

fun EventTarget.imageButton(
    image: String,
    op: Button.() -> Unit = {}
) {
    jfxButton {
        addClass(Styles.jfxRippler)
        style {
            backgroundImage += URI(image)
        }
        isFocusTraversable = false
        op()
    }
}

fun EventTarget.textButton(text: String, action: () -> Unit = {}) {
    jfxButton(text) {
        style { fontSize = 18.px }
        if (text.isEmpty()) {
            isVisible = false
        }
        action {
            val controller = find(SettingsController::class)
            controller.showTerminalInfo = false
            action()
        }
    }
}

fun EventTarget.toggleButton(property: Property<Boolean>, title: String) {
    jfxToggleButton(property, title) {
        runLater(100.millis) {
            if (isLinux) {
                (skin?.skinnable as? Labeled)?.graphic?.cursor = Cursor.NONE
            }
        }
    }
}

fun AdminCenterViewVbox.infoDialogCustom(
    message: String,
    title: String = "정보",
    iconType: IconType = IconType.Info,
    delay: Duration = 3.seconds,
    buttons: List<String> = listOf("확인"),
    action: (Int) -> Unit = {},
) {
    infoDialog(message, title, iconType, delay, buttons) {
        action(it)
        settingsDialog()
    }
}

fun AdminCenterViewVbox.confirmDialog(
    title: String,
    message: String,
    cancelled: () -> Unit = {},
    action: () -> Unit
) {
    timeoutDialog(
        title = title,
        message = message,
        iconType = IconType.Consent,
        delay = AdminView.defaultTimeout,
        buttons = listOf("아니오", "예")
    ) {
        if (it == -1) {
            cancelled()
            return@timeoutDialog
        }
        if (it == 1) {
            action()
        } else {
            cancelled()
            settingsDialog()
        }
    }
}
