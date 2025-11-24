/*
 * Copyright (C) 2017/2025 SNCF Connect & Tech
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

package ai.tock.bot.connector.twitter.json

import ai.tock.bot.connector.twitter.model.Image
import ai.tock.bot.connector.twitter.model.Media
import ai.tock.bot.connector.twitter.model.Video
import ai.tock.shared.jackson.JacksonDeserializer
import ai.tock.shared.jackson.read
import ai.tock.shared.jackson.readValue
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import mu.KotlinLogging

internal class MediaDeserializer : JacksonDeserializer<Media>() {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override fun deserialize(
        jp: JsonParser,
        ctxt: DeserializationContext,
    ): Media? {
        data class MediaFields(
            var image: String? = null,
            var video: String? = null,
            var imageType: String? = null,
            var width: Int? = null,
            var height: Int? = null,
            var videoType: String? = null,
            var other: EmptyJson? = null,
        )

        val (image, video, imageType, width, height, videoType) =
            jp.read<MediaFields> { fields, name ->
                with(fields) {
                    when (name) {
                        "image" -> image = jp.readValue()
                        "video" -> video = jp.readValue()
                        "image_type" -> imageType = jp.readValue()
                        "w" -> width = jp.readValue()
                        "h" -> height = jp.readValue()
                        "video_type" -> videoType = jp.readValue()
                        else -> other = jp.readUnknownValue()
                    }
                }
            }

        return if (image != null) {
            Image(imageType!!, width!!, height!!)
        } else if (video != null) {
            Video(videoType!!)
        } else {
            logger.error { "unknown media type" }
            null
        }
    }
}
