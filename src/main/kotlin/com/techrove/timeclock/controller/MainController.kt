package com.techrove.timeclock.controller

import com.techrove.timeclock.Settings
import com.techrove.timeclock.controller.model.*
import com.techrove.timeclock.extensions.decodeHex
import com.techrove.timeclock.extensions.onChangeTrue
import com.techrove.timeclock.extensions.toLocalDate
import com.techrove.timeclock.io.*
import com.techrove.timeclock.isLinux
import com.techrove.timeclock.security.KeyHelper
import com.techrove.timeclock.server.admin.AdminServer
import com.techrove.timeclock.utils.SwUpdateUtils
import com.techrove.timeclock.view.custom.IconType
import javafx.beans.property.*
import kotlinx.coroutines.*
import kotlinx.coroutines.javafx.JavaFx
import mu.KotlinLogging
import tornadofx.*
import java.io.File
import java.net.URI
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.*


private val logger = KotlinLogging.logger("MainController")

@Suppress("NonAsciiCharacters")
class MainController : BaseController(Dispatchers.IO) {

    /** window os 인 경우 MOCK 동작 */
    private val MOCK = !isLinux

    /** 지문 인식 후 사용자 입력한 생일 정보 model */
    val dobModel = PersonModel(Person("00000000000"))

    /** 관리자 암호 입력 model */
    val passwordModel = PasswordModel(Password())
    var password by passwordModel.password

    /** sw업데이트 암호 입력 model */
    val swUpdatePasswordModel = PasswordModel(Password())
    var swUpdatePassword by swUpdatePasswordModel.password

    /** sFTP 암호 입력 model */
    val sFTPPasswordModel = PasswordModel(Password())
    var sFTPPassword by passwordModel.password

    /** network on/off property */
    val networkOnProperty = false.toProperty()
    var networkOn by networkOnProperty

    /** server on/off property */
    val serverOnProperty = false.toProperty()
    var serverOn by serverOnProperty

    /** 상태바 info icon on property. 현재 항상 on */
    val infoOnProperty = true.toProperty()
    var infoOn by infoOnProperty

    /** 상태바 시간 표시 property */
    val timeProperty = SimpleObjectProperty(LocalDateTime.now())
    var time by timeProperty

    /** 체온 측정 시작 property */
    val measureTemperatureProperty = false.toProperty()
    var measureTemperature by measureTemperatureProperty

    /** 측정한 체온 정보 property */
    val temperatureProperty = SimpleFloatProperty()
    var temperature by temperatureProperty

    /** 체온 측정 재시도 count property */
    val temperatureCountProperty = SimpleIntegerProperty()
    var temperatureCount by temperatureCountProperty

    /** 측정한 체온이 있는지 여부 flag property */
    val hasTemperatureProperty = temperatureCountProperty.booleanBinding { it!!.toInt() > 0 }

    /** 고온 flag property */
    val temperatureOverProperty = false.toProperty()
    var temperatureOver by temperatureOverProperty

    /** 출퇴근 동작시 촬영산 사진 정보 property */
    val photoProperty = SimpleStringProperty()

    /** 출근 시작 flag property */
    val gotoWorkProperty = false.toProperty()
    var gotoWork by gotoWorkProperty

    /** 지문 인식(출근) 시작 flag property */
    val gotoWorkByFingerProperty = false.toProperty()
    var gotoWorkByFinger by gotoWorkByFingerProperty

    /** 지문(출근) 정보 property */
    val gotoWorkFingerTemplateProperty = "".toProperty()
    var gotoWorkFingerTemplate by gotoWorkFingerTemplateProperty

    /** 퇴근 시작 flag property */
    val getOffWorkProperty = false.toProperty()
    var getOffWork by getOffWorkProperty

    /** 지문 인식(퇴근) 시작 flag property */
    val getOffWorkByFingerProperty = false.toProperty()
    var getOffWorkByFinger by getOffWorkByFingerProperty

