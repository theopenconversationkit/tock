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

import ai.tock.bot.connector.ConnectorType
import ai.tock.bot.definition.BotAnswerInterceptor
import ai.tock.bot.definition.IntentAware
import ai.tock.bot.definition.StoryHandler
import ai.tock.bot.definition.StoryHandlerListener
import ai.tock.bot.engine.BotBus
import ai.tock.bot.engine.action.Action
import ai.tock.bot.orchestration.orchestrator.OrchestratorService
import ai.tock.bot.orchestration.shared.AskEligibilityToOrchestratorRequest
import ai.tock.bot.orchestration.shared.AvailableOrchestrationResponse
import ai.tock.bot.orchestration.shared.EligibilityOrchestrationResponse
import ai.tock.bot.orchestration.shared.OrchestrationMetaData
import ai.tock.bot.orchestration.shared.OrchestrationResponse
import ai.tock.bot.orchestration.shared.OrchestrationTargetedBot
import ai.tock.bot.orchestration.shared.ResumeOrchestrationRequest
import ai.tock.bot.orchestration.shared.SecondaryBotAction
import ai.tock.shared.longProperty
import mu.KotlinLogging
import java.time.Duration

private val logger = KotlinLogging.logger {}

private val orchestrationLockTTL: Duration = Duration.ofMinutes(
    longProperty(
        "tock_orchestration_lock_ttl_in_min",
        60
    )
)

/**
 * Should pass control to orchestrator ?
 */
