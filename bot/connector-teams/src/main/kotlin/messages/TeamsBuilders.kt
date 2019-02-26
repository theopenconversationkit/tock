package fr.vsct.tock.bot.connector.teams.messages

import com.microsoft.bot.schema.models.CardAction
import fr.vsct.tock.bot.connector.teams.teamsConnectorType
import fr.vsct.tock.bot.engine.BotBus
import fr.vsct.tock.bot.engine.I18nTranslator

fun BotBus.withTeams(messageProvider: () -> TeamsBotMessage): BotBus {
    return withMessage(teamsConnectorType, messageProvider)
}

fun I18nTranslator.teamsMessage(
    text: String
) : TeamsBotTextMessage = TeamsBotTextMessage(text)

fun I18nTranslator.teamsMessageWithButtonCard(
    urlText: String,
    links: List<CardAction>
) : TeamsCardAction = TeamsCardAction(urlText, links)
