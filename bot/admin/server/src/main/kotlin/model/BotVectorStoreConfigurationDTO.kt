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

import ai.tock.bot.admin.bot.vectorstore.BotVectorStoreConfiguration
import ai.tock.genai.orchestratorcore.mappers.LLMSettingMapper
import ai.tock.genai.orchestratorcore.mappers.VectorStoreSettingMapper
import ai.tock.genai.orchestratorcore.models.Constants
import ai.tock.genai.orchestratorcore.models.vectorstore.VectorStoreSettingDTO
import ai.tock.genai.orchestratorcore.utils.SecurityUtils
import org.litote.kmongo.newId
import org.litote.kmongo.toId

data class BotVectorStoreConfigurationDTO(
    val id: String? = null,
    val namespace: String,
    val botId: String,
    val enabled: Boolean = false,
    val setting: VectorStoreSettingDTO,
) {
    constructor(configuration: BotVectorStoreConfiguration) : this(
        configuration._id.toString(),
        configuration.namespace,
        configuration.botId,
        configuration.enabled,
        VectorStoreSettingMapper.toDTO(configuration.setting),
    )

    fun toBotVectorStoreConfiguration(): BotVectorStoreConfiguration =
        BotVectorStoreConfiguration(
            id?.toId() ?: newId(),
            namespace,
            botId,
            enabled,
            VectorStoreSettingMapper.toEntity(
                namespace,
                botId,
                Constants.GEN_AI_VECTOR_STORE,
                setting
            )
        )
}



