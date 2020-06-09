/*
 * Copyright (C) 2017/2020 e-voyageurs technologies
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

import ai.tock.bot.connector.ConnectorMessage
import ai.tock.bot.engine.action.Action
import ai.tock.bot.engine.action.SendChoice
import ai.tock.bot.engine.action.SendSentence
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = SecondaryBotSendSentence::class, name = "sendSentence"),
    JsonSubTypes.Type(value = SecondaryBotSendChoice::class, name = "sendChoice")
)
interface SecondaryBotAction {

    fun toAction(metaData: OrchestrationMetaData) : Action

    companion object {
        fun from(action : Action) : SecondaryBotAction? = when (action) {
            is SendSentence -> SecondaryBotSendSentence(messages = action.messages, text = action.text?.toString())
            is SendChoice -> SecondaryBotSendChoice(intentName = action.intentName, parameters = action.parameters)
            else -> null
        }
    }
}

data class SecondaryBotSendSentence(
    val messages: List<ConnectorMessage>,
    val text: String?
) : SecondaryBotAction {
    override fun toAction(
        metaData: OrchestrationMetaData
    ): Action = SendSentence(
        playerId = metaData.playerId,
        applicationId = metaData.applicationId,
        recipientId = metaData.recipientId,
        text = text,
        messages = messages.toMutableList()
    )
}

data class SecondaryBotSendChoice(
    val intentName: String,
    val parameters: Map<String, String> = emptyMap()
) : SecondaryBotAction{
    override fun toAction(
        metaData: OrchestrationMetaData
    ): Action = SendChoice(
        playerId = metaData.playerId,
        applicationId = metaData.applicationId,
        recipientId = metaData.recipientId,
        intentName = intentName,
        parameters = parameters
    )
}