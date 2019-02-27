package fr.vsct.tock.bot.connector.teams.messages

import fr.vsct.tock.bot.connector.ConnectorMessage
import fr.vsct.tock.bot.connector.ConnectorType
import fr.vsct.tock.bot.connector.teams.teamsConnectorType

abstract class TeamsBotMessage(val text: String?) : ConnectorMessage {

    override val connectorType: ConnectorType
        get() = teamsConnectorType

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TeamsBotMessage) return false

        if (text != other.text) return false

        return true
    }

    override fun hashCode(): Int {
        return text?.hashCode() ?: 0
    }


}