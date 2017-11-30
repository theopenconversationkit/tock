package fr.vsct.tock.bot.connector.slack.model

import com.fasterxml.jackson.annotation.JsonIgnore
import fr.vsct.tock.bot.connector.ConnectorMessage
import fr.vsct.tock.bot.connector.ConnectorType
import fr.vsct.tock.bot.connector.slack.SlackConnectorProvider

abstract class SlackConnectorMessage : ConnectorMessage {

    override val connectorType: ConnectorType @JsonIgnore get() = SlackConnectorProvider.connectorType

}