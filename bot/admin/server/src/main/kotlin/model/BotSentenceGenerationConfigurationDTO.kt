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

import ai.tock.bot.admin.bot.sentencegeneration.BotSentenceGenerationConfiguration
import ai.tock.genai.orchestratorcore.mappers.LLMSettingMapper
import ai.tock.genai.orchestratorcore.models.Constants
import ai.tock.genai.orchestratorcore.models.llm.LLMSettingDTO
import ai.tock.genai.orchestratorcore.utils.SecurityUtils
import org.litote.kmongo.newId
import org.litote.kmongo.toId

data class BotSentenceGenerationConfigurationDTO(
    val id: String? = null,
    val namespace: String,
    val botId: String,
    val enabled: Boolean = false,
    val nbSentences: Int,
    val llmSetting: LLMSettingDTO,
) {
    constructor(configuration: BotSentenceGenerationConfiguration) : this(
        configuration._id.toString(),
        configuration.namespace,
        configuration.botId,
        configuration.enabled,
        configuration.nbSentences,
        LLMSettingMapper.toDTO(configuration.llmSetting),
    )

    fun toSentenceGenerationConfiguration(): BotSentenceGenerationConfiguration =
        BotSentenceGenerationConfiguration(
            id?.toId() ?: newId(),
            namespace,
            botId,
            enabled,
            nbSentences,
            LLMSettingMapper.toEntity(
                namespace,
                botId,
                Constants.GEN_AI_COMPLETION_SENTENCE_GENERATION,
                llmSetting
            )
        )
}



