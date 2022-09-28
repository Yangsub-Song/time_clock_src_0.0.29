package com.techrove.timeclock

import com.techrove.timeclock.utils.Preference
import java.time.LocalDateTime

/**
 * 설정 object
 */
object Settings: Preference() {
    private const val VERSION_MAJOR = 0
    private const val VERSION_MINOR = 1
    private const val VERSION_PATCH = 51     // Yade20220917, 0.0.29->0.0.30
                                            // Yade20220924, 0.0.30->0.1.1  - SW 업데이트 암호 변경
                                            // Yade20220925, 0.0.30->0.1.2  - sFTP 암호 변경
                                            // Yade20220925, 0.0.30->0.1.3  - terminalId, placeId, adminHost, cwmaHost 하드코딩 제거
                                            // Yade20220926, 0.0.30->0.1.4  - 무결성 체크 후 에러 시, showIntegrityErrorDialog() 표시
                                            // Yade20220928, 0.0.30->0.1.5  - ADMIN_KEY, DEFAULT_KEY 하드코딩 제거

    const val VERSION = "$VERSION_MAJOR.$VERSION_MINOR.$VERSION_PATCH"

    //const val DEFAULT_KEY = "AQIDBAUGBwgJCgsMDQ4PEBESExQVFhcYGRobHB0eHyA="  // Yade0927
    var DEFAULT_KEY: String by preference(userPref, "DEFAULT_KEY", "")  // Yade0928

    const val PASSWORD_VALID_YEAR = 2L

    const val KEY_VALID_YEAR = 2L

    const val INTEGRITY_CHECK_HOURS = 24L

    //var adminHost: String by preference(userPref, "adminHost", "http://testing.centrali.co.kr")
    // var adminHost: String by preference(userPref, "adminHost", "http://aplexcorp.iptime.org")
    var adminHost: String by preference(userPref, "adminHost", "http://aplexcorp.iptime.org")

//    const val ADMIN_KEY = "WnZr4u7x!A%D*G-K"
    var ADMIN_KEY: String by preference(userPref, "ADMIN_KEY", "")  // Yade0927
    var cwmaHost: String by preference(userPref, "cwmaHost", "https://test_ecard.cwma.or.kr")

    // Yade0925
//    var terminalId: String by preference(userPref, "terminalId", "100103970101713715177")
//    var placeCd: String by preference(userPref, "placeCd", "064536")
    var terminalId: String by preference(userPref, "terminalId", "")
    var placeCd: String by preference(userPref, "placeCd", "")

    var password: String by preference(userPref, "password", "")
    var passwordRenewedDate: LocalDateTime by preference(userPref, "password_renewed", LocalDateTime.now())

    var swUpdatePassword: String by preference(userPref, "swUpdatePassword", "")                                    // Yade0924
    var swUpdatePasswordRenewedDate: LocalDateTime by preference(userPref, "swUpdatePassword_renewed", LocalDateTime.now())    // Yade0924
    var sFTPPassword: String by preference(userPref, "sFTPPassword", "")                                         // Yade0925
    var sFTPPasswordRenewedDate: LocalDateTime by preference(userPref, "sFTPPassword_renewed", LocalDateTime.now())        // Yade0925

    var volume: Int by preference(userPref, "volume", 100)

    var beep: Boolean by preference(userPref, "beep", true)

    val measureTemperatureOption: Int by preference(userPref, "temp_opt", 0)

    var takePicture: Boolean by preference(userPref, "take_picture", true)

    var keyRenewedDate: LocalDateTime by preference(userPref, "key_renewed", LocalDateTime.now())

}
