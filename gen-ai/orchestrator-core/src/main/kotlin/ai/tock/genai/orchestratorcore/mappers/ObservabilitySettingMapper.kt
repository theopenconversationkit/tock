/*
 * Copyright (C) 2017/2025 SNCF Connect & Tech
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

import ai.tock.genai.orchestratorcore.models.observability.LangfuseObservabilitySetting
import ai.tock.genai.orchestratorcore.models.observability.ObservabilitySetting
import ai.tock.genai.orchestratorcore.models.observability.ObservabilitySettingDTO
import ai.tock.genai.orchestratorcore.utils.SecurityUtils

/**
 * The Observability Setting Mapper
 */
object ObservabilitySettingMapper {
    /**
     * Convert the Observability setting to a DTO
     * @param entity the [ObservabilitySetting] as recorded in the database
     * @return [ObservabilitySettingDTO]
     */
    fun toDTO(entity: ObservabilitySetting): ObservabilitySettingDTO =
        with(entity) {
            when (this) {
                is LangfuseObservabilitySetting -> {
                    val secretKey = SecurityUtils.fetchSecretKeyValue(secretKey)
                    return LangfuseObservabilitySetting(secretKey, publicKey, url, publicUrl)
                }

                else ->
                    throw IllegalArgumentException("Unsupported Observability Setting")
            }
        }

    /**
     * Convert the Observability setting DTO to an Entity
     * @param namespace the application namespace
     * @param botId the bot ID (also known as application name)
     * @param feature the feature name
     * @param dto the [ObservabilitySettingDTO]
     * @param rawByForce force the creation of a raw secret key
     * @return [ObservabilitySetting]
     */
    fun toEntity(
        namespace: String,
        botId: String,
        feature: String,
        dto: ObservabilitySettingDTO,
        rawByForce: Boolean = false,
    ): ObservabilitySetting =
        with(dto) {
            when (this) {
                is LangfuseObservabilitySetting -> {
                    val secretKey = SecurityUtils.createSecretKey(namespace, botId, feature, secretKey, rawByForce)
                    return LangfuseObservabilitySetting(secretKey, publicKey, url, publicUrl.takeIf { !it.isNullOrBlank() })
                }
                else ->
                    throw IllegalArgumentException("Unsupported Observability Setting")
            }
        }
}
