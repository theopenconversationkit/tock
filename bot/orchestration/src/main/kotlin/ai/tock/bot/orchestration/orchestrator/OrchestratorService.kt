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

package ai.tock.bot.orchestration.orchestrator

import ai.tock.bot.orchestration.shared.AskEligibilityToOrchestratedBotRequest
import ai.tock.bot.orchestration.shared.AskEligibilityToOrchestratorRequest
import ai.tock.bot.orchestration.shared.NoOrchestrationResponse
import ai.tock.bot.orchestration.shared.NoOrchestrationStatus.ERROR
import ai.tock.bot.orchestration.shared.NoOrchestrationStatus.NOT_AVAILABLE
import ai.tock.bot.orchestration.shared.OrchestrationResponse
import ai.tock.bot.orchestration.shared.ResumeOrchestrationRequest
import ai.tock.bot.orchestration.shared.SecondaryBotAvailableResponse
import ai.tock.bot.orchestration.shared.SecondaryBotNoResponse

open class OrchestratorService(
    private val orchestratedBots: OrchestratedRuntimeBots
) {

    fun askOrchestration(request: AskEligibilityToOrchestratorRequest): OrchestrationResponse {

        val eligibleBots = request.eligibleTargetBots.mapNotNull { eligibleBot -> orchestratedBots.get(eligibleBot) }

        return when (val response = getTheBestAnswer(eligibleBots, request)) {
            null -> NoOrchestrationResponse(
                status = NOT_AVAILABLE
            )
            else -> response
        }
    }

    fun resumeOrchestration(request: ResumeOrchestrationRequest): OrchestrationResponse {
        val secondaryBot = orchestratedBots.get(request.targetBot)

        return secondaryBot?.resumeOrchestration(request).let { response ->
            when (response) {
                is SecondaryBotAvailableResponse -> response.toOrchestratorResponse(request.targetBot)
                is SecondaryBotNoResponse -> NoOrchestrationResponse(status = response.status)
                else -> NoOrchestrationResponse(status = ERROR)
            }
        }
    }

    protected open fun getTheBestAnswer(
        eligibleBots: List<OrchestratedRuntimeBot>,
        request: AskEligibilityToOrchestratorRequest
    ): OrchestrationResponse? {
        return eligibleBots
            .map { it to it.askOrchestration(request.toBotRequest()) } // TODO to run in parallel
            .filter { (_, response) -> response.indice > 0 }
            .maxByOrNull { (_, response) -> response.indice }
            ?.let { (bot, response) -> response.toOrchestratorResponse(bot.target) }
    }

    private fun AskEligibilityToOrchestratorRequest.toBotRequest() = AskEligibilityToOrchestratedBotRequest(
        data = data,
        action = action,
        metadata = metadata
    )
}
