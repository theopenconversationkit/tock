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

import ai.tock.bot.engine.message.GenericMessage

data class SlackMessageOut(
    val text: String,
    val channel: String? = null,
    val attachments: List<SlackMessageAttachment> = emptyList(),
) : SlackConnectorMessage() {
    override fun toGenericMessage(): GenericMessage? =
        GenericMessage(
            texts = mapOf(GenericMessage.TEXT_PARAM to text),
            choices = attachments.filter { it.hasOnlyActions() }.flatMap { it.actions }.map { it.toChoice() },
            subElements = attachments.filter { !it.hasOnlyActions() }.map { it.toGenericElement() },
        )
}
