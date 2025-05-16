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
import ai.tock.bot.definition.Intent
import ai.tock.bot.definition.StoryHandler
import ai.tock.bot.definition.StoryHandlerListener
import ai.tock.bot.engine.BotBus

internal class ChatBaseStoryHandlerListener(private val apiKey: String, private val client: ChatBaseClient, private val version: String) :
    StoryHandlerListener {

    override fun startAction(botBus: BotBus, handler: StoryHandler): Boolean {
        val intent = (botBus.intent as? Intent)?.name ?: botBus.intent.toString()
        client.message(
            Message(
                apiKey = apiKey,
                type = Type.USER,
                platform = botBus.platform,
                userId = botBus.userId.id,
                message = botBus.userText ?: "",
                intent = intent,
                notHandled = intent == "unknown",
                version = version
            )
        )
        return true
    }
}
