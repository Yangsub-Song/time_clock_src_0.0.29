package com.techrove.timeclock

import com.techrove.timeclock.utils.Preference
import java.time.LocalDateTime

/**
 * 설정 object
 */
object Settings: Preference() {
    private const val VERSION_MAJOR = 0
    private const val VERSION_MINOR = 0
    private const val VERSION_PATCH = 30    // Yade20220917

    const val VERSION = "$VERSION_MAJOR.$VERSION_MINOR.$VERSION_PATCH"

    const val DEFAULT_KEY = "AQIDBAUGBwgJCgsMDQ4PEBESExQVFhcYGRobHB0eHyA="

    const val PASSWORD_VALID_YEAR = 2L

    const val KEY_VALID_YEAR = 2L

    const val INTEGRITY_CHECK_HOURS = 24L

    //var adminHost: String by preference(userPref, "adminHost", "http://testing.centrali.co.kr")
    var adminHost: String by preference(userPref, "adminHost", "http://aplexcorp.iptime.org")

    const val ADMIN_KEY = "WnZr4u7x!A%D*G-K"

    var cwmaHost: String by preference(userPref, "cwmaHost", "https://test_ecard.cwma.or.kr")

    var terminalId: String by preference(userPref, "terminalId", "100103970101713715177")

    var placeCd: String by preference(userPref, "placeCd", "064536")

    var password: String by preference(userPref, "password", "")

    var passwordRenewedDate: LocalDateTime by preference(userPref, "password_renewed", LocalDateTime.now())

    var volume: Int by preference(userPref, "volume", 100)

    var beep: Boolean by preference(userPref, "beep", true)

    val measureTemperatureOption: Int by preference(userPref, "temp_opt", 0)

    var takePicture: Boolean by preference(userPref, "take_picture", true)

    var keyRenewedDate: LocalDateTime by preference(userPref, "key_renewed", LocalDateTime.now())

}
