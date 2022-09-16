package com.techrove.timeclock.io

enum class Key(private val value: String) {
    Number1("18"),
    Number2("14"),
    Number3("12"),
    Register("11"),

    Number4("28"),
    Number5("24"),
    Number6("22"),
    Store("21"),

    Number7("48"),
    Number8("44"),
    Number9("42"),
    Cancel("41"),

    Left("88"),
    Number0("84"),
    Right("82"),
    Ok("81"),

    None("");

    companion object {
        fun fromString(value: String) = values().find { it.value == value } ?: Key.None
    }
}
