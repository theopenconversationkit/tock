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

package ai.tock.bot.connector.twitter.json

import ai.tock.bot.connector.twitter.model.AbstractOption
import ai.tock.bot.connector.twitter.model.Option
import ai.tock.bot.connector.twitter.model.OptionWithoutDescription
import ai.tock.shared.jackson.JacksonDeserializer
import ai.tock.shared.jackson.read
import ai.tock.shared.jackson.readValue
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import mu.KotlinLogging

internal class OptionDeserializer : JacksonDeserializer<AbstractOption>() {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override fun deserialize(jp: JsonParser, ctxt: DeserializationContext): AbstractOption? {

        data class MediaFields(
            var label: String? = null,
            var description: String? = null,
            var metadata: String? = null,
            var other: EmptyJson? = null
        )

        val (label, description, metadata) = jp.read<MediaFields> { fields, name ->
            with(fields) {
                when (name) {
                    "label" -> label = jp.readValue()
                    "description" -> description = jp.readValue()
                    "metadata" -> metadata = jp.readValue()
                    else -> other = jp.readUnknownValue()
                }
            }
        }

        return if (description == null) {
            OptionWithoutDescription.of(label!!, metadata!!)
        } else {
            Option.of(label!!, description, metadata!!)
        }
    }
}
