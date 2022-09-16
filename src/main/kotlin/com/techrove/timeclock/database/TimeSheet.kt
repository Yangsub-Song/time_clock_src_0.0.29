package com.techrove.timeclock.database

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.datetime


enum class TimeSheetType(val value: Int) {
    GoingToWork(1),

    GetOffWork(2);

    companion object {
        fun fromInt(value: Int) = values().first { it.value == value }
    }
}

object TimeSheets : IntIdTable() {
    val terminalId = varchar("terminal_id", 100)
    val placeCd = varchar("place_cd", 100)
    val date = datetime("date").index()
    val type = enumeration("type", TimeSheetType::class)
    val cardNumber = varchar("card_no", 128).nullable()
    val dob = varchar("dob", 48).nullable()
    val fingerPrint = text("finger_print").nullable()
}

class TimeSheet(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<TimeSheet>(TimeSheets)

    var terminalId by TimeSheets.terminalId
    var placeCd by TimeSheets.placeCd
    var date by TimeSheets.date
    var type by TimeSheets.type
    var cardNumber by TimeSheets.cardNumber
    var dob by TimeSheets.dob
    var fingerPrint by TimeSheets.fingerPrint

    override fun toString(): String {
        return "TimeSheet(terminalId='$terminalId', placeCd='$placeCd', date=$date, type=$type, cardNumber=$cardNumber, dob=$dob, fingerPrint=$fingerPrint)"
    }
}
