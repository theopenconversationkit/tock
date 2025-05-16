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

package ai.tock.bot.connector.messenger.model.send

import ai.tock.bot.engine.action.SendAttachment.AttachmentType.image
import ai.tock.bot.engine.message.GenericElement
import ai.tock.shared.mapNotNullValues
import ai.tock.shared.security.TockObfuscatorService.obfuscate
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * List or generic template subElements.
 */
data class Element(
    val title: String,
    @JsonProperty("image_url") val imageUrl: String? = null,
    val subtitle: String? = null,
    val buttons: List<Button>? = null
) {

    internal constructor(
        title: CharSequence,
        subtitle: CharSequence?,
        imageUrl: String?,
        buttons: List<Button>?
    ) : this(title.toString(), imageUrl, subtitle?.toString(), buttons?.takeUnless { it.isEmpty() })

    fun toGenericElement(): GenericElement {
        return GenericElement(
            choices = buttons?.map { it.toChoice() } ?: emptyList(),
            texts = mapNotNullValues(
                Element::title.name to title,
                Element::subtitle.name to subtitle
            ),
            attachments = imageUrl
                ?.let { listOf(ai.tock.bot.engine.message.Attachment(imageUrl, image)) }
                ?: emptyList()
        )
    }

    fun obfuscate(): Element {
        return Element(obfuscate(title)!!, imageUrl, obfuscate(subtitle), buttons)
    }
}
