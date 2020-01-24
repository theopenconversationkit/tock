/*
 * Copyright (C) 2017/2019 e-voyageurs technologies
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

package ai.tock.bot.connector.web

import ai.tock.bot.connector.ConnectorMessage
import ai.tock.bot.connector.media.MediaCard
import ai.tock.bot.connector.media.MediaCarousel
import ai.tock.bot.definition.IntentAware
import ai.tock.bot.definition.Parameters
import ai.tock.bot.definition.StoryHandlerDefinition
import ai.tock.bot.definition.StoryStep
import ai.tock.bot.engine.Bus
import ai.tock.bot.engine.I18nTranslator
import ai.tock.bot.engine.action.SendChoice

/**
 * Adds a Web [ConnectorMessage] if the current connector is Web.
 * You need to call [BotBus.send] or [BotBus.end] later to send this message.
 */
fun <T : Bus<T>> T.withWeb(messageProvider: () -> WebMessage): T {
    return withMessage(webConnectorType, messageProvider)
}

/**
 * Creates a text with buttons.
 */
fun I18nTranslator.webMessage(title: CharSequence, vararg buttons: WebButton): WebMessage =
    WebMessage(
        translate(title).toString(), buttons.toList()
    )

/**
 * Creates a text with a list of buttons.
 */
fun I18nTranslator.webMessage(title: CharSequence, buttons: List<WebButton>): WebMessage =
    WebMessage(
        translate(title).toString(), buttons
    )

/**
 * Creates a button.
 */
fun <T : Bus<T>> T.webButton(
    title: CharSequence,
    targetIntent: IntentAware? = null,
    step: StoryStep<out StoryHandlerDefinition>? = null,
    parameters: Parameters = Parameters()
): WebButton =
    WebButton(
        translate(title).toString(),
        targetIntent?.let { i -> SendChoice.encodeChoiceId(this, i, step, parameters.toMap()) }
    )

/**
 * Creates a button from a text.
 */
fun <T : Bus<T>> T.webTextButton(text: CharSequence): WebButton =
    WebButton(translate(text).toString())

/**
 * Creates a [WebMessage] from a [MediaCard].
 */
fun <T : Bus<T>> T.webCard(card: MediaCard): WebMessage = WebMessage(card = card)

/**
 * Creates a [WebMessage] from a [MediaCarousel].
 */
fun <T : Bus<T>> T.webCarousel(carousel: MediaCarousel): WebMessage = WebMessage(carousel = carousel)
