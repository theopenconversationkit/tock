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

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import tools.jackson.databind.ValueDeserializer
import tools.jackson.core.JsonParser as JsonParser3
import tools.jackson.core.JsonToken as JsonToken3
import tools.jackson.databind.DeserializationContext as DeserializationContext3
import tools.jackson.databind.annotation.JsonDeserialize as JsonDeserialize3

internal object UnknownValueDeserializer : JsonDeserializer<UnknownValue>() {
    override fun deserialize(
        p: JsonParser,
        ctxt: DeserializationContext,
    ): UnknownValue {
        while (p.currentToken == JsonToken.FIELD_NAME) {
            p.nextToken()
            if (p.currentToken?.isStructStart == true) {
                p.skipChildren()
            }
            p.nextToken()
        }
        return UnknownValue()
    }
}

internal class UnknownValueDeserializer3 : ValueDeserializer<UnknownValue>() {
    override fun deserialize(
        p: JsonParser3,
        ctxt: DeserializationContext3,
    ): UnknownValue {
        while (p.currentToken() == JsonToken3.PROPERTY_NAME) {
            p.nextToken()
            if (p.currentToken()?.isStructStart == true) {
                p.skipChildren()
            }
            p.nextToken()
        }
        return UnknownValue()
    }
}

/**
 * Used when real value type is unavailable.
 */
@JsonDeserialize(using = UnknownValueDeserializer::class)
@JsonDeserialize3(using = UnknownValueDeserializer3::class)
class UnknownValue : Value
