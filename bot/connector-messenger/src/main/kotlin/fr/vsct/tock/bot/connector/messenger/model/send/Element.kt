/*
 * Copyright (C) 2017 VSCT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.vsct.tock.bot.connector.messenger.model.send

import com.fasterxml.jackson.annotation.JsonProperty
import fr.vsct.tock.bot.engine.action.SendAttachment.AttachmentType.image
import fr.vsct.tock.bot.engine.message.SentenceSubElement
import fr.vsct.tock.shared.mapNotNullValues

data class Element(val title: String,
                   @JsonProperty("image_url") val imageUrl: String? = null,
                   val subtitle: String? = null,
                   val buttons: List<Button>? = null) {

    fun toSentenceSubElement(): SentenceSubElement {
        return SentenceSubElement(
                choices = buttons?.map { it.toChoice() } ?: emptyList(),
                texts = mapNotNullValues(
                        Element::title.name to title,
                        Element::subtitle.name to subtitle
                ),
                attachments = imageUrl
                        ?.let { listOf(fr.vsct.tock.bot.engine.message.Attachment(imageUrl, image)) }
                        ?: emptyList()
        )
    }
}