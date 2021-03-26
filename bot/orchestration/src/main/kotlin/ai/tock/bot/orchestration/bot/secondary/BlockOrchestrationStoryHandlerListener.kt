package ai.tock.bot.orchestration.bot.secondary

import ai.tock.bot.definition.IntentAware
import ai.tock.bot.definition.StoryHandler
import ai.tock.bot.definition.StoryHandlerListener
import ai.tock.bot.engine.BotBus

const val DEFAULT_ORCHESTRATION_BLOCKED_MESSAGE = "This action is not allowed in orchestrated path."

sealed class OrchestrationBlockedIntent(private val intents: List<IntentAware>, internal val message: CharSequence) {
    protected abstract fun concerns(primaryBot: String): Boolean
    fun applyTo(primaryBot: String, intent: IntentAware?) =
        concerns(primaryBot) && intents.any { it.wrap(intent?.wrappedIntent()) }
}

class BlockedIntentRegardlessPrimary(
    intents: List<IntentAware>,
    message: CharSequence = DEFAULT_ORCHESTRATION_BLOCKED_MESSAGE
) : OrchestrationBlockedIntent(intents, message) {

    constructor(
        vararg intents: IntentAware,
        message: CharSequence = DEFAULT_ORCHESTRATION_BLOCKED_MESSAGE
    ) : this(intents.asList(), message)

    override fun concerns(primaryBot: String) = true
}

class BlockedIntentForPrimary(
    private val primaryBotId: String,
    intents: List<IntentAware>,
    message: CharSequence = DEFAULT_ORCHESTRATION_BLOCKED_MESSAGE
) : OrchestrationBlockedIntent(intents, message) {

    constructor(
        primaryBotId: String,
        vararg intents: IntentAware,
        message: CharSequence = DEFAULT_ORCHESTRATION_BLOCKED_MESSAGE
    ) : this(primaryBotId, intents.asList(), message)

    override fun concerns(primaryBot: String) = primaryBot == primaryBotId
}

class BlockOrchestrationStoryHandlerListener(private vararg val restrictions: OrchestrationBlockedIntent) :
    StoryHandlerListener {

    override fun startAction(botBus: BotBus, handler: StoryHandler): Boolean {
        botBus.action.metadata.orchestratedBy?.let { primaryBot ->
            restrictions.firstOrNull { it.applyTo(primaryBot, botBus.intent) }?.let {
                botBus.end(it.message)
                return false
            }
        }

        return super.startAction(botBus, handler)
    }
}
