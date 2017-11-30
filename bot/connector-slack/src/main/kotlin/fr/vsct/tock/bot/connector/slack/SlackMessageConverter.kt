package fr.vsct.tock.bot.connector.slack

import fr.vsct.tock.bot.connector.slack.model.SlackConnectorMessage
import fr.vsct.tock.bot.connector.slack.model.SlackMessageOut
import fr.vsct.tock.bot.engine.action.Action
import fr.vsct.tock.bot.engine.action.SendSentence
import mu.KotlinLogging


internal object SlackMessageConverter {

    val logger = KotlinLogging.logger {}

    fun toMessageOut(action: Action): SlackConnectorMessage? {
        logger.info { action.javaClass }
        return when (action) {
            is SendSentence ->
                if (action.hasMessage(SlackConnectorProvider.connectorType) ) {
                    action.message(SlackConnectorProvider.connectorType) as SlackConnectorMessage
                } else {
                    SlackMessageOut(action.stringText ?: "")
                }
            else -> {
                logger.warn { "Action $action not supported" }
                null
            }
        }
    }
}

