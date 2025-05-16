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

package ai.tock.analytics.chatbase

import ai.tock.analytics.chatbase.model.Message
import ai.tock.analytics.chatbase.model.Type
import ai.tock.bot.definition.BotAnswerInterceptor
import ai.tock.bot.engine.BotBus
import ai.tock.bot.engine.action.Action
import ai.tock.bot.engine.action.SendSentence

internal class ChatBaseBotAnswerinterceptor(private val apiKey: String, private val client: ChatBaseClient, private val version: String) :
    BotAnswerInterceptor {

    override fun handle(action: Action, bus: BotBus): Action {
        val messages = when (action) {
            is SendSentence -> action.messages.map { message ->
                Message(
                    apiKey = apiKey,
                    type = Type.AGENT,
                    platform = bus.platform,
                    userId = action.recipientId.id,
                    message = action.toMessage().toPrettyString(),
                    version = version
                )
            }
            else -> listOf()
        }

        messages.map { message ->
            client.message(message)
        }
        return action
    }
}
