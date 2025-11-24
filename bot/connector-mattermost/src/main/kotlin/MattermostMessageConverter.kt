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

package ai.tock.bot.connector.mattermost

import ai.tock.bot.connector.mattermost.model.Footnote
import ai.tock.bot.connector.mattermost.model.MattermostConnectorMessage
import ai.tock.bot.connector.mattermost.model.MattermostMessageOut
import ai.tock.bot.engine.action.Action
import ai.tock.bot.engine.action.SendSentence
import ai.tock.bot.engine.action.SendSentenceWithFootnotes
import mu.KotlinLogging

internal object MattermostMessageConverter {
    val logger = KotlinLogging.logger {}

    fun toMessageOut(
        action: Action,
        channelId: String? = null,
        tockUsername: String? = null,
    ): MattermostConnectorMessage? {
        return when (action) {
            is SendSentence ->
                if (action.hasMessage(MattermostConnectorProvider.connectorType)) {
                    action.message(MattermostConnectorProvider.connectorType) as MattermostConnectorMessage
                } else {
                    action.stringText?.run {
                        if (isBlank()) {
                            null
                        } else {
                            MattermostMessageOut(
                                this,
                                channel = channelId,
                                username = tockUsername,
                            )
                        }
                    }
                }

            is SendSentenceWithFootnotes -> {
                val stringText = action.text.toString()
                MattermostMessageOut(
                    stringText,
                    channel = channelId,
                    username = tockUsername,
                    footnotes =
                        action.footnotes.map { footnote ->
                            Footnote(
                                footnote.identifier,
                                footnote.title,
                                footnote.url,
                                footnote.content,
                                footnote.score,
                            )
                        },
                )
            }

            else -> {
                logger.warn { "Action $action not supported" }
                null
            }
        }
    }
}