class RunOrchestrationStoryListener(
    private val configuration: PrimaryBotConfiguration,
    private val orchestrator: OrchestratorService,
    private val orchestrationRepository: OrchestrationRepository = MongoOrchestrationRepository,
    private val orchestrationEnabled: (() -> Boolean) = { true }
) : StoryHandlerListener {

    override fun startAction(botBus: BotBus, handler: StoryHandler): Boolean = with(botBus) {
        val currentOrchestration = orchestrationRepository.get(action.playerId)

        return when {
            botBus.blockHandoverToSecondaryBot -> true
            currentOrchestration != null -> resumeOrchestration(currentOrchestration)
            intent.inStartOrchestrationList() && orchestrationEnabled() -> startOrchestration()
            else -> true // no need for orchestration, let's continue on this bot
        }
    }

    private fun BotBus.startOrchestration(): Boolean {
        logger.info { "Try to start an orchestration for intent ${intent?.wrappedIntent()?.name ?: "???"}" }

        val botAction: SecondaryBotAction? = SecondaryBotAction.from(action, botDefinition.botId)
        if (botAction == null) {
            logger.info { "Failed to start orchestration caused by an unhandled Tock action ${action.javaClass.name}" }
            return true
        }

        return when (val eligibility = callOrchestrationForEligibility(botAction)) {
            is EligibilityOrchestrationResponse -> handleEligibleOrchestration(eligibility, botAction)
            else -> {
                logger.info { "Fail to start an orchestration caused by a ${eligibility?.javaClass?.name ?: "null"} eligibility from the orchestrator" }
                true
            }
        }
    }

    private fun BotBus.handleEligibleOrchestration(
        eligibility: EligibilityOrchestrationResponse,
        botAction: SecondaryBotAction
    ): Boolean {
        logger.info { "Eligibility with ${eligibility.targetBot} : ${eligibility.botResponse.indice}" }

        val response = callOrchestrationForFirstAction(eligibility, botAction)
        return when (response) {
            is AvailableOrchestrationResponse -> {
                logger.info { "Start an orchestration with ${response.targetBot}" }
                orchestrationRepository.create(
                    action.playerId,
                    response.botResponse.metaData,
                    response.targetBot,
                    response.botResponse.actions
                )
                configuration.primaryBotOrchestrationEventHandler.onStarOrchestration(this, response)

                response.botResponse.actions.forEach {
                    send(it.toAction(response.botResponse.metaData))
                }
                end()
                false
            }
            else -> {
                logger.info { "Fail to start an orchestration caused by a ${response.javaClass.name ?: "null"} response from the orchestrator" }
                true
            }
        }
    }

    private fun BotBus.callOrchestrationForFirstAction(
        eligibility: EligibilityOrchestrationResponse,
        botAction: SecondaryBotAction
    ): OrchestrationResponse {
        return orchestrator.resumeOrchestration(
            ResumeOrchestrationRequest(
                targetBot = eligibility.targetBot,
                action = botAction,
                metadata = OrchestrationMetaData(playerId = userId, applicationId = applicationId, recipientId = botId)
            )
        )
    }

    private fun BotBus.callOrchestrationForEligibility(botAction: SecondaryBotAction): OrchestrationResponse? {
        return configuration.getOrchestrationData(this)?.let { data ->
            orchestrator.askOrchestration(
                AskEligibilityToOrchestratorRequest(
                    eligibleTargetBots = targetConnectorType.getEligibleBots(),
                    data = data,
                    action = botAction,
                    metadata = OrchestrationMetaData(
                        playerId = userId,
                        applicationId = applicationId,
                        recipientId = botId
                    )
                )
            )
        }
    }

    private fun BotBus.resumeOrchestration(orchestration: Orchestration): Boolean {
        logger.info { "Try to resume the orchestration to ${orchestration.targetBot}" }

        if (!orchestration.locked) {
            if (intent.inStopOrchestrationList()) {
                logger.info { "End of the orchestration caused by the ${intent?.wrappedIntent()?.name ?: "???"} intent" }
                orchestrationRepository.end(orchestration.playerId)

                return configuration.primaryBotOrchestrationEventHandler.onStopOrchestration(this, orchestration) == ComeBackFromSecondary.EXECUTE_INITIAL_STORY
            }

            if (intent.inNoOrchestrationList()) {
                logger.info { "End of the orchestration caused by the ${intent?.wrappedIntent()?.name ?: "???"} intent" }
                orchestrationRepository.end(orchestration.playerId)

                return configuration.primaryBotOrchestrationEventHandler.onNoOrchestration(this, orchestration) == ComeBackFromSecondary.EXECUTE_INITIAL_STORY
            }

            if (configuration.takeBackOrchestration?.invoke(this) == true) {
                logger.info { "End of the orchestration caused by take back" }
                orchestrationRepository.end(orchestration.playerId)

                return configuration.primaryBotOrchestrationEventHandler.onTakeBackOrchestration(this, orchestration) == ComeBackFromSecondary.EXECUTE_INITIAL_STORY
            }
        }

        val botAction: SecondaryBotAction? = SecondaryBotAction.from(action, botDefinition.botId)
        if (botAction == null) {
            logger.info { "End of the orchestration caused by an unhandled Tock action ${action.javaClass.name}" }
            orchestrationRepository.end(orchestration.playerId)
            send("Damn, the conversation with ${orchestration.targetBot.botLabel} was interrupted.")
            return true
        }

        val response = orchestrator.resumeOrchestration(
            ResumeOrchestrationRequest(
                targetBot = orchestration.targetBot,
                action = botAction,
                metadata = OrchestrationMetaData(playerId = userId, applicationId = applicationId, recipientId = botId)
            )
        )
        return when (response) {
            is AvailableOrchestrationResponse -> {
                logger.info { "Resume the orchestration to ${orchestration.targetBot}" }
                response.botResponse.actions.forEach { secondaryBotAction ->
                    orchestrationRepository.update(orchestration.id, secondaryBotAction)
                    send(secondaryBotAction.toAction(response.botResponse.metaData))
                }
                end()
                false
            }
            else -> {
                logger.info { "End of the orchestration caused by a ${response.javaClass.name} response from the orchestrator" }
                orchestrationRepository.end(orchestration.playerId)

                if (orchestration.targetBot.fallbackStory != null) {
                    handleAndSwitchStory(orchestration.targetBot.fallbackStory)
                } else {
                    end("Damn, the conversation with ${orchestration.targetBot.botLabel} was interrupted.")
                    handleAndSwitchStory(configuration.comebackStory)
                }
                false
            }
        }
    }

    private fun ConnectorType.getEligibleBots(): List<OrchestrationTargetedBot> =
        configuration.getEligibleTargetBots(this)

    private fun IntentAware?.inStartOrchestrationList(): Boolean =
        this?.wrappedIntent() in configuration.startOrchestrationIntentList

    private fun IntentAware?.inStopOrchestrationList(): Boolean =
        this?.wrappedIntent() in configuration.stopOrchestrationIntentList

    private fun IntentAware?.inNoOrchestrationList(): Boolean =
        this?.wrappedIntent() in configuration.noOrchestrationIntentList
}

var BotBus.blockHandoverToSecondaryBot: Boolean
    get() = userTimeline.userState.getFlag("block_handover_to_secondary_bot")?.toBoolean() ?: false
    set(value) {
        userTimeline.userState.setFlag("block_handover_to_secondary_bot", orchestrationLockTTL, value.toString())
    }

var BotBus.blockTakeOverFromPrimaryBot: Boolean
    get() = userTimeline.userState.getFlag("block_take_over_from_primary_bot")?.toBoolean() ?: false
    set(value) {
        userTimeline.userState.setFlag("block_take_over_from_primary_bot", orchestrationLockTTL, value.toString())
    }

/**
 * This interceptor fills secondary bot response actions with orchestration information.
 * It must be registered by secondary bot.
 */
internal class OrchestrationSecondaryBotResponseInterceptor() : BotAnswerInterceptor {
    override fun handle(action: Action, bus: BotBus): Action {
        action.metadata.orchestrationLock = bus.blockTakeOverFromPrimaryBot
        return action
    }
}
