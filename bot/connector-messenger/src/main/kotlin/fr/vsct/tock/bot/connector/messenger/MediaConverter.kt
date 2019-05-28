/*
 * Copyright (C) 2017/2019 VSCT
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

package fr.vsct.tock.bot.connector.messenger

import fr.vsct.tock.bot.connector.ConnectorMessage
import fr.vsct.tock.bot.connector.media.MediaAction
import fr.vsct.tock.bot.connector.media.MediaCard
import fr.vsct.tock.bot.connector.media.MediaMessage
import fr.vsct.tock.bot.connector.messenger.model.send.Attachment
import fr.vsct.tock.bot.connector.messenger.model.send.AttachmentMessage
import fr.vsct.tock.bot.connector.messenger.model.send.Button
import fr.vsct.tock.bot.connector.messenger.model.send.MediaType
import fr.vsct.tock.bot.connector.messenger.model.send.QuickReply
import fr.vsct.tock.bot.connector.messenger.model.send.UrlPayload
import fr.vsct.tock.bot.connector.messenger.model.send.UserAction
import fr.vsct.tock.bot.engine.BotBus
import fr.vsct.tock.bot.engine.action.SendAttachment.AttachmentType
import fr.vsct.tock.bot.engine.action.SendAttachment.AttachmentType.image
import fr.vsct.tock.bot.engine.action.SendAttachment.AttachmentType.video

typealias MessengerAttachmentType = fr.vsct.tock.bot.connector.messenger.model.send.AttachmentType

internal object MediaConverter {

    fun toConnectorMessage(message: MediaMessage): BotBus.() -> List<ConnectorMessage> = {
        fun toButton(action: MediaAction): Button =
            if (action.url != null) urlButton(action.title, action.url!!)
            else nlpPostbackButton(action.title)

        fun toButtons(actions: List<MediaAction>, buttonsLimit: Int = 3): List<Button> =
            actions.filter { it.url != null }.take(buttonsLimit).map { toButton(it) }

        fun toQuickReplies(actions: List<MediaAction>): List<QuickReply> =
            actions.filter { it.url == null }.map { nlpQuickReply(it.title) }

        fun toUserActions(actions: List<MediaAction>, buttonsLimit: Int = 3): List<UserAction> =
            if (actions.size <= buttonsLimit) {
                actions.map { toButton(it) }
            } else {
                toButtons(actions, buttonsLimit) + toQuickReplies(actions)
            }

        fun BotBus.textWithButtons(text: CharSequence, actions: List<MediaAction>): ConnectorMessage =
            if (actions.isEmpty()) text(text)
            else buttonsTemplate(text, toUserActions(actions))

        fun toAttachment(type: AttachmentType, url: String): AttachmentMessage =
            AttachmentMessage(
                Attachment(
                    MessengerAttachmentType.fromTockAttachmentType(type),
                    UrlPayload.getUrlPayload(this, url)
                )
            )

        if (message is MediaCard) {
            val file = message.file
            val title = message.title
            val subTitle = message.subTitle
            val actions = message.actions
            if (file != null) {
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
        } else {
            emptyList()
        }
    }
}