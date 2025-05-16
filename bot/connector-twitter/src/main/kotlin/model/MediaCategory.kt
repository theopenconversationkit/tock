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

package ai.tock.bot.connector.twitter.model

import ai.tock.bot.connector.media.MediaFile
import ai.tock.bot.connector.twitter.model.MediaCategory.GIF
import ai.tock.bot.connector.twitter.model.MediaCategory.IMAGE
import ai.tock.bot.connector.twitter.model.MediaCategory.VIDEO
import ai.tock.bot.engine.action.SendAttachment.AttachmentType.image
import ai.tock.bot.engine.action.SendAttachment.AttachmentType.video

enum class MediaCategory(val mediaCategory: String) {
    GIF("dm_gif"),
    IMAGE("dm_image"),
    VIDEO("dm_video")
}

fun MediaFile.toMediaCategory(): MediaCategory? =
    when (type) {
        image -> if (name.endsWith(".gif", true)) GIF else IMAGE
        video -> VIDEO
        else -> null
    }
