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

package ai.tock.bot.connector.messenger.json.webhook

import ai.tock.bot.connector.messenger.model.webhook.FacebookLocation
import ai.tock.bot.connector.messenger.model.webhook.LocationPayload
import ai.tock.bot.connector.messenger.model.webhook.Payload
import ai.tock.bot.connector.messenger.model.webhook.UrlPayload
import ai.tock.shared.jackson.JacksonDeserializer
import ai.tock.shared.jackson.read
import ai.tock.shared.jackson.readValue
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import mu.KotlinLogging

/**
 *
 */
internal class PayloadDeserializer : JacksonDeserializer<Payload>() {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override fun deserialize(
        jp: JsonParser,
        ctxt: DeserializationContext,
    ): Payload? {
        data class PayloadFields(
            var coordinates: FacebookLocation? = null,
            var url: String? = null,
            var other: EmptyJson? = null,
        )

        val (coordinates, url) =
            jp.read<PayloadFields> { fields, name ->
                with(fields) {
                    when (name) {
                        LocationPayload::coordinates.name -> coordinates = jp.readValue()
                        UrlPayload::url.name -> url = jp.valueAsString
                        else -> other = jp.readUnknownValue()
                    }
                }
            }

        return if (coordinates != null) {
            LocationPayload(coordinates)
        } else if (url != null) {
            UrlPayload(url)
        } else {
            logger.warn { "unknown payload" }
            null
        }
    }
}
