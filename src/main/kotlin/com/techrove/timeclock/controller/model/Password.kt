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

/**
 * sw업데이트암호 정보
 */
class SWUpdatePassword(password: String = "") {
    val swUpdatePasswordProperty = SimpleStringProperty(password)
}

/**
 * sw업데이트 암호 정보 모델
 */
class SWUpdatePasswordModel(password: Password) : ItemViewModel<Password>(password) {
    val password = bind(Password::passwordProperty)
}

/**
 * sFTP 암호 정보
 */
class SFTPPassword(password: String = "") {
    val sFTPPasswordProperty = SimpleStringProperty(password)
}

/**
 * sFTP 암호 정보 모델
 */
class SFTPPasswordModel(password: Password) : ItemViewModel<Password>(password) {
    val password = bind(Password::passwordProperty)
}
