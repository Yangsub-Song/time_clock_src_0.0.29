package com.techrove.timeclock

import com.techrove.timeclock.extensions.withOpacity
import com.techrove.timeclock.view.custom.PasswordFieldSkin
import javafx.geometry.Pos
import javafx.scene.effect.BlurType
import javafx.scene.effect.DropShadow
import javafx.scene.paint.Color
import javafx.scene.paint.CycleMethod
import javafx.scene.paint.LinearGradient
import javafx.scene.paint.Stop
import javafx.scene.text.FontWeight
import javafx.scene.text.TextAlignment
import tornadofx.*
import java.net.URI

class Styles : Stylesheet() {
    companion object {
        val main by cssclass()
        val admin by cssclass()

        val top by cssclass()
        val center by cssclass()
        val centerTimeAmPm by cssclass()
        val centerTime by cssclass()
        val centerNotification by cssclass()
        val bottom by cssclass()
        val bigButton by cssclass()

        val test by cssclass()
        val testButton by cssclass()

        val settings by cssclass()

        val dialogContainer by cssclass()
        val dialog by cssclass()
        val dialogTitle by cssclass()
        val dialogContent by cssclass()
        val dialogButton by cssclass()
        val large by csspseudoclass()
        val error by csspseudoclass()

        val toggleColor by cssproperty<Color>("-jfx-toggle-color")
        val ripplerFill by cssproperty<Color>("-jfx-rippler-fill")
        val disableVisualFocus by cssproperty<Boolean>("-jfx-disable-visual-focus")

        val jfxRippler by cssclass()

        private val primaryColor = Color.CORNFLOWERBLUE
        private val primaryBrighterColor = primaryColor.brighter()
        private val primaryDarkerColor = primaryColor.darker()
        private val errorColor = Color.rgb(252, 182, 186)

        private val radius = 6.px
        private val borderSize = 3.px


        val customFont = loadFont("/font/NotoSansKR-Medium.otf", 26.0)
        val customFontSmall = loadFont("/font/NotoSansKR-Medium.otf", 18.0)
        //private val customFont = loadFont("/font/NanumSquareR.ttf", 27.0)
        //private val customFont = loadFont("/font/NanumBarunGothicBold.ttf", 17.0)
        //private val customFont = loadFont("/font/LeferiPointBlack.ttf", 17.0)
        //private val customBoldFont = loadFont("/font/NanumGothicBold.ttf", 17.0)
    }

