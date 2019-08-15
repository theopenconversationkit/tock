/*
 * Copyright (C) 2017/2019 VSCT
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

package fr.vsct.tock.bot.connector.web

import fr.vsct.tock.bot.connector.ConnectorMessage
import fr.vsct.tock.bot.connector.media.MediaMessage
import fr.vsct.tock.bot.definition.IntentAware
import fr.vsct.tock.bot.definition.Parameters
import fr.vsct.tock.bot.definition.StoryHandlerDefinition
import fr.vsct.tock.bot.definition.StoryStep
import fr.vsct.tock.bot.engine.Bus
import fr.vsct.tock.bot.engine.I18nTranslator
import fr.vsct.tock.bot.engine.action.SendChoice

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
    translate(text).toString().let { t -> WebButton(t, SendChoice.encodeNlpChoiceId(t)) }

/**
 * Creates a [WebMessage] from a [MediaMessage].
 */
fun <T : Bus<T>> T.webMediaMessage(media: MediaMessage): WebMessage = WebMessage(media = media)
