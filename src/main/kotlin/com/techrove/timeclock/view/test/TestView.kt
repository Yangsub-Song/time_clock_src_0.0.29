package com.techrove.timeclock.view.test

import com.techrove.timeclock.Styles
import com.techrove.timeclock.controller.test.TestController
import com.techrove.timeclock.controller.test.TestData
import com.techrove.timeclock.controller.mock.MockTestController
import com.techrove.timeclock.isLinux
import com.techrove.timeclock.view.MainView
import javafx.geometry.Pos
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import tornadofx.*

class TestView : View("TEST") {
    //private val controller = find(TestController::class)
    private val controller = find(if (isLinux) TestController::class else MockTestController::class)

    override val root = stackpane {
        vbox {
            addClass(Styles.test)

            ///////////////////////////////////////////////////////////////////////////
            // KEYPAD
            ///////////////////////////////////////////////////////////////////////////
/*
        label(text = "키패드 코드:").apply {
            isFocusTraversable = true
            addEventHandler(KeyEvent.KEY_PRESSED) { key ->
                text = "키패드 코드:  ${key.code.name}"
                key.consume()
            }
            runLater(1.millis) { requestFocus() }
        }

        label(text = "GPIO:").apply {
        }
*/

            ///////////////////////////////////////////////////////////////////////////
            // 지문
            ///////////////////////////////////////////////////////////////////////////

            testButton(controller.fingerPrint)

            ///////////////////////////////////////////////////////////////////////////
            // 온도
            ///////////////////////////////////////////////////////////////////////////

            testButton(controller.temperature, true)

            ///////////////////////////////////////////////////////////////////////////
            // 카메라
            ///////////////////////////////////////////////////////////////////////////

            testButton(controller.camera) {
                val size = 128.0
                stackpane {
                    prefWidth = size
                    prefHeight = size
                    //style {
                    //    backgroundColor += Color.RED
                    //}
                    alignment = Pos.CENTER_LEFT
                    imageview(controller.imageProperty) {
                        fitWidth = size
                        isPreserveRatio = true
                    }
                }
            }

            ///////////////////////////////////////////////////////////////////////////
            // 카드리더기
            ///////////////////////////////////////////////////////////////////////////

            testButton(controller.cardReader, true)

            ///////////////////////////////////////////////////////////////////////////
            // 사운도
            ///////////////////////////////////////////////////////////////////////////
            testButton(controller.sound)

            ///////////////////////////////////////////////////////////////////////////
            // ETC
            ///////////////////////////////////////////////////////////////////////////

            addClass(Styles.testButton)
            button(text = "종료") {
                isFocusTraversable = false
                action {
                    replaceWith(find<MainView>())
                    //close()
                    controller.destroy()
                }
            }

            ///////////////////////////////////////////////////////////////////////////
            // LOG
            ///////////////////////////////////////////////////////////////////////////

            textarea {
                isEditable = false
                isWrapText = true
                vgrow = Priority.ALWAYS
                controller.logTail.onChange {
                    it?.let {
                        if (text.length > 36 * 1024) {
                            clear()
                        }
                        appendText(it)
                    }
                }
            }
        }
    }

    private fun VBox.testButton(
        testData: TestData,
        disabled: Boolean = false,
        op: VBox.() -> Unit = {}
    ) {
        hbox(alignment = Pos.CENTER_LEFT) {
            addClass(Styles.testButton)
            button(testData.title) {
                isDisable = disabled
                action {
                    testData.action(scope = controller)
                }
                isFocusTraversable = false
            }
            vbox(alignment = Pos.CENTER_LEFT, spacing = 8) {
                label {
                    textProperty().bind(testData.valueProperty)
                    textFillProperty().bind(testData.colorProperty)
                }
                op()
            }
        }
    }
}