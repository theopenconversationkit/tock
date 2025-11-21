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

import ai.tock.bot.connector.openai.OpenAIConnector.Companion.defaultModel
import ai.tock.shared.Dice
import com.aallam.openai.api.chat.ChatCompletion

data class OpenAIConnectorResponse(val messages: List<OpenAIConnectorMessage>) {

    fun toOpenAIResponse(): ChatCompletion =
        ChatCompletion(
            id = Dice.newId(),
            created = System.currentTimeMillis(),
            model = defaultModel.id,
            choices = messages.map { it.toOpenAIChoice() },
        )
}
