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

package ai.tock.bot.connector.ga

import ai.tock.bot.connector.ga.model.response.GABasicCard
import ai.tock.bot.connector.ga.model.response.GAItem
import ai.tock.bot.connector.ga.model.response.GALinkOutSuggestion
import ai.tock.bot.connector.ga.model.response.GARichResponse
import ai.tock.bot.connector.ga.model.response.GASuggestion
import ai.tock.bot.engine.I18nTranslator
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * Provides a [GARichResponse] with all available parameters.
 */
fun richResponse(
    items: List<GAItem>,
    linkOutSuggestion: GALinkOutSuggestion? = null,
    suggestions: List<GASuggestion>
): GARichResponse =
    // not more than 8 suggestions
    GARichResponse(items, suggestions.take(8), linkOutSuggestion)

/**
 * Provides a [GARichResponse] with suggestions as [String].
 */
fun I18nTranslator.richResponse(
    items: List<GAItem>,
    linkOutSuggestion: GALinkOutSuggestion? = null,
    vararg suggestions: CharSequence
): GARichResponse = richResponse(items, linkOutSuggestion, mapSuggestions(suggestions))

/**
 * Provides a [GARichResponse] without linkOutSuggestion.
 */
fun I18nTranslator.richResponse(items: List<GAItem>, vararg suggestions: CharSequence): GARichResponse =
    richResponse(items, null, mapSuggestions(suggestions))

/**
 * Provides a [GARichResponse] without linkOutSuggestion.
 */
fun I18nTranslator.richResponse(items: List<GAItem>, suggestions: List<CharSequence>): GARichResponse =
    richResponse(items, null, mapSuggestions(suggestions))

/**
 * Provides a [GARichResponse] with only one [GAItem].
 */
fun I18nTranslator.richResponse(
    item: GAItem,
    linkOutSuggestion: GALinkOutSuggestion? = null,
    suggestions: List<CharSequence>
): GARichResponse = richResponse(listOf(item), linkOutSuggestion, mapSuggestions(suggestions))

/**
 * Provides a [GARichResponse] with only one [GAItem].
 */
fun I18nTranslator.richResponse(
    item: GAItem,
    linkOutSuggestion: GALinkOutSuggestion? = null,
    vararg suggestions: CharSequence
): GARichResponse = richResponse(item, linkOutSuggestion, suggestions.toList())

/**
 * Provides a [GARichResponse] with text item.
 */
fun I18nTranslator.richResponse(
    text: CharSequence,
    linkOutSuggestion: GALinkOutSuggestion? = null,
    suggestions: List<CharSequence>
): GARichResponse = richResponse(item(simpleResponse(text)), linkOutSuggestion, suggestions)

/**
 * Provides a [GARichResponse] with text item.
 */
fun I18nTranslator.richResponse(
    text: CharSequence,
    linkOutSuggestion: GALinkOutSuggestion? = null,
    vararg suggestions: CharSequence
): GARichResponse = richResponse(text, linkOutSuggestion, suggestions.toList())

/**
 * Provides a [GARichResponse] with text item.
 */
fun I18nTranslator.richResponse(text: CharSequence, vararg suggestions: CharSequence): GARichResponse =
    richResponse(text, suggestions.toList())

/**
 * Provides a [GARichResponse] with text item.
 */
fun I18nTranslator.richResponse(text: CharSequence, suggestions: List<CharSequence>): GARichResponse =
    richResponse(text, null, suggestions)

/**
 * Provides a [GARichResponse] with a text and a [GABasicCard].
 */
fun I18nTranslator.richResponse(
    text: CharSequence,
    basicCard: GABasicCard,
    linkOutSuggestion: GALinkOutSuggestion? = null,
    suggestions: List<CharSequence>
): GARichResponse =
    richResponse(listOf(item(simpleResponse(text)), item(basicCard)), linkOutSuggestion, mapSuggestions(suggestions))

/**
 * Provides a [GARichResponse] with a text and a [GABasicCard].
 */
fun I18nTranslator.richResponse(
    text: CharSequence,
    basicCard: GABasicCard,
    suggestions: List<CharSequence>
): GARichResponse = richResponse(text, basicCard, null, suggestions)

/**
 * Provides a [GARichResponse] with a text and a [GABasicCard].
 */
fun I18nTranslator.richResponse(
    text: CharSequence,
    basicCard: GABasicCard,
    vararg suggestions: CharSequence
): GARichResponse = richResponse(text, basicCard, suggestions.toList())

/**
 * Provides a [GARichResponse] with a [GABasicCard].
 */
fun I18nTranslator.richResponse(
    basicCard: GABasicCard,
    linkOutSuggestion: GALinkOutSuggestion? = null,
    suggestions: List<CharSequence>
): GARichResponse = richResponse(item(basicCard), linkOutSuggestion, suggestions)

/**
 * Provides a [GARichResponse] with a [GABasicCard].
 */
fun I18nTranslator.richResponse(basicCard: GABasicCard, suggestions: List<CharSequence>): GARichResponse =
    richResponse(basicCard, null, suggestions)

/**
 * Provides a [GARichResponse] with a [GABasicCard].
 */
fun I18nTranslator.richResponse(basicCard: GABasicCard, vararg suggestions: CharSequence): GARichResponse =
    richResponse(basicCard, suggestions.toList())

private fun I18nTranslator.mapSuggestions(suggestions: Array<out CharSequence>): List<GASuggestion> =
    suggestions.map { suggestion(it) }

private fun I18nTranslator.mapSuggestions(suggestions: List<CharSequence>): List<GASuggestion> =
    suggestions.map { suggestion(it) }

/**
 * Provides a [GASuggestion].
 */
fun I18nTranslator.suggestion(text: CharSequence): GASuggestion {
    val t = translate(text)
    if (t.length > 25) {
        logger.warn { "title $t has more than 25 chars" }
    }
    return GASuggestion(t.toString())
}