    /** 지문(퇴근) 정보 property */
    val getOffWorkFingerTemplateProperty = "".toProperty()
    var getOffWorkFingerTemplate by getOffWorkFingerTemplateProperty

    /** 사진 촬영 flag property */
    val takePictureProperty = gotoWorkProperty or getOffWorkProperty

    /** 출/퇴근 재시도 count property */
    val retryCountProperty = SimpleIntegerProperty(1)
    var retryCount by retryCountProperty

    /** 정보 popup dialog message property */
    val infoMessageProperty = SimpleObjectProperty<InfoMessage>()
    var infoMessage: InfoMessage?
        get() = infoMessageProperty.value
        set(value) {
            runLater {
                infoMessageProperty.value = value
            }
        }

    /** 화면 중앙에 표시되는 notification message property */
    val notificationMessageProperty = SimpleStringProperty(null)
    var notificationMessage by notificationMessageProperty

    /** 유효성 ok flag property */
    val integrityOkProperty = SimpleBooleanProperty(true)

    /** 암호 만료 flag property */
    var passwordExpiredProperty = SimpleBooleanProperty(false)
    var passwordExpired by passwordExpiredProperty

    /** 기기 오류 상태 */
    private var deviceStatus: String? = null

    init {
        initClock()
        initNetwork()
        initGotoWork()
        initGetOffWork()
        monitorDeviceStatus()

        if (MOCK) {
            initMock()
        }
        takePictureProperty.onChange {
            if (!it) {
                //photoProperty.value = null
            } else {
                if (Settings.takePicture) {
                    takePicture()
                } else {
                    photoProperty.value = null
                }
            }
        }
    }

    /**
     * 시간 정보 업데이트. 주기적 동작 실행
     * 
     * - 유효성 확인
     * - 암호키 자동 갱신
     * - 관리자 암호 유효기간 확인
     * - 관리자 서버로 상태 전송
     */
    private fun initClock() {
        launch(Dispatchers.JavaFx) {
            var timeCheckIntegrity = LocalDateTime.now()
            var hour = 0

            while (isActive) {
                time = LocalDateTime.now()
                
                // 1시간 주기 확인 동작
                if (hour != time.hour) {
                    hour = time.hour

                    // 유효성 확인. program 시작 이후 하루 한번
                    if (time.isAfter(timeCheckIntegrity.plusHours(Settings.INTEGRITY_CHECK_HOURS))) {
                        timeCheckIntegrity = time
                        checkIntegrity()
                    }
                    // 암호키 자동 갱신 확인. 이전 갱신 부터 2년 후
                    if (time.isAfter(Settings.keyRenewedDate.plusYears(Settings.KEY_VALID_YEAR))) {
                        renewKeys()
                    }
                    // 관리자 암호 변경 확인
                    checkPasswordExpiry()
                    // 관리자 서버 상태 전송
                    sendDeviceStatus()
                    // 사진 저장 유효 기간 확인
                    deleteOldPhotos()
                }
                val wait = (60 - time.second) * 1000L - 100
                delay(wait)
            }
        }
    }

    /**
     * 관리자 암호 유혀 기간 체크
     */
    fun checkPasswordExpiry(): Boolean {
        val now = LocalDateTime.now()
        val passwordChangeDate = Settings.passwordRenewedDate.plusYears(Settings.PASSWORD_VALID_YEAR)
        val diff = ChronoUnit.DAYS.between(now, passwordChangeDate)
        notificationMessage = if (diff in 1..30) "암호 만료 ${diff}일 전입니다. 암호를 변경해주세요." else null
        passwordExpired = (diff <= 0)
        return passwordExpired
    }

    /**
     * 유효성 체크
     */
    private fun checkIntegrity() {
        launch(Dispatchers.Default) {
            logger.warn { "# integrity check #" }
            if (KeyHelper.checkSwIntegrity()) {
                KeyHelper.checkKeyIntegrity()
            }
            withContext(Dispatchers.JavaFx) {
                integrityOkProperty.value = KeyHelper.allIntegrityOk
            }
        }
    }

