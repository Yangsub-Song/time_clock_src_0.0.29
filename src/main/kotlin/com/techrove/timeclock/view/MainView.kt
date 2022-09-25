package com.techrove.timeclock.view


import com.techrove.timeclock.Settings
import com.techrove.timeclock.Styles
import com.techrove.timeclock.controller.MainController
import com.techrove.timeclock.controller.admin.RegisterFingerController
import com.techrove.timeclock.io.Audio
import com.techrove.timeclock.security.KeyHelper
import com.techrove.timeclock.view.admin.AdminView
import com.techrove.timeclock.view.custom.*
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.input.MouseEvent
import kotlinx.coroutines.launch
import tornadofx.*

/**
 * 메인 화면 View
 */
class MainView : View("TimeClock") {
    private val controller: MainController by inject()
    private var firstDock = true

    override val root = stackpane {
        borderpane {
            addClass(Styles.main)

            top<TopView>()
            center<CenterView>()
            bottom<BottomView>()
        }
        initRegisterFinger()
    }

    override fun onDock() {
        super.onDock()

        if (firstDock) {
            installHwKeyCheck()
            installHiddenTouch {
                // 1. SW 오류시 SW update, KEY 오류시 key 갱신 실행 하도록 설정 이동.
                // 2. 암호 갱신 만료시
                // 그 외에는 기능 막아 놓음
                if (!KeyHelper.allIntegrityOk || controller.passwordExpired) {
                    replaceWith(find<AdminView>())
                }
            }
        }

        runLater {
            controller.integrityOkProperty.onChange { ok ->
                if (!ok) {
                    showIntegrityErrorDialog()
                } else {
                    timeoutDialogClose()
                }
            }
            if (KeyHelper.allIntegrityOk) {
                // 초기 암호 설정 확인
                if (Settings.password.isEmpty()) {
                    replaceWith(find<AdminView>())
                } else {
                    // 암호 유효기간 확인
                    if (controller.checkPasswordExpiry()) {
                        root.tryAdminView(controller, changePassword = true)
                    }
                }
            } else {
                showIntegrityErrorDialog()
            }
        }
        firstDock = false
    }

    fun showIntegrityErrorDialog() {     // Yade0926 private => ' '
        timeoutDialog(
            "오류",
            when {
                !KeyHelper.swIntegrityOk -> "SW 유효성 오류가 발생했습니다."
                !KeyHelper.keyIntegrityOk -> "보안키 유효성 오류가 발생했습니다."
                else -> "알 수 없는 유효성 오류가 발생했습니다."
            } + "\n관리자에게 문의하십시오",
            iconType = IconType.Error,
            closable = false,
        )
    }

    ///////////////////////////////////////////////////////////////////////////
    // HW key check
    ///////////////////////////////////////////////////////////////////////////
    private var keySequence = mutableListOf<KeyCode>()
    private val keySequenceHidden = listOf(
        KeyCode.LEFT,
        KeyCode.DIGIT3,
        KeyCode.DIGIT5,
        KeyCode.DIGIT7,
        KeyCode.DIGIT1,
        KeyCode.DIGIT5,
        KeyCode.DIGIT9,
        KeyCode.RIGHT,
        KeyCode.ENTER,
    )

    private fun installHwKeyCheck() {
        var lastTick = 0L
        primaryStage.scene?.addEventFilter(KeyEvent.KEY_RELEASED) { event ->
            if (/*!dialogIsClosable || */!isDocked) return@addEventFilter
            if (!KeyHelper.allIntegrityOk) return@addEventFilter

            val tick = System.currentTimeMillis()
            if (tick - lastTick < 250) {
                return@addEventFilter
            }
            lastTick = tick

            ///////////////////////////////////////////////////////////////////////////
            // hidden key
            ///////////////////////////////////////////////////////////////////////////
            if (!timeoutDialogIsActive) {
                keySequence.add(event.code)
                println(keySequence)
                if (keySequence.size == keySequenceHidden.size) {
                    if (keySequence == keySequenceHidden) {
                        keySequence.clear()
                        replaceWith(find<AdminView>())
                    } else {
                        keySequence.removeFirst()
                    }
                }
            }

            // TODO: move controller property settings to controller
            when (event.code) {
                // 출근 HW 키 처리
                KeyCode.HOME -> {
                    // dialog 표시중 HW 키 disable
                    if (timeoutDialogIsActive) return@addEventFilter
                    agreementDialog(forFingerRegistration = false) {
                        controller.getOffWork = false
                        if (Settings.measureTemperatureOption != 0) {
                            controller.gotoWork = false
                            controller.measureTemperature = true
                        } else {
                            controller.gotoWork = true
                            controller.measureTemperature = false
                        }
                        controller.retryCount = 1
                    }
                }
                // 퇴근 HW 키 처리
                KeyCode.END -> {
                    // dialog 표시중 HW 키 disable
                    if (timeoutDialogIsActive) return@addEventFilter
                    agreementDialog(forFingerRegistration = false) {
                        controller.measureTemperature = false
                        controller.gotoWork = false
                        controller.getOffWork = true
                        controller.retryCount = 1
                    }
                }
                // 지문 등록 HW 키 처리
                KeyCode.INSERT -> {
                    // dialog 표시중 HW 키 disable
                    if (timeoutDialogIsActive) return@addEventFilter
                    controller.retryCount = -1
                    controller.measureTemperature = false
                    controller.gotoWork = false
                    controller.getOffWork = false
                    controller.launch { Audio.stop() }
                    agreementDialog {
                        val controller = find(RegisterFingerController::class)
                        controller.retryCount = 1
                        controller.fingerRegister = true
                    }
                    controller.retryCount = 1
                }
                // 취소 HW 키 처리
                KeyCode.ESCAPE -> {
                    if (controller.gotoWork || controller.getOffWork) {
                        controller.retryCount = -1
                        controller.measureTemperature = false
                        controller.gotoWork = false
                        controller.getOffWork = false
                        controller.launch { Audio.stop() }
                        controller.retryCount = 1
                        timeoutDialogClose()
                    }
                }
                else -> {}
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // hidden touch
    ///////////////////////////////////////////////////////////////////////////

    private var touchSequence = mutableListOf<Int>()
    private val touchSequenceHidden = listOf(0, 4, 4, 0, 0, 0, 2)

    private fun installHiddenTouch(block: () -> Unit) {
        primaryStage.addEventFilter(MouseEvent.MOUSE_RELEASED) { event ->
            if (event.y < 50) {
                val x = event.x / (768 / 5)
                touchSequence.add(x.toInt())
                if (touchSequence.size == touchSequenceHidden.size) {
                    if (touchSequence == touchSequenceHidden) {
                        touchSequence.clear()
                        block()
                    } else {
                        touchSequence.removeFirst()
                    }
                }
            }
        }
    }
}
