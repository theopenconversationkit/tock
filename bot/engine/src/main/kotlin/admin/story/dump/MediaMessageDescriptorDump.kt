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
import ai.tock.bot.connector.media.MediaCardDescriptor
import ai.tock.bot.connector.media.MediaCarouselDescriptor
import ai.tock.bot.connector.media.MediaMessageDescriptor
import ai.tock.bot.connector.media.MediaMessageType

interface MediaMessageDescriptorDump {
    val type: MediaMessageType

    companion object {
        fun toDump(message: MediaMessageDescriptor?): MediaMessageDescriptorDump? =
            when (message) {
                null -> null
                is MediaCardDescriptor -> MediaCardDescriptorDump(message)
                is MediaActionDescriptor -> MediaActionDescriptorDump(message)
                is MediaCarouselDescriptor -> MediaCarouselDescriptorDump(message)
                else -> error("unknown message $message")
            }
    }

    fun toMedia(controller: StoryDefinitionConfigurationDumpController): MediaMessageDescriptor
}
