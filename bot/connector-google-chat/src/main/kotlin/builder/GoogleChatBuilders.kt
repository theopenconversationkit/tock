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

package ai.tock.bot.connector.googlechat.builder

import ai.tock.bot.connector.ConnectorMessage
import ai.tock.bot.connector.ConnectorType
import ai.tock.bot.connector.googlechat.GoogleChatConnectorMessage
import ai.tock.bot.connector.googlechat.GoogleChatConnectorTextMessageOut
import ai.tock.bot.engine.Bus
import ai.tock.bot.engine.I18nTranslator

internal const val GOOGLE_CHAT_CONNECTOR_TYPE_ID = "google_chat"

val googleChatConnectorType = ConnectorType(GOOGLE_CHAT_CONNECTOR_TYPE_ID)

/**
 * Sends a Hangouts Chat message only if the [ConnectorType] of the current [Bus] is [googleChatConnectorType].
 */
fun <T : Bus<T>> T.sendToGoogleChat(
    delay: Long = defaultDelay(currentAnswerIndex),
    messageProvider: T.() -> GoogleChatConnectorMessage
): T {
    if (targetConnectorType == googleChatConnectorType) {
        withMessage(messageProvider(this))
        send(delay)
    }
    return this
}

/**
 * Sends a Google Chat message as last bot answer, only if the [ConnectorType] of the current [Bus] is [googleChatConnectorType].
 */
fun <T : Bus<T>> T.endForGoogleChat(
    delay: Long = defaultDelay(currentAnswerIndex),
    messageProvider: T.() -> GoogleChatConnectorMessage
): T {
    if (targetConnectorType == googleChatConnectorType) {
        withMessage(messageProvider(this))
        end(delay)
    }
    return this
}

/**
 * Adds a Google Chat [ConnectorMessage] if the current connector is [googleChatConnectorType].
 * You need to call [Bus.send] or [Bus.end] later to send this message.
 */
fun <T : Bus<T>> T.withGoogleChat(messageProvider: () -> GoogleChatConnectorMessage): T {
    return withMessage(googleChatConnectorType, messageProvider)
}

fun I18nTranslator.textMessage(message: CharSequence, vararg args: Any?): GoogleChatConnectorTextMessageOut {
    return GoogleChatConnectorTextMessageOut(translate(message, args).toString())
}
