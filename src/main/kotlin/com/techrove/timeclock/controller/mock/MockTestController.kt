package com.techrove.timeclock.controller.mock

import com.techrove.timeclock.controller.test.TestController
import com.techrove.timeclock.controller.test.TestData
import kotlinx.coroutines.*
import mu.KotlinLogging

private val logger = KotlinLogging.logger("TestController")

class MockTestController : TestController() {
    private var i = 0

    ///////////////////////////////////////////////////////////////////////////
    // 지문
    ///////////////////////////////////////////////////////////////////////////
    
    override val fingerPrint = TestData("지문", "버튼 클릭시 지문 스캔을 합니다.") {
        logger.info { "## 지문 ##" }
        if (i == 0) {
            "123"
        } else {
            null
        }.also {
            if (++i == 2) i = 0
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // 온도센서
    ///////////////////////////////////////////////////////////////////////////
    
    override val temperature = TestData("온도센서", "버튼 클릭시 온도를 읽습니다.") {
        logger.info { "## 온도센서 ##" }
        if (i == 0) {
            "123"
        } else {
            null
        }.also {
            if (++i == 2) i = 0
        }
    }

    protected override fun initController() {
        launch {
            var i = 0
            while (isActive) {
                delay(300)
                temperature.valueProperty.value = "$i °C"
                i++
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // 카드리더기
    ///////////////////////////////////////////////////////////////////////////
    
    override val cardReader = TestData("카드리더기", "카드 태깅시 번호를 표시합니다.") {
        logger.info { "## 카드리더기 ##" }
        if (i == 0) {
            "123456780129"
        } else {
            null
        }.also {
            if (++i == 2) i = 0
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // 카메라
    ///////////////////////////////////////////////////////////////////////////
    
    override val camera = TestData("카메라", "버튼 클릭시 카메라 캡춰를 진행합니다.") {
        logger.info { "## 카메라 ##" }
        imageProperty.value = null
        imageProperty.value = "https://picsum.photos/128"
        description
    }
}
