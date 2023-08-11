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

import ai.tock.bot.admin.answer.AnswerConfigurationType
import ai.tock.bot.admin.answer.RagAnswerConfiguration

/**
 * A bot rag answer configuration
 */
data class BotRagAnswerConfiguration(
    val activation: Boolean?,
    val engine: String,
    val embeddingEngine: String,
    val temperature: String,
    val prompt: String,
    val params: Map<String, String>,
    val noAnswerRedirection: String?
) :
    BotAnswerConfiguration(AnswerConfigurationType.rag) {

    constructor(conf: RagAnswerConfiguration) : this(
        conf.activation,
        conf.engine,
        conf.embeddingEngine,
        conf.temperature,
        conf.prompt,
        conf.params,
        conf.noAnswerRedirection,
    )


    fun toRagAnswerConfiguration(): RagAnswerConfiguration = RagAnswerConfiguration(
        activation,
        engine,
        embeddingEngine,
        temperature,
        prompt,
        params,
        noAnswerRedirection
    )
}
