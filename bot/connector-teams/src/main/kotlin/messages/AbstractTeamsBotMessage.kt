package fr.vsct.tock.bot.connector.teams.messages

import fr.vsct.tock.bot.connector.ConnectorMessage
import fr.vsct.tock.bot.connector.ConnectorType
import fr.vsct.tock.bot.connector.teams.teamsConnectorType

abstract class TeamsBotMessage(val text: String?) : ConnectorMessage {

    override val connectorType: ConnectorType
        get() = teamsConnectorType
}