    /**
     * 키 갱신
     */
    private fun renewKeys() {
        launch(Dispatchers.Default) {
            logger.warn { "# renew keys #" }
            KeyHelper.renewKeys()
        }
    }

    /**
     * 관리자 서버로 기기 상태 전송. 자동 SW 업데이트
     */
    private fun sendDeviceStatus() {
        launch(Dispatchers.Default) {
            // add random delay
            delay((5..30).random() * 1000L)
            logger.warn { "# send device status #" }
            AdminServer.단말기정보(deviceStatus).let { (otaUrl, otaVersion, port) ->
                if (otaUrl == null) {
                    logger.error { "admin server comm error" }
                } else {
                    val deviceVersion = Version.parse(Settings.VERSION)
                    if (deviceVersion < otaVersion) {
                        logger.warn { "update is started $deviceVersion -> $otaVersion" }
                        retryCount = -1
                        measureTemperature = false
                        gotoWork = false
                        getOffWork = false
                        launch { Audio.stop() }
                        retryCount = 1
                        infoMessage =
                            InfoMessage(
                                "업데이트",
                                "SW 업데이트($otaVersion) 중입니다...",
                                IconType.Wait,
                                delay = null,
                                buttons = emptyList()
                            )
                        val (success, message) = SwUpdateUtils.swUpdateByOta(otaUrl, port)
                        if (success) {
                            logger.warn { "update success" }
                            infoMessage = InfoMessage(
                                "업데이트",
                                "SW 업데이트가 완료되었습니다.\n재시작 합니다.",
                                IconType.Info,
                                buttons = emptyList()
                            )
                            delay(2000)
                            SwUpdateUtils.restartApp()
                        } else {
                            infoMessage = InfoMessage(
                                "업데이트",
                                "SW 업데이트를 실패했습니다.\n$message",
                                IconType.Error,
                            )
                        }
                    }
                }
            }
        }
    }

    /**
     * Windows 개발시 카드/지문 자동 동작 기능
     */
    private fun initMock() {
        gotoWorkProperty.onChangeTrue {
            launch {
                delay(2000)
                withContext(Dispatchers.JavaFx) {
                    if (gotoWork) {
                        RfReader.onRfCardTagged?.let { it("6262750400677563") }
                    }
                }
            }
        }
        getOffWorkProperty.onChangeTrue {
            launch {
                delay(2000)
                withContext(Dispatchers.JavaFx) {
                    if (getOffWork) {
                        RfReader.onRfCardTagged?.let { it("6262750400677563") }
                    }
                }
            }
        }
    }

    /**
     * 지문 스캔 동작
     */
    suspend fun scanFingerTemplate(scan: Boolean = true): String? {
        if (MOCK) {
            delay(3000)
            return if (scan) {
                Base64.getEncoder()
                    .encodeToString("45171219910055463743f0198e3d44b0140c2505617a8737057070873d45d068881285e179872686412084110740260b1c07611d8427c89078890d8920258828c9b125092c0b008189344b612f8e2ccb70328a414b8054bc0d4ba081853a4cf1460e0e8d012205098d302e872b0e204186318e803d8d2dcef1960affffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffbbbcfffffffffbaaaabbbbcfffffff9aaaaabbbbccfffffa99aaaaaaabcccdfffa9999aaaaaabccdfffa9999999999abbffff9999998888889bfff89988888877764ffff88888887776643ffff88877777766533fffff887777666553ffffff77766666654ffffffff7fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff0eefff000000000000000000000000000000000000000000000000000000000000000000000000".decodeHex())
            } else {
                null
            }
        } else {
            return if (scan) {
                Suprema.scanTemplate()?.let { Base64.getEncoder().encodeToString(it) }
            } else {
                Suprema.cancel()
                null
            }
        }
    }

