package ai.tock.bot.connector

import ai.tock.bot.admin.bot.BotApplicationConfiguration
import ai.tock.bot.engine.ConnectorController

/**
 * Ability from any module to add a service to a ConnectorController
 * Need to be registered using [ai.tock.bot.engine.BotRepository.registerConnectorService].
 */
interface ConnectorService {

    /**
     * Install service.
     */
    fun install(controller: ConnectorController, configuration: BotApplicationConfiguration)
}
