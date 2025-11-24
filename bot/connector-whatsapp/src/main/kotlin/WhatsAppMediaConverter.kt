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

package ai.tock.bot.connector.whatsapp

import ai.tock.bot.connector.ConnectorMessage
import ai.tock.bot.connector.media.MediaAction
import ai.tock.bot.connector.media.MediaCard
import ai.tock.bot.connector.media.MediaCarousel
import ai.tock.bot.connector.media.MediaMessage
import ai.tock.bot.connector.whatsapp.model.send.WhatsAppBotTextMessage
import ai.tock.bot.engine.BotBus

object WhatsAppMediaConverter {
    fun toConnectorMessage(message: MediaMessage): BotBus.() -> List<ConnectorMessage> =
        {
            when (message) {
                is MediaCard -> fromMediaCard(message)
                is MediaCarousel -> fromMediaCarousel(message)
                else -> emptyList()
            }
        }

    private fun BotBus.fromMediaCarousel(message: MediaCarousel): List<ConnectorMessage> = message.cards.flatMap { fromMediaCard(it) }

    private fun BotBus.fromMediaCard(message: MediaCard): List<ConnectorMessage> {
        val text =
            listOfNotNull(
                message.title,
                message.subTitle,
            ).joinToString("\n\n")
        val (nlpActions, links) = message.actions.partition { it.url == null }
        return if (nlpActions.isNotEmpty()) {
            listOf(
                replyButtonMessage(text, nlpActions.map { nlpQuickReply(it.title) }),
            ) + toTextMessages(links)
        } else {
            listOf(whatsAppText(text)) + toTextMessages(links)
        }
    }

    private fun BotBus.toTextMessages(links: List<MediaAction>): List<WhatsAppBotTextMessage> =
        links.map { link ->
            whatsAppText(
                text = "${link.title} :\n${link.url}",
                previewUrl = true,
            )
        }
}
