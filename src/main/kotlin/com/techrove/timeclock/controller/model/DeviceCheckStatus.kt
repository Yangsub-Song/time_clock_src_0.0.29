package com.techrove.timeclock.controller.model

/**
 * 기기 상태 확인 상태
 */
enum class DeviceCheckStatus {
    DeviceChecking,
    DeviceOk,
    DeviceError;

    override fun toString(): String = when (this) {
        DeviceChecking -> "확인 중..."
        DeviceOk -> "정상"
        DeviceError -> "오류"
    }
}