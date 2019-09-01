package fr.vsct.tock.bot.connector.twitter.model

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import fr.vsct.tock.bot.connector.twitter.json.MediaDeserializer
import fr.vsct.tock.bot.connector.twitter.json.OptionDeserializer
import fr.vsct.tock.bot.engine.message.Choice

@JsonDeserialize(using = OptionDeserializer::class)
abstract class AbstractOption {
    abstract fun toChoice(): Choice
}