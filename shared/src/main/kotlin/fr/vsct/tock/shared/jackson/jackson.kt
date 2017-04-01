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
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlin.reflect.KClass

val mapper: ObjectMapper by lazy {
    val mapper = jacksonObjectMapper()
    mapper.findAndRegisterModules()
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)
    mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
    mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true)
    mapper
}

fun <T : Any> ObjectMapper.readValue(content: String, valueType: KClass<T>): T = readValue(content, valueType.java)

fun <T : Any> JsonParser.readValueAs(klass: KClass<T>) = this.readValueAs(klass.java)

inline fun <reified T : Any> JsonParser.readListValuesAs(): List<T> {
    return readValueAs<List<T>>(object : TypeReference<List<T>>() {}) ?: emptyList()
}

fun <T : Any> SimpleModule.addDeserializer(type: KClass<T>, deser: JsonDeserializer<out T>) = addDeserializer(type.java, deser)

fun <T : Any> SimpleModule.addSerializer(type: KClass<T>, ser: JsonSerializer<in T>) = addSerializer(type.java, ser)