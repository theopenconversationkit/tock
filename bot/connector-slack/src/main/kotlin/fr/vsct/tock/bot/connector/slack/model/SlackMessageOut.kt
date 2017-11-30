package fr.vsct.tock.bot.connector.slack.model


data class SlackMessageOut(val text: String,
                           val channel: String? = null) : SlackConnectorMessage()