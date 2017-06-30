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
import fr.vsct.tock.bot.connector.messenger.model.send.ListElementStyle
import fr.vsct.tock.bot.connector.messenger.model.send.ListPayload
import fr.vsct.tock.bot.connector.messenger.model.send.Payload
import fr.vsct.tock.bot.connector.messenger.model.send.PayloadType
import fr.vsct.tock.bot.connector.messenger.model.send.UrlPayload
import fr.vsct.tock.shared.jackson.JacksonDeserializer
import fr.vsct.tock.shared.jackson.read
import fr.vsct.tock.shared.jackson.readListValues
import fr.vsct.tock.shared.jackson.readValue
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
                var templateType: PayloadType? = null,
                var url: String? = null,
                var attachmentId: String? = null,
                var isReusable: Boolean? = null,
                var text: String? = null,
                var buttons: List<Button>? = null,
                var elements: List<Element>? = null,
                var topElementStyle: ListElementStyle? = null)

        val (templateType, url, attachmentId, isReusable, text, buttons, elements, topElementStyle)
                = jp.read<PayloadFields> { fields, name ->
            with(fields) {
                when (name) {
                    "template_type" -> templateType = jp.readValue()
                    UrlPayload::url.name -> url = jp.valueAsString
                    "is_reusable" -> isReusable = jp.valueAsBoolean
                    "attachment_id" -> attachmentId = jp.valueAsString
                    GenericPayload::elements.name -> elements = jp.readListValues()
                    ButtonPayload::buttons.name -> buttons = jp.readListValues()
                    ButtonPayload::text.name -> text = jp.valueAsString
                    "top_element_style" -> topElementStyle = jp.readValue()
                    else -> unknownValue
                }
            }
        }

        return if (templateType != null) {
            when (templateType) {
                PayloadType.generic -> GenericPayload(elements ?: emptyList())
                PayloadType.button -> ButtonPayload(text ?: "", buttons ?: emptyList())
                PayloadType.list -> ListPayload(elements ?: emptyList(), topElementStyle, buttons)
            }
        } else if (url != null || attachmentId != null) {
            UrlPayload(url, attachmentId, isReusable)
        } else {
            logger.warn { "invalid payload" }
            null
        }
    }
}