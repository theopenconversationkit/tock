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


import ai.tock.bot.admin.bot.observability.BotObservabilityConfiguration
import ai.tock.genai.orchestratorcore.mappers.ObservabilitySettingMapper
import ai.tock.genai.orchestratorcore.models.Constants
import ai.tock.genai.orchestratorcore.models.observability.ObservabilitySettingDTO
import ai.tock.genai.orchestratorcore.utils.SecurityUtils
import org.litote.kmongo.newId
import org.litote.kmongo.toId

data class BotObservabilityConfigurationDTO(
    val id: String? = null,
    val namespace: String,
    val botId: String,
    val enabled: Boolean = false,
    val setting: ObservabilitySettingDTO,
) {
    constructor(configuration: BotObservabilityConfiguration) : this(
        configuration._id.toString(),
        configuration.namespace,
        configuration.botId,
        configuration.enabled,
        ObservabilitySettingMapper.toDTO(configuration.setting),
    )

    fun toBotObservabilityConfiguration(): BotObservabilityConfiguration =
        BotObservabilityConfiguration(
            id?.toId() ?: newId(),
            namespace,
            botId,
            enabled,
            ObservabilitySettingMapper.toEntity(
                namespace,
                botId,
                Constants.GEN_AI_OBSERVABILITY,
                setting
            ),
        )
}



