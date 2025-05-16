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
import ai.tock.bot.connector.ga.model.response.GAButton
import ai.tock.bot.connector.ga.model.response.GAImage
import ai.tock.bot.engine.I18nTranslator

/**
 * Provides a [GABasicCard] with all available parameters.
 */
fun I18nTranslator.basicCard(
    title: CharSequence?,
    subtitle: CharSequence?,
    formattedText: CharSequence?,
    image: GAImage?,
    buttons: List<GAButton>
): GABasicCard {

    val t = translateAndReturnBlankAsNull(title)
    val s = translateAndReturnBlankAsNull(subtitle)
    val f = translateAndReturnBlankAsNull(formattedText)

    return GABasicCard(t?.toString() ?: "", s?.toString() ?: "", f?.toString(), image, buttons)
}

/**
 * Provides a [GABasicCard] with only one [GAButton] (only one is supported for now anyway).
 */
fun I18nTranslator.basicCard(
    title: CharSequence? = null,
    subtitle: CharSequence? = null,
    formattedText: CharSequence? = null,
    image: GAImage? = null,
    button: GAButton? = null
): GABasicCard = basicCard(title, subtitle, formattedText, image, listOfNotNull(button))

/**
 * Provides a [GABasicCard] without formattedText.
 */
fun I18nTranslator.basicCard(
    title: CharSequence,
    subtitle: CharSequence,
    image: GAImage,
    button: GAButton
): GABasicCard = basicCard(title, subtitle, null, image, button)

/**
 * Provides a [GABasicCard] with title and button.
 */
fun I18nTranslator.basicCard(title: CharSequence, button: GAButton): GABasicCard =
    basicCard(title, null, button = button)

/**
 * Provides a [GABasicCard] with title and subtitle.
 */
fun I18nTranslator.basicCard(title: CharSequence, subtitle: CharSequence): GABasicCard =
    basicCard(title, subtitle, null)

/**
 * Provides a [GABasicCard] with title and image.
 */
fun I18nTranslator.basicCard(title: CharSequence, image: GAImage): GABasicCard = basicCard(title, null, null, image)

/**
 * Provides a [GABasicCard] with title, subtitle and image.
 */
fun I18nTranslator.basicCard(title: CharSequence, subtitle: CharSequence, image: GAImage): GABasicCard =
    basicCard(title, subtitle, null, image)

/**
 * Provides a [GABasicCard] with an image.
 */
fun I18nTranslator.basicCard(image: GAImage): GABasicCard = basicCard(null, null, null, image)
