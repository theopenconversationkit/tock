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

package fr.vsct.tock.bot.connector.messenger.model.send

import fr.vsct.tock.bot.connector.messenger.model.send.AttachmentType.audio
import fr.vsct.tock.bot.connector.messenger.model.send.AttachmentType.file
import fr.vsct.tock.bot.connector.messenger.model.send.AttachmentType.image
import fr.vsct.tock.bot.connector.messenger.model.send.AttachmentType.template
import fr.vsct.tock.bot.connector.messenger.model.send.AttachmentType.video
import fr.vsct.tock.bot.engine.message.SentenceElement

/**
 *
 */
data class AttachmentMessage(val attachment: Attachment) : Message() {

    override fun toSentenceElement(): SentenceElement? {
        return when (attachment.type) {
            audio, file, image, video -> SentenceElement(
                    this,
                    attachments = listOf(
                            fr.vsct.tock.bot.engine.message.Attachment(
                                    (attachment.payload as UrlPayload).url ?: "",
                                    attachment.type.toTockAttachmentType()
                            ))
            )
            template -> attachment.payload.toSentenceElement()
        }
    }
}