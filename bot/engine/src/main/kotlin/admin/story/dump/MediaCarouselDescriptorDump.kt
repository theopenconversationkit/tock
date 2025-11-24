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

import ai.tock.bot.connector.media.MediaCarouselDescriptor
import ai.tock.bot.connector.media.MediaMessageType
import ai.tock.bot.connector.media.MediaMessageType.carousel

/**
 * The [MediaCarousel] descriptor.
 */
data class MediaCarouselDescriptorDump(val cards: List<MediaCardDescriptorDump>) : MediaMessageDescriptorDump {
    override val type: MediaMessageType = carousel

    constructor(media: MediaCarouselDescriptor) : this(media.cards.map { MediaCardDescriptorDump(it) })

    override fun toMedia(controller: StoryDefinitionConfigurationDumpController): MediaCarouselDescriptor = MediaCarouselDescriptor(cards.map { it.toMedia(controller) })
}
