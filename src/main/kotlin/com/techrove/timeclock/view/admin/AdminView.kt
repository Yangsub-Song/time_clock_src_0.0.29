package com.techrove.timeclock.view.admin

import com.techrove.timeclock.Styles
import com.techrove.timeclock.controller.admin.RegisterFingerController
import com.techrove.timeclock.controller.admin.SettingsController
import com.techrove.timeclock.io.Audio
import com.techrove.timeclock.view.MainView
import com.techrove.timeclock.view.TopView
import com.techrove.timeclock.view.custom.agreementDialog
import com.techrove.timeclock.view.custom.onTimeoutDialogClosed
import com.techrove.timeclock.view.custom.onTimeoutDialogTimeout
import com.techrove.timeclock.view.custom.timeoutDialogClose
import javafx.event.EventHandler
import javafx.scene.input.KeyEvent
import javafx.scene.input.MouseEvent
import javafx.util.Duration
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import tornadofx.*

class AdminTopView : TopView()

///////////////////////////////////////////////////////////////////////////
// AdminView
///////////////////////////////////////////////////////////////////////////

/**
 * 관리자 화면 메인 view
 */
class AdminView : View("Admin") {
    private val controller: SettingsController by inject()
    val registerFingerShortCut: Boolean? by param()
    val changePasswordShortCut: Boolean? by param()

    private val eventHandlerPressed = TestKeyEventHandler(true)
    private val eventHandlerReleased = TestKeyEventHandler(false)
    private val mouseEventHandler = MouseEventHandler()
    private var idleTimeoutTask: FXTimerTask? = null

    override val root = stackpane {
        borderpane {
            addClass(Styles.admin)

            top<AdminTopView>()
            center<AdminCenterView>()
        }
    }

    inner class TestKeyEventHandler(private val pressed: Boolean) : EventHandler<KeyEvent> {
        override fun handle(event: KeyEvent) {
            resetTimeout()
        }
    }

    inner class MouseEventHandler : EventHandler<MouseEvent> {
        override fun handle(event: MouseEvent) {
            if (event.eventType == MouseEvent.MOUSE_PRESSED || event.eventType == MouseEvent.MOUSE_RELEASED || event.eventType == MouseEvent.MOUSE_MOVED) {
                resetTimeout()
            }
        }
    }

    override fun onDock() {
        controller.start()

        if (registerFingerShortCut == true) {
            runLater {
                agreementDialog {
                    val controller = find(RegisterFingerController::class)
                    controller.retryCount = 1
                    controller.fingerRegister = true
                }
            }
        } else if (changePasswordShortCut == true) {
            runLater {
                find(AdminCenterView::class).root.passwordChangeDialog(true)
            }
        }

        primaryStage.addEventFilter(KeyEvent.KEY_PRESSED, eventHandlerPressed)
        primaryStage.addEventFilter(KeyEvent.KEY_RELEASED, eventHandlerReleased)
        primaryStage.addEventFilter(MouseEvent.ANY, mouseEventHandler)
        resetTimeout()
        onTimeoutDialogClosed = { resetTimeout() }
        onTimeoutDialogTimeout = {
            if (it.toSeconds() > 3) {
                primaryStage.uiComponent<View>()?.replaceWith(find<MainView>())
            }
        }
    }

    override fun onUndock() {
        controller.stop()
        primaryStage.removeEventFilter(KeyEvent.KEY_PRESSED, eventHandlerPressed)
        primaryStage.removeEventFilter(KeyEvent.KEY_RELEASED, eventHandlerReleased)
        primaryStage.removeEventFilter(MouseEvent.ANY, mouseEventHandler)
        onTimeoutDialogClosed = null
        onTimeoutDialogTimeout = null
        GlobalScope.launch { Audio.stop() }
    }

    private fun resetTimeout(delay: Duration = defaultTimeout) {
        idleTimeoutTask?.cancel()
        idleTimeoutTask = runLater(delay) {
            if (controller.idleTimeOutEnabled && isDocked) {
//            if (timeoutDialogIsActive) {
//                resetTimeout(1.seconds)
//            } else {
                timeoutDialogClose()
                primaryStage.uiComponent<View>()?.replaceWith(find<MainView>())
//            }
            }
        }
    }

    companion object {
        val defaultTimeout = 30.seconds
    }
}
