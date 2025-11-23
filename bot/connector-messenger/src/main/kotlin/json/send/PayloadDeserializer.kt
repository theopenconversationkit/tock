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

package ai.tock.bot.connector.messenger.json.send

import ai.tock.bot.connector.messenger.model.send.Button
import ai.tock.bot.connector.messenger.model.send.ButtonPayload
import ai.tock.bot.connector.messenger.model.send.Element
import ai.tock.bot.connector.messenger.model.send.GenericPayload
import ai.tock.bot.connector.messenger.model.send.ListElementStyle
import ai.tock.bot.connector.messenger.model.send.ListPayload
import ai.tock.bot.connector.messenger.model.send.MediaElement
import ai.tock.bot.connector.messenger.model.send.MediaPayload
import ai.tock.bot.connector.messenger.model.send.Payload
import ai.tock.bot.connector.messenger.model.send.PayloadType
import ai.tock.bot.connector.messenger.model.send.UrlPayload
import ai.tock.shared.jackson.JacksonDeserializer
import ai.tock.shared.jackson.read
import ai.tock.shared.jackson.readListValues
import ai.tock.shared.jackson.readValue
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.TreeNode
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.node.ArrayNode
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
            var templateType: PayloadType? = null,
            var url: String? = null,
            var attachmentId: String? = null,
            var isReusable: Boolean? = null,
            var text: String? = null,
            var buttons: List<Button>? = null,
            var elements: List<Any>? = null,
            var topElementStyle: ListElementStyle? = null,
            var sharable: Boolean? = null,
            var other: EmptyJson? = null,
        )

        val (
            templateType, url, attachmentId, isReusable,
            text, buttons, elements, topElementStyle, sharable,
        ) =
            jp.read<PayloadFields> { fields, name ->
                with(fields) {
                    when (name) {
                        "template_type" -> templateType = jp.readValue()
                        UrlPayload::url.name -> url = jp.valueAsString
                        "is_reusable" -> isReusable = jp.valueAsBoolean
                        "attachment_id" -> attachmentId = jp.valueAsString
                        GenericPayload::elements.name ->
                            elements =
                                jp.readValueAsTree<TreeNode>().run {
                                    if ((this as ArrayNode).elementAt(0).has("media_type")) {
                                        jp.codec.treeToValue(
                                            this,
                                            Array<MediaElement>::class.java,
                                        ).toList()
                                    } else {
                                        jp.codec.treeToValue(
                                            this,
                                            Array<Element>::class.java,
                                        ).toList()
                                    }
                                }
                        ButtonPayload::buttons.name -> buttons = jp.readListValues()
                        ButtonPayload::text.name -> text = jp.valueAsString
                        "top_element_style" -> topElementStyle = jp.readValue()
                        MediaPayload::sharable.name -> sharable = jp.valueAsBoolean
                        else -> other = jp.readUnknownValue()
                    }
                }
            }

        return if (templateType != null) {
            @Suppress("UNCHECKED_CAST")
            when (templateType) {
                PayloadType.generic -> GenericPayload(elements as? List<Element> ?: emptyList())
                PayloadType.button -> ButtonPayload(text ?: "", buttons ?: emptyList())
                PayloadType.list -> ListPayload(elements as? List<Element> ?: emptyList(), topElementStyle, buttons)
                PayloadType.media -> MediaPayload(elements as? List<MediaElement> ?: emptyList(), sharable ?: false)
            }
        } else if (url != null || attachmentId != null) {
            UrlPayload(url, attachmentId, isReusable)
        } else {
            logger.warn { "invalid payload" }
            null
        }
    }
}
