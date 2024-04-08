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
     * @param dto the [LLMSettingDTO]
     * @param secretName the secret name
     * @return [LLMSetting]
     */
    fun toEntity(dto: LLMSettingDTO, secretName: String): LLMSetting =
        with(dto){
            val secretKey = SecurityUtils.getSecretKey(apiKey, secretName)
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