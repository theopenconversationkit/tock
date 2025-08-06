/*
 * Copyright (C) 2017/2025 SNCF Connect & Tech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ai.tock.bot.connector.googlechat

import ai.tock.bot.engine.action.Action
import ai.tock.bot.engine.action.SendSentence
import ai.tock.bot.engine.action.SendSentenceWithFootnotes
import io.grpc.lb.v1.FallbackResponse
import mu.KotlinLogging

internal object GoogleChatMessageConverter {

    private val logger = KotlinLogging.logger {}

    fun toMessageOut(action: Action, condensedFootnotes: Boolean = false): GoogleChatConnectorMessage? = when (action) {
        is SendSentence -> sendSentence(action)
        is SendSentenceWithFootnotes -> sendSentenceWithFootnotes(action, condensedFootnotes)
        else -> {
            logger.warn { "Action $action not supported" }
            null
        }
    }

    private fun sendSentence(action: SendSentence): GoogleChatConnectorMessage? {
        return if (action.hasMessage(GoogleChatConnectorProvider.connectorType)) {
            action.message(GoogleChatConnectorProvider.connectorType) as GoogleChatConnectorMessage
        } else {
            action.stringText
                ?.takeUnless { it.isBlank() }
                ?.let { GoogleChatMarkdown.toGoogleChat(it.toString()) }
                ?.let(::GoogleChatConnectorTextMessageOut)
        }
    }

    private fun sendSentenceWithFootnotes(
        action: SendSentenceWithFootnotes,
        condensedFootnotes: Boolean
    ): GoogleChatConnectorMessage {
        val formatted = GoogleChatFootnoteFormatter.format(
            action.text,
            action.footnotes,
            condensed = condensedFootnotes
        )
        val parsed = GoogleChatMarkdown.toGoogleChat(formatted)
        return GoogleChatConnectorTextMessageOut(parsed)
    }
}