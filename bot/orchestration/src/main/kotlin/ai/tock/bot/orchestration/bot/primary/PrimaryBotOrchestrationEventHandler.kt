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

package ai.tock.bot.orchestration.bot.primary

import ai.tock.bot.engine.BotBus
import ai.tock.bot.orchestration.shared.AvailableOrchestrationResponse

interface PrimaryBotOrchestrationEventHandler {
    /**
     * Event when the primary bot delegate conversation to a secondary bot
     * @param bus
     * @param orchestrationResponse chosen response (secondary bot more relevant)
     */
    fun onStarOrchestration(
        bus: BotBus,
        orchestrationResponse: AvailableOrchestrationResponse,
    )

    /**
     * Event when a Intent in stopOrchestrationIntentList (primary bot configuration) is enabled
     *
     * @param bus
     * @param orchestration current orchestration
     */
    fun onStopOrchestration(
        bus: BotBus,
        orchestration: Orchestration,
    ): ComeBackFromSecondary

    /**
     * Event when a Intent in noOrchestrationIntentList (primary bot configuration) is enabled
     *
     * @param bus
     * @param orchestration current orchestration
     */
    fun onNoOrchestration(
        bus: BotBus,
        orchestration: Orchestration,
    ): ComeBackFromSecondary

    /**
     * Event when takeBackOrchestration() (primary bot configuration) is true
     *
     * @param bus
     * @param orchestration current orchestration
     */
    fun onTakeBackOrchestration(
        bus: BotBus,
        orchestration: Orchestration,
    ): ComeBackFromSecondary
}
