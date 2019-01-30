package fr.vsct.tock.bot.connector.twitter.model.outcoming

import com.fasterxml.jackson.annotation.JsonIgnore
import fr.vsct.tock.bot.connector.twitter.model.TwitterConnectorMessage
import fr.vsct.tock.bot.engine.user.PlayerId
import fr.vsct.tock.bot.engine.user.PlayerType

data class OutcomingEvent(val event: AbstractOutcomingEvent) : TwitterConnectorMessage() {
    @JsonIgnore
    fun playerId(playerType: PlayerType): PlayerId = event.playerId(playerType)
    @JsonIgnore
    fun recipientId(playerType: PlayerType): PlayerId = event.recipientId(playerType)
}