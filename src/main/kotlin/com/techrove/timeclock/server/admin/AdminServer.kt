@file:Suppress("NonAsciiCharacters", "FunctionName")

package com.techrove.timeclock.server.admin

import com.techrove.timeclock.Settings
import com.techrove.timeclock.controller.model.Version
import com.techrove.timeclock.isLinux
import com.techrove.timeclock.server.admin.converter.AdminEncryptionConverterFactory
import com.techrove.timeclock.server.admin.model.req.AdminTerminalStatus
import mu.KotlinLogging
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.io.File
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLSession

private val logger = KotlinLogging.logger("ADMIN")

object AdminServer {

    private val service: AdminService by lazy {

        val logging = HttpLoggingInterceptor().apply {
//            setLevel(HttpLoggingInterceptor.Level.HEADERS)
//            setLevel(HttpLoggingInterceptor.Level.BASIC)
//            setLevel(HttpLoggingInterceptor.Level.BODY)
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .hostnameVerifier(NullHostNameVerifier())
            .build()

        Retrofit.Builder()
            .baseUrl(Settings.adminHost)
            .client(client)
            .addConverterFactory(AdminEncryptionConverterFactory())
            .build()
            .run {
                create(AdminService::class.java)
            }
    }

    private class NullHostNameVerifier : HostnameVerifier {
        override fun verify(hostname: String, session: SSLSession?): Boolean {
            return true
        }
    }

    suspend fun 단말기정보(deviceStatus: String? = null): Triple<String?, Version, Int> {
        return try {
            val temperature = if (isLinux) {
                File("/sys/class/thermal/thermal_zone1/temp").readLines().let {
                    it[0].toDouble() / 1000
                }
            } else {
                40.0
            }.toInt()
            val memTotal = Runtime.getRuntime().totalMemory() / 1024 / 1024
            val memFree = Runtime.getRuntime().freeMemory() / 1024 / 1024
            val diskTotal = File("/").totalSpace / 1024 / 1024
            val diskFree = File("/").freeSpace / 1024 / 1024

            service.단말기상태(
                AdminTerminalStatus(
                    eCode = deviceStatus,
                    statTemp = temperature,
                    memUsage = memTotal - memFree,
                    memTotal = memTotal,
                    diskUsage = diskTotal - diskFree,
                    diskTotal = diskTotal
                )
            ).let { response ->
                logger.debug { response }
                Triple(response.otaUri, Version.parse(response.version), response.port)
            }
        } catch (e: Exception) {
            Triple(null, Version(0, 0, 0), 0)
        }
    }
}
