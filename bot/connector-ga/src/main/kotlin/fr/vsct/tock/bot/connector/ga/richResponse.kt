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

package fr.vsct.tock.bot.connector.ga

import fr.vsct.tock.bot.connector.ga.model.response.GABasicCard
import fr.vsct.tock.bot.connector.ga.model.response.GAItem
import fr.vsct.tock.bot.connector.ga.model.response.GALinkOutSuggestion
import fr.vsct.tock.bot.connector.ga.model.response.GARichResponse
import fr.vsct.tock.bot.connector.ga.model.response.GASuggestion
import fr.vsct.tock.bot.engine.BotBus
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * Provides a [GARichResponse] with all available parameters.
 */
fun BotBus.richResponse(items: List<GAItem>, linkOutSuggestion: GALinkOutSuggestion? = null, suggestions: List<GASuggestion>): GARichResponse
        = GARichResponse(items, suggestions, linkOutSuggestion)

/**
 * Provides a [GARichResponse] with suggestions as [String].
 */
fun BotBus.richResponse(items: List<GAItem>, linkOutSuggestion: GALinkOutSuggestion? = null, vararg suggestions: CharSequence): GARichResponse
        = richResponse(items, linkOutSuggestion, mapSuggestions(suggestions))

/**
 * Provides a [GARichResponse] without linkOutSuggestion.
 */
fun BotBus.richResponse(items: List<GAItem>, vararg suggestions: CharSequence): GARichResponse
        = richResponse(items, null, mapSuggestions(suggestions))

/**
 * Provides a [GARichResponse] without linkOutSuggestion.
 */
fun BotBus.richResponse(items: List<GAItem>, suggestions: List<CharSequence>): GARichResponse
        = richResponse(items, null, mapSuggestions(suggestions))

/**
 * Provides a [GARichResponse] with only one [GAItem].
 */
fun BotBus.richResponse(item: GAItem, linkOutSuggestion: GALinkOutSuggestion? = null, suggestions: List<CharSequence>): GARichResponse
        = richResponse(listOf(item), linkOutSuggestion, mapSuggestions(suggestions))

/**
 * Provides a [GARichResponse] with only one [GAItem].
 */
fun BotBus.richResponse(item: GAItem, linkOutSuggestion: GALinkOutSuggestion? = null, vararg suggestions: CharSequence): GARichResponse
        = richResponse(item, linkOutSuggestion, suggestions.toList())


/**
 * Provides a [GARichResponse] with text item.
 */
fun BotBus.richResponse(text: CharSequence, linkOutSuggestion: GALinkOutSuggestion? = null, suggestions: List<CharSequence>): GARichResponse
        = richResponse(item(simpleResponse(text)), linkOutSuggestion, suggestions)

/**
 * Provides a [GARichResponse] with text item.
 */
fun BotBus.richResponse(text: CharSequence, linkOutSuggestion: GALinkOutSuggestion? = null, vararg suggestions: CharSequence): GARichResponse
        = richResponse(text, linkOutSuggestion, suggestions.toList())

/**
 * Provides a [GARichResponse] with text item.
 */
fun BotBus.richResponse(text: CharSequence, vararg suggestions: CharSequence): GARichResponse
        = richResponse(text, null, suggestions.toList())


/**
 * Provides a [GARichResponse] with a [GABasicCard].
 */
fun BotBus.richResponse(basicCard: GABasicCard, linkOutSuggestion: GALinkOutSuggestion? = null, suggestions: List<CharSequence>): GARichResponse
        = richResponse(item(basicCard), linkOutSuggestion, suggestions)

/**
 * Provides a [GARichResponse] with a [GABasicCard].
 */
fun BotBus.richResponse(basicCard: GABasicCard, suggestions: List<CharSequence>): GARichResponse
        = richResponse(basicCard, null, suggestions)

/**
 * Provides a [GARichResponse] with a [GABasicCard].
 */
fun BotBus.richResponse(basicCard: GABasicCard, vararg suggestions: CharSequence): GARichResponse
        = richResponse(basicCard, suggestions.toList())


private fun BotBus.mapSuggestions(suggestions: Array<out CharSequence>): List<GASuggestion> = suggestions.map { suggestion(it) }

private fun BotBus.mapSuggestions(suggestions: List<CharSequence>): List<GASuggestion> = suggestions.map { suggestion(it) }


/**
 * Provides a [GASuggestion].
 */
fun BotBus.suggestion(text: CharSequence): GASuggestion {
    val t = translate(text)
    if (t.length > 25) {
        logger.warn { "title $t has more than 25 chars" }
    }
    return GASuggestion(t.toString())
}
