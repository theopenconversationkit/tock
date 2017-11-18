/*
 * Copyright (C) 2017 VSCT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.vsct.tock.shared.jackson

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.KeyDeserializer
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.litote.kmongo.Id
import org.litote.kmongo.id.IdGenerator
import org.litote.kmongo.id.ObjectIdToStringGenerator
import org.litote.kmongo.id.jackson.IdJacksonModule
import org.litote.kmongo.id.jackson.IdToStringSerializer
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.valueParameters

//TODO remove this one when kmongo 3.6.0 is released
internal class StringToIdKeyDeserializer(private val idGenerator: IdGenerator? = null) : KeyDeserializer() {

    override fun deserializeKey(key: String, ctxt: DeserializationContext): Any
            = IdGenerator
            .defaultGenerator
            .idClass
            .constructors
            .firstOrNull { it.valueParameters.size == 1 && it.valueParameters.first().type.classifier == String::class }
            ?.call(key)
            ?: error("no constructor with a single string arg found for ${IdGenerator.defaultGenerator.idClass}")

}

internal val hackModule = SimpleModule().apply {
    addKeySerializer(Id::class.java, IdToStringSerializer())
    addKeyDeserializer(Id::class.java, StringToIdKeyDeserializer())
}

val mapper: ObjectMapper by lazy {
    jacksonObjectMapper()
            .findAndRegisterModules()
            //force java time module
            .registerModule(JavaTimeModule())
            //register id module
            .registerModule(hackModule)
            .registerModule(IdJacksonModule())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true)
            .configure(MapperFeature.PROPAGATE_TRANSIENT_MARKER, true)
}

inline fun <reified T : Any> JsonParser.readValue() = this.readValueAs(T::class.java)

/**
 * Returns the current field name, with the value ready to read.
 *
 * @return the field name, null if [JsonToken.END_OBJECT]
 */
fun JsonParser.fieldNameWithValueReady(): String? {
    if (currentToken == JsonToken.END_OBJECT) {
        return null
    }
    val firstToken = nextToken()
    if (firstToken == JsonToken.END_OBJECT) {
        return null
    }
    val fieldName = currentName
    nextToken()
    return fieldName
}

/**
 */
fun JsonParser.checkEndToken() {
    if (currentToken != JsonToken.END_OBJECT) {
        nextToken()
        checkEndToken()
    }
}

inline fun <reified FIELDS : Any> JsonParser.read(readValue: (FIELDS, String) -> Unit): FIELDS {
    return FIELDS::class.createInstance().let { fields ->
        while (true) {
            fieldNameWithValueReady()?.apply {
                readValue.invoke(fields, this)
            } ?: break
        }
        fields
    }
}

inline fun <reified T : Any> JsonParser.readListValues(): List<T> {
    return readValueAs<List<T>>(object : TypeReference<List<T>>() {}) ?: emptyList()
}

fun <T : Any> SimpleModule.addDeserializer(type: KClass<T>, deser: JsonDeserializer<out T>) = addDeserializer(type.java, deser)

fun <T : Any> SimpleModule.addSerializer(type: KClass<T>, ser: JsonSerializer<in T>) = addSerializer(type.java, ser)