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

import ai.tock.bot.connector.ga.model.GAIntent
import ai.tock.bot.connector.ga.model.response.GAExpectedIntent
import ai.tock.bot.connector.ga.model.response.GAImage
import ai.tock.bot.connector.ga.model.response.GAListItem
import ai.tock.bot.connector.ga.model.response.GAListSelect
import ai.tock.bot.connector.ga.model.response.GARichResponse
import ai.tock.bot.connector.ga.model.response.GASuggestion
import ai.tock.bot.definition.IntentAware
import ai.tock.bot.definition.Parameters
import ai.tock.bot.definition.StoryHandlerDefinition
import ai.tock.bot.definition.StoryStep
import ai.tock.bot.engine.Bus
import ai.tock.bot.engine.I18nTranslator
import ai.tock.translator.raw
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * Provides a message with a [GAListSelect].
 */
fun I18nTranslator.gaMessage(
    gaRichResponse: GARichResponse,
    listItems: List<GAListItem>,
    title: CharSequence? = null
): GAResponseConnectorMessage =
    gaMessage(
        inputPrompt(gaRichResponse),
        listOf(
            expectedTextIntent(),
            expectedIntentForList(listItems, title)
        )
    )

/**
 * Provides a message with a [GAListSelect] and a list of [GASuggestion].
 */
fun I18nTranslator.gaMessageForList(
    items: List<GAListItem>,
    title: CharSequence? = null,
    suggestions: List<CharSequence> = emptyList()
): GAResponseConnectorMessage = gaMessage(richResponse(emptyList(), suggestions), items, title)

/**
 *  Add a basic card if only one element in the items list in order to avoid the limitation of 2 items.
 *
 *  @param items the carousel items
 *  @param title the optional list title
 *  @param suggestions the suggestions
 *  @param oneItemTitle if not null and if there is only one item, use this as title. If null [GAListItem.title] is used as title
 *  @param oneItemSubtitle if not null and if there is only one item, use this as subtitle. If null and the image is not null, [title] is used as subtitle
 *  @param oneItemDescription if not null and if there is only one item, use this as description. If null and the image is null, [GAListItem.description] is used as description
 *  @param oneItemSuggestions the additional suggestion if there is only one item
 */
fun I18nTranslator.gaFlexibleMessageForList(
    items: List<GAListItem>,
    title: CharSequence? = null,
    suggestions: List<CharSequence> = emptyList(),
    oneItemTitle: CharSequence? = null,
    oneItemSubtitle: CharSequence? = null,
    oneItemDescription: CharSequence? = null,
    oneItemSuggestions: List<CharSequence> = emptyList()
): GAResponseConnectorMessage {
    return if (items.size == 1) {
        val one = items.first()
        gaMessage(
            richResponse(
                basicCard(
                    oneItemTitle ?: one.title.raw,
                    if (one.image != null) oneItemSubtitle ?: title else title,
                    if (one.image != null) oneItemDescription else oneItemDescription ?: one.description?.raw,
                    one.image
                ),
                suggestions + oneItemSuggestions
            )
        )
    } else {
        gaMessageForList(items, title, suggestions)
    }
}

/**
 * Provides a [GAExpectedIntent] with a [GAListSelect].
 */
fun I18nTranslator.expectedIntentForList(items: List<GAListItem>, title: CharSequence? = null): GAExpectedIntent {
    if (items.size < 2) {
        error("must have at least 2 - current size = ${items.size}")
    } else {
        val t = translateAndReturnBlankAsNull(title)

        return GAExpectedIntent(
            GAIntent.option,
            optionValueSpec(
                listSelect = GAListSelect(
                    t?.toString(),
                    if (items.size > 30) {
                        logger.warn { "too many items $items - keep only first 30" }
                        items.subList(0, 30)
                    } else {
                        items
                    }
                )
            )
        )
    }
}

/**
 * Provides a [GAListItem] with [String] parameters without description.
 */
fun <T : Bus<T>> T.listItem(
    title: CharSequence,
    targetIntent: IntentAware,
    vararg parameters: Pair<String, String>
): GAListItem = listItem<T>(title, targetIntent, null, null, null, *parameters)

/**
 * Provides a [GAListItem] with [String] parameters with description and optional imageUrl.
 */
fun <T : Bus<T>> T.listItem(
    title: CharSequence,
    targetIntent: IntentAware,
    description: CharSequence,
    imageUrl: String? = null,
    vararg parameters: Pair<String, String>
): GAListItem = listItem(title, targetIntent, null, description, imageUrl, *parameters)

/**
 * Provides a [GAListItem] with [String] parameters without description.
 */
fun <T : Bus<T>> T.listItem(
    title: CharSequence,
    targetIntent: IntentAware,
    parameters: Parameters
): GAListItem = listItem(title, targetIntent, null, null, parameters)

/**
 * Provides a [GAListItem] with [String] parameters  with description and optional imageUrl.
 */
fun <T : Bus<T>> T.listItem(
    title: CharSequence,
    targetIntent: IntentAware,
    description: CharSequence?,
    imageUrl: String? = null,
    parameters: Parameters
): GAListItem = listItem(title, targetIntent, null, description, imageUrl, *parameters.toArray())

/**
 * Provides a [GAListItem] with [StoryStep] and [Parameters] parameters without description.
 */
fun <T : Bus<T>> T.listItem(
    title: CharSequence,
    targetIntent: IntentAware,
    step: StoryStep<out StoryHandlerDefinition>?,
    parameters: Parameters
): GAListItem = listItem(title, targetIntent, step, null, null, parameters)

/**
 * Provides a [GAListItem] with [StoryStep] and [Parameters] parameters without description.
 */
fun <T : Bus<T>> T.listItem(
    title: CharSequence,
    targetIntent: IntentAware,
    step: StoryStep<out StoryHandlerDefinition>?,
    vararg parameters: Pair<String, String>
): GAListItem = listItem<T>(title, targetIntent, step, null, null, *parameters)

/**
 * Provides a [GAListItem] with [StoryStep] and [Parameters] parameters.
 */
fun <T : Bus<T>> T.listItem(
    title: CharSequence,
    targetIntent: IntentAware,
    step: StoryStep<out StoryHandlerDefinition>?,
    description: CharSequence? = null,
    imageUrl: String? = null,
    parameters: Parameters
): GAListItem = listItem(title, targetIntent, step, description, imageUrl, *parameters.toArray())

/**
 * Provides a [GAListItem] with [StoryStep] and [Parameters] parameters.
 */
fun <T : Bus<T>> T.listItem(
    title: CharSequence,
    targetIntent: IntentAware,
    step: StoryStep<out StoryHandlerDefinition>?,
    description: CharSequence? = null,
    imageUrl: String? = null,
    vararg parameters: Pair<String, String>
): GAListItem {
    val t = translate(title)
    val d = translateAndReturnBlankAsNull(description)
    return GAListItem(
        optionInfo(
            t,
            targetIntent,
            step,
            *parameters
        ),
        t.toString(),
        d?.toString(),
        if (imageUrl == null) null else GAImage(imageUrl, t.toString())
    )
}
