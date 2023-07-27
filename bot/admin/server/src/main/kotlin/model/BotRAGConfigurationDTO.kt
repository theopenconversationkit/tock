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

package ai.tock.bot.admin.model

import ai.tock.bot.admin.bot.BotRAGConfiguration
import org.litote.kmongo.Id
import org.litote.kmongo.newId

data class BotRAGConfigurationDTO(
    val _id: Id<BotRAGConfiguration>?,
    val namespace: String,
    val botId: String,
    val enabled: Boolean?,
    val engine: String,
    val embeddingEngine: String,
    val temperature: String,
    val prompt: String,
    val params: Map<String, String>,
    val noAnswerRedirection: String,

) {
    fun toBotRAGConfiguration(): BotRAGConfiguration =
        BotRAGConfiguration(
            _id ?: newId(),
            namespace,
            botId,
            enabled ?: false,
            engine,
            embeddingEngine,
            temperature,
            prompt,
            params,
            noAnswerRedirection
        )
}
