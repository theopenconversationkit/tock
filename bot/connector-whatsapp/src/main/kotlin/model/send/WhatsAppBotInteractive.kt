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

package ai.tock.bot.connector.whatsapp.model.send

import ai.tock.bot.engine.action.SendChoice
import ai.tock.bot.engine.message.Choice
import com.fasterxml.jackson.annotation.JsonProperty

/**
 *
 */
enum class WhatsAppBotInteractiveType {
    list, button, product, product_list
}

data class WhatsAppBotInteractive(
    var type: WhatsAppBotInteractiveType,
    val header: WhatsAppBotInteractiveHeader? = null,
    val body: WhatsAppBotBody? = null, // optional for product type
    val footer: WhatsAppBotFooter? = null,
    val action: WhatsAppBotAction? = null,
)

data class WhatsAppBotInteractiveHeader(
    var type: WhatsAppBotHeaderType,
    val document: WhatsAppBotMedia? = null,
    val image: WhatsAppBotMedia? = null,
    val video: WhatsAppBotMedia? = null,
    val text: String? = null
)

enum class WhatsAppBotHeaderType {
    text, video, image, document
}

data class WhatsAppBotMedia(
    val id: String? = null,
    val link: String? = null,
    val caption: String? = null,
    val filename: String? = null,
    val provider: String? = null,
)

data class WhatsAppBotBody(
    val text: String
)

data class WhatsAppBotFooter(
    val text: String
)

data class WhatsAppBotAction(
    val button: String? = null,
    val buttons: List<WhatsAppBotActionButton>? = null,
    val sections: List<WhatsAppBotActionSection>? = null,
    @JsonProperty("catalog_id")
    val catalogId: String? = null,
    @JsonProperty("product_retailer_id")
    val productRetailerId: String? = null,
)

data class WhatsAppBotActionButton(
    val type: String = "reply",
    val reply: WhatsAppBotActionButtonReply,
) {
    fun toChoice(): Choice {
        return SendChoice.decodeChoiceId(reply.id)
            .let { (intent, params) ->
                Choice(
                    intent,
                    params + (SendChoice.TITLE_PARAMETER to reply.title)
                )
            }
    }
}

data class WhatsAppBotActionButtonReply(
    val title: String,
    val id: String,
)

data class WhatsAppBotActionSection(
    val title: String? = null,
    val rows: List<WhatsAppBotRow>? = null,
    @JsonProperty("product_items")
    val productItems: List<WhatsAppBotActionSectionProduct>? = null,
)

data class WhatsAppBotRow(
    val id: String,
    val title: String,
    val description: String? = null,
) {
    fun toChoice() : Choice =
        SendChoice.decodeChoiceId(id)
            .let { (intent, params) ->
                Choice(
                    intent,
                    params + (SendChoice.TITLE_PARAMETER to title)
                )
            }
}

data class WhatsAppBotActionSectionProduct(
    @JsonProperty("product_retailer_id")
    val productRetailerId: String,
)
