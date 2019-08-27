package fr.vsct.tock.bot.definition

import fr.vsct.tock.bot.engine.BotBus
import fr.vsct.tock.bot.engine.action.Action

/**
 * Change or update answer before sending to the user
 * Need to be registered using [fr.vsct.tock.bot.engine.BotRepository.registerBotAnswerInterceptor].
 */
interface BotAnswerInterceptor {

    /**
     * Returns the replacement action.
     */
    fun handle(action: Action, bus: BotBus): Action = action

}