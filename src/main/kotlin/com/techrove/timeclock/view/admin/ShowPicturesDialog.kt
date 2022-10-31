package com.techrove.timeclock.view.admin

import com.techrove.timeclock.controller.admin.PictureController
import com.techrove.timeclock.extensions.imageViewEncrypted
import com.techrove.timeclock.io.Audio
import com.techrove.timeclock.security.Key
import com.techrove.timeclock.view.custom.timeoutDialog
import tornadofx.*
import java.io.File
import com.techrove.timeclock.security.KeyHelper    // Yade0920
import com.techrove.timeclock.view.MainView
import com.techrove.timeclock.view.custom.IconType
import mu.KotlinLogging

private val logger = KotlinLogging.logger("MainController") // Yade0920

/**
 * 사진 viewer dialog
 */
fun AdminCenterViewVbox.showPicturesDialog() {
    // 키 유효성 체크. UI 처리는 MainView 에서 함.
    if (KeyHelper.checkKeyIntegrity() && KeyHelper.checkKeyIntegrity2()) // Yade0920, 1020
        logger.info { "무결성 체크 OK" }// [인증용로그]
    else {
        logger.info { "무결성 체크 Error(사진보기)" }// [인증용로그]
        find(MainView::class).showIntegrityErrorDialog()   // Yade0926
        return@showPicturesDialog
    }
    val controller = find(PictureController::class)
    Audio.play("beep1.wav")
    timeoutDialog(
        title = "사진 보기",
        large = true,
        buttons = listOf("닫기"),
        op = {
            stackpane {
                style {
                    padding = box(16.px)
                    prefHeight = 700.px
                }
                vbox {
                    hbox {
                        prefHeight = 700.0
                        hiddenWhen(controller.photoListLoadingProperty)
                        listview<File> {
                            fun reload() {
                                asyncItems {
                                    controller.loadPictures().also {
                                        runLater(100.millis) {
                                            bindSelected(controller.photoSelectedProperty)
                                            selectionModel.select(0)
                                            requestFocus()
                                            scrollTo(0)
                                        }
                                    }
                                }
                            }
                            reload()
                            cellFormat {
                                text = it.path.removePrefix("./pictures/")
                            }
                            controller.photoSortDescendingProperty.onChange {
                                reload()
                            }
                        }
                        spacer()
                        imageViewEncrypted(controller.photoSelectedProperty.stringBinding { it?.toURI()?.toString() ?: "/main/MEDIA_Cam.png" }) {
                            isPreserveRatio = true
                            fitWidth = 310.0
                            fitHeight = 220.0
                        }
                        spacer()
                    }
                    spacer()
                    toggleButton(controller.photoSortDescendingProperty, "최신 먼저 보기")
                }
                label("로딩 중...") {
                    visibleWhen(controller.photoListLoadingProperty.and(controller.photoEmptyProperty.not()))
                }
                label("사진이 없습니다.") {
                    visibleWhen(controller.photoEmptyProperty)
                }
            }
        }
    ) {
        if (it == -1) return@timeoutDialog
        settingsDialog()
    }
}
