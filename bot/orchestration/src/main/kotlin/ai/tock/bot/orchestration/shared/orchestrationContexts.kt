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

package ai.tock.bot.orchestration.shared

import ai.tock.bot.connector.ConnectorType
import ai.tock.bot.definition.StoryDefinition
import ai.tock.bot.engine.user.PlayerId
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

data class OrchestrationTargetedBot(
    val botId: String,
    val botLabel: String,
    val connectorId: String,
    val connectorType: ConnectorType,
    val fallbackStory: StoryDefinition? = null
)

data class OrchestrationMetaData(
    val playerId: PlayerId,
    val applicationId: String,
    val recipientId: PlayerId
)

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = OrchestrationSentence::class, name = "sentence")
)
interface OrchestrationData

data class OrchestrationSentence(
    val userText: String
) : OrchestrationData

enum class NoOrchestrationStatus {
    NOT_AVAILABLE, ERROR, END
}
