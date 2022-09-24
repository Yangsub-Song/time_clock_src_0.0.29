package com.techrove.timeclock.view.admin

import com.techrove.timeclock.Styles
import com.techrove.timeclock.controller.admin.SettingsController
import com.techrove.timeclock.view.custom.timeoutDialog
import javafx.geometry.Pos
import tornadofx.*

/**
 * 관리자/설정 dialog
 */
fun AdminCenterViewVbox.settingsDialog() {
    val controller = find(SettingsController::class)

    controller.showTerminalInfo = true
    timeoutDialog("SETTING", delay = AdminView.defaultTimeout, buttons = listOf("닫기"), op = {
        val spacing = 8
        vbox(spacing = spacing, alignment = Pos.CENTER) {
            addClass(Styles.dialogButton)
            hbox(spacing = spacing, alignment = Pos.CENTER) {
                spacer()
                textButton("기기 설정") { optionsDialog() }
                spacer()
                textButton("단말기 정보 변경") { terminalInfoDialog() }
                spacer()
                textButton("단말기 상태 정보") { terminalStatusDialog() }
                spacer()
            }
            hbox(spacing = spacing, alignment = Pos.CENTER) {
                spacer()
                textButton("관리자 암호 변경") { passwordChangeDialog() }
                spacer()
                textButton("SW 업데이트") { swUpdateDialog() }
                spacer()
                textButton("OTA 업데이트") { otaUpdateDialog() }
                spacer()
            }
            hbox(spacing = spacing, alignment = Pos.CENTER) {
                spacer()
                textButton("공장 초기화") { factoryResetDialog() }
                spacer()
                textButton("사진보기") { showPicturesDialog() }
                spacer()
                textButton("재시작") { restartDialog() }
                spacer()
            }
            hbox(spacing = spacing, alignment = Pos.CENTER) {
                spacer()
                textButton("EMI 테스트") { emiTestDialog() }
                spacer()
                textButton("보안키 갱신") { showKeyRenewDialog() }
                spacer()
                textButton("보안 체크") { checkIntegrityDialog() }
                spacer()
            }
            hbox(spacing = spacing, alignment = Pos.CENTER) { // Yade0923
                spacer()
                textButton("SW업데이트 암호 변경") { updateSWPasswordChangeDialog() }
                spacer()
                textButton("추가예정1") { showKeyRenewDialog() }
                spacer()
                textButton("추가예정2") { checkIntegrityDialog() }
                spacer()
            }
        }
    }) {
        controller.showTerminalInfo = false
    }
}
