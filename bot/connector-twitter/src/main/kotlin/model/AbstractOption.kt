package ai.tock.bot.connector.twitter.model

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import ai.tock.bot.connector.twitter.json.MediaDeserializer
import ai.tock.bot.connector.twitter.json.OptionDeserializer
import ai.tock.bot.engine.message.Choice

@JsonDeserialize(using = OptionDeserializer::class)
abstract class AbstractOption {
    abstract fun toChoice(): Choice
}