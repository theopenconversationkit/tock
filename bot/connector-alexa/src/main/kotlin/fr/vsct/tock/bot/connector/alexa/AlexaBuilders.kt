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

package fr.vsct.tock.bot.connector.alexa

import com.amazon.speech.ui.Image
import com.amazon.speech.ui.StandardCard
import fr.vsct.tock.bot.connector.ConnectorMessage
import fr.vsct.tock.bot.connector.ConnectorType
import fr.vsct.tock.bot.engine.BotBus
import fr.vsct.tock.bot.engine.I18nTranslator
import fr.vsct.tock.translator.UserInterfaceType

internal const val ALEXA_CONNECTOR_TYPE_ID = "alexa"

/**
 * The Alexa [ConnectorType].
 */
val alexaConnectorType = ConnectorType(ALEXA_CONNECTOR_TYPE_ID, UserInterfaceType.voiceAssistant)

/**
 * Add a [ConnectorMessage] for Alexa.
 */
fun BotBus.withAlexa(messageProvider: () -> AlexaMessage): BotBus {
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

