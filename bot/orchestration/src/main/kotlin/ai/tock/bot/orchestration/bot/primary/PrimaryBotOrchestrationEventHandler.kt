package ai.tock.bot.orchestration.bot.primary

import ai.tock.bot.engine.BotBus
import ai.tock.bot.orchestration.shared.AvailableOrchestrationResponse

interface PrimaryBotOrchestrationEventHandler {
    /**
     * Event when the primary bot delegate conversation to a secondary bot
     * @param bus
     * @param orchestrationResponse chosen response (secondary bot more relevant)
     */
    fun onStarOrchestration(bus: BotBus, orchestrationResponse: AvailableOrchestrationResponse)

    /**
     * Event when a Intent in stopOrchestrationIntentList (primary bot configuration) is enabled
     *
     * @param bus
     * @param orchestration current orchestration
     */
    fun onStopOrchestration(bus: BotBus, orchestration: Orchestration): ComeBackFromSecondary

    /**
     * Event when a Intent in noOrchestrationIntentList (primary bot configuration) is enabled
     *
     * @param bus
     * @param orchestration current orchestration
     */
    fun onNoOrchestration(bus: BotBus, orchestration: Orchestration): ComeBackFromSecondary

    /**
     * Event when takeBackOrchestration() (primary bot configuration) is true
     *
     * @param bus
     * @param orchestration current orchestration
     */
    fun onTakeBackOrchestration(bus: BotBus, orchestration: Orchestration): ComeBackFromSecondary
}
