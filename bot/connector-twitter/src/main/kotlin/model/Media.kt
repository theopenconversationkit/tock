package ai.tock.bot.connector.twitter.model

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import ai.tock.bot.connector.twitter.json.MediaDeserializer

@JsonDeserialize(using = MediaDeserializer::class)
abstract class Media