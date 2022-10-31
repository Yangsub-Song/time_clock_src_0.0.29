package com.techrove.timeclock.view.custom

import com.techrove.timeclock.Styles
import com.techrove.timeclock.extensions.findChildOfId
import com.techrove.timeclock.extensions.getLastTextField
import com.techrove.timeclock.extensions.hasTextField
import com.techrove.timeclock.extensions.jfxButton
import com.techrove.timeclock.io.Audio
import com.techrove.timeclock.io.Video
import javafx.beans.value.ObservableValue
import javafx.geometry.Pos
import javafx.scene.control.TextField
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.layout.Pane
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.robot.Robot
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.comtel2000.keyboard.control.KeyboardPane
import org.comtel2000.keyboard.control.KeyboardType
import tornadofx.*


var dialogIsClosable = true

private var keyEventAttached = false

private val keyboardPane = KeyboardPane().apply {
    keyBoardStyle = "/KeyboardButtonStyle.css"
    load()
}

private val IconType.imageFile
    get() = when (this) {
        IconType.None -> null
        IconType.Info -> "/main/ICO_Complete.png"
        IconType.Error -> "/main/ICO_Error.png"
        IconType.Wait -> "/main/ICO_Wait.png"
        IconType.PassWord -> "/main/ICO_Lock.png"
        IconType.Consent -> "/main/ICO_Contract.png"
        IconType.Temperature -> "/main/ICO_Temperature.png"
        IconType.FingerPrint -> "/main/ICO_Fingerprint.png"
        IconType.FingerPrintOk -> "/main/ICO_FingerprintOK.png"
    }

/**
 * custom dialog
 */
class CustomDialog(
    title: String?,
    message: String?,
    iconType: IconType = IconType.None,
    large: Boolean = false,
    private val keyboard: Boolean = false,
    showCameraIcon: Boolean = false,
    buttons: List<String> = emptyList(),
    lastButtonEnabledWhen: ObservableValue<Boolean>? = null,
    private val closable: Boolean = false,
    op: VBox.() -> Unit = {},
    onClicked: (Int) -> Unit = {}
) : View("") {

    override val root = vbox(alignment = Pos.TOP_CENTER) {
        addClass(Styles.dialogContainer)
        spacer { maxHeight = 50.0 }
        vbox {
            addClass(Styles.dialog)
            if (large) {
                addClass(Styles.large)
            }

            ///////////////////////////////////////////////////////////////////////////
            // TITLE
            ///////////////////////////////////////////////////////////////////////////
            title?.let {
                label(title) {
                    addClass(Styles.dialogTitle)
                }
            }

            ///////////////////////////////////////////////////////////////////////////
            // ICON / MESSAGE
            ///////////////////////////////////////////////////////////////////////////
            vbox {
                vgrow = Priority.ALWAYS
                addClass(Styles.dialogContent)
                ///////////////////////////////////////////////////////////////////////////
                // ICON
                ///////////////////////////////////////////////////////////////////////////
                spacer { maxHeight = 12.0 }
                imageview(iconType.imageFile)
                spacer { maxHeight = 32.0 }
                ///////////////////////////////////////////////////////////////////////////
                // MESSAGE
                ///////////////////////////////////////////////////////////////////////////
                message?.let { label(it) }
                op()
            }

            ///////////////////////////////////////////////////////////////////////////
            // BUTTONS
            ///////////////////////////////////////////////////////////////////////////
            stackpane {
                hbox {
                    addClass(Styles.dialogButton)
                    buttons.forEachIndexed { i, it ->
                        val last = i == buttons.size - 1
                        jfxButton(it) {
                            if (it.isEmpty()) {
                                isVisible = false
                            }
                            if (last) {
                                id = "last_button"
                                lastButtonEnabledWhen?.let {
                                    enableWhen(it)
                                    textFillProperty().bind(it.objectBinding { enabled ->
                                        if (enabled == true) Color.rgb(0x20, 0x88, 0x00) else Color.rgb(0xbe, 0xbe, 0xbe)
                                    })
                                }
                                // positive button focus request by default
                                runLater {
                                    if (buttons.size > 1 && currentStage?.scene?.root?.hasTextField() != true) {
                                        requestFocus()
                                    }
                                }
                            }
                            style {
                                textFill = if (last && buttons.size > 1) {
                                    Color.rgb(0x20, 0x88, 0x00)
                                } else Color.rgb(0xd6, 0x00, 0x00)
                            }
                            action {
                                //close()
                                detach()
                                onClicked(i)
                            }
                            isFocusTraversable = false
                        }
                        if (!last) {
                            spacer()
                        }
                    }
                }
                if (showCameraIcon) {
                    imageview("/main/ICO_Cam.png") {
                        isPreserveRatio = true
                        fitHeight = 54.0
                    }
                }
            }
        }
        spacer()
    }

    fun attach() {
        if (keyboard) {
            runLater {
                keyboardPane.attachTo(this) {
                    isVisible = true
                    setKeyboardType(KeyboardType.TEXT)
                }
            }
        }
//        root.opacity = 0.0
//        currentStage?.scene?.root?.add(this)
//        root.opacityProperty().animate(1.0, 300.millis, Interpolator.EASE_BOTH)
        currentStage?.scene?.root?.add(this)
        if (!closable) {
            dialogIsClosable = false
        }

        if (!keyEventAttached) {
            primaryStage.addEventHandler(KeyEvent.KEY_RELEASED) {
                (primaryStage.scene?.focusOwner as? TextField)?.run {
                    when (it.code) {
                        KeyCode.ENTER -> {
                            // 모든 text field valid 시 마지막 text field 에서 enter 입력시 positive button 자동 click
                            if (getLastTextField() == this) {
                                // get last button
                                scene.root.findChildOfId("last_button")?.let { lastButton ->
                                    if (!lastButton.isDisabled) {
                                        lastButton.requestFocus()
                                        Robot().apply {
                                            keyPress(KeyCode.ENTER)
                                            keyRelease(KeyCode.ENTER)
                                        }
                                        return@addEventHandler
                                    }
                                }
                            }
                            Robot().apply {
                                keyPress(KeyCode.TAB)
                                keyRelease(KeyCode.TAB)
                            }
                        }
                        KeyCode.ESCAPE -> {
                            Robot().apply {
                                keyPress(KeyCode.BACK_SPACE)
                                keyRelease(KeyCode.BACK_SPACE)
                            }
                        }
                        else -> {}
                    }
                }
            }
            keyEventAttached = true
        }
    }

    fun detach(stopAudio: Boolean = true) {
//        root.opacityProperty().animate(0.0, 300.millis, Interpolator.EASE_BOTH)
//        runLater(300.millis) {
//            removeFromParent()
//        }
        keyboardPane.removeFromParent()
        removeFromParent()
        dialogIsClosable = true
        //Video.play("ad.mp4")
        if (stopAudio) {
            GlobalScope.launch {
                Audio.stop()
            }
        }
    }
}

