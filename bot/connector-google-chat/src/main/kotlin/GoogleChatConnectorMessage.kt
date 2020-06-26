package ai.tock.bot.connector.googlechat

import ai.tock.bot.connector.ConnectorMessage
import ai.tock.bot.connector.ConnectorType
import ai.tock.bot.connector.googlechat.builder.googleChatConnectorType
import com.fasterxml.jackson.annotation.JsonIgnore
import com.google.api.services.chat.v1.model.Message


abstract class GoogleChatConnectorMessage : ConnectorMessage {

    override val connectorType: ConnectorType @JsonIgnore get() = googleChatConnectorType

    abstract fun toGoogleMessage(): Message

}
