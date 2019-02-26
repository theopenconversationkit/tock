package fr.vsct.tock.bot.connector.teams.messages

import com.microsoft.bot.schema.models.CardAction

class TeamsBotTextMessage(text: String) : TeamsBotMessage(text)

class TeamsCardAction(
    val actionTitle: String,
    val buttons: List<CardAction>): TeamsBotMessage(null)