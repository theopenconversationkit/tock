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

package ai.tock.bot.connector.teams.messages

import ai.tock.bot.engine.action.SendAttachment
import ai.tock.bot.engine.message.Attachment
import ai.tock.bot.engine.message.Choice
import ai.tock.bot.engine.message.GenericElement
import ai.tock.bot.engine.message.GenericMessage
import ai.tock.bot.engine.message.GenericMessage.Companion.TITLE_PARAM
import ai.tock.shared.mapNotNullValues
import com.microsoft.bot.schema.CardAction
import com.microsoft.bot.schema.CardImage

class TeamsCarousel(val listMessage: List<TeamsBotMessage>) : TeamsBotMessage(null) {
    override fun toGenericMessage(): GenericMessage? {
        return GenericMessage(
            subElements = listMessage.map { GenericElement(it.toGenericMessage() ?: GenericMessage()) },
        )
    }
}

class TeamsBotTextMessage(text: String) : TeamsBotMessage(text) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TeamsBotTextMessage) return false
        if (!super.equals(other)) return false
        return true
    }

    override fun toString(): String {
        return text ?: ""
    }

    override fun toGenericMessage(): GenericMessage {
        return GenericMessage(
            connectorType = connectorType,
            texts = mapOf("text" to toString()),
        )
    }
}

class TeamsHeroCard(
    val title: String?,
    val subtitle: String?,
    val attachmentContent: String,
    val images: List<CardImage>?,
    val buttons: List<CardAction>?,
    val tap: CardAction?,
) : TeamsBotMessage(null) {
    override fun equals(other: Any?): Boolean {
        if (null == other) return false
        if (other !is TeamsHeroCard) return false
        if (title != other.title ||
            subtitle != other.subtitle ||
            attachmentContent != other.attachmentContent
        ) {
            return false
        }
        if ((images?.size ?: -1) != (other.images?.size ?: -1)) return false
        images?.forEach { self ->
            if (other.images == null) return false
            if (!other.images.any {
                    ((it.tap != null && self.tap != null && it.tap?.equalsTo(self.tap) == true) || (it.tap == null && self.tap == null)) &&
                        it.alt == self.alt &&
                        it.url == self.url
                }
            ) {
                return false
            }
        }
        if ((buttons?.size ?: -1) != (other.buttons?.size ?: -1)) return false
        buttons?.forEach { self ->
            if (other.buttons == null) return false
            if (!other.buttons.any { it.equalsTo(self) }) return false
        }
        if ((tap != null && other.tap != null) && !tap.equalsTo(other.tap)) return false
        return true
    }

    override fun toString(): String {
        val images = images?.map { it.url } ?: ""
        val buttons = buttons?.map { it.value } ?: ""
        return "TeamsHeroCard(" +
            "title=$title, " +
            "subtitle=$subtitle, " +
            "attachmentContent=$attachmentContent, " +
            "images=$images, " +
            "buttons=$buttons, " +
            "tap=${tap?.value})"
    }

    override fun toGenericMessage(): GenericMessage {
        return GenericMessage(
            connectorType = connectorType,
            texts =
                mapNotNullValues(
                    TITLE_PARAM to title,
                    "subtitle" to subtitle,
                    "attachmentContent" to attachmentContent,
                ),
            choices =
                buttons?.map {
                    Choice(
                        intentName = it.text?.toString() ?: it.title,
                        parameters =
                            mapNotNullValues(
                                "title" to it.title?.toString(),
                                "value" to it.value?.toString(),
                                "displayText" to it.displayText,
                                "text" to it.text,
                                "type" to it.type.toString(),
                            ),
                    )
                } ?: emptyList(),
            attachments =
                images?.map {
                    Attachment(
                        url = it.url,
                        type = SendAttachment.AttachmentType.image,
                    )
                } ?: emptyList(),
        )
    }
}

class TeamsCardAction(
    val actionTitle: String,
    val buttons: List<CardAction>,
) : TeamsBotMessage(null) {
    override fun toGenericMessage(): GenericMessage? {
        return GenericMessage(
            connectorType = connectorType,
            texts = mapNotNullValues(TITLE_PARAM to actionTitle),
            choices =
                buttons.map {
                    Choice(
                        intentName = it.text?.toString() ?: it.title,
                        parameters =
                            mapNotNullValues(
                                "title" to it.title?.toString(),
                                "value" to it.value?.toString(),
                                "displayText" to it.displayText,
                                "text" to it.text,
                                "type" to it.type.toString(),
                            ),
                    )
                },
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TeamsCardAction) return false
        if (!super.equals(other)) return false

        if (actionTitle != other.actionTitle) return false
        buttons.forEach { self ->
            if (!other.buttons.any { it.equalsTo(self) }) return false
        }

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + actionTitle.hashCode()
        result = 31 * result + buttons.hashCode()
        return result
    }

    override fun toString(): String {
        val allValues = buttons.groupBy { it.title }.mapValues { it.value[0].value }
        return "TeamsCardAction(actionTitle='$actionTitle', buttons=$allValues)"
    }
}

fun CardAction.equalsTo(other: CardAction?): Boolean {
    return image == other?.image &&
        text == other?.text &&
        title == other?.title &&
        displayText == other?.displayText &&
        type?.name == other?.type?.name &&
        value == other?.value
}
