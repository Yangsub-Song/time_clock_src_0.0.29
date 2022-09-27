package com.techrove.timeclock.server.cwma

import com.techrove.timeclock.Settings
import com.techrove.timeclock.security.KeyHelper
import com.techrove.timeclock.security.encrypt
import com.techrove.timeclock.server.cwma.converter.CwmaEncryptionConverterFactory
import mu.KotlinLogging
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLSession

private val logger = KotlinLogging.logger("CWMA")

object CwmaServer {

    var serverUrl = Settings.cwmaHost
    var terminalId = Settings.terminalId
    var placeCd = Settings.placeCd

    var key: String = ""
        get() = encryptionConverterFactory.getKey()
        set(value) {
            field = value.encrypt(KeyHelper.masterKey, "master")
            encryptionConverterFactory.setKey(field)
        }
    var version: String? = null

    private val encryptionConverterFactory = CwmaEncryptionConverterFactory()

    init {
        try {
//            key = Settings.DEFAULT_KEY // Yade0927
            key = "0xFF" // Yade0928
        } catch (_: Exception) {
	        // 초기 SW 유효성 오류시 encryption key null exception 처리
            // NOTE: unhandled exception 은 DefaultErrorHandler 에서 처리하나
            // network key 는 init 에서 초기화 하므로 error handler 에서 처리 불가
        }
    }

    val service: CwmaService by lazy {
        val logging = HttpLoggingInterceptor().apply {
            //setLevel(HttpLoggingInterceptor.Level.HEADERS)
            //setLevel(HttpLoggingInterceptor.Level.BASIC)
            //setLevel(HttpLoggingInterceptor.Level.BODY)
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(HeaderInterceptor())
            .addInterceptor(logging)
            .hostnameVerifier(NullHostNameVerifier())
            .build()

        Retrofit.Builder()
            .baseUrl(serverUrl)
            .client(client)
            //.addConverterFactory(EnumConverterFactory())
            .addConverterFactory(encryptionConverterFactory)
            .build()
            .run {
                create(CwmaService::class.java)
            }
    }

    private class HeaderInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
            val origin = chain.request()
            val request = origin.newBuilder().apply {
                version?.let {
                    addHeader("kmsVer", it)
                }
                addHeader("kmsId", encryptionConverterFactory.encrypt(placeCd + terminalId))
            }
            logger.debug { "   " + origin.url }
            return chain.proceed(request.build())
        }
    }

    private class NullHostNameVerifier : HostnameVerifier {
        override fun verify(hostname: String, session: SSLSession?): Boolean {
            return true
        }
    }
}

