package com.techrove.timeclock.utils

import com.techrove.timeclock.extensions.fullString
import com.techrove.timeclock.extensions.toLocalDateTime
import java.time.LocalDateTime
import java.util.prefs.Preferences
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import mu.KotlinLogging                                         // Yade1013
import org.jetbrains.exposed.sql.exists

private val logger = KotlinLogging.logger("Preference")     // Yade1013

open class Preference {
    protected val userPref by lazy { Preferences.userNodeForPackage(Preference::class.java) }

    inline fun <reified T: Any> preference(preferences: Preferences, key: String, defaultValue: T)
            = PreferenceDelegate(preferences, key, defaultValue, T::class)

    fun clear() {
// Yade1013
//        userPref.clear()
        userPref.put("PASSWORD", "")
        userPref.put("PASSWORD_RENEWED", "")
//        userPref.put("key_renewed", "")
        // sw업데이트&sFTP는 나중에 처리
//        userPref.put("swUpdatePassword", "")
//        userPref.put("swUpdatePassword_renewed", "")
//        userPref.put("sFTPPassword", "")
//        userPref.put("sFTPPassword_renewed", "")
//        try {
//            var process = Runtime.getRuntime()
//                .exec("sudo sed -i '/password\\\"/d' /root/.java/.userPrefs/com/techrove/timeclock/utils/prefs.xml")
//            if (process != null)
//                logger.info("[Prefs] password 삭제 성공")
//            else
//                logger.info("[Prefs] password 삭제 실패")
//
//            process = Runtime.getRuntime()
//                .exec("sudo sed -i '/password_renewed\\\"/d' /root/.java/.userPrefs/com/techrove/timeclock/utils/prefs.xml")
//            if (process != null)
//                logger.info("[Prefs] password_renewed 삭제 성공")
//            else
//                logger.info("[Prefs] password_renewed 삭제 실패")
//            process = Runtime.getRuntime()
//                .exec("sudo sed -i '/key_renewed\\\"/d' /root/.java/.userPrefs/com/techrove/timeclock/utils/prefs.xml")
//            if (process != null)
//                logger.info("[Prefs] key_renewed 성공")
//            else
//                logger.info("[Prefs] key_renewded 삭제 실패")
//            process = Runtime.getRuntime()
//                .exec("sudo sed -i '/swUpdatePassword\\\"/d' /root/.java/.userPrefs/com/techrove/timeclock/utils/prefs.xml")
//            if (process != null)
//                logger.info("[Prefs] swUpdatePassword 성공")
//            else
//                logger.info("[Prefs] swUpdatePassword 삭제 실패")
//            process = Runtime.getRuntime()
//                .exec("sudo sed -i '/swUpdatePassword_renewed\\\"/d' /root/.java/.userPrefs/com/techrove/timeclock/utils/prefs.xml")
//            if (process != null)
//                logger.info("[Prefs] swUpdatePassword_renewed 성공")
//            else
//                logger.info("[Prefs] swUpdatePassword_renewed 삭제 실패")
//            process = Runtime.getRuntime()
//                .exec("sudo sed -i '/sFTPPPassword\\\"/d' /root/.java/.userPrefs/com/techrove/timeclock/utils/prefs.xml")
//            if (process != null)
//                logger.info("[Prefs] sFTPPPassword 성공")
//            else
//                logger.info("[Prefs] sFTPPPassword 삭제 실패")
//            process = Runtime.getRuntime()
//                .exec("sudo sed -i '/sFTPPassword_renewed\\\"/d' /root/.java/.userPrefs/com/techrove/timeclock/utils/prefs.xml")
//            if (process != null)
//                logger.info("[Prefs] sFTPPassword_renewed 성공")
//            else
//                logger.info("[Prefs] sFTPPassword_renewed 삭제 실패")
//        } finally {
//            logger.info("설정 파일 초기화 실패 finally")
//        }
    }

    class PreferenceDelegate<T: Any>(
        private val preferences: Preferences,
        private val key: String,
        private val defaultValue: T,
        private val type: KClass<T>
    ): ReadWriteProperty<Any, T> {

        @Suppress("UNCHECKED_CAST")
        override fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
            with (preferences) {
                when (type) {
                    Int::class -> putInt(key, value as Int)
                    Long::class -> putLong(key, value as Long)
                    Float::class -> putFloat(key, value as Float)
                    Boolean::class -> putBoolean(key, value as Boolean)
                    String::class -> put(key, value as String)
                    ByteArray::class -> putByteArray(key, value as ByteArray)
                    LocalDateTime::class -> put(key, (value as LocalDateTime).fullString)
                    else -> error("Unsupported preference type $type.")
                }
            }
        }

        @Suppress("UNCHECKED_CAST")
        override fun getValue(thisRef: Any, property: KProperty<*>): T {
            return with (preferences) {
                when (type) {
                    Int::class -> getInt(key, defaultValue as Int)
                    Long::class -> getLong(key, defaultValue as Long)
                    Float::class -> getFloat(key, defaultValue as Float)
                    Boolean::class -> getBoolean(key, defaultValue as Boolean)
                    String::class -> get(key, defaultValue as String)
                    ByteArray::class -> getByteArray(key, defaultValue as ByteArray)
                    LocalDateTime::class -> get(key, "").toLocalDateTime ?: LocalDateTime.now()
                    else -> error("Unsupported preference type $type.")
                }
            } as T
        }

    }
}