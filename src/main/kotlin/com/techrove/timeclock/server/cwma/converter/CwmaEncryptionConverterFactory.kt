package com.techrove.timeclock.server.cwma.converter

import com.google.gson.GsonBuilder
import com.techrove.timeclock.security.KeyHelper
import com.techrove.timeclock.security.decrypt
import com.techrove.timeclock.security.digest
import com.techrove.timeclock.utils.toHexString
import ksign.jce.util.EncDecSample
import ksign.jce.util.JCEUtil
import mu.KotlinLogging
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import tornadofx.urlEncoded
import java.lang.reflect.Type
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime


private val logger = KotlinLogging.logger("CWMA")

//@Target(PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
internal annotation class Encrypt

class CwmaEncryptionConverterFactory : Converter.Factory() {
    init {
        JCEUtil.initProvider("./")
    }

    private val sample = EncDecSample()
    private var key = ""
    private var keySha: ByteArray = byteArrayOf()
    private val algorithm = "seed"

    private val String.urlDecoded
        get() = URLDecoder.decode(this, StandardCharsets.UTF_8)

    private val String.encrypted: String
        get() = sample.LocalSymmEncrypt(this, getKey().toByteArray().also {
            logger.trace { "ENC key:${it.toHexString()}, iv:?" }
        }, algorithm).urlEncoded

    private val String.decrypted: String
        get() =
            String(
                sample.LocalSymmDecrypt(
                    this.urlDecoded.removeSurrounding("\""),
                    getKey().toByteArray().also {
                        logger.trace { "DEC key:${it.toHexString()}, iv:?" }
                    },
                    algorithm
                )
            )
                .urlDecoded

    private val gson = GsonBuilder()
        .setDateFormat("yyyyMMddHHmmss")
        .registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeSerializer())
        .create()

    fun getKey(): String {
        return if (key.toByteArray().digest().contentEquals(keySha)) {
            key.decrypt(KeyHelper.masterKey, "master")
        } else {
            ""
        }
    }

    fun setKey(keyString: String) {
        key = keyString
        keySha = keyString.toByteArray().digest()
    }

    fun encrypt(s: String, encode: Boolean = true): String {
        return try {
            if (encode) {
                s.encrypted
            } else {
                sample.LocalSymmEncrypt(s, getKey().toByteArray().also {
                    logger.trace { "ENC key:${it.toHexString()}, iv:?" }
                }, algorithm)
            }
        } catch (e: Exception) {
            ""
        }
    }

    fun decrypt(s: String): String {
        return s.decrypted
    }

    override fun stringConverter(
        type: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit
    ): Converter<Any, String>? {
        annotations.find { it.annotationClass == Encrypt::class }?.let {
            return Converter { obj ->
                gson.toJson(obj, type).let {
                    logger.trace { "=> $it" }
                    try {
                        sample.LocalSymmEncrypt(it, getKey().toByteArray().also {
                            logger.trace { "ENC key:${it.toHexString()}, iv:?" }
                        }, algorithm)
                    } catch (e: Exception) {
                        return@Converter null
                    }
                }
            }
        } ?: run {
            return null //super.stringConverter(type, annotations, retrofit)
        }
    }

    override fun requestBodyConverter(
        type: Type,
        parameterAnnotations: Array<out Annotation>,
        methodAnnotations: Array<out Annotation>,
        retrofit: Retrofit
    ): Converter<Any, RequestBody> {
        return Converter { obj ->
            try {
                "data=${gson.toJson(obj).also { println("=> $it") }.encrypted}".also { println("=> $it") }
            } catch (e: Exception) {
                ""
            }.toRequestBody()
        }
    }

    override fun responseBodyConverter(
        type: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit
    ): Converter<ResponseBody, Any> {
        return Converter { body ->
            val resBody = body.string()
            logger.trace { "<= $resBody" }
            gson.fromJson(
                resBody.decrypted.also { logger.trace { "<= $it" } },
                type
            )
        }
    }
}
