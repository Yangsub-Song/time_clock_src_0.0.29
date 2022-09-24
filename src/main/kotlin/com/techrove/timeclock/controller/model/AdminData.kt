package com.techrove.timeclock.controller.model

import com.techrove.timeclock.Settings
import javafx.beans.property.SimpleStringProperty
import tornadofx.ItemViewModel


class AdminData(terminalId: String, placeCd: String) {
    val terminalIdProperty = SimpleStringProperty(terminalId)
    val placeCdProperty = SimpleStringProperty(placeCd)
    val passWord1Property = SimpleStringProperty("")
    val passWord2Property = SimpleStringProperty("")
    val swUpdatePassWord1Property = SimpleStringProperty("")
    val swUpdatePassWord2Property = SimpleStringProperty("")
}

class AdminModel : ItemViewModel<AdminData>(AdminData(Settings.terminalId, Settings.placeCd)) {
    val terminalId = bind(AdminData::terminalIdProperty)
    val placeCd = bind(AdminData::placeCdProperty)
    val password1 = bind(AdminData::passWord1Property)
    val password2 = bind(AdminData::passWord2Property)
    val swUpdatePassword1 = bind(AdminData::swUpdatePassWord1Property)  // Yade0924
    val swUpdatePassword2 = bind(AdminData::swUpdatePassWord2Property)  // Yade0924

    fun isDeviceInfoChanged(): Boolean {
        return (Settings.terminalId != terminalId.value || Settings.placeCd != placeCd.value)
    }

    fun updateDeviceInfo() {
        Settings.terminalId = terminalId.value
        Settings.placeCd = placeCd.value
    }

    fun resetDeviceInfo() {
        terminalId.value = Settings.terminalId
        placeCd.value = Settings.placeCd
    }

    fun resetPassword() {
        password1.value = ""
        password2.value = ""
    }
    fun resetSWUpdatePassword() {   // Yade0924
        swUpdatePassword1.value = ""
        swUpdatePassword2.value = ""
    }
}
