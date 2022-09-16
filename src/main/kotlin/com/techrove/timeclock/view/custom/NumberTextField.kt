package com.techrove.timeclock.view.custom

import com.techrove.timeclock.Styles
import com.techrove.timeclock.extensions.isRegistrationNumber
import javafx.beans.value.ObservableValue
import javafx.event.EventTarget
import javafx.scene.Node
import javafx.scene.control.PasswordField
import javafx.scene.control.TextField
import javafx.scene.control.skin.TextFieldSkin
import tornadofx.*


class PasswordFieldSkin(passwordField: PasswordField) : TextFieldSkin(passwordField) {
    override fun maskText(txt: String): String {
        val textField = skinnable
        val n = textField.length
        val passwordBuilder = StringBuilder(n)
        if (n > 0) {
            if (n > 1) {
                repeat(n - 1) {
                    passwordBuilder.append(BULLET)
                }
            }
            passwordBuilder.append(textField.text.last())
        }
        return passwordBuilder.toString()
    }

    companion object {
        const val BULLET = '\u2022'
    }
}

class EmptyMessageDecorator : Decorator {
    override fun decorate(node: Node) {
    }

    override fun undecorate(node: Node) {
    }
}

fun EventTarget.numberTextField(
    property: ObservableValue<String>,
    maxLen: Int,
    regexValid: String,
    registrationNumber: Boolean = false,
    password: Boolean = false,
    numberOnly: Boolean = false,
    op: TextField.() -> Unit = {}
) = (if (password) passwordfield() else textfield()).apply {
    bind(property)

    if (password && !numberOnly) {
        filterInput { it.controlNewText.let { it.length <= maxLen } }
    } else {
        filterInput { it.controlNewText.let { it.all { it.isDigit() } && it.length <= maxLen } }
    }
    validator {
        // remove decoration
        decorationProvider = { _ -> EmptyMessageDecorator() }
        val value = property.value
        val valid  = if (registrationNumber && value.length == 13) {
            value.isRegistrationNumber.also {
                if (!it) {
                    return@validator error("주민등록번호를 확인하세요")
                }
            }
        } else {
            regexValid.toRegex().matches(value)
        }
        if (valid) {
            removeClass(Styles.error)
            null
        } else {
            addClass(Styles.error)
            error()
        }
    }

    selectedTextProperty().onChange {
        it?.let {
            deselect()
            positionCaret(it.length)
        }
    }

    op(this)
}
