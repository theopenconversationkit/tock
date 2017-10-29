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
import fr.vsct.tock.bot.connector.ga.model.response.GAButton
import fr.vsct.tock.bot.connector.ga.model.response.GAImage
import fr.vsct.tock.bot.engine.BotBus

/**
 * Provides a [GABasicCard] with all available parameters.
 */
fun BotBus.basicCard(
        title: CharSequence?,
        subtitle: CharSequence?,
        formattedText: CharSequence?,
        image: GAImage?,
        buttons: List<GAButton>): GABasicCard {

    val t = translateAndSetBlankAsNull(title)
    val s = translateAndSetBlankAsNull(subtitle)
    val f = translateAndSetBlankAsNull(formattedText)

    return GABasicCard(t, s, f, image, buttons)
}

/**
 * Provides a [GABasicCard] with only one [GAButton] (only one is supported for now anyway).
 */
fun BotBus.basicCard(
        title: CharSequence? = null,
        subtitle: CharSequence? = null,
        formattedText: CharSequence? = null,
        image: GAImage? = null,
        button: GAButton? = null): GABasicCard
        = basicCard(title, subtitle, formattedText, image, listOfNotNull(button))

/**
 * Provides a [GABasicCard] without formattedText.
 */
fun BotBus.basicCard(title: CharSequence, subtitle: CharSequence, image: GAImage, button: GAButton): GABasicCard
        = basicCard(title, subtitle, null, image, button)

/**
 * Provides a [GABasicCard] with title and button.
 */
fun BotBus.basicCard(title: CharSequence, button: GAButton): GABasicCard
        = basicCard(title, null, button = button)

/**
 * Provides a [GABasicCard] with title and subtitle.
 */
fun BotBus.basicCard(title: CharSequence, subtitle: CharSequence): GABasicCard
        = basicCard(title, subtitle, null)

/**
 * Provides a [GABasicCard] with title and image.
 */
fun BotBus.basicCard(title: CharSequence, image: GAImage): GABasicCard
        = basicCard(title, null, null, image)

/**
 * Provides a [GABasicCard] with title, subtitle and image.
 */
fun BotBus.basicCard(title: CharSequence, subtitle: CharSequence, image: GAImage): GABasicCard
        = basicCard(title, subtitle, null, image)

/**
 * Provides a [GABasicCard] with an image.
 */
fun BotBus.basicCard(image: GAImage): GABasicCard
        = basicCard(null, null, null, image)
