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

package ai.tock.nlp.entity

import ai.tock.nlp.entity.CustomValueWrapper.CustomValueDeserializer
import ai.tock.nlp.entity.CustomValueWrapper.CustomValueDeserializer3
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.core.TreeNode
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import tools.jackson.databind.ValueDeserializer
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.reflect.KClass
import tools.jackson.core.JsonParser as JsonParser3
import tools.jackson.core.JsonToken as JsonToken3
import tools.jackson.core.TreeNode as TreeNode3
import tools.jackson.databind.DeserializationContext as DeserializationContext3
import tools.jackson.databind.annotation.JsonDeserialize as JsonDeserialize3

private val logger = Logger.getLogger(CustomValueWrapper::class.qualifiedName)

private fun loadValueClass(className: String): Class<*>? =
    if (ValueResolverRepository.isCustomValueType(className)) {
        try {
            Class.forName(className)
        } catch (e: ClassNotFoundException) {
            logger.log(Level.WARNING, "custom value class not found", e)
            null
        }
    } else {
        logger.warning { "custom value ignored, call ValueResolverRepository#registerCustomValueType for $className" }
        null
    }

/**
 * A jackson wrapper to store class name with dynamic type value.
 * Usually, it is better to extend [Value] directly.
 *
 * Only the classes registered with [ValueResolverRepository.registerCustomValueType] are deserialized.
 */
@JsonDeserialize(using = CustomValueDeserializer::class)
@JsonDeserialize3(using = CustomValueDeserializer3::class)
data class CustomValueWrapper(
    val klass: String,
    val value: Any?,
) : Value {
    internal class CustomValueDeserializer : JsonDeserializer<CustomValueWrapper>() {
        override fun deserialize(
            jp: JsonParser,
            context: DeserializationContext,
        ): CustomValueWrapper? {
            var fieldName = jp.fieldNameWithValueReady()
            if (fieldName != null) {
                val classValue: Class<*>? = loadValueClass(jp.text)
                fieldName = jp.fieldNameWithValueReady()
                if (fieldName != null) {
                    if (classValue == null) {
                        jp.readValueAsTree<TreeNode>()
                        jp.checkEndToken()
                        return null
                    } else {
                        val value = jp.readValueAs(classValue)
                        jp.checkEndToken()
                        return CustomValueWrapper(classValue.name, value)
                    }
                } else {
                    jp.checkEndToken()
                    return if (classValue == null) null else CustomValueWrapper(classValue.name, null)
                }
            }
            return null
        }

        private fun JsonParser.fieldNameWithValueReady(): String? {
            if (currentToken == JsonToken.END_OBJECT) {
                return null
            }
            // the type deserializer gives the parser on the first field
            if (currentToken != JsonToken.FIELD_NAME && nextToken() == JsonToken.END_OBJECT) {
                return null
            }
            val fieldName = currentName
            nextToken()
            return fieldName
        }

        private fun JsonParser.checkEndToken() {
            if (currentToken != JsonToken.END_OBJECT) {
                nextToken()
                checkEndToken()
            }
        }
    }

    /**
     * The Mongo mapping uses Jackson 3.
     */
    internal class CustomValueDeserializer3 : ValueDeserializer<CustomValueWrapper>() {
        override fun deserialize(
            jp: JsonParser3,
            context: DeserializationContext3,
        ): CustomValueWrapper? {
            jp.fieldNameWithValueReady() ?: return null
            val classValue: Class<*>? = loadValueClass(jp.string)
            if (jp.fieldNameWithValueReady() == null) {
                jp.checkEndToken()
                return classValue?.let { CustomValueWrapper(it.name, null) }
            }
            // unlike Jackson 2, the parser stays on the last token of the value, which may be the end of a struct
            val struct = jp.currentToken().isStructStart
            val value = if (classValue == null) jp.readValueAsTree<TreeNode3>() else jp.readValueAs(classValue)
            if (struct) {
                jp.nextToken()
            }
            jp.checkEndToken()
            return classValue?.let { CustomValueWrapper(it.name, value) }
        }

        private fun JsonParser3.fieldNameWithValueReady(): String? {
            if (currentToken() == JsonToken3.END_OBJECT) {
                return null
            }
            // the type deserializer gives the parser on the first field
            if (currentToken() != JsonToken3.PROPERTY_NAME && nextToken() == JsonToken3.END_OBJECT) {
                return null
            }
            val fieldName = currentName()
            nextToken()
            return fieldName
        }

        private fun JsonParser3.checkEndToken() {
            while (currentToken() != JsonToken3.END_OBJECT) {
                nextToken()
            }
        }
    }

    constructor(klass: KClass<*>, value: Any?) : this(klass.java.name, value)

    constructor(value: Any) : this(value::class, value)
}
