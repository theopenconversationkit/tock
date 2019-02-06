package fr.vsct.tock.bot.connector.twitter.model

import com.fasterxml.jackson.annotation.JsonProperty

data class Entities(
    val hashtags: List<Hashtag>,
    @JsonProperty("user_mentions") val mentions: List<Mention>,
    val urls: List<Url>,
    val symbols: List<Symbol>
)