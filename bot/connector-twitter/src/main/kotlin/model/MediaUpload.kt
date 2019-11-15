package ai.tock.bot.connector.twitter.model

import com.fasterxml.jackson.annotation.JsonProperty

data class MediaUpload(
    @JsonProperty("media_id") val mediaId: Long,
    @JsonProperty("media_id_string") val mediaIdString: String,
    val size: Long?,
    @JsonProperty("expires_after_secs") val expiresAfterSecs: Long,
    val media: Media?
)