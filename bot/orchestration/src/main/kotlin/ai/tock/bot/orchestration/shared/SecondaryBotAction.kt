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

import ai.tock.bot.connector.SerializableConnectorMessage
import ai.tock.bot.engine.action.Action
import ai.tock.bot.engine.action.ActionMetadata
import ai.tock.bot.engine.action.SendChoice
import ai.tock.bot.engine.action.SendSentence
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type",
)
@JsonSubTypes(
    JsonSubTypes.Type(value = SecondaryBotSendSentence::class, name = "sendSentence"),
    JsonSubTypes.Type(value = SecondaryBotSendChoice::class, name = "sendChoice"),
)
interface SecondaryBotAction {
    val metadata: ActionMetadata

    fun toAction(metaData: OrchestrationMetaData): Action

    companion object {
        fun from(
            action: Action,
            botId: String,
        ): SecondaryBotAction? =
            when (action) {
                is SendSentence ->
                    SecondaryBotSendSentence(
                        messages = action.messages.filterIsInstance<SerializableConnectorMessage>(),
                        text = action.text?.toString(),
                        metadata = action.metadata.copy(orchestratedBy = botId),
                    )
                is SendChoice -> SecondaryBotSendChoice(intentName = action.intentName, parameters = action.parameters, metadata = action.metadata.copy(orchestratedBy = botId))
                else -> null
            }
    }
}

data class SecondaryBotSendSentence(
    val messages: List<SerializableConnectorMessage>,
    val text: String?,
    override val metadata: ActionMetadata,
) : SecondaryBotAction {
    override fun toAction(metaData: OrchestrationMetaData): Action =
        SendSentence(
            playerId = metaData.playerId,
            applicationId = metaData.applicationId,
            recipientId = metaData.recipientId,
            text = text,
            messages = messages.toMutableList(),
            metadata = metadata,
        )
}

data class SecondaryBotSendChoice(
    val intentName: String,
    val parameters: Map<String, String> = emptyMap(),
    override val metadata: ActionMetadata,
) : SecondaryBotAction {
    override fun toAction(metaData: OrchestrationMetaData): Action =
        SendChoice(
            playerId = metaData.playerId,
            applicationId = metaData.applicationId,
            recipientId = metaData.recipientId,
            intentName = intentName,
            parameters = parameters,
            metadata = metadata,
        )
}
