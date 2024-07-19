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

package ai.tock.genai.orchestratorcore.models.llm


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
    JsonSubTypes.Type(value = OpenAILLMSetting::class, name = Constants.OPEN_AI),
    JsonSubTypes.Type(value = AzureOpenAILLMSetting::class, name = Constants.AZURE_OPEN_AI_SERVICE)
)
abstract class LLMSettingBase<T>(
    val provider: LLMProvider,
    open val apiKey: T,
    open val temperature: String,
    open val prompt: String
) {
    abstract fun copyWithTemperature(temperature: String): LLMSettingBase<T>
}

typealias LLMSettingDTO = LLMSettingBase<String>
typealias LLMSetting = LLMSettingBase<SecretKey>
