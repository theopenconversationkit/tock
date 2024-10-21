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

package ai.tock.genai.orchestratorcore.models.em

import ai.tock.genai.orchestratorcore.mappers.EMSettingMapper
import ai.tock.genai.orchestratorcore.models.Constants
import ai.tock.shared.security.key.SecretKey
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "provider"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = OpenAIEMSetting::class, name = Constants.OPEN_AI),
    JsonSubTypes.Type(value = OllamaEMSetting::class, name = Constants.OLLAMA),
    JsonSubTypes.Type(value = AzureOpenAIEMSetting::class, name = Constants.AZURE_OPEN_AI_SERVICE)
)
abstract class EMSettingBase<T>(
    val provider: EMProvider,
    open val apiKey: T? = null,
)

typealias EMSettingDTO = EMSettingBase<String>
typealias EMSetting = EMSettingBase<SecretKey>

// Extension functions for DTO conversion
fun EMSetting.toDTO(): EMSettingDTO = EMSettingMapper.toDTO(this)