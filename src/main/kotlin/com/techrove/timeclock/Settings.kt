package com.techrove.timeclock

import com.techrove.timeclock.utils.Preference
import java.time.LocalDateTime

/**
 * 설정 object
 */
object Settings: Preference() {
    private const val VERSION_MAJOR = 0
    private const val VERSION_MINOR = 1
    private const val VERSION_PATCH = 32    // Yade20220917, 0.0.29->0.0.30
                                            // Yade20220924, 0.0.30->0.1.1  - SW 업데이트 암호 변경
                                            // Yade20220925, 0.0.30->0.1.2  - sFTP 암호 변경
                                            // Yade20220925, 0.0.30->0.1.3  - terminalId, placeId, adminHost, cwmaHost 하드코딩 제거
                                            // Yade20220926, 0.0.30->0.1.4  - 무결성 체크 후 에러 시, showIntegrityErrorDialog() 표시
                                            // Yade20220928, 0.0.30->0.1.5  - ADMIN_KEY, DEFAULT_KEY 하드코딩 제거 without encrption
                                            // Yade20220930, 0.0.30->0.1.52  - ADMIN_KEY, DEFAULT_KEY - 암호화해서 prefs.xml에 보관한 뒤, 프로그램에서 읽어 복호화한 후 키로 사용
                                            // Yade20221003, 0.1.52->0.0.30  - terminalId, placeId 미초기화에 따른 보안키 등록 에러 수정
                                            // Yade20221005, 0.1.54->0.1.30  - KTC향 릴리스
                                            // Yade20221014, 0.1.57->0.1.31  - KTC향 인증 버전 릴리스 - sw업데이트 암호/sFTP 암호 버튼 비활성화 문제 해결
                                            // Yade20221017, 0.1.59->0.1.32  - KTC향 인증 버전 릴리스 - 관리자/디폴트키 AES-256 암호화/복호와


    const val VERSION = "$VERSION_MAJOR.$VERSION_MINOR.$VERSION_PATCH"

    var DEFAULT_KEY = ""
    var DEFAULT_KEY_ENC: String by preference(userPref, "DEFAULT_KEY_ENC", "")
    var DEFAULT_KEY_AES_ENC: String by preference(userPref, "DEFAULT_KEY_AES_ENC", "")
    const val PASSWORD_VALID_YEAR = 2L

    const val KEY_VALID_YEAR = 2L

    const val INTEGRITY_CHECK_HOURS = 24L

    //var adminHost: String by preference(userPref, "adminHost", "http://testing.centrali.co.kr")
//    var adminHost: String by preference(userPref, "adminHost", "http://aplexcorp.iptime.org")
    //    var adminHost: String by preference(userPref, "adminHost", "http://aplexcorp.iptime.org")
    var adminHost: String by preference(userPref, "adminHost", "http://ez-m.co.kr")

//    const val ADMIN_KEY = "WnZr4u7x!A%D*G-K"
    var ADMIN_KEY = "" // : String by preference(userPref, "ADMIN_KEY", "")  // Yade0927 // ""WnZr4u7x!A%D*G-K"
    var ADMIN_KEY_ENC: String by preference(userPref, "ADMIN_KEY_ENC", "")  // Yade0927
    var ADMIN_KEY_AES_ENC: String by preference(userPref, "ADMIN_KEY_AES_ENC", "")
    var cwmaHost: String by preference(userPref, "cwmaHost", "https://test_ecard.cwma.or.kr")

    // Yade0925
    var terminalId: String by preference(userPref, "terminalId", "100103970101713715177")   // terminalId와 placeCd의 초기값이 널이면 보안키 등록에서 에러가 남.
    var placeCd: String by preference(userPref, "placeCd", "064536")
//    var terminalId: String by preference(userPref, "terminalId", "")
//    var placeCd: String by preference(userPref, "placeCd", "")

    var password: String by preference(userPref, "PASSWORD", "")
    var passwordRenewedDate: LocalDateTime by preference(userPref, "PASSWORD_RENEWED", LocalDateTime.now())

    var swUpdatePassword: String by preference(userPref, "SWUPDATE_PASSWORD", "")                                    // Yade0924
    var swUpdatePasswordRenewedDate: LocalDateTime by preference(userPref, "SWUPDATE_PASSWORD_RENEWED", LocalDateTime.now())    // Yade0924
    var sFTPPassword: String by preference(userPref, "SFTP_PASSWORD", "")                                         // Yade0925
    var sFTPPasswordRenewedDate: LocalDateTime by preference(userPref, "SFTP_PASSWORD_RENEWED", LocalDateTime.now())        // Yade0925

    var volume: Int by preference(userPref, "volume", 100)

    var beep: Boolean by preference(userPref, "beep", true)

    val measureTemperatureOption: Int by preference(userPref, "temp_opt", 0)

    var takePicture: Boolean by preference(userPref, "take_picture", true)

    var keyRenewedDate: LocalDateTime by preference(userPref, "KEY_RENEWED", LocalDateTime.now())

//    var keyRenewedDate2: LocalDateTime by preference(userPref, "KEY_RENEWED2", LocalDateTime.now())
}
