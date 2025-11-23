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

import ai.tock.analytics.chatbase.model.Platform
import ai.tock.bot.engine.BotBus
import ai.tock.bot.engine.BotRepository

fun BotRepository.enableChatbase(
    apiKey: String,
    version: String,
) {
    val client = ChatBaseClient()
    registerStoryHandlerListener(ChatBaseStoryHandlerListener(apiKey, client, version))
    registerBotAnswerInterceptor(ChatBaseBotAnswerinterceptor(apiKey, client, version))
}

internal val BotBus.platform: String
    get() {
        val platform =
            Platform.values().find { it.name.equals(targetConnectorType.id, ignoreCase = true) } ?: Platform.CUSTOM
        return "${platform.label}-$applicationId"
    }
