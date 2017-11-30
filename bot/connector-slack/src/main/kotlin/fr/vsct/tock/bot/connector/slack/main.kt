package fr.vsct.tock.bot.connector.slack

import fr.vsct.tock.bot.engine.BotRepository
import fr.vsct.tock.bot.engine.ConnectorConfigurationRepository


fun addSlackConnector(applicationId: String, path: String, name: String, outToken1: String, outToken2: String, outToken3: String, baseUrl: String? = null) {
    ConnectorConfigurationRepository.addConfiguration(
            SlackConnectorProvider.newConfiguration(
                    applicationId,
                    path,
                    outToken1,
                    outToken2,
                    outToken3,
                    name,
                    baseUrl
                    ))
    BotRepository.registerConnectorProvider(SlackConnectorProvider)
}

