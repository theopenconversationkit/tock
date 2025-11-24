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
import ai.tock.shared.jackson.AnyValueWrapper.AnyValueSerializer
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import mu.KotlinLogging
import kotlin.reflect.KClass

/**
 * A jackson wrapper to store class name with dynamic type value.
 * Use with care, as it stores the class name in json.
 */
@JsonDeserialize(using = AnyValueDeserializer::class)
@JsonSerialize(using = AnyValueSerializer::class)
data class AnyValueWrapper(val klass: String, val value: Any?) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    internal class AnyValueSerializer : JsonSerializer<AnyValueWrapper>() {
        override fun serialize(
            value: AnyValueWrapper,
            gen: JsonGenerator,
            serializers: SerializerProvider,
        ) {
            gen.writeStartObject()
            gen.writeFieldName(AnyValueWrapper::klass.name)
            gen.writeString(value.klass)
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
                val classValue: Class<*>? =
                    try {
                        // TODO remove replace in 20.3
                        Class.forName(jp.text.replace("fr.vsct.tock", "ai.tock"))
                    } catch (e: Exception) {
                        logger.warn("deserialization error for class ${e.message}")
                        null
                    }
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
                        return AnyValueWrapper(classValue.name, value)
                    }
                } else {
                    jp.checkEndToken()
                    return if (classValue == null) null else AnyValueWrapper(classValue.name, null)
                }
            }
            return null
        }
    }

    constructor(klass: KClass<*>, value: Any?) : this(klass.java.name, value)

    constructor(value: Any) : this(value::class, value)
}
