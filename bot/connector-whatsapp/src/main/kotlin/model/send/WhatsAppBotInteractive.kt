/*
 * Copyright (C) 2017/2021 e-voyageurs technologies
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

/**
 *
 */
enum class WhatsAppBotInteractiveType {
    list, button, product, product_list
}

data class WhatsAppBotInteractive(
    var type: WhatsAppBotInteractiveType,
    val header: WhatsAppBotInteractiveHeader? = null,
    val body: WhatsAppBotBody? = null,
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
    val catalog_id: String? = null,
    val product_retailer_id: String? = null,
)

data class WhatsAppBotActionButton(
    val type: String = "reply",
    val reply: WhatsAppBotActionButtonReply,
)

data class WhatsAppBotActionButtonReply(
    val title: String,
    val id: String,
)

data class WhatsAppBotActionSection(
    val title: String? = null,
    val rows: List<WhatsAppBotRow>? = null,
    val product_items: List<WhatsAppBotActionSectionProduct>? = null,
)

data class WhatsAppBotRow(
    val id: String? = null,
    val title: String? = null,
    val description: String? = null,
)

data class WhatsAppBotActionSectionProduct(
    val product_retailer_id: String,
)
