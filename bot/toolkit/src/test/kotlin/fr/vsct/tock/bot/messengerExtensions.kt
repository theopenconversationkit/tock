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

package fr.vsct.tock.bot

import fr.vsct.tock.bot.connector.messenger.MessengerConfiguration
import fr.vsct.tock.bot.connector.messenger.model.send.Attachment
import fr.vsct.tock.bot.connector.messenger.model.send.AttachmentMessage
import fr.vsct.tock.bot.connector.messenger.model.send.AttachmentType
import fr.vsct.tock.bot.connector.messenger.model.send.ButtonPayload
import fr.vsct.tock.bot.connector.messenger.model.send.Element
import fr.vsct.tock.bot.connector.messenger.model.send.GenericPayload
import fr.vsct.tock.bot.connector.messenger.model.send.ListElementStyle
import fr.vsct.tock.bot.connector.messenger.model.send.ListPayload
import fr.vsct.tock.bot.connector.messenger.model.send.QuickReply
import fr.vsct.tock.bot.connector.messenger.model.send.TextMessage
import fr.vsct.tock.bot.connector.messenger.model.send.UrlPayload
import fr.vsct.tock.bot.connector.messenger.model.send.UserAction
import fr.vsct.tock.bot.engine.BotBus

/**
 *
 */
fun BotBus.messengerButtons(text: String, vararg actions: UserAction): AttachmentMessage {
    return AttachmentMessage(
            Attachment(
                    AttachmentType.template,
                    ButtonPayload(
                            text, UserAction.extractButtons(actions.toList())
                    )
            ),
            UserAction.extractQuickReplies(actions.toList())
    )
}

fun BotBus.messengerList(
        e1: Element,
        e2: Element,
        e3: Element? = null,
        e4: Element? = null,
        topElementStyle: ListElementStyle? = null,
        vararg actions: UserAction): AttachmentMessage {
    return messengerList(listOfNotNull(e1, e2, e3, e4), topElementStyle, *actions)
}

fun BotBus.messengerList(
        elements: List<Element>,
        topElementStyle: ListElementStyle? = null,
        vararg actions: UserAction): AttachmentMessage {
    if (elements.size < 2 || elements.size > 4) {
        error("must have at least 2 elements and at most 4")
    }
    if (topElementStyle != ListElementStyle.compact
            && elements.any { it.imageUrl == null }) {
        error("imageUrl of elements may not be null with large element style")
    }

    return AttachmentMessage(
            Attachment(
                    AttachmentType.template,
                    ListPayload(
                            elements,
                            topElementStyle,
                            UserAction.extractButtons(actions.toList())
                                    .run {
                                        if (isEmpty()) null
                                        else if (size > 1) error("only one button max")
                                        else this
                                    })
            ),
            UserAction.extractQuickReplies(actions.toList())
    )
}

fun BotBus.messengerGeneric(vararg elements: Element): AttachmentMessage {
    return messengerGeneric(elements.toList())
}

fun BotBus.messengerGeneric(elements: List<Element>, vararg quickReplies: QuickReply): AttachmentMessage {
    if (elements.isEmpty() || elements.size > 10) {
        error("must have at least 1 elements and at most 10")
    }

    return AttachmentMessage(
            Attachment(
                    AttachmentType.template,
                    GenericPayload(
                            elements
                    )
            ),
            quickReplies.run { if (isEmpty()) null else toList() }
    )
}

fun BotBus.messengerAttachment(attachmentUrl: String, type: AttachmentType, vararg quickReplies: QuickReply): AttachmentMessage {
    return when (type) {
        AttachmentType.image -> messengerImage(attachmentUrl, *quickReplies)
        AttachmentType.audio -> messengerAudio(attachmentUrl, *quickReplies)
        AttachmentType.video -> messengerVideo(attachmentUrl, *quickReplies)
        else -> error { "not supported attachment type $type" }
    }
}

private fun BotBus.messengerAttachmentType(
        attachmentUrl: String,
        type: AttachmentType,
        useCache: Boolean = MessengerConfiguration.reuseAttachmentByDefault,
        vararg quickReplies: QuickReply): AttachmentMessage {
    return AttachmentMessage(
            Attachment(
                    type,
                    UrlPayload.getUrlPayload(applicationId, attachmentUrl, useCache && !userPreferences.test)
            ),
            quickReplies.run { if (isEmpty()) null else toList() }
    )
}

fun BotBus.messengerImage(imageUrl: String, vararg quickReplies: QuickReply): AttachmentMessage {
    return messengerAttachmentType(imageUrl, AttachmentType.image, quickReplies = *quickReplies)
}

fun BotBus.messengerAudio(audioUrl: String, vararg quickReplies: QuickReply): AttachmentMessage {
    return messengerAttachmentType(audioUrl, AttachmentType.audio, quickReplies = *quickReplies)
}

fun BotBus.messengerVideo(videoUrl: String, vararg quickReplies: QuickReply): AttachmentMessage {
    return messengerAttachmentType(videoUrl, AttachmentType.video, quickReplies = *quickReplies)
}

fun BotBus.messengerQuickReplies(text: String, vararg quickReplies: QuickReply): TextMessage {
    return TextMessage(text, quickReplies.toList())
}