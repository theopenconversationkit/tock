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

package fr.vsct.tock.bot.connector.messenger.json.send

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import fr.vsct.tock.bot.connector.messenger.model.send.Button
import fr.vsct.tock.bot.connector.messenger.model.send.ButtonPayload
import fr.vsct.tock.bot.connector.messenger.model.send.Element
import fr.vsct.tock.bot.connector.messenger.model.send.GenericPayload
import fr.vsct.tock.bot.connector.messenger.model.send.ModelType
import fr.vsct.tock.bot.connector.messenger.model.send.Payload
import fr.vsct.tock.bot.connector.messenger.model.send.UrlPayload
import fr.vsct.tock.shared.jackson.JacksonDeserializer
import fr.vsct.tock.shared.jackson.read
import fr.vsct.tock.shared.jackson.readListValuesAs
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
                var templateType: ModelType? = null,
                var url: String? = null,
                var text: String? = null,
                var buttons: List<Button>? = null,
                var elements: List<Element>? = null)

        val (templateType, url, text, buttons, elements) = jp.read<PayloadFields> { fields, name ->
            with(fields) {
                when (name) {
                    "template_type" -> templateType = jp.readValueAs(ModelType::class)
                    UrlPayload::url.name -> url = jp.valueAsString
                    GenericPayload::elements.name -> elements = jp.readListValuesAs()
                    ButtonPayload::buttons.name -> buttons = jp.readListValuesAs()
                    ButtonPayload::text.name -> text = jp.valueAsString
                    else -> unknownValue
                }
            }
        }

        return if (templateType != null) {
            when (templateType) {
                ModelType.generic -> GenericPayload(elements ?: emptyList())
                ModelType.button -> ButtonPayload(text ?: "", buttons ?: emptyList())
            }
        } else if (url != null) {
            UrlPayload(url)
        } else {
            logger.warn { "invalid message" }
            null
        }
    }
}