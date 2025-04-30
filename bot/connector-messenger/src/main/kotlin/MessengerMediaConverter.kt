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

package ai.tock.bot.connector.messenger

import ai.tock.bot.connector.ConnectorMessage
import ai.tock.bot.connector.media.MediaAction
import ai.tock.bot.connector.media.MediaCard
import ai.tock.bot.connector.media.MediaCarousel
import ai.tock.bot.connector.media.MediaMessage
import ai.tock.bot.connector.messenger.model.send.Attachment
import ai.tock.bot.connector.messenger.model.send.AttachmentMessage
import ai.tock.bot.connector.messenger.model.send.Button
import ai.tock.bot.connector.messenger.model.send.MediaType
import ai.tock.bot.connector.messenger.model.send.QuickReply
import ai.tock.bot.connector.messenger.model.send.UrlPayload
import ai.tock.bot.connector.messenger.model.send.UserAction
import ai.tock.bot.engine.BotBus
import ai.tock.bot.engine.action.SendAttachment.AttachmentType
import ai.tock.bot.engine.action.SendAttachment.AttachmentType.image
import ai.tock.bot.engine.action.SendAttachment.AttachmentType.video

private typealias MessengerAttachmentType = ai.tock.bot.connector.messenger.model.send.AttachmentType

object MessengerMediaConverter {

    private fun BotBus.toButton(action: MediaAction): Button =
        if (action.url != null) urlButton(action.title, action.url!!)
        else nlpPostbackButton(action.title)

    private fun BotBus.toButtons(actions: List<MediaAction>, buttonsLimit: Int = 3, onlyUrl: Boolean = true): List<Button> =
        actions.filter { !onlyUrl || it.url != null }.take(buttonsLimit).map { toButton(it) }

    private fun BotBus.toQuickReplies(actions: List<MediaAction>): List<QuickReply> =
        actions.filter { it.url == null }.map { nlpQuickReply(it.title) }

    private fun BotBus.toUserActions(actions: List<MediaAction>, buttonsLimit: Int = 3): List<UserAction> =
        if (actions.size <= buttonsLimit) {
            actions.map { toButton(it) }
        } else {
            toButtons(actions, buttonsLimit) + toQuickReplies(actions)
        }

    private fun BotBus.textWithButtons(text: CharSequence, actions: List<MediaAction>): ConnectorMessage =
        if (actions.isEmpty()) text(text)
        else buttonsTemplate(text, toUserActions(actions))

    private fun BotBus.toAttachment(type: AttachmentType, url: String): AttachmentMessage =
        AttachmentMessage(
            Attachment(
                MessengerAttachmentType.fromTockAttachmentType(type),
                UrlPayload.getUrlPayload(this, url)
            )
        )

    fun toConnectorMessage(message: MediaMessage): BotBus.() -> List<ConnectorMessage> = {
        when (message) {
            is MediaCard -> fromMediaCard(message)
            is MediaCarousel -> fromMediaCarousel(message)
            else -> emptyList()
        }
    }

    private fun BotBus.fromMediaCarousel(message: MediaCarousel): List<ConnectorMessage> =
        listOf(
            genericTemplate(
                message.cards.map { card ->
                    genericElement(
                        card.title ?: card.subTitle ?: "",
                        card.subTitle.takeIf { card.title != null },
                        card.file?.url,
                        toButtons(card.actions, onlyUrl = false)
                    )
                },
                toQuickReplies(message.cards.flatMap { it.actions.drop(3) })
            )
        )

    private fun BotBus.fromMediaCard(message: MediaCard): List<ConnectorMessage> {
        val file = message.file
        val title = message.title
        val subTitle = message.subTitle
        val actions = message.actions
        return if (file != null) {
            when (file.type) {
                image ->
                    listOf(
                        if (title == null && subTitle == null && actions.isNotEmpty())
                            mediaTemplate(file.url, actions = toUserActions(actions))
                        else if (title != null || subTitle != null)
                            genericTemplate(
                                listOf(
                                    genericElement(
                                        title ?: subTitle!!,
                                        subTitle.takeIf { title != null },
                                        file.url,
                                        toButtons(actions)
                                    )
                                ),
                                toQuickReplies(actions)
                            )
                        else toAttachment(file.type, file.url)
                    )
                video ->
                    listOfNotNull(
                        if (subTitle != null) title?.let { text(it) } else null,
                        if (title == null && subTitle == null && actions.isNotEmpty())
                            mediaTemplate(file.url, MediaType.video, actions = toUserActions(actions))
                        else toAttachment(file.type, file.url),
                        if (title != null || subTitle != null) subTitle?.let { textWithButtons(it, actions) }
                        else null
                    )
                else ->
                    listOfNotNull(
                        if (subTitle != null) title?.let { text(it) } else null,
                        toAttachment(file.type, file.url),
                        if (title != null || subTitle != null)
                            textWithButtons(subTitle ?: title!!, actions)
                        else null
                    )
            }
        } else {
            listOfNotNull(
                if (title != null) textWithButtons(title, if (subTitle == null) actions else emptyList())
                else null,
                if (subTitle != null) textWithButtons(subTitle, actions)
                else null
            )
        }
    }
}
