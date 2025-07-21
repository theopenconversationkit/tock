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
package ai.tock.bot.connector.googlechat

import ai.tock.bot.engine.action.Action
import ai.tock.bot.engine.action.Footnote
import ai.tock.bot.engine.action.SendSentence
import ai.tock.bot.engine.action.SendSentenceWithFootnotes
import mu.KotlinLogging

internal object GoogleChatMessageConverter {

    val logger = KotlinLogging.logger {}

    fun toMessageOut(action: Action): GoogleChatConnectorMessage? {
        return when (action) {
            is SendSentence ->
                if (action.hasMessage(GoogleChatConnectorProvider.connectorType)) {
                    action.message(GoogleChatConnectorProvider.connectorType) as GoogleChatConnectorMessage
                } else {
                    action.stringText?.takeUnless { it.isBlank() }?.let { GoogleChatConnectorTextMessageOut(it) }
                }

            is SendSentenceWithFootnotes -> {
                val fullText = formatFootnotesWithText(action.text, action.footnotes)
                GoogleChatConnectorTextMessageOut(fullText)
            }

            else -> {
                logger.warn { "Action $action not supported" }
                null
            }
        }
    }

    private fun formatFootnotesWithText(text: CharSequence, footnotes: List<Footnote>): String {
        if (footnotes.isEmpty()) return text.toString()

        val formattedFootnotes = footnotes.joinToString(separator = "\n") { footnote ->
            val title = footnote.title.trim()
            val url = footnote.url?.trim()

            when {
                !url.isNullOrBlank() && title.isNotBlank() -> "<$url|$title>"
                !url.isNullOrBlank() -> "<$url>"
                else -> title
            }
        }

        return "$text\n\n$formattedFootnotes"
    }
}
