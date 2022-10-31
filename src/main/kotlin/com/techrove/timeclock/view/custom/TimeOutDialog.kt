package com.techrove.timeclock.view.custom

import javafx.beans.value.ObservableValue
import javafx.event.EventHandler
import javafx.scene.input.KeyEvent
import javafx.scene.input.MouseEvent
import javafx.scene.layout.Pane
import javafx.scene.layout.VBox
import javafx.util.Duration
import tornadofx.*

private var dialog: CustomDialog? = null
private var dialogTimeoutTask: FXTimerTask? = null

val timeoutDialogIsActive get() = dialog != null
var onTimeoutDialogTimeout: ((delay: Duration) -> Unit)? = null
var onTimeoutDialogClosed: (() -> Unit)? = null

fun View.timeoutDialog(title: String? = null,
                       message: String? = null,
                       iconType: IconType = IconType.None,
                       large: Boolean = false,
                       keyboard: Boolean = false,
                       showCameraIcon: Boolean = false,
                       lastEnabledWhen: ObservableValue<Boolean>? = null,
                       delay: Duration? = null,
                       closable: Boolean = false,
                       buttons: List<String> = emptyList(),
                       op: VBox.() -> Unit = {},
                       action: (Int) -> Unit = {},
): CustomDialog? {
    return (root as? Pane)?.timeoutDialog(title, message, iconType, large, keyboard, showCameraIcon, lastEnabledWhen, delay, closable, buttons, op, action)
}

fun Pane.timeoutDialog(
    title: String? = null,
    message: String? = null,
    iconType: IconType = IconType.None,
    large: Boolean = false,
    keyboard: Boolean = false,
    showCameraIcon: Boolean = false,
    lastEnabledWhen: ObservableValue<Boolean>? = null,
    delay: Duration? = null,
    closable: Boolean = false,
    buttons: List<String> = emptyList(),
    op: VBox.() -> Unit = {},
    action: (Int) -> Unit = {},
): CustomDialog? {

    fun resetTimeout() {
        dialogTimeoutTask?.cancel()
        delay?.let {
            dialogTimeoutTask = runLater(it) {
                timeoutDialogClose()
                action(-1)
                onTimeoutDialogTimeout?.invoke(delay)
            }
        }
    }

    resetTimeout()
    timeoutDialogClose()

    dialog = customDialog(
        title,
        message,
        iconType,
        large,
        keyboard,
        showCameraIcon,
        lastButtonEnabledWhen = lastEnabledWhen,
        closable = closable,
        op = op,
        buttons = buttons
    ) {
        timeoutDialogClose()
        action(it)
    }

    dialog?.root?.run {
        addEventFilter(KeyEvent.KEY_TYPED) { resetTimeout() }
        addEventFilter(MouseEvent.MOUSE_MOVED) { resetTimeout() }
        addEventFilter(MouseEvent.MOUSE_CLICKED) { resetTimeout() }
    }

    dialog?.attach()
    return dialog
}

fun timeoutDialogClose() {
    dialog?.detach(false)
    dialog = null
    onTimeoutDialogClosed?.invoke()
}

fun Pane.infoDialog(
    message: String,
    title: String = "정보",
    iconType: IconType = IconType.Info,
    delay: Duration = 3.seconds,
    buttons: List<String> = listOf("확인"),
    action: (Int) -> Unit = {},
) {
    timeoutDialog(
        title,
        message,
        iconType = iconType,
        delay = delay,
        buttons = buttons
    ) {
        action(it)
    }
}
