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

import ai.tock.bot.engine.action.Action
import ai.tock.bot.engine.event.Event
import ai.tock.bot.engine.user.PlayerId
import ai.tock.bot.engine.user.PlayerType
import ai.tock.shared.Dice

interface OrchestrationRequest

data class AskEligibilityToOrchestratorRequest(
    val eligibleTargetBots: List<OrchestrationTargetedBot>,
    val data: OrchestrationData,
    val action: SecondaryBotAction?,
    val metadata: OrchestrationMetaData?
) : OrchestrationRequest

data class AskEligibilityToOrchestratedBotRequest(
    val data: OrchestrationData,
    val action: SecondaryBotAction?,
    val metadata: OrchestrationMetaData?
) : OrchestrationRequest {

    fun toAction(applicationId: String): Action {

        val handoverMetaData = metadata ?: OrchestrationMetaData(
            playerId = PlayerId(Dice.newId(), PlayerType.user),
            applicationId = applicationId,
            recipientId = PlayerId(applicationId, PlayerType.bot)
        )

        return when (val handoverData = data) {
            is OrchestrationSentence -> action?.toAction(handoverMetaData) ?: error("empty message $handoverData")
            else -> error("unknown message $handoverData")
        }
    }
}

data class ResumeOrchestrationRequest(
    val targetBot: OrchestrationTargetedBot,
    val action: SecondaryBotAction,
    val metadata: OrchestrationMetaData
) : OrchestrationRequest {
    fun toAction(): Event = action.toAction(metadata)
}
