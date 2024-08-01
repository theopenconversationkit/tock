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

import ai.tock.genai.orchestratorcore.models.em.AzureOpenAIEMSetting
import ai.tock.genai.orchestratorcore.models.em.EMSetting
import ai.tock.genai.orchestratorcore.models.em.EMSettingDTO
import ai.tock.genai.orchestratorcore.models.em.OpenAIEMSetting
import ai.tock.genai.orchestratorcore.utils.SecurityUtils


/**
 * The Embedding Setting Mapper
 */
object EMSettingMapper {

    /**
     * Convert the Embedding setting to a DTO
     * @param entity the [EMSetting] as recorded in the database
     * @return [EMSettingDTO]
     */
    fun toDTO(entity: EMSetting): EMSettingDTO =
        with(entity){
            val secretKey = SecurityUtils.fetchSecretKeyValue(apiKey)
            when(this){
                is OpenAIEMSetting ->
                    OpenAIEMSetting(secretKey, model)
                is AzureOpenAIEMSetting ->
                    AzureOpenAIEMSetting(secretKey, apiBase, deploymentName, apiVersion)
                else ->
                    throw IllegalArgumentException("Unsupported EM Setting")
            }
        }

    /**
     * Convert the Embedding setting DTO to an Entity
     * @param namespace the application namespace
     * @param botId the bot ID (also known as application name)
     * @param feature the feature name
     * @param dto the [EMSettingDTO]
     * @return [EMSetting]
     */
    fun toEntity(namespace: String, botId: String, feature: String, dto: EMSettingDTO): EMSetting =
        with(dto){
            val secretKey = SecurityUtils.createSecretKey(namespace, botId, feature, apiKey)
            when(this){
                is OpenAIEMSetting ->
                    OpenAIEMSetting(secretKey, model)
                is AzureOpenAIEMSetting ->
                    AzureOpenAIEMSetting(secretKey, apiBase, deploymentName, apiVersion)
                else ->
                    throw IllegalArgumentException("Unsupported EM Setting")
            }
        }

}