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

package ai.tock.bot.connector.messenger.model.send

import ai.tock.bot.connector.ConnectorMessage
import ai.tock.bot.connector.messenger.model.send.AttachmentType.audio
import ai.tock.bot.connector.messenger.model.send.AttachmentType.file
import ai.tock.bot.connector.messenger.model.send.AttachmentType.image
import ai.tock.bot.connector.messenger.model.send.AttachmentType.template
import ai.tock.bot.connector.messenger.model.send.AttachmentType.video
import ai.tock.bot.engine.message.GenericMessage

/**
 *
 */
class AttachmentMessage(val attachment: Attachment, quickReplies: List<QuickReply>? = null) :
    Message(quickReplies?.run { if (isEmpty()) null else this }) {
    override fun toGenericMessage(): GenericMessage? {
        return when (attachment.type) {
            audio, file, image, video ->
                GenericMessage(
                    this,
                    attachments =
                        listOf(
                            ai.tock.bot.engine.message.Attachment(
                                (attachment.payload as UrlPayload).url ?: "",
                                attachment.type.toTockAttachmentType(),
                            ),
                        ),
                )
            template -> attachment.payload.toGenericMessage()
        }?.run {
            if (quickReplies?.isNotEmpty() == true) {
                copy(
                    choices = choices + quickReplies.mapNotNull { it.toChoice() },
                    locations = locations + quickReplies.mapNotNull { it.toLocation() },
                )
            } else {
                this
            }
        }
    }

    override fun obfuscate(): ConnectorMessage {
        return when (attachment.type) {
            template ->
                AttachmentMessage(
                    attachment.copy(payload = attachment.payload.obfuscate()),
                    quickReplies,
                )
            else -> this
        }
    }

    override fun findElements(): List<Element> =
        with(attachment.payload) {
            when (this) {
                is GenericPayload -> elements
                is ListPayload -> elements
                else -> emptyList()
            }
        }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as AttachmentMessage

        if (attachment != other.attachment) return false
        if (quickReplies != other.quickReplies) return false

        return true
    }

    override fun hashCode(): Int {
        return attachment.hashCode()
    }

    override fun toString(): String {
        return "AttachmentMessage(attachment=$attachment,quickReplies=$quickReplies)"
    }

    override fun copy(quickReplies: List<QuickReply>?): Message = AttachmentMessage(attachment, quickReplies)
}
