package fr.vsct.tock.bot.connector.twitter.model

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import fr.vsct.tock.bot.connector.twitter.json.MediaDeserializer

@JsonDeserialize(using = MediaDeserializer::class)
abstract class Media