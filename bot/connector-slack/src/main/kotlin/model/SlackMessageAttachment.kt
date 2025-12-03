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

package ai.tock.bot.connector.slack.model

import ai.tock.bot.engine.action.SendAttachment
import ai.tock.bot.engine.message.Attachment
import ai.tock.bot.engine.message.GenericElement
import com.fasterxml.jackson.annotation.JsonProperty

data class SlackMessageAttachment(
    val actions: List<Button>,
    val fields: List<AttachmentField>,
    val fallback: String,
    val color: String,
    val text: String? = null,
    val pretext: String? = null,
    @get:JsonProperty("callback_id")
    val callbackId: String = "default",
) {
    fun hasOnlyActions(): Boolean = actions.isNotEmpty() && fields.isEmpty() && text == null && pretext == null

    fun toGenericElement(): GenericElement =
        GenericElement(
            choices = actions.map { it.toChoice() },
            attachments = fields.map { Attachment(it.value, SendAttachment.AttachmentType.file) },
            texts = mapOf(::text.name to (text ?: "")),
        )
}
