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

package ai.tock.bot.admin.annotation

import ai.tock.genai.orchestratorcore.models.Constants
import ai.tock.genai.orchestratorcore.models.llm.AzureOpenAILLMSetting
import ai.tock.genai.orchestratorcore.models.llm.OllamaLLMSetting
import ai.tock.genai.orchestratorcore.models.llm.OpenAILLMSetting
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import org.litote.kmongo.Id
import java.time.Instant

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "type"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = BotAnnotationEventComment::class, name = "COMMENT"),
    JsonSubTypes.Type(value = BotAnnotationEventChange::class, name = "STATE"),
)
abstract class BotAnnotationEvent (
    open val eventId: Id<BotAnnotationEvent>,
    open val type: BotAnnotationEventType,
    open val creationDate: Instant,
    open val lastUpdateDate: Instant,
    open val user: String
    )