    /** 사진 촬영 */
    private fun takePicture() = launch(Dispatchers.Default) {
        val now = LocalDateTime.now()
        val path = "./pictures"
        val dir = File(
            "$path/%02d%02d%02d".format(
                now.year - 2000,
                now.monthValue,
                now.dayOfMonth
            )
        )
        // create folder if not exits
        try {
            if (!dir.exists()) {
                dir.mkdirs()
            }
        } catch (e: Exception) {
            logger.error { e }
        }

        // take capture
        try {
            val photoFile = File(
                dir,
                "%02d%02d%02d.jpg".format(now.hour, now.minute, now.second)
            )
            Camera.takePicture(photoFile, encrypt = true)
            withContext(Dispatchers.JavaFx) {
                photoProperty.value = photoFile.toURI().toString()
            }

        } catch (e: Exception) {
            logger.error { e }
        }
    }

    /**
     * 90일 이전 사진 모두 삭제
     */
    private fun deleteOldPhotos() {
        launch(Dispatchers.Default) {
            val now = LocalDateTime.now()
            val path = "./pictures"

            try {
                val dateToDelete = now.minusDays(90).toLocalDate()
                File(path).list { _, name ->
                    name.toLocalDate?.isBefore(dateToDelete) ?: false
                }?.forEach {
                    logger.info { "=== REMOVING $it ===" }
                    File(path, it).deleteRecursively()
                }
            } catch (e: Exception) {
                logger.error { e }
            }
        }
    }

    /** 
     * 이전 사진 촬영시 저장한 파일 삭제
     * 
     * 출퇴근 오류나 취소시 삭제함
     */
    fun deleteLastPhoto() {
        try {
            File(URI(photoProperty.value)).delete()
        } catch (e: Exception) {
            logger.error { e }
        }
    }

    /**
     * 기기 오류 상태 모니터링
     */
    private fun monitorDeviceStatus() {
        launch(Dispatchers.Default) {
            /**
            B	하드웨어 장애
            J	소프트웨어 장애

            1	(Hostcom) : 통신(LTE 라우터)
            2	(Access sensor)  : 접근센서
            3	(Finger) : 지문인식
            4	(Camera): 카메라
            5	(Radio frequency): RF Reader
            6	(Temperature sensor) : 온도 센서
            7	(liquid crystal display) : OLED/ LCD 제어부
            8	(Keypad) : 키패드
            9	(PCAP Touch) : 정전식 터치
            F	기타

            0	추후 사용
            00~FF	하드웨어/소프트웨어 장애 코드
             */
            while (true) {
                //if (KeyPad.isHwStatusTriggered || RfReader.isHwStatusTriggered || Suprema.isHwStatusTriggered || TempSensor.isHwStatusTriggered) {
/*
                if (!KeyPad.isHwOk || !RfReader.isHwOk || !Suprema.isHwOk || !TempSensor.isHwOk) {
                    val sb = StringBuilder()
                    if (!KeyPad.isHwOk) {
                        sb.append("키패드 [B8001]")
                    }
                    if (!RfReader.isHwOk) {
                        sb.append("\nRF리더기 [B5001]")
                    }
                    if (!Suprema.isHwOk) {
                        sb.append("\n지문인식기 [B3001]")
                    }
                    if (!TempSensor.isHwOk) {
                        sb.append("\n온도센서 [B6001]")
                    }
                    infoMessage = InfoMessage("오류", sb.toString(), IconType.Error)
                }
*/
                deviceStatus = if (!KeyPad.isHwOk) {
                    "B8001"
                } else if (!RfReader.isHwOk) {
                    "B5001"
                } else if (!Suprema.isHwOk) {
                    "B3001"
                } else if (!TempSensor.isHwOk) {
                    "B6001"
                } else {
                    null
                }
                delay(10000)
            }
        }
    }

    /**
     * 출퇴근 오류시 info popup 공통 처리
     */
    fun on출퇴근Error() {
        runLater {
            infoMessage = InfoMessage(
                "안내",
                "관리자에게 문의하세요!\n(처음 화면으로 이동합니다)",
                IconType.Error,
                buttons = emptyList()
            )
        }
    }
}

