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

import ai.tock.bot.engine.message.GenericElement
import ai.tock.bot.engine.message.GenericMessage

/**
 * A list of [MediaCard].
 */
data class MediaCarousel(val cards: List<MediaCard>) : MediaMessage {
    override fun checkValidity(): Boolean = cards.isNotEmpty()

    override fun toGenericMessage(): GenericMessage? = GenericMessage(subElements = cards.mapNotNull { it.toGenericMessage() }.map { GenericElement(it) })
}
