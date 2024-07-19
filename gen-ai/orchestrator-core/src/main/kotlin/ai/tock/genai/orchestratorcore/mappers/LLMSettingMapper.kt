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

package ai.tock.genai.orchestratorcore.mappers

import ai.tock.genai.orchestratorcore.models.llm.AzureOpenAILLMSetting
import ai.tock.genai.orchestratorcore.models.llm.LLMSetting
import ai.tock.genai.orchestratorcore.models.llm.LLMSettingDTO
import ai.tock.genai.orchestratorcore.models.llm.OpenAILLMSetting
import ai.tock.genai.orchestratorcore.utils.SecurityUtils

/**
 * The Large Language Model Setting Mapper
 */
object LLMSettingMapper {

    /**
     * Convert the LLM setting to a DTO
     * @param entity the [LLMSetting] as recorded in the database
     * @return [LLMSettingDTO]
     */
    fun toDTO(entity: LLMSetting): LLMSettingDTO =
        with(entity){
            val secretKey = SecurityUtils.fetchSecretKeyValue(apiKey)
            when(this){
                is OpenAILLMSetting ->
                    OpenAILLMSetting(secretKey, temperature, prompt, model)
                is AzureOpenAILLMSetting ->
                    AzureOpenAILLMSetting(secretKey, temperature, prompt, apiBase, deploymentName, apiVersion)
                else ->
                    throw IllegalArgumentException("Unsupported LLM Setting")
            }
        }

    /**
     * Convert the LLM setting DTO to an Entity
     * @param namespace the application namespace
     * @param botId the bot ID (also known as application name)
     * @param feature the feature name
     * @param dto the [LLMSettingDTO]
     * @return [LLMSetting]
     */
    fun toEntity(namespace: String, botId: String, feature: String, dto: LLMSettingDTO): LLMSetting =
        with(dto){
            val secretKey = SecurityUtils.createSecretKey(namespace, botId, feature, apiKey)
            when(this){
                is OpenAILLMSetting ->
                    OpenAILLMSetting(secretKey, temperature, prompt, model)
                is AzureOpenAILLMSetting ->
                    AzureOpenAILLMSetting(secretKey, temperature, prompt, apiBase, deploymentName, apiVersion)
                else ->
                    throw IllegalArgumentException("Unsupported LLM Setting")
            }
        }

}