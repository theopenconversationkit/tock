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

package fr.vsct.tock.bot.connector.media

import fr.vsct.tock.bot.engine.message.GenericMessage
import fr.vsct.tock.bot.engine.message.GenericMessage.Companion.SUBTITLE_PARAM
import fr.vsct.tock.bot.engine.message.GenericMessage.Companion.TITLE_PARAM
import fr.vsct.tock.shared.mapNotNullValues


/**
 * A media card. At least one of [title], [subTitle] or [file] is not null.
 */
data class MediaCard(
    val title: CharSequence?,
    val subTitle: CharSequence?,
    val file: MediaFile?,
    val actions: List<MediaAction> = emptyList()
) : MediaMessage {

    override fun isValid(): Boolean = title != null || subTitle != null || file != null

    override fun toGenericMessage(): GenericMessage? =
        GenericMessage(
            choices = actions.map { it.toChoice() },
            texts = mapNotNullValues(
                TITLE_PARAM to title?.toString(),
                SUBTITLE_PARAM to subTitle?.toString()
            ),
            attachments = listOfNotNull(file?.toAttachment())
        )
}