package com.techrove.timeclock.view.custom

import com.techrove.timeclock.Styles
import com.techrove.timeclock.controller.admin.RegisterFingerController
import com.techrove.timeclock.io.Audio
import javafx.scene.paint.Color
import javafx.scene.text.TextAlignment
import tornadofx.*

fun View.agreementDialog(forFingerRegistration: Boolean = true, agreed: ()->Unit = {}) {
    ///////////////////////////////////////////////////////////////////////////
    // Register Finger Print
    ///////////////////////////////////////////////////////////////////////////
    Audio.play(
        if (forFingerRegistration) "지문등록을 위하여 귀하의 개인정보 수집 및 이용에 동의하십니까.wav" else
            "귀하의 개인정보 수집 및 이용에 동의하십니까.wav"
    )
    timeoutDialog(
        title = "개인정보 수집 및 이용 동의",
        message = if (forFingerRegistration) "지문등록을 위하여 귀하의 개인정보 수집 및 이용에 동의하십니까?" else
            "귀하의 개인정보 수집 및 이용에 동의하십니까?",
        iconType = IconType.Consent,
        delay = 30.seconds,
        buttons = listOf("아니오", "동의함"),
        op = {
            style { padding = box(32.px, 64.px) }
            textflow {
                textAlignment = TextAlignment.LEFT
                text("\n개인정보의 수집 목적 : 건설근로자 전자카드단말기 지문등록 목적\n").apply {
                    fill = Color.GRAY
                    font = Styles.customFontSmall
                }
                text("개인정보의 항목 : 주민등록(외국인등록) 번호, 핸드폰번호, 지문, 카드번호\n").apply {
                    fill = Color.GRAY
                    font = Styles.customFontSmall
                }
                text("개인정보의 보유 및 이용 기간 : 사진90일, 지문정보2년, 기타 출/퇴근 정보(카드정보, 이름 등) 준영구").apply {
                    fill = Color.GRAY
                    font = Styles.customFontSmall
                }
            }
        }
    ) {
        if (it == 1) {
            agreed()
            if (forFingerRegistration) {
                val controller = find(RegisterFingerController::class)
                controller.retryCount = 1
                controller.fingerRegister = true
            }
        }
    }
}
