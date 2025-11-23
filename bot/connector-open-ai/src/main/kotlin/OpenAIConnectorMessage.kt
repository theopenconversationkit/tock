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

package ai.tock.bot.connector.openai

import ai.tock.bot.connector.ConnectorMessage
import ai.tock.bot.connector.ConnectorType
import ai.tock.bot.connector.media.MediaCard
import ai.tock.bot.connector.media.MediaCarousel
import ai.tock.bot.connector.media.MediaMessage
import ai.tock.bot.connector.openai.OpenAIConnector.Companion.defaultModel
import ai.tock.bot.engine.action.SendAttachment.AttachmentType.image
import ai.tock.bot.engine.message.Choice
import ai.tock.bot.engine.message.GenericMessage
import ai.tock.shared.Dice
import ai.tock.shared.mapNotNullValues
import com.aallam.openai.api.chat.ChatChoice
import com.aallam.openai.api.chat.ChatChunk
import com.aallam.openai.api.chat.ChatCompletionChunk
import com.aallam.openai.api.chat.ChatDelta
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole

data class OpenAIConnectorMessage(
    val text: String? = null,
    val suggestions: List<String> = emptyList(),
    val mediaMessage: MediaMessage? = null,
) : ConnectorMessage {
    override val connectorType: ConnectorType = openAIConnectorType

    override fun toGenericMessage(): GenericMessage =
        GenericMessage(
            connectorType = connectorType,
            texts = mapNotNullValues(GenericMessage.TEXT_PARAM to text),
            choices = suggestions.map { Choice.fromText(it) },
        )

    fun toOpenAIChunk(): ChatCompletionChunk =
        ChatCompletionChunk(
            id = Dice.newId(),
            created = System.currentTimeMillis(),
            model = defaultModel.id,
            choices =
                listOf(
                    ChatChunk(
                        index = 0,
                        delta =
                            ChatDelta(
                                role = ChatRole.Assistant,
                                content = formatText(),
                            ),
                    ),
                ),
        )

    fun toOpenAIChoice(): ChatChoice =
        ChatChoice(
            index = 0,
            message =
                ChatMessage(
                    role = ChatRole.Assistant,
                    content = formatText(),
                ),
        )

    private fun formatText(): String =
        (text ?: "") +
            (
                suggestions.takeUnless { it.isEmpty() }
                    ?.joinToString(separator = "\n", prefix = "\n", postfix = "") {
                        "- $it"
                    } ?: ""
            ) +
            if (mediaMessage == null) {
                ""
            } else {
                when (mediaMessage) {
                    is MediaCard -> mediaMessage.formatCard()
                    is MediaCarousel ->
                        mediaMessage.cards.joinToString(
                            separator = "\n\n",
                            prefix = "",
                        ) { it.formatCard() }

                    else -> "[unsupported message]"
                }
            }

    private fun MediaCard.formatCard(): String =
        "---\n" + (
            file?.run {
                if (type == image) {
                    "![$name]($url)\n"
                } else {
                    "[$name]($url)\n"
                }
            } ?: ""
        ) +
            (title?.run { "# $this\n" } ?: "") +
            (subTitle?.run { "## $this\n" } ?: "") +
            (
                actions.takeUnless { it.isEmpty() }?.joinToString(
                    separator = "\n- ",
                    prefix = "- ",
                ) { a -> a.url?.let { url -> "[${a.title}]($url)" } ?: a.title } ?: ""
            ) +
            "\n---\n"
}
