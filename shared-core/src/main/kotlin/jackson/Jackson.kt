/*
 * Copyright (C) 2017/2025 SNCF Connect & Tech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ai.tock.shared.jackson

import ai.tock.shared.error
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.Module
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import mu.KotlinLogging
import org.litote.jackson.JacksonModuleServiceLoader
import tools.jackson.databind.ValueSerializer
import java.util.ServiceConfigurationError
import java.util.ServiceLoader
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance

private val logger = KotlinLogging.logger {}

/**
 * Load dynamically a Jackson 2 module for the Tock [mapper].
 *
 * The Mongo mapping uses Jackson 3: its modules are loaded with [org.litote.jackson.JacksonModuleServiceLoader].
 */
fun interface Jackson2ModuleServiceLoader {
    /**
     * The module to load.
     */
    fun module(): Module
}

internal val jacksonAdditionalModules: List<Module> by lazy {
    try {
        ServiceLoader
            .load(Jackson2ModuleServiceLoader::class.java)
            .map { it.module() }
            .also { logger.debug { "additional modules: $it" } }
    } catch (e: Exception) {
        logger.error(e)
        emptyList<Module>()
    }
}

/**
 * The Tock jackson mapper.
 */
val mapper: ObjectMapper by lazy {
    logger.info { "init jackson mapper" }
    jacksonObjectMapper()
        .findAndRegisterModules()
        // force java time module
        .registerModule(JavaTimeModule())
        .registerModule(Jackson2IdModule())
        .registerModules(jacksonAdditionalModules)
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .setSerializationInclusion(JsonInclude.Include.NON_NULL)
        .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
        .configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true)
        .configure(MapperFeature.PROPAGATE_TRANSIENT_MARKER, true)
        .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
        .configure(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS, false)
        .apply {
            // the generated serializers are Jackson 3 ones: keep their format, which contains the null properties
            generatedSerializerTypes.forEach { addMixIn(it, IncludeNullProperties::class.java) }
        }
}

@JsonInclude(JsonInclude.Include.ALWAYS)
private interface IncludeNullProperties

/**
 * The classes with a serializer generated from [org.litote.jackson.data.JacksonData].
 */
private val generatedSerializerTypes: List<Class<*>>
    get() =
        try {
            ServiceLoader
                .load(JacksonModuleServiceLoader::class.java)
                .filterIsInstance<ValueSerializer<*>>()
                .mapNotNull { it.handledType() }
        } catch (e: ServiceConfigurationError) {
            logger.error(e)
            emptyList()
        }

/**
 * Read value from a [JsonParser].
 */
inline fun <reified T : Any> JsonParser.readValue() = this.readValueAs(T::class.java)

/**
 * Return the current field name, with the value ready to read.
 *
 * @return the field name, null if [JsonToken.END_OBJECT]
 */
fun JsonParser.fieldNameWithValueReady(): String? {
    if (currentToken?.isStructEnd == true) {
        return null
    }
    if (currentToken != JsonToken.FIELD_NAME) {
        nextToken()
        if (currentToken?.isStructEnd == true) {
            return null
        }
    }
    val fieldName = currentName
    nextToken()
    return fieldName
}

internal fun JsonParser.checkEndToken() {
    if (currentToken?.isStructEnd != true) {
        nextToken()
        checkEndToken()
    }
}

/**
 * Read fields from a [JsonParser].
 */
inline fun <reified FIELDS : Any> JsonParser.read(readValue: (FIELDS, String) -> Unit): FIELDS =
    FIELDS::class.createInstance().let { fields ->
        while (true) {
            fieldNameWithValueReady()?.apply {
                readValue.invoke(fields, this)
            } ?: break
        }
        fields
    }

/**
 * Read a list of values from a [JsonParser].
 */
inline fun <reified T : Any> JsonParser.readListValues(): List<T> = readValueAs<List<T>>(object : TypeReference<List<T>>() {}) ?: emptyList()

/**
 * Add a deserializer in the [SimpleModule].
 */
fun <T : Any> SimpleModule.addDeserializer(
    type: KClass<T>,
    deser: JsonDeserializer<out T>,
) = addDeserializer(type.java, deser)

/**
 * Add a serializer in the [SimpleModule].
 */
fun <T : Any> SimpleModule.addSerializer(
    type: KClass<T>,
    ser: JsonSerializer<in T>,
) = addSerializer(type.java, ser)
