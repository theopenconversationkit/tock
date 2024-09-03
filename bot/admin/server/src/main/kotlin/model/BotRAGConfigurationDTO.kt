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

import ai.tock.bot.admin.bot.rag.BotRAGConfiguration
import ai.tock.genai.orchestratorcore.mappers.EMSettingMapper
import ai.tock.genai.orchestratorcore.mappers.LLMSettingMapper
import ai.tock.genai.orchestratorcore.models.Constants
import ai.tock.genai.orchestratorcore.models.em.EMSettingDTO
import ai.tock.genai.orchestratorcore.models.llm.LLMSettingDTO
import ai.tock.genai.orchestratorcore.utils.SecurityUtils
import ai.tock.genai.orchestratorcore.utils.VectorStoreUtils
import org.litote.kmongo.newId
import org.litote.kmongo.toId

data class BotRAGConfigurationDTO(
    val id: String? = null,
    val namespace: String,
    val botId: String,
    val enabled: Boolean = false,
    val llmSetting: LLMSettingDTO,
    val emSetting: EMSettingDTO,
    val indexSessionId: String? = null,
    val indexName: String? = null,
    val noAnswerSentence: String,
    val noAnswerStoryId: String? = null,
) {
    constructor(configuration: BotRAGConfiguration) : this(
        configuration._id.toString(),
        configuration.namespace,
        configuration.botId,
        configuration.enabled,
        LLMSettingMapper.toDTO(configuration.llmSetting),
        EMSettingMapper.toDTO(configuration.emSetting),
        configuration.indexSessionId,
        indexName = null,
        configuration.noAnswerSentence,
        configuration.noAnswerStoryId
    )

    fun toBotRAGConfiguration(): BotRAGConfiguration =
        BotRAGConfiguration(
            id?.toId() ?: newId(),
            namespace,
            botId,
            enabled,
            LLMSettingMapper.toEntity(
                namespace,
                botId,
                Constants.GEN_AI_RAG_QUESTION_ANSWERING,
                llmSetting
            ),
            EMSettingMapper.toEntity(
                namespace,
                botId,
                Constants.GEN_AI_RAG_EMBEDDING_QUESTION,
                emSetting
            ),
            indexSessionId = indexSessionId,
            noAnswerSentence = noAnswerSentence,
            noAnswerStoryId = noAnswerStoryId
        )
}



