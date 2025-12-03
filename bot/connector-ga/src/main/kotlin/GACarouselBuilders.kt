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
import ai.tock.bot.connector.ga.model.response.GABasicCard
import ai.tock.bot.connector.ga.model.response.GACarouselItem
import ai.tock.bot.connector.ga.model.response.GACarouselSelect
import ai.tock.bot.connector.ga.model.response.GAExpectedIntent
import ai.tock.bot.connector.ga.model.response.GAImage
import ai.tock.bot.definition.IntentAware
import ai.tock.bot.definition.Parameters
import ai.tock.bot.definition.StoryStep
import ai.tock.bot.definition.StoryStepDef
import ai.tock.bot.engine.Bus
import ai.tock.bot.engine.I18nTranslator
import ai.tock.translator.raw
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * Provides a message with a [GACarouselSelect].
 */
fun I18nTranslator.gaMessageForCarousel(
    items: List<GACarouselItem>,
    suggestions: List<CharSequence> = emptyList(),
): GAResponseConnectorMessage {
    if (items.size < 2) {
        error("must have at least 2 - current size = ${items.size}")
    } else {
        return gaMessage(
            inputPrompt(richResponse(emptyList(), suggestions)),
            listOf(
                expectedTextIntent(),
                expectedIntentForCarousel(items),
            ),
        )
    }
}

/**
 *  Add a basic card if only one element in the items list, in order to avoid the limitation of 2 items.
 *
 *  @param items the carousel items
 *  @param suggestions the suggestions
 *  @param oneItemTitle if not null and if there is only one item, use this as title. If null [GACarouselItem.title] is used as title
 *  @param oneItemSubtitle if not null and if there is only one item, use this as subtitle. If null and the image is not null, [GACarouselItem.description] is used as subtitle
 *  @param oneItemDescription if not null and if there is only one item, use this as description. If null and the image is null, [GACarouselItem.description] is used as description
 *  @param oneItemSuggestions the additional suggestion if there is only one item
 */
fun I18nTranslator.gaFlexibleMessageForCarousel(
    items: List<GACarouselItem>,
    suggestions: List<CharSequence> = emptyList(),
    oneItemTitle: CharSequence? = null,
    oneItemSubtitle: CharSequence? = null,
    oneItemDescription: CharSequence? = null,
    oneItemSuggestions: List<CharSequence> = emptyList(),
): GAResponseConnectorMessage {
    return gaFlexibleMessageForCarousel(
        items,
        suggestions,
        oneItemSuggestions,
    ) { one ->
        basicCard(
            oneItemTitle ?: one.title.raw,
            if (one.image != null) oneItemSubtitle ?: one.description?.raw else oneItemSubtitle,
            if (one.image != null) oneItemDescription else oneItemDescription ?: one.description?.raw,
            one.image,
        )
    }
}

/**
 *  Add a basic card if only one element in the items list, in order to avoid the limitation of 2 items.
 *
 *  @param items the carousel items
 *  @param suggestions the suggestions
 *  @param oneItemSuggestions the additional suggestion if there is only one item
 *  @param oneItemBasicCardProvider provides the basic card if only one item
 */
fun I18nTranslator.gaFlexibleMessageForCarousel(
    items: List<GACarouselItem>,
    suggestions: List<CharSequence> = emptyList(),
    oneItemSuggestions: List<CharSequence> = emptyList(),
    oneItemBasicCardProvider: (GACarouselItem) -> GABasicCard = {
        basicCard(
            it.title.raw,
            if (it.image != null) it.description?.raw else null,
            if (it.image == null) it.description?.raw else null,
            it.image,
        )
    },
): GAResponseConnectorMessage {
    return if (items.size == 1) {
        gaMessage(
            richResponse(
                oneItemBasicCardProvider.invoke(items.first()),
                suggestions + oneItemSuggestions,
            ),
        )
    } else {
        gaMessageForCarousel(items, suggestions)
    }
}

/**
 * Provides a [GAExpectedIntent] with a [GACarouselSelect].
 */
fun I18nTranslator.expectedIntentForCarousel(items: List<GACarouselItem>): GAExpectedIntent {
    return GAExpectedIntent(
        GAIntent.option,
        optionValueSpec(
            carouselSelect =
                GACarouselSelect(
                    if (items.size > 10) {
                        logger.warn { "too many items $items - keep only first 10" }
                        items.subList(0, 10)
                    } else {
                        items
                    },
                ),
        ),
    )
}

/**
 * Provides a [GACarouselItem] with [String] parameters.
 */
fun <T : Bus<T>> T.carouselItem(
    targetIntent: IntentAware,
    title: CharSequence,
    description: CharSequence? = null,
    image: GAImage? = null,
    vararg parameters: Pair<String, String>,
): GACarouselItem = carouselItem(targetIntent, null, title, description, image, *parameters)

/**
 * Provides a [GACarouselItem] with [Parameters] parameters.
 */
fun <T : Bus<T>> T.carouselItem(
    targetIntent: IntentAware,
    title: CharSequence,
    description: CharSequence? = null,
    image: GAImage? = null,
    parameters: Parameters,
): GACarouselItem = carouselItem(targetIntent, null, title, description, image, parameters)

/**
 * Provides a [GACarouselItem] with [StoryStep] and [Parameters] parameters.
 */
fun <T : Bus<T>> T.carouselItem(
    targetIntent: IntentAware,
    step: StoryStepDef?,
    title: CharSequence,
    description: CharSequence? = null,
    image: GAImage? = null,
    parameters: Parameters,
): GACarouselItem = carouselItem(targetIntent, step, title, description, image, *parameters.toArray())

/**
 * Provides a [GACarouselItem] with [StoryStep] and [String] parameters.
 */
fun <T : Bus<T>> T.carouselItem(
    targetIntent: IntentAware,
    step: StoryStepDef?,
    title: CharSequence,
    description: CharSequence? = null,
    image: GAImage? = null,
    vararg parameters: Pair<String, String>,
): GACarouselItem {
    val t = translate(title)
    return GACarouselItem(
        optionInfo(
            t,
            targetIntent,
            step,
            *parameters,
        ),
        t.toString(),
        translate(description).toString(),
        image,
    )
}
