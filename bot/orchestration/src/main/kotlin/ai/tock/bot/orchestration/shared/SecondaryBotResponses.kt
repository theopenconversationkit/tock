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
import ai.tock.bot.engine.action.SendSentence
import ai.tock.bot.engine.user.PlayerId
import ai.tock.bot.engine.user.PlayerType
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type",
)
@JsonSubTypes(
    JsonSubTypes.Type(value = SecondaryBotAvailableResponse::class, name = "availableResponse"),
    JsonSubTypes.Type(value = SecondaryBotEligibilityResponse::class, name = "eligibleResponse"),
    JsonSubTypes.Type(value = SecondaryBotNoResponse::class, name = "noResponse"),
)
interface SecondaryBotResponse {
    val metaData: OrchestrationMetaData
    val indice: Double

    fun toOrchestratorResponse(bot: OrchestrationTargetedBot): OrchestrationResponse

    companion object {
        fun fromActions(
            applicationId: String,
            actions: List<Action>,
        ): SecondaryBotResponse {
            val sentences = actions.filterIsInstance<SendSentence>()
            val answerMetadata = OrchestrationMetaData(PlayerId(applicationId, PlayerType.bot), applicationId, sentences.first().recipientId)
            return if (sentences.isNotEmpty()) {
                SecondaryBotAvailableResponse(
                    actions =
                        sentences.map { sentence ->
                            SecondaryBotSendSentence(messages = sentence.messages.filterIsInstance<SerializableConnectorMessage>(), text = sentence.text?.toString(), metadata = sentence.metadata)
                        },
                    metaData = answerMetadata,
                )
            } else {
                SecondaryBotNoResponse(
                    status = NoOrchestrationStatus.NOT_AVAILABLE,
                    metaData = answerMetadata,
                )
            }
        }
    }
}

open class SecondaryBotEligibilityResponse(
    override val indice: Double,
    override val metaData: OrchestrationMetaData,
) : SecondaryBotResponse {
    override fun toOrchestratorResponse(bot: OrchestrationTargetedBot) =
        EligibilityOrchestrationResponse(
            targetBot = bot,
            botResponse = this,
        )
}

open class SecondaryBotAvailableResponse(
    val actions: List<SecondaryBotAction>,
    override val indice: Double = 1.0,
    override val metaData: OrchestrationMetaData,
) : SecondaryBotResponse {
    override fun toOrchestratorResponse(bot: OrchestrationTargetedBot) =
        AvailableOrchestrationResponse(
            targetBot = bot,
            botResponse = this,
        )
}

open class SecondaryBotNoResponse(
    val status: NoOrchestrationStatus,
    override val indice: Double = 0.0,
    override val metaData: OrchestrationMetaData,
) : SecondaryBotResponse {
    override fun toOrchestratorResponse(bot: OrchestrationTargetedBot) = NoOrchestrationResponse(status)
}
