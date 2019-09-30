package ai.tock.bot.connector.twitter.model.outcoming

import com.fasterxml.jackson.annotation.JsonIgnore
import ai.tock.bot.connector.twitter.model.AttachmentData
import ai.tock.bot.connector.twitter.model.TwitterConnectorMessage
import ai.tock.bot.engine.message.GenericMessage
import ai.tock.bot.engine.user.PlayerId
import ai.tock.bot.engine.user.PlayerType

data class OutcomingEvent(val event: AbstractOutcomingEvent, @JsonIgnore val attachmentData: AttachmentData? = null) :
    TwitterConnectorMessage() {
    override fun toGenericMessage(): GenericMessage? =
                    event.toGenericMessage()

    @JsonIgnore
    fun playerId(playerType: PlayerType): PlayerId = event.playerId(playerType)

    @JsonIgnore
    fun recipientId(playerType: PlayerType): PlayerId = event.recipientId(playerType)
}