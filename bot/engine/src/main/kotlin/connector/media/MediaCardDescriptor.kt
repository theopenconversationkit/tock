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

package ai.tock.bot.connector.media

import ai.tock.bot.connector.media.MediaMessageType.card
import ai.tock.bot.engine.BotBus
import ai.tock.translator.I18nLabelValue

/**
 * The [MediaCard] descriptor.
 */
data class MediaCardDescriptor(
    val title: I18nLabelValue?,
    val subTitle: I18nLabelValue?,
    val file: MediaFileDescriptor?,
    val actions: List<MediaActionDescriptor> = emptyList(),
    val fillCarousel: Boolean = false
) : MediaMessageDescriptor {

    override val type: MediaMessageType = card

    override fun toMessage(bus: BotBus): MediaCard =
        MediaCard(
            bus.translate(title).takeUnless { it.isBlank() },
            bus.translate(subTitle).takeUnless { it.isBlank() },
            file?.toMessage(bus),
            actions.map { it.toMessage(bus) }
        )

    override fun checkValidity(): Boolean = title != null || subTitle != null || file != null
}
