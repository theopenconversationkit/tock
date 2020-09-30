/*
 * Copyright (C) 2017/2020 e-voyageurs technologies
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

package ai.tock.bot.connector.web.send

import ai.tock.bot.connector.media.MediaFile
import ai.tock.bot.engine.config.UploadedFilesService
import ai.tock.bot.engine.message.Attachment
import ai.tock.bot.engine.message.GenericMessage
import ai.tock.shared.mapNotNullValues

data class WebImage(
    val file: MediaFile,
    val title: CharSequence
) {
    fun toGenericMessage(): GenericMessage? {
        return GenericMessage(
            texts = mapNotNullValues(
                GenericMessage.TITLE_PARAM to title.toString(),
            ),
            attachments = listOf(Attachment(file.url, UploadedFilesService.attachmentType(file.url)))
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is WebImage) return false
        if (title != other.title) return false
        if (file != other.file) return false

        return true
    }

    override fun hashCode(): Int {
        var result = title.hashCode()
        result = 31 * result + file.hashCode()
        return result
    }
}
