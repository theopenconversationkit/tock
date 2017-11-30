package fr.vsct.tock.bot.connector.slack

import fr.vsct.tock.bot.connector.slack.model.SlackMessageIn
import fr.vsct.tock.bot.engine.action.SendSentence
import fr.vsct.tock.bot.engine.event.Event
import fr.vsct.tock.bot.engine.user.PlayerId
import mu.KotlinLogging


internal object SlackRequestConverter {

    private val logger = KotlinLogging.logger {}

    fun toEvent(message: SlackMessageIn, applicationId: String): Event? {
        val safeMessage = message
        safeMessage.text = message.getRealMessage()
        return SendSentence(
                PlayerId(message.user_id),
                applicationId,
                PlayerId(""),
                safeMessage.text,
                mutableListOf(safeMessage)
        )
    }
}