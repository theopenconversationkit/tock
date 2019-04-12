package fr.vsct.tock.bot.connector.twitter.model

import com.fasterxml.jackson.annotation.JsonProperty

data class Image(
    @JsonProperty("image_type") val imageType: String,
    @JsonProperty("w") val width: Int,
    @JsonProperty("h") val height: Int
) : Media()