package ai.tock.bot.definition

import ai.tock.bot.engine.BotBus
import ai.tock.bot.engine.action.Action

/**
 * Change or update answer before sending to the user
 * Need to be registered using [ai.tock.bot.engine.BotRepository.registerBotAnswerInterceptor].
 */
interface BotAnswerInterceptor {

    /**
     * Returns the replacement action.
     */
    fun handle(action: Action, bus: BotBus): Action = action

}