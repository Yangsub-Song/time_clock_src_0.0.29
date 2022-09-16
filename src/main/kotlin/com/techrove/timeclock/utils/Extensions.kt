package com.techrove.timeclock.utils

import com.techrove.timeclock.io.Audio
import com.techrove.timeclock.server.cwma.model.res.Response
import java.io.File
import java.nio.ByteBuffer
import java.util.*

fun ByteArray.printHex(header: String) {
    print("${header}: ")
    forEach {
        print(String.format("%02x ", it))
    }
    println()
}

val ByteArray.encodedToString: String get() = Base64.getEncoder().encodeToString(this)

val String.decodedToByteArray: ByteArray get() = Base64.getDecoder().decode(this)

val ByteArray?.toString: String get() = this?.let { String(it) } ?: ""

fun ByteArray.toHexString() = joinToString("") { "%02x".format(it) }

fun ByteBuffer.getString(len: Int) =
    ByteArray(len).let { ba ->
        get(ba, 0, len)
        String(ba)
    }


val File.extension: String
    get() = name.substringAfterLast('.', "")

fun Response.playAudio(ok: String) {
    if (result.isOk) {
        Audio.play(ok)
    } else {
        when (result.code) {
            "1243" -> "주민등록번호를 확인해 주세요.wav"
            "1244" -> "지문 정보 오류가 발생했습니다.wav"
            "1245" -> "카드 번호 오류가 발생했습니다.wav"
            "1246" -> "카드 번호 암호화 여부 오류가 발생했습니다.wav"
            "1300" -> "데이터 처리오류.wav"
            "1310" -> "근로자 등록 오류가 발생했습니다.wav"
            "1320" -> "지문 등록 오류가 발생했습니다.wav"
            "1330" -> "지문 갱신 오류가 발생했습니다.wav"
            "1500" -> "정보가 없습니다.wav"
            "1510" -> "근로자 정보가 없습니다.wav"
            "1520" -> "지문 정보가 없습니다.wav"
            "1530" -> "일치하는 지문이 없습니다.wav"
            "1540" -> "지문 유효기간 오류가 발생했습니다.wav"
            "1550" -> "카드 정보가 없습니다.wav"
            "1560" -> "단말 정보가 없습니다.wav"
            "1570" -> "지문등록을 실패 하였습니다.wav"
            "1580" -> "지문등록을 실패 하였습니다.wav"
            "1910" -> "이미 출근 처리되어있습니다.wav"
            "1920" -> "출근 없이 퇴근 처리할 수 없습니다.wav"
            "1930" -> "출근 후 10분 이내 퇴근할 수 없습니다.wav"
            "2001" -> "예상치 못한 오류가 발생했습니다.wav"
            else -> "알 수 없는 오류가 발생했습니다.wav"
        }?.let {
            Audio.play(it)
        }
    }
}
