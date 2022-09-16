package com.techrove.timeclock.utils

import com.techrove.timeclock.extensions.fullString
import com.techrove.timeclock.extensions.toLocalDateTime
import java.time.LocalDateTime
import java.util.prefs.Preferences
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

open class Preference {
    protected val userPref by lazy { Preferences.userNodeForPackage(Preference::class.java) }

    inline fun <reified T: Any> preference(preferences: Preferences, key: String, defaultValue: T)
            = PreferenceDelegate(preferences, key, defaultValue, T::class)

    fun clear() {
        userPref.clear()
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