package com.techrove.timeclock.view

import com.techrove.timeclock.Styles
import com.techrove.timeclock.controller.MainController
import com.techrove.timeclock.extensions.imageViewEncrypted
import com.techrove.timeclock.extensions.onChangeTrue
import com.techrove.timeclock.view.custom.timeoutDialog
import javafx.geometry.Pos
import javafx.scene.paint.Color
import tornadofx.*


class BottomView : View("bottom") {
    private val controller: MainController by inject()

    override val root = hbox {
        addClass(Styles.bottom)

        ///////////////////////////////////////////////////////////////////////////
        // BOTTOM buttons
        ///////////////////////////////////////////////////////////////////////////

        admin(controller)
        addFinger(this@BottomView)
        spacer()
        goToWork(controller, this@BottomView)
        getOffWork(controller, this@BottomView)

        ///////////////////////////////////////////////////////////////////////////
        // INFORMATION popup
        ///////////////////////////////////////////////////////////////////////////

        controller.infoMessageProperty.onChange {
            if (it == null) return@onChange
            timeoutDialog(
                it.title,
                it.message,
                delay = it.delay,
                iconType = it.iconType,
                large = !it.imageFile.isNullOrEmpty(),
                closable = true,
                buttons = it.buttons ?: listOf("닫기"),
                op = {
                    it.errorMessage?.let { errorMessage ->
                        spacer { prefHeight = 32.0 }
                        label(errorMessage) {
                            style {
                                textFill = Color.rgb(0xd6, 0x00, 0x00)
                            }
                        }
                    }
                    it.formMessages?.let { formMessages ->
                        spacer { maxHeight = 32.0 }
                        form {
                            fieldset {
                                formMessages.forEach { (title, message) ->
                                    field(title) {
                                        textfield(message) {
                                            isFocusTraversable = false
                                            alignment = Pos.CENTER
                                        }
                                    }
                                }
                            }
                        }
                    }
                    it.imageFile?.let { imageFile ->
                        spacer()
                        if (it.imageEncrypted) {
                            imageViewEncrypted(imageFile) {
                                isPreserveRatio = false
                                fitWidth = 310.0
                                fitHeight = 220.0
                            }
                        } else {
                            imageview(imageFile) {
                                isPreserveRatio = false
                                fitWidth = 310.0
                                fitHeight = 220.0
                            }
                        }
                    }
                }
            ) {
                controller.infoMessage = null
                if (it == 0) {
                    // cancel 출퇴근 retry
                    controller.retryCount = -1
                }
            }.apply {
                //if (error) {
                //    this?.root?.addClass(Styles.error)
                //}
            }
        }

        // 관리자 암호 갱신 처리
        controller.passwordExpiredProperty.onChangeTrue {
            tryAdminView(controller, changePassword = true)
        }
    }
}
