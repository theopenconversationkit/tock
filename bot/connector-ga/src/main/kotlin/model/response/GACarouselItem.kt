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

package ai.tock.bot.connector.ga.model.response

import ai.tock.bot.engine.action.SendAttachment
import ai.tock.bot.engine.message.Attachment
import ai.tock.bot.engine.message.GenericElement
import ai.tock.shared.mapNotNullValues

data class GACarouselItem(
    val optionInfo: GAOptionInfo,
    val title: String,
    val description: String? = null,
    val image: GAImage? = null,
) {
    fun toGenericElement(): GenericElement {
        return GenericElement(
            choices = listOf(optionInfo.toChoice()),
            texts =
                mapNotNullValues(
                    GACarouselItem::title.name to title,
                    GACarouselItem::description.name to description,
                ),
            attachments =
                image?.url
                    ?.let { listOf(Attachment(it, SendAttachment.AttachmentType.image)) }
                    ?: emptyList(),
        )
    }
}
