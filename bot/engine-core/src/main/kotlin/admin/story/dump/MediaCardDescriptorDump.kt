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

import ai.tock.bot.connector.media.MediaCardDescriptor
import ai.tock.bot.connector.media.MediaMessageType
import ai.tock.bot.connector.media.MediaMessageType.card
import ai.tock.translator.I18nLabelValue

/**
 * The [MediaCard] descriptor.
 */
data class MediaCardDescriptorDump(
    val title: I18nLabelValue?,
    val subTitle: I18nLabelValue?,
    val file: MediaFileDescriptorDump?,
    val actions: List<MediaActionDescriptorDump> = emptyList(),
) : MediaMessageDescriptorDump {
    override val type: MediaMessageType = card

    constructor(media: MediaCardDescriptor) : this(
        media.title,
        media.subTitle,
        media.file?.let { MediaFileDescriptorDump(it) },
        media.actions.map { MediaActionDescriptorDump(it) },
    )

    override fun toMedia(controller: StoryDefinitionConfigurationDumpController): MediaCardDescriptor =
        MediaCardDescriptor(
            title?.withNamespace(controller.targetNamespace),
            subTitle?.withNamespace(controller.targetNamespace),
            file?.toFile(controller),
            actions.map { it.toMedia(controller) },
        )
}
