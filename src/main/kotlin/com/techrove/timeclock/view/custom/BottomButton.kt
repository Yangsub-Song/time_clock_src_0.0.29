package com.techrove.timeclock.view.custom

import com.techrove.timeclock.Styles
import com.techrove.timeclock.extensions.jfxButton
import javafx.geometry.Pos
import javafx.scene.control.ButtonBase
import javafx.scene.layout.Pane
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import tornadofx.*
import java.net.URI

/**
 * 하단 버튼
 */
fun Pane.bottomButton(
    image: String,
    title: String? = null,
    subTitle: String? = null,
    color: Color = Color.WHITE,
    op: VBox.(button: ButtonBase) -> Unit = {}
) {
    vbox(alignment = Pos.CENTER, spacing = 5) {
        var button: ButtonBase? = null
        stackpane {
            button = jfxButton(title) {
                addClass(Styles.jfxRippler)
                style {
                    backgroundImage += URI(image)
                    textFill = color
                }
                isFocusTraversable = false
            }
        }
        subTitle?.let { label(it) }
        button?.let { op(it) }
    }
}
