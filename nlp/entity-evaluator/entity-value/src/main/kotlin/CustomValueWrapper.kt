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
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.core.TreeNode
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import java.util.logging.Logger
import kotlin.reflect.KClass

/**
 * A jackson wrapper to store class name with dynamic type value.
 * Usually, it is better to extend [Value] directly.
 */
@JsonDeserialize(using = CustomValueDeserializer::class)
data class CustomValueWrapper(val klass: String, val value: Any?) : Value {

    internal class CustomValueDeserializer : JsonDeserializer<CustomValueWrapper>() {

        override fun deserialize(jp: JsonParser, context: DeserializationContext): CustomValueWrapper? {
            var fieldName = jp.fieldNameWithValueReady()
            if (fieldName != null) {
                val classValue: Class<*>? =
                    try {
                        Class.forName(jp.text)
                    } catch (e: Exception) {
                        val className = CustomValueWrapper::class.qualifiedName
                        Logger.getLogger(className).throwing(className, className, e)
                        null
                    }
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
            val firstToken = nextToken()
            if (firstToken == JsonToken.END_OBJECT) {
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

    constructor(klass: KClass<*>, value: Any?) : this(klass.java.name, value)

    constructor(value: Any) : this(value::class, value)
}
