package com.techrove.timeclock.security

import com.techrove.timeclock.utils.decodedToByteArray
import com.techrove.timeclock.utils.encodedToString
import mu.KotlinLogging
import java.io.File

private val logger = KotlinLogging.logger("Cipher")

fun String.encrypt(key: ByteArray, tag: String = ""): String {
    logger.trace { "ENC [$tag]" }
    return Cipher.encrypt(key, this.toByteArray()).encodedToString
}

fun String.decrypt(key: ByteArray, tag: String = ""): String {
    logger.trace { "DEC [$tag]" }
    return Cipher.decrypt(key, this.decodedToByteArray)?.let { String(it) } ?: ""
}

fun ByteArray.encrypt(key: ByteArray): ByteArray =
    Cipher.encrypt(key, this)

fun ByteArray.decrypt(key: ByteArray): ByteArray =
    Cipher.decrypt(key, this) ?: byteArrayOf()

fun File.encrypt(key: ByteArray, file: File, tag: String = "") {
    try {
        logger.trace { "ENC [$tag]" }
        Cipher.encrypt(key, readBytes()).let {
            file.writeBytes(it)
        }
    } catch (e: Exception) {
        logger.error { e }
    }
}

fun File.decrypt(key: ByteArray, tag: String = ""): ByteArray? {
    try {
        logger.trace { "DEC [$tag]" }
        Cipher.decrypt(key, readBytes())?.let {
            return it
        }
    } catch (e: Exception) {
        logger.error { e }
    }
    return null
}
