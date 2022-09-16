package com.techrove.timeclock.controller.model

import javafx.beans.property.SimpleStringProperty
import tornadofx.ItemViewModel

/**
 * 암호 정보
 */
class Password(password: String = "") {
    val passwordProperty = SimpleStringProperty(password)
}

/**
 * 암호 정보 모델
 */
class PasswordModel(password: Password) : ItemViewModel<Password>(password) {
    val password = bind(Password::passwordProperty)
}
