package com.techrove.timeclock.server.admin.converter

import com.google.gson.*
import com.techrove.timeclock.Settings
import com.techrove.timeclock.server.cwma.converter.Encrypt
import mu.KotlinLogging
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.io.UnsupportedEncodingException
import java.lang.reflect.Type
import java.nio.charset.StandardCharsets
import java.security.GeneralSecurityException
import java.security.Key
import java.security.NoSuchAlgorithmException
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec


private val logger = KotlinLogging.logger("ADMIN")

private class AES256Util {
    private val iv: String = key.substring(0, 16)
    private val keySpec: Key
    private val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")

    /**
     * 16자리의 키값을 입력하여 객체를 생성한다.
     */
    init {
        val keyBytes = ByteArray(16)
        val b = key.toByteArray(StandardCharsets.UTF_8)
        var len = b.size
        if (len > keyBytes.size) {
            len = keyBytes.size
        }
        System.arraycopy(b, 0, keyBytes, 0, len)
        keySpec = SecretKeySpec(keyBytes, "AES")
    }

    /**
     * AES256 으로 암호화 한다.
     */
    @Throws(
        NoSuchAlgorithmException::class,
        GeneralSecurityException::class,
        UnsupportedEncodingException::class
    )
    fun encrypt(str: String): String {
        logger.trace("ENC key:${key} iv:${iv}")
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, IvParameterSpec(iv.toByteArray()))
        val encrypted: ByteArray = cipher.doFinal(str.toByteArray(StandardCharsets.UTF_8))
        return String(Base64.getEncoder().encode(encrypted))
    }

    /**
     * AES256으로 암호화된 txt를 복호화한다.
     */
    @Throws(
        NoSuchAlgorithmException::class,
        GeneralSecurityException::class,
        UnsupportedEncodingException::class
    )
    fun decrypt(str: String): String {
        logger.trace("DEC key:${key} iv:${iv}")
        cipher.init(Cipher.DECRYPT_MODE, keySpec, IvParameterSpec(iv.toByteArray()))
        val byteStr: ByteArray = Base64.getDecoder().decode(str.toByteArray())
        return String(cipher.doFinal(byteStr), StandardCharsets.UTF_8)
    }

    companion object {
//        const val key = Settings.ADMIN_KEY
        var key = Settings.ADMIN_KEY    // Yade0927
    }
}

class AdminEncryptionConverterFactory : Converter.Factory() {
    private val cipher = AES256Util()
    private val gson = GsonBuilder()
        .create()

    override fun stringConverter(
        type: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit
    ): Converter<Any, String>? {
        annotations.find { it.annotationClass == Encrypt::class }?.let {
            return Converter { obj ->
                gson.toJson(obj, type).let { json ->
                    logger.trace("=> $json")
                    cipher.encrypt(json).also { enc ->
                        logger.trace("=> ENC:$enc")
                    }
                }
            }
        } ?: run {
            return Converter { obj ->
                gson.toJson(obj, type).also { json ->
                    logger.trace("=> $json")
                }
            }
        }
    }

    override fun responseBodyConverter(
        type: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit
    ): Converter<ResponseBody, Any> {
        return Converter { body ->
            gson.fromJson(
                body.string().trim().let { res ->
                    logger.trace { "<= $res" }
                    try {
                        cipher.decrypt(res).also {
                            logger.trace { "<= DEC:$it" }
                        }
                    } catch (e: Exception) {
                        res
                    }
                },
                type
            )
        }
    }
}
