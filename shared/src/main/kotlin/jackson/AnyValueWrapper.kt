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

import ai.tock.shared.jackson.AnyValueWrapper.AnyValueDeserializer
import ai.tock.shared.jackson.AnyValueWrapper.AnyValueDeserializer3
import ai.tock.shared.jackson.AnyValueWrapper.AnyValueSerializer
import ai.tock.shared.jackson.AnyValueWrapper.AnyValueSerializer3
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import mu.KotlinLogging
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.ValueDeserializer
import tools.jackson.databind.ValueSerializer
import kotlin.reflect.KClass
import tools.jackson.core.JsonGenerator as JsonGenerator3
import tools.jackson.core.JsonParser as JsonParser3
import tools.jackson.databind.DeserializationContext as DeserializationContext3
import tools.jackson.databind.annotation.JsonDeserialize as JsonDeserialize3
import tools.jackson.databind.annotation.JsonSerialize as JsonSerialize3

/**
 * A jackson wrapper to store class name with dynamic type value.
 * Use with care, as it stores the class name in json.
 */
@JsonDeserialize(using = AnyValueDeserializer::class)
@JsonSerialize(using = AnyValueSerializer::class)
@JsonDeserialize3(using = AnyValueDeserializer3::class)
@JsonSerialize3(using = AnyValueSerializer3::class)
data class AnyValueWrapper(
    val klass: KClass<*>,
    val value: Any?,
) {
    companion object {
        private val logger = KotlinLogging.logger {}

        private fun loadClass(className: String): Class<*>? =
            try {
                Class.forName(className)
            } catch (e: Exception) {
                logger.warn("deserialization error for class ${e.message}")
                null
            }
    }

    internal class AnyValueSerializer : JsonSerializer<AnyValueWrapper>() {
        override fun serialize(
            value: AnyValueWrapper,
            gen: JsonGenerator,
            serializers: SerializerProvider,
        ) {
            gen.writeStartObject()
            gen.writeFieldName(AnyValueWrapper::klass.name)
            gen.writeString(value.klass.java.name)
            serializers.defaultSerializeField(AnyValueWrapper::value.name, value.value, gen)
            gen.writeEndObject()
        }
    }

    internal class AnyValueDeserializer : JsonDeserializer<AnyValueWrapper>() {
        override fun deserialize(
            jp: JsonParser,
            context: DeserializationContext,
        ): AnyValueWrapper? {
            var fieldName = jp.fieldNameWithValueReady()
            if (fieldName != null) {
                val classValue: Class<*>? = loadClass(jp.text)
                fieldName = jp.fieldNameWithValueReady()
                if (fieldName != null) {
                    if (classValue == null) {
                        if (jp.currentToken.isStructStart) {
                            jp.skipChildren()
                        }
                        jp.nextToken()
                        jp.checkEndToken()
                        return null
                    } else {
                        val value = jp.readValueAs(classValue)
                        jp.checkEndToken()
                        return AnyValueWrapper(classValue.kotlin, value)
                    }
                } else {
                    jp.checkEndToken()
                    return if (classValue == null) null else AnyValueWrapper(classValue.kotlin, null)
                }
            }
            return null
        }
    }

    /**
     * The Mongo mapping uses Jackson 3.
     */
    internal class AnyValueSerializer3 : ValueSerializer<AnyValueWrapper>() {
        override fun serialize(
            value: AnyValueWrapper,
            gen: JsonGenerator3,
            serializers: SerializationContext,
        ) {
            gen.writeStartObject()
            gen.writeName(AnyValueWrapper::klass.name)
            gen.writeString(value.klass.java.name)
            serializers.defaultSerializeProperty(AnyValueWrapper::value.name, value.value, gen)
            gen.writeEndObject()
        }
    }

    /**
     * The Mongo mapping uses Jackson 3.
     */
    internal class AnyValueDeserializer3 : ValueDeserializer<AnyValueWrapper>() {
        override fun deserialize(
            jp: JsonParser3,
            context: DeserializationContext3,
        ): AnyValueWrapper? = jp.readWrappedValue(::loadClass) { klass, value -> AnyValueWrapper(klass.kotlin, value) }
    }

    constructor(value: Any) : this(value::class, value)
}
