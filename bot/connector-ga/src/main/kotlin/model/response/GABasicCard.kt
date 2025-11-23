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

import ai.tock.bot.engine.message.GenericMessage
import ai.tock.shared.mapNotNullValues

/**
 *
 */
data class GABasicCard(
    val title: String? = null,
    val subtitle: String? = null,
    val formattedText: String? = null,
    val image: GAImage? = null,
    val buttons: List<GAButton> = emptyList(),
) {
    fun toGenericMessage(): GenericMessage {
        return GenericMessage(
            texts =
                mapNotNullValues(
                    GABasicCard::title.name to title,
                    GABasicCard::subtitle.name to subtitle,
                    GABasicCard::formattedText.name to formattedText,
                ),
            choices = buttons.map { it.toChoice() },
            attachments = listOfNotNull(image?.toAttachment()),
            metadata = image?.toMetadata() ?: emptyMap(),
        )
    }
}
