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

import ai.tock.shared.jackson.ConstrainedValueWrapper.ConstrainedValueDeserializer
import ai.tock.shared.jackson.ConstrainedValueWrapper.ConstrainedValueSerializer
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import mu.KotlinLogging
import java.util.concurrent.CopyOnWriteArraySet
import kotlin.reflect.KClass

private val constrainedTypes = CopyOnWriteArraySet<String>()

/**
 * Add other constrained types.
 */
fun addConstrainedTypes(types: Set<KClass<*>>) {
    constrainedTypes.addAll(types.map { it.java.name })
}

/**
 * A jackson wrapper to store class name with dynamic type value.
 * Only allowed types can be deserialized.
 */
@JsonDeserialize(using = ConstrainedValueDeserializer::class)
@JsonSerialize(using = ConstrainedValueSerializer::class)
data class ConstrainedValueWrapper<T : Any>(val klass: String, val value: T?) {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    internal class ConstrainedValueSerializer : JsonSerializer<ConstrainedValueWrapper<*>>() {
        override fun serialize(value: ConstrainedValueWrapper<*>, gen: JsonGenerator, serializers: SerializerProvider) {
            gen.writeStartObject()
            gen.writeFieldName(ConstrainedValueWrapper<*>::klass.name)
            gen.writeString(value.klass)
            serializers.defaultSerializeField(ConstrainedValueWrapper<*>::value.name, value.value, gen)
            gen.writeEndObject()
        }
    }

    internal class ConstrainedValueDeserializer : JsonDeserializer<ConstrainedValueWrapper<*>>() {

        override fun deserialize(jp: JsonParser, context: DeserializationContext): ConstrainedValueWrapper<*>? {
            var fieldName = jp.fieldNameWithValueReady()
            if (fieldName != null) {
                val classValue: Class<*>? =
                    try {
                        // TODO remove replace in 20.3
                        val replace = jp.text.replace("fr.vsct.tock", "ai.tock")
                        if (constrainedTypes.contains(replace)) {
                            Class.forName(replace)
                        } else {
                            throw AssertionError("unregister class: $replace")
                        }
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
                        return ConstrainedValueWrapper(classValue.name, value)
                    }
                } else {
                    jp.checkEndToken()
                    return if (classValue == null) null else ConstrainedValueWrapper(classValue.name, null)
                }
            }
            return null
        }
    }

    constructor(klass: KClass<*>, value: T?) : this(klass.java.name, value)

    constructor(value: T) : this(value::class, value)
}
