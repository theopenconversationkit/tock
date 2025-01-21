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

import ai.tock.genai.orchestratorcore.models.em.*
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
            when(this){
                is OpenAIEMSetting ->
                    OpenAIEMSetting(
                        apiKey = SecurityUtils.fetchSecretKeyValue(apiKey),
                        model = model,
                        baseUrl = baseUrl
                    )
                is AzureOpenAIEMSetting ->
                    AzureOpenAIEMSetting(
                        apiKey = SecurityUtils.fetchSecretKeyValue(apiKey),
                        apiBase = apiBase,
                        deploymentName = deploymentName,
                        apiVersion = apiVersion,
                        model = model
                    )
                is OllamaEMSetting ->
                    OllamaEMSetting(model = model, baseUrl = baseUrl)
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
     * @param rawByForce force the creation of a raw secret key
     * @return [EMSetting]
     */
    fun toEntity(
        namespace: String,
        botId: String,
        feature: String,
        dto: EMSettingDTO,
        rawByForce: Boolean = false
    ): EMSetting =
        with(dto){
            when(this){
                is OpenAIEMSetting ->
                    OpenAIEMSetting(
                        apiKey = SecurityUtils.createSecretKey(namespace, botId, feature, apiKey, rawByForce),
                        model = model,
                        baseUrl = baseUrl
                    )
                is AzureOpenAIEMSetting ->
                    AzureOpenAIEMSetting(
                        SecurityUtils.createSecretKey(namespace, botId, feature, apiKey, rawByForce),
                        apiBase = apiBase,
                        deploymentName = deploymentName,
                        apiVersion = apiVersion,
                        model = model
                    )
                is OllamaEMSetting ->
                    OllamaEMSetting(model = model, baseUrl = baseUrl)
                else ->
                    throw IllegalArgumentException("Unsupported EM Setting")
            }
        }

}