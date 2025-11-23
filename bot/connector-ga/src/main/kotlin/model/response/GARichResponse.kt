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

import ai.tock.bot.engine.message.GenericElement
import ai.tock.bot.engine.message.GenericMessage

data class GARichResponse(
    val items: List<GAItem>,
    val suggestions: List<GASuggestion> = emptyList(),
    val linkOutSuggestion: GALinkOutSuggestion? = null,
) {
    fun toGenericMessage(): GenericMessage? {
        val e =
            items
                .firstOrNull()
                ?.toGenericMessage()
                ?: GenericMessage(
                    subElements =
                        items.mapNotNull {
                            it.toGenericMessage()?.let { GenericElement(it) }
                        },
                )

        return e.copy(
            choices = e.choices + listOfNotNull(linkOutSuggestion?.toChoice()),
            texts = e.texts + suggestions.mapIndexed { i, s -> "suggestion$i" to s.title }.toMap(),
        )
    }
}
