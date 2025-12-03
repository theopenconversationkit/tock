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

package ai.tock.bot.connector.whatsapp.cloud.model.send.message.content

import ai.tock.bot.engine.action.SendChoice
import ai.tock.bot.engine.message.Choice
import com.fasterxml.jackson.annotation.JsonProperty

@Suppress("ktlint:standard:enum-entry-name-case")
enum class WhatsAppCloudBotInteractiveType {
    list,
    button,
    location_request_message,
    product,
    product_list,
    cta_url,
}

data class WhatsAppCloudBotInteractive(
    var type: WhatsAppCloudBotInteractiveType,
    val header: WhatsAppCloudBotInteractiveHeader? = null,
    // optional for product type
    val body: WhatsAppCloudBotBody? = null,
    val footer: WhatsAppCloudBotFooter? = null,
    val action: WhatsAppCloudBotAction,
)

data class WhatsAppCloudBotInteractiveHeader(
    var type: WhatsAppCloudBotHeaderType,
    val document: WhatsAppCloudBotMedia? = null,
    val image: WhatsAppCloudBotMediaImage? = null,
    val video: WhatsAppCloudBotMedia? = null,
    val text: String? = null,
)

@Suppress("ktlint:standard:enum-entry-name-case")
enum class WhatsAppCloudBotHeaderType {
    text,
    video,
    image,
    document,
}

data class WhatsAppCloudBotMedia(
    val id: String? = null,
    val link: String? = null,
    val caption: String? = null,
    val filename: String? = null,
    val provider: String? = null,
)

data class WhatsAppCloudBotMediaImage(
    val id: String,
)

data class WhatsAppCloudBotBody(
    val text: String,
)

data class WhatsAppCloudBotFooter(
    val text: String,
)

data class WhatsAppCloudBotAction(
    val name: String? = null,
    val button: String? = null,
    val buttons: List<WhatsAppCloudBotActionButton>? = null,
    val sections: List<WhatsAppCloudBotActionSection>? = null,
    @JsonProperty("catalog_id")
    val catalogId: String? = null,
    @JsonProperty("product_retailer_id")
    val productRetailerId: String? = null,
    val parameters: ParametersUrl? = null,
)

data class ParametersUrl(
    @JsonProperty("display_text") val displayText: String,
    @JsonProperty("url") val url: String,
)

data class WhatsAppCloudBotActionButton(
    val type: String = "reply",
    val reply: WhatsAppCloudBotActionButtonReply,
) {
    fun toChoice(): Choice {
        return SendChoice.decodeChoiceId(reply.id)
            .let { (intent, params) ->
                Choice(
                    intent,
                    params + (SendChoice.TITLE_PARAMETER to reply.title),
                )
            }
    }
}

data class WhatsAppCloudBotActionButtonReply(
    val title: String,
    val id: String,
)

data class WhatsAppCloudBotActionSection(
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
    fun toChoice(): Choice =
        SendChoice.decodeChoiceId(id)
            .let { (intent, params) ->
                Choice(
                    intent,
                    params + (SendChoice.TITLE_PARAMETER to title),
                )
            }
}

data class WhatsAppBotActionSectionProduct(
    @JsonProperty("product_retailer_id")
    val productRetailerId: String,
)
