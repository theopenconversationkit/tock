/*
 * Copyright (C) 2017/2021 e-voyageurs technologies
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ai.tock.bot.connector.whatsapp.cloud.model.webhook.message.content

import com.fasterxml.jackson.annotation.JsonProperty

data class Referral(
    @JsonProperty("source_url") val sourceUrl: String,
    @JsonProperty("source_type") val sourceType: String,
    @JsonProperty("source_id") val sourceId: String,
    val ref: String = "",
    val headline: String,
    val body: String,
    @JsonProperty("media_type") val mediaType: MediaType,
    @JsonProperty("image_url") val imageUrl: String?,
    @JsonProperty("video_url") val videoUrl: String?,
    @JsonProperty("thumbnail_url") val thumbnailUrl: String?,
    @JsonProperty("ctwa_clid") val ctwaClid: String
)

enum class MediaType {
    IMAGE, VIDEO;

    companion object {
        fun fromString(type: String): MediaType = when (type.lowercase()) {
            "image" -> IMAGE
            "video" -> VIDEO
            else -> throw IllegalArgumentException("Unsupported media type: $type")
        }
    }
}
