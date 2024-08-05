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

package ai.tock.genai.orchestratorcore.models.observability


import ai.tock.genai.orchestratorcore.models.Constants
import ai.tock.genai.orchestratorcore.models.security.SecretKey
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "provider"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = LangfuseObservabilitySetting::class, name = Constants.LANGFUSE)
)
abstract class ObservabilitySettingBase<T>(
    val provider: ObservabilityProvider
)

typealias ObservabilitySettingDTO = ObservabilitySettingBase<String>
typealias ObservabilitySetting = ObservabilitySettingBase<SecretKey>
