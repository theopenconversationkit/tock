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

package ai.tock.bot.admin.story.dump

import ai.tock.bot.connector.media.MediaFileDescriptor
import ai.tock.bot.engine.action.SendAttachment.AttachmentType
import ai.tock.bot.engine.config.UploadedFilesService
import ai.tock.bot.engine.config.UploadedFilesService.fileId
import ai.tock.bot.engine.config.UploadedFilesService.getFileContentFromId
import ai.tock.bot.engine.config.UploadedFilesService.uploadFile
import ai.tock.shared.Dice
import ai.tock.translator.I18nLabel

/**
 * A file descriptor.
 */
class MediaFileDescriptorDump(
    val suffix: String,
    val name: String,
    val data: ByteArray?,
    val id: String = Dice.newId(),
    val type: AttachmentType = UploadedFilesService.attachmentType(suffix),
    val externalUrl: String? = null,
    val description: I18nLabel?,
) {
    constructor(file: MediaFileDescriptor) :
        this(
            file.suffix,
            file.name,
            if (file.externalUrl == null) getFileContentFromId(fileId(file.id, file.suffix)) else null,
            file.id,
            file.type,
            file.externalUrl,
            file.description,
        )

    fun toFile(controller: StoryDefinitionConfigurationDumpController): MediaFileDescriptor? =
        if (externalUrl != null) {
            MediaFileDescriptor(
                suffix,
                name,
                id,
                type,
                externalUrl,
                description,
            )
        } else {
            val content = getFileContentFromId(fileId(id, suffix))
            if (content != null && content.size == data?.size) {
                MediaFileDescriptor(
                    suffix,
                    name,
                    id,
                    type,
                    description = description,
                )
            } else if (data != null) {
                uploadFile(controller.targetNamespace, name, data, description = description)
            } else {
                null
            }
        }
}
