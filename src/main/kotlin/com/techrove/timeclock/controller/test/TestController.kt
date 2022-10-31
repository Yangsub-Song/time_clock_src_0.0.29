package com.techrove.timeclock.controller.test

import com.techrove.timeclock.io.*
import com.techrove.timeclock.isLinux
import com.techrove.timeclock.utils.toHexString
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.paint.Color
import kotlinx.coroutines.*
import kotlinx.coroutines.javafx.JavaFx
import mu.KotlinLogging
import org.apache.commons.io.input.Tailer
import org.apache.commons.io.input.TailerListenerAdapter
import tornadofx.Controller
import tornadofx.getValue
import tornadofx.objectBinding
import tornadofx.setValue
import java.io.File
import kotlin.system.exitProcess

private val logger = KotlinLogging.logger("TestController")

open class TestController :Controller(), CoroutineScope {
    private val job: Job = Job()

    override val coroutineContext get() = Dispatchers.JavaFx + job

    init {
        initController()
        RfReader.onRfCardTagged = { s ->
            cardReader.valueProperty.value = s
            launch {
                delay(3000)
                cardReader.valueProperty.value = cardReader.description
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // 지문
    ///////////////////////////////////////////////////////////////////////////

    open val fingerPrint = TestData("지문", "버튼 클릭시 지문 스캔을 합니다.") {
        logger.info { "## 지문 ##" }
        Suprema.scanTemplate()?.toHexString().also {
            logger.trace { it }
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // 온도센서
    ///////////////////////////////////////////////////////////////////////////

    open val temperature = TestData("온도센서", "버튼 클릭시 온도를 읽습니다.") {
        logger.info { "## 온도센서 ##" }
        TempSensor.getTemperature()?.let { "%.1f °C".format(it) }
    }

    protected open fun initController() {
        launch(Dispatchers.IO) {
            while (isActive) {
                delay(1000)
                val value = TempSensor.getTemperature()?.let { "%.1f °C".format(it) } ?: "오류 발생"
                withContext(Dispatchers.JavaFx) {
                    temperature.valueProperty.value = value
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // SOUND
    ///////////////////////////////////////////////////////////////////////////

    private var isPlaying = false

    open val sound = TestData("사운드", "버튼 클릭시 샘플 음악이 재생됩니다.") {
        logger.info { "## 사운드 ##" }
        when {
            isPlaying -> {
                Audio.stop()
                isPlaying = false
                description
            }
            else -> {
                isPlaying = true
                Audio.play("test.wav", false) {
                    value = description
                    isPlaying = false
                }
                "재생 중..."
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // 카드리더기
    ///////////////////////////////////////////////////////////////////////////

    open val cardReader = TestData("카드리더기", "카드 태깅시 번호를 표시합니다.") {
        null
    }

    ///////////////////////////////////////////////////////////////////////////
    // 카메라
    ///////////////////////////////////////////////////////////////////////////

    val imageProperty = SimpleStringProperty()
    private var imageIndex = 0

    open val camera = TestData("카메라", "버튼 클릭시 카메라 캡춰를 진행합니다.") {
        logger.info { "## 카메라 ##" }
        val imageFile = File("test${imageIndex}.jpg")
        if (++imageIndex == 2) {
            imageIndex = 0
        }
        if (Camera.takePicture(imageFile)) {
            imageProperty.value = imageFile.toURI().toString()
            description
        } else {
            null
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // 로그
    ///////////////////////////////////////////////////////////////////////////

    open val logTail = SimpleStringProperty()

    private var tailer: Tailer? = null

    init {
        tailer = Tailer(File("log/log.log"), object : TailerListenerAdapter() {
            override fun handle(line: String) {
                launch {
                    logTail.value = line + "\n"
                }
            }
        }, 300, true).apply {
            launch(Dispatchers.Default) {
                tailer?.run()
            }
        }
    }

    fun destroy() {
        tailer?.stop()
        if (isLinux) {
            //"/home/linaro/run.sh".runCommand()
            exitProcess(0)
        }
    }
}


open class TestData(val title: String, val description: String = "", private val exec: suspend TestData.() -> String? = { null }) {
    val valueProperty = SimpleStringProperty(description)
    var value by valueProperty

    val errorProperty = SimpleBooleanProperty(false)
    var error by errorProperty

    val colorProperty = errorProperty.objectBinding { if (it == true) Color.RED else Color.WHITE }

    fun action(scope: CoroutineScope) {
        scope.launch {
            withContext(Dispatchers.IO) { exec() }?.let {
                value = it
                error = false
            } ?: run {
                value = "오류 발생"
                error = true
            }
        }
    }
}