    init {
        //importStylesheet("/styles.css")

        root {
            //backgroundImage += URI("/main/bg.png")
//            backgroundSize += BackgroundSize(
//                BackgroundSize.AUTO,
//                BackgroundSize.AUTO,
//                true,
//                true,
//                false,
//                false, //true
//            )
            focusColor = Color.TRANSPARENT
            customFont?.let { fontFamily = it.family } //"Courier New"
            label {
                textFill = Color.WHITE
                fontSize = 16.px
                fontWeight = FontWeight.BOLD
            }
        }

        main {
            backgroundImage += URI("/main/bg.png")
        }
        admin {
            backgroundImage += URI("/main/bg_admin.png")
            jfxRippler {
                ripplerFill.value = Color.DARKGRAY
                backgroundRadius += box(9.px)
            }
        }
        top {
            alignment = Pos.CENTER
            padding = box(0.px, 31.px)
            prefHeight = 33.px
            spacing = 14.px
            label {
                fontSize = 21.px
            }
        }

        center {
            alignment = Pos.TOP_CENTER
            label {
                fontSize = 41.px
                textFill = Color.GRAY
            }
            centerNotification {
                fontSize = 20.px
                textFill = Color.DARKRED
            }
            centerTimeAmPm {
                fontSize = 55.px
                textFill = Color.rgb(123, 203, 209)
            }
            centerTime {
                fontSize = 90.px
                textFill = Color.WHITE
            }
        }

        bottom {
            backgroundImage += URI("/main/menu_bg.png")
            alignment = Pos.CENTER
            padding = box(0.px, 8.px)
            prefHeight = 180.px
            spacing = 6.px
            label {
                fontSize = 17.px
            }
            button {
                minWidth = 250.px
                minHeight = 110.px
            }
            bigButton {
                minWidth = 110.px
                minHeight = 110.px
            }
            jfxRippler {
                ripplerFill.value = Color.DARKGRAY
                backgroundRadius += box(9.px)
                fontSize = 27.px
                customFont?.let { fontFamily = it.family }
                fontWeight = FontWeight.BOLD
            }
        }

        dialogContainer {
            backgroundColor += Color.BLACK.withOpacity(0.4)
            and(error) {
/*
                dialog {
                    //borderRadius += box(2.px)
                    //borderWidth = multi(box(10.px))
                    borderColor = multi(box(Color.rgb(138, 0, 0)))
                    //backgroundColor += Color.rgb(252, 225, 227)
                }
*/
            }
        }

        dialog {
            backgroundColor += Color.rgb(0xdc, 0xdc, 0xdc)
            backgroundRadius += box(6.px)
            //borderWidth = multi(box(5.px))
            //borderColor = multi(box(Color.rgb(61, 61, 61)))
            //borderRadius += box(5.px)
            padding = box(0.px, 14.px)
            alignment = Pos.CENTER
            spacing = 0.px
            prefWidth = 700.px
            maxWidth = prefWidth
            prefHeight = 680.px
            maxHeight = prefHeight
            and(large) {
                prefHeight = 924.px
                maxHeight = prefHeight
            }
            dialogTitle {
                textFill = Color.rgb(0x33, 0x33, 0x33)
                fontSize = 30.px
                prefHeight = 90.px
                textAlignment = TextAlignment.CENTER
                //fontWeight = FontWeight.BOLD
            }
            dialogContent {
                alignment = Pos.TOP_CENTER
                backgroundColor += Color.WHITE
                backgroundRadius += box(6.px)
                effect = DropShadow(BlurType.TWO_PASS_BOX, Color.DARKGRAY, 5.0, 0.3, 0.0, 0.0)
                padding = box(20.px, 16.px, 16.px, 16.px)
                label {
                    textFill = Color.rgb(0x33, 0x33, 0x33)
                    fontSize = 26.px
                    customFont?.let { fontFamily = it.family }
                    fontWeight = FontWeight.NORMAL
                    wrapText = true
                    textAlignment = TextAlignment.CENTER
                }
            }
            form {
                label {
                    textFill = Color.DARKGRAY
                    fontSize = 20.px
                }
                fieldset {
                    spacing = 4.px
                }
                passwordField {
                    skin = PasswordFieldSkin::class
                }
                textField {
                    fontSize = 20.px
                    alignment = Pos.CENTER
                    and(error) {
                        focusColor = errorColor
                        faintFocusColor = errorColor
                        //backgroundColor += Color.RED
                    }
                }
            }
            radioButton {
                fontSize = 20.px
                customFont?.let { fontFamily = it.family }
            }
        }
        dialogButton {
            alignment = Pos.CENTER_LEFT
            spacing = 16.px
            prefHeight = 100.px
            button {
                backgroundColor += LinearGradient(
                    0.0,
                    0.0,
                    0.0,
                    1.0,
                    true,
                    CycleMethod.REFLECT,
                    Stop(0.0, Color.WHITE),
                    Stop(0.9, c(230, 230, 230)),
                    Stop(1.0, c(240, 240, 240))
                )
                backgroundRadius += box(6.px)
                borderRadius += box(5.px)
                borderColor = multi(box(Color.LIGHTGRAY))
                borderWidth = multi(box(1.px))
                effect = DropShadow(BlurType.TWO_PASS_BOX, Color.GRAY, 1.0, 0.3, 0.0, 0.0)
                minWidth = 160.px
                minHeight = 62.px
                textFill = Color.BLACK
                fontSize = 26.px
                and(disabled) {
                    // override default opacity settings for jfxbutton
                    opacity = 1.0
                }
                disableVisualFocus.value = true
            }
        }

        settings {
            backgroundImage += URI("/bg.png")
            padding = box(64.px)
            spacing = 32.px
            alignment = Pos.TOP_LEFT

            label {
                textFill = Color.WHITE
                fontSize = 18.px
            }
            button {
                prefWidth = 768.px
                prefHeight = 64.px
                textFill = Color.WHITE
                fontSize = 18.px
                backgroundColor += Color.STEELBLUE.withOpacity(0.7)
            }
            jfxRippler {
                fillWidth = true
                ripplerFill.value = Color.DARKGRAY
                backgroundRadius += box(9.px)
                fontSize = 27.px
                customFont?.let { fontFamily = it.family }
                fontWeight = FontWeight.BOLD
            }
            form {
                label {
                    textFill = Color.DARKGRAY
                    fontSize = 20.px
                }
                fieldset {
                    spacing = 4.px
                }
                textField {
                    fontSize = 20.px
                    and(error) {
                        backgroundColor += Color.RED
                    }
                }
            }
        }

        toggleButton {
            fontSize = 20.px
            toggleColor.value = Color.rgb(0x00, 0x97, 0x6e)
        }

        test {
            backgroundImage += URI("/bg.png")
            padding = box(32.px)
            backgroundColor += Color.BLACK.withOpacity(0.5)
            spacing = 32.px
            alignment = Pos.TOP_LEFT

            label {
                textFill = Color.WHITE
                fontSize = 16.px
            }
        }

        testButton {
            spacing = 32.px
            alignment = Pos.CENTER_LEFT
            button {
                textFill = Color.WHITE
                fontSize = 18.px
                fontWeight = FontWeight.BOLD
                val color = Color.rgb(10, 40, 87)
                backgroundColor += color
                minWidth = 164.px
                minHeight = 48.px
                borderWidth = multi(box(3.px))
                borderColor = multi(box(color.brighter()))
                and(pressed) {
                    backgroundColor += color.brighter()
                    borderColor = multi(box(color))
                }
            }
        }

        // form
        form {
            padding = box(8.px, 32.px)
            fieldset {
                spacing = 4.px
            }
            label {
                fontSize = 18.px
                textFill = Color.WHITE
            }
            spinner {
                fontSize = 18.px
                prefHeight = 48.px
                prefWidth = 800.px
            }
        }

        if (false) {
            // textarea
            textArea {
                backgroundColor = multi(Color.TRANSPARENT)

                textFill = Color.WHITE
                promptTextFill = Color.WHITE
                highlightFill = Color.TRANSPARENT
                highlightTextFill = Color.WHITE
                displayCaret = false
                fontWeight = FontWeight.LIGHT
                //fontStyle = FontPosture.REGULAR
                fontSize = 14.px
                fontFamily = "Courier New"
                prefWidth = 600.px

                content {
                    padding = box(8.px)
                    backgroundColor = multi(Color.TRANSPARENT)
                }
            }

            // scrollbar
            scrollBar {
                prefWidth = 24.px
                backgroundColor = multi(primaryDarkerColor)
                borderColor = multi(box(primaryDarkerColor))
/*
            backgroundRadius = multi(box(0.px, radius, radius, 0.px))
            borderRadius = multi(box(0.px, radius, radius, 0.px))
*/
                track {
                    backgroundColor = multi(primaryDarkerColor)
                    borderColor = multi(box(primaryDarkerColor))
                }
                thumb {
                    backgroundColor = multi(primaryColor)
                }
                incrementButton {
                    prefHeight = 24.px
                    backgroundColor = multi(primaryDarkerColor)
                }
                decrementButton {
                    prefHeight = 24.px
                    backgroundColor = multi(primaryDarkerColor)
                }
            }

            scrollPane {
                backgroundColor = multi(Color.TRANSPARENT)
                viewport {
                    backgroundColor = multi(Color.TRANSPARENT)
                }
            }

            // tab pane
            tabPane {
                tabMinWidth = 100.px

                tabContentArea {
                    backgroundColor += Color.RED.withOpacity(0.1)
                    backgroundRadius = multi(box(0.px, radius, radius, radius))
                    borderColor = multi(box(primaryBrighterColor))
                    borderWidth = multi(box(borderSize))
                    borderRadius = multi(box(0.px, radius, radius, radius))
                }
                tabHeaderArea {
                    padding = box(0.px, 0.px, -2.px, 0.px)
                    prefHeight = 60.px
                }
                tabHeaderBackground {
                    backgroundColor = multi(Color.TRANSPARENT)
                }
                tab {
                    prefHeight = 60.px
                    backgroundColor = multi(primaryDarkerColor)
                    backgroundRadius = multi(box(radius, radius, 0.px, 0.px))
                    backgroundInsets = multi(box(0.px, 3.px, 0.px, 0.px))
                    tabLabel {
                        padding = box(0.px, 3.px, 0.px, 0.px)
                        fontSize = 18.px
                    }
                    and(selected) {
                        backgroundColor = multi(primaryBrighterColor)
                        tabLabel {
                            textFill = Color.SNOW
                            fontWeight = FontWeight.BOLD
                        }
                    }
                }
            }
        }
    }
}
