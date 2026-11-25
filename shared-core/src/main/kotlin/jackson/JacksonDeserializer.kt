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

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken.START_ARRAY
import com.fasterxml.jackson.core.JsonToken.START_OBJECT
import com.fasterxml.jackson.databind.JsonDeserializer
import mu.KotlinLogging

/**
 * Convenient base class for jackson [JsonDeserializer].
 */
abstract class JacksonDeserializer<T> : JsonDeserializer<T>() {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    object EmptyJson

    protected fun JsonParser.readUnknownValue(): EmptyJson {
        val name = currentName
        skipChildren()
        when (currentToken()) {
            START_OBJECT, START_ARRAY -> skipChildren()
            else -> nextToken()
        }
        logger.warn { "Unsupported field: $name" }
        return EmptyJson
    }
}
