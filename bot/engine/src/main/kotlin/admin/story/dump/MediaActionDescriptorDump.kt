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

import ai.tock.bot.connector.media.MediaActionDescriptor
import ai.tock.bot.connector.media.MediaMessageType
import ai.tock.bot.connector.media.MediaMessageType.action
import ai.tock.translator.I18nLabelValue

/**
 * Descriptor for [MediaAction].
 */
data class MediaActionDescriptorDump(val title: I18nLabelValue, var url: String? = null) : MediaMessageDescriptorDump {

    override val type: MediaMessageType = action

    constructor(media: MediaActionDescriptor) : this(
        media.title,
        media.url
    )

    override fun toMedia(controller: StoryDefinitionConfigurationDumpController): MediaActionDescriptor =
        MediaActionDescriptor(title.withNamespace(controller.targetNamespace), url)
}
