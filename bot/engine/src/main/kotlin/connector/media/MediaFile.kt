/*
 * Copyright (C) 2017/2019 VSCT
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

package ai.tock.bot.connector.media

import ai.tock.bot.engine.action.SendAttachment.AttachmentType
import ai.tock.bot.engine.config.UploadedFilesService
import ai.tock.bot.engine.message.Attachment

/**
 * An uploaded file.
 */
data class MediaFile(
    val url: String,
    val name: String,
    val type: AttachmentType = UploadedFilesService.attachmentType(url)
) {

    internal fun toAttachment(): Attachment = Attachment(url, type)
}