fun Pane.customDialog(
    title: String? = null,
    message: String? = null,
    iconType: IconType = IconType.None,
    large: Boolean = false,
    keyboard: Boolean = false,
    showCameraIcon: Boolean = false,
    buttons: List<String> = emptyList(),
    lastButtonEnabledWhen: ObservableValue<Boolean>? = null,
    closable: Boolean = false,
    op: VBox.() -> Unit = {},
    onClicked: (Int) -> Unit = {}
) = CustomDialog(
    title,
    message,
    iconType,
    large,
    keyboard,
    showCameraIcon,
    op = op,
    lastButtonEnabledWhen = lastButtonEnabledWhen,
    closable = closable,
    onClicked = onClicked,
    buttons = buttons
).apply {
    Video.stop()
}

/*
private fun UIComponent.openDialog(
    stageStyle: StageStyle = StageStyle.TRANSPARENT,
    modality: Modality = Modality.APPLICATION_MODAL,
    owner: Window? = currentWindow,
    block: Boolean = false,
    resizable: Boolean = false
): Stage? {
    if (modalStage == null) {
        modalStage = Stage(stageStyle)
        // modalStage needs to be set before this code to make close() work in blocking mode
        with(modalStage!!) {
            aboutToBeShown = true
            isResizable = resizable

            initModality(modality)
            if (owner != null) initOwner(owner)

            Scene(root).apply {
                FX.applyStylesheetsTo(this)
                scene = this
                properties["tornadofx.scene"] = this
                fill = Color.TRANSPARENT
                if (isLinux) {
                    cursor = Cursor.NONE
                }
            }

            y = (owner?.y ?: 0.0) + 64
            x = (owner?.x ?: 0.0) + 15

            opacity = 0.0
            //root.opacity = 0.0
            //fade(1000.millis, 1.0)
            runLater(300.millis) {
                opacity = 1.0
            }

            setOnShown {
                aboutToBeShown = false
                customDialogIsActive = true
            }

            setOnHidden {
                modalStage = null
                customDialogIsActive = false
            }
            if (block) showAndWait() else show()
        }
    } else {
        if (!modalStage!!.isShowing)
            modalStage!!.show()
    }

    return modalStage
}
*/
