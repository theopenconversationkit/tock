package fr.vsct.tock.bot.connector.twitter

import fr.vsct.tock.bot.connector.twitter.model.MessageCreate
import fr.vsct.tock.bot.connector.twitter.model.MessageData
import fr.vsct.tock.bot.connector.twitter.model.Recipient
import fr.vsct.tock.bot.connector.twitter.model.outcoming.DirectMessageOutcomingEvent
import fr.vsct.tock.bot.connector.twitter.model.outcoming.OutcomingEvent
import fr.vsct.tock.bot.engine.action.Action
import fr.vsct.tock.bot.engine.action.SendSentence
import mu.KotlinLogging

internal object TwitterMessageConverter {

    val logger = KotlinLogging.logger {}

    fun toOutcomingEvent(action: Action): OutcomingEvent? {
        return when (action) {
            is SendSentence ->
                if (action.hasMessage(TwitterConnectorProvider.connectorType)) {
                    action.message(TwitterConnectorProvider.connectorType) as OutcomingEvent
                } else {
                    action.stringText?.run {
                        if (isBlank()) null else OutcomingEvent(
                            DirectMessageOutcomingEvent(
                                MessageCreate(
                                    target = Recipient(action.recipientId.id),
                                    sourceAppId = action.applicationId,
                                    senderId = action.playerId.id,
                                    messageData = MessageData(this)
                                )
                            )
                        )
                    }
                }
            else -> {
                logger.warn { "Action $action not supported" }
                null
            }
        }
    }
}