package com.techrove.timeclock.server.cwma.converter

import com.google.gson.*
import java.lang.reflect.Type
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

internal class LocalDateTimeSerializer : JsonSerializer<LocalDateTime?>,
    JsonDeserializer<LocalDateTime> {
    private val formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")

    override fun serialize(
        localDateTime: LocalDateTime?,
        srcType: Type,
        context: JsonSerializationContext
    ): JsonElement {
        return JsonPrimitive(formatter.format(localDateTime))
    }

    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): LocalDateTime? = try {
        (json?.asString)?.let { LocalDateTime.parse(it, formatter) }
    } catch (e: Exception) {
        null
    }
}