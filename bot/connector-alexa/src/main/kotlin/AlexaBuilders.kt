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

package ai.tock.bot.connector.alexa

import ai.tock.bot.connector.ConnectorMessage
import ai.tock.bot.connector.ConnectorType
import ai.tock.bot.engine.Bus
import ai.tock.bot.engine.I18nTranslator
import ai.tock.translator.UserInterfaceType
import com.amazon.speech.ui.Image
import com.amazon.speech.ui.StandardCard

internal const val ALEXA_CONNECTOR_TYPE_ID = "alexa"

/**
 * The Alexa [ConnectorType].
 */
val alexaConnectorType = ConnectorType(ALEXA_CONNECTOR_TYPE_ID, UserInterfaceType.voiceAssistant)

/**
 * Sends an Alexa message only if the [ConnectorType] of the current [BotBus] is [alexaConnectorType].
 */
fun <T : Bus<T>> T.sendToAlexa(
    messageProvider: T.() -> AlexaMessage,
    delay: Long = defaultDelay(currentAnswerIndex)
): T {
    if (targetConnectorType == alexaConnectorType) {
        withMessage(messageProvider(this))
        send(delay)
    }
    return this
}

/**
 * Sends an Alexa message as last bot answer, only if the [ConnectorType] of the current [BotBus] is [alexaConnectorType].
 */
fun <T : Bus<T>> T.endForAlexa(
    messageProvider: T.() -> AlexaMessage,
    delay: Long = defaultDelay(currentAnswerIndex)
): T {
    if (targetConnectorType == alexaConnectorType) {
        withMessage(messageProvider(this))
        end(delay)
    }
    return this
}

/**
 * Adds an Alexa [ConnectorMessage] if the current connector is Alexa.
 * You need to call [BotBus.send] or [BotBus.end] later to send this message.
 */
fun <T : Bus<T>> T.withAlexa(messageProvider: () -> AlexaMessage): T {
    return withMessage(alexaConnectorType, messageProvider)
}

/**
 * End the conversation with the skill.
 */
fun alexaEndConversation(): AlexaMessage = AlexaMessage(true)

/**
 * Set a reprompt.
 */
fun I18nTranslator.alexaReprompt(reprompt: CharSequence): AlexaMessage =
    AlexaMessage(false, reprompt = translate(reprompt).toString())

/**
 * Add the specified card.
 */
fun I18nTranslator.alexaStandardCard(
    title: CharSequence,
    text: CharSequence,
    smallImageUrl: String,
    largeImageUrl: String = smallImageUrl
): AlexaMessage =
    AlexaMessage(
        false,
        StandardCard().apply {
            setTitle(translate(title).toString())
            setText(translate(text).toString())
            image = Image().apply {
                setSmallImageUrl(smallImageUrl)
                setLargeImageUrl(largeImageUrl)
            }
        }
    )
