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

import tools.jackson.core.JsonParser
import tools.jackson.core.JsonToken
import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.JsonNode
import tools.jackson.databind.ValueDeserializer

/**
 * Return the current field name, with the value ready to read.
 *
 * @return the field name, null if [JsonToken.END_OBJECT]
 */
internal fun JsonParser.fieldNameWithValueReady(): String? {
    if (currentToken()?.isStructEnd == true) {
        return null
    }
    if (currentToken() != JsonToken.PROPERTY_NAME) {
        nextToken()
        if (currentToken()?.isStructEnd == true) {
            return null
        }
    }
    val fieldName = currentName()
    nextToken()
    return fieldName
}

internal fun JsonParser.checkEndToken() {
    while (currentToken()?.isStructEnd != true) {
        nextToken()
    }
}

/**
 * Read an object with the class name of the value, then the value.
 *
 * @return null if the object is empty, or if the class can not be loaded
 */
internal inline fun <R> JsonParser.readWrappedValue(
    loadClass: (String) -> Class<*>?,
    wrap: (Class<*>, Any?) -> R,
): R? {
    fieldNameWithValueReady() ?: return null
    val klass = loadClass(string)
    if (fieldNameWithValueReady() == null) {
        checkEndToken()
        return klass?.let { wrap(it, null) }
    }
    if (klass == null) {
        if (currentToken().isStructStart) {
            skipChildren()
        }
        nextToken()
        checkEndToken()
        return null
    }
    // unlike Jackson 2, the parser stays on the last token of the value, which may be the end of a struct
    val struct = currentToken().isStructStart
    val value = readValueAs(klass)
    if (struct) {
        nextToken()
    }
    checkEndToken()
    return wrap(klass, value)
}

/**
 * Lets the Mongo mapping (Jackson 3) read a class that has a Jackson 2 deserializer, by delegating to the Tock [mapper].
 *
 * The value is converted to json: only for classes that do not contain BSON specific types (dates, binary...).
 */
abstract class Jackson2DelegateDeserializer<T : Any>(
    private val type: Class<T>,
) : ValueDeserializer<T>() {
    override fun deserialize(
        p: JsonParser,
        ctxt: DeserializationContext,
    ): T? = mapper.readValue(p.readValueAsTree<JsonNode>().toString(), type)
}
