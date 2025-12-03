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

import ai.tock.bot.engine.user.PlayerId
import ai.tock.bot.orchestration.shared.AskEligibilityToOrchestratedBotRequest
import ai.tock.bot.orchestration.shared.NoOrchestrationStatus.END
import ai.tock.bot.orchestration.shared.NoOrchestrationStatus.NOT_AVAILABLE
import ai.tock.bot.orchestration.shared.OrchestrationMetaData
import ai.tock.bot.orchestration.shared.OrchestrationTargetedBot
import ai.tock.bot.orchestration.shared.ResumeOrchestrationRequest
import ai.tock.bot.orchestration.shared.SecondaryBotNoResponse
import ai.tock.bot.orchestration.shared.SecondaryBotResponse

abstract class OrchestratedRuntimeBot(
    open val target: OrchestrationTargetedBot,
) {
    open fun askOrchestration(request: AskEligibilityToOrchestratedBotRequest): SecondaryBotResponse =
        SecondaryBotNoResponse(
            status = NOT_AVAILABLE,
            metaData = request.metadata ?: OrchestrationMetaData(PlayerId("unknown"), target.botId, PlayerId("orchestrator")),
        )

    open fun resumeOrchestration(request: ResumeOrchestrationRequest): SecondaryBotResponse =
        SecondaryBotNoResponse(
            status = END,
            metaData = request.metadata,
        )
}
