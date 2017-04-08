/*
 * Copyright (C) 2017 VSCT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.vsct.tock.bot.connector.messenger.json.webhook

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import fr.vsct.tock.bot.connector.messenger.model.webhook.LocationPayload
import fr.vsct.tock.bot.connector.messenger.model.webhook.Payload
import fr.vsct.tock.bot.connector.messenger.model.webhook.UrlPayload
import fr.vsct.tock.bot.engine.user.UserLocation
import fr.vsct.tock.shared.jackson.JacksonDeserializer
import fr.vsct.tock.shared.jackson.read
import fr.vsct.tock.shared.jackson.readValueAs
import mu.KotlinLogging

/**
 *
 */
internal class PayloadDeserializer : JacksonDeserializer<Payload>() {

    companion object {
        private val logger = KotlinLogging.logger {}
    }


    override fun deserialize(jp: JsonParser, ctxt: DeserializationContext): Payload? {
        data class PayloadFields(
                var coordinates: UserLocation? = null,
                var url: String? = null)

        val (coordinates, url) = jp.read<PayloadFields> { fields, name ->
            with(fields) {
                when (name) {
                    LocationPayload::coordinates.name -> coordinates = jp.readValueAs(UserLocation::class)
                    UrlPayload::url.name -> url = jp.valueAsString
                    else -> unknownValue
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