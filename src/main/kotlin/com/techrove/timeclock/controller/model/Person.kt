package com.techrove.timeclock.controller.model

import javafx.beans.property.SimpleStringProperty
import tornadofx.ItemViewModel

/**
 * 사용자 정보. (전화번호, 주민번호)
 */
class Person(phoneNumber: String = "", residentNumber: String = "") {
    val phoneNumberProperty = SimpleStringProperty(phoneNumber)
    //val phoneNumber by phoneNumberProperty
    val residentNumberProperty = SimpleStringProperty(residentNumber)
    //val residentNumber by residentNumberProperty
}

/**
 * 사용자 정보 model. (전화번호, 주민번호)
 */
class PersonModel(person: Person) : ItemViewModel<Person>(person) {
    val phoneNumber = bind(Person::phoneNumberProperty)
    val residentNumber = bind(Person::residentNumberProperty)

    fun reset() {
        phoneNumber.value = ""
        residentNumber.value = ""
    }
}

