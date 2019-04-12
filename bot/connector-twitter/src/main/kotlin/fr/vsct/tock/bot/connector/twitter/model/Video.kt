package fr.vsct.tock.bot.connector.twitter.model

import com.fasterxml.jackson.annotation.JsonProperty

data class Video(
    @JsonProperty("video_type") val videoType: String
) : Media()