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

package ai.tock.bot.connector.mattermost

import ai.tock.bot.connector.ConnectorMessage
import ai.tock.bot.connector.ConnectorType
import ai.tock.bot.connector.mattermost.model.*
import ai.tock.bot.engine.Bus
import ai.tock.bot.engine.I18nTranslator

/**
 *
 */
internal const val MATTERMOST_CONNECTOR_TYPE_ID = "mattermost"

/**
 * The Mattermost connector type.
 */
val mattermostConnectorType = ConnectorType(MATTERMOST_CONNECTOR_TYPE_ID)

/**
 * Sends a Mattermost message only if the [ConnectorType] of the current [BotBus] is [mattermostConnectorType].
 */
fun <T : Bus<T>> T.sendToMattermost(
    delay: Long = defaultDelay(currentAnswerIndex),
    messageProvider: T.() -> MattermostConnectorMessage
): T {
    if (targetConnectorType == mattermostConnectorType) {
        withMessage(messageProvider(this))
        send(delay)
    }
    return this
}

/**
 * Sends a Mattermost message as last bot answer, only if the [ConnectorType] of the current [BotBus] is [mattermostConnectorType].
 */
fun <T : Bus<T>> T.endForMattermost(
    delay: Long = defaultDelay(currentAnswerIndex),
    messageProvider: T.() -> MattermostConnectorMessage
): T {
    if (targetConnectorType == mattermostConnectorType) {
        withMessage(messageProvider(this))
        end(delay)
    }
    return this
}

/**
 * Adds a Mattermost [ConnectorMessage] if the current connector is Mattermost.
 * You need to call [BotBus.send] or [BotBus.end] later to send this message.
 */
fun <T : Bus<T>> T.withMattermost(messageProvider: () -> MattermostConnectorMessage): T {
    return withMessage(mattermostConnectorType, messageProvider)
}

fun I18nTranslator.textMessage(
    message: CharSequence,
    channel: String? = null,
    username: String? = null
): MattermostMessageOut {
    return MattermostMessageOut(translate(message).toString(), channel = channel, username = username)
}

fun I18nTranslator.textMessageLinks(
    message: CharSequence,
    channel: String? = null,
    username: String? = null,
    links: List<Link> = emptyList()
): MattermostMessageOut {
    val t = translate(message).toString()

    return MattermostMessageOut(t, channel = channel, username = username, links = links)
}

fun I18nTranslator.multiLineMessage(lines: List<CharSequence>, channel: String? = null): MattermostMessageOut =
    MattermostMessageOut(lines.joinToString("\n") { translate(it).toString() }, channel)

fun I18nTranslator.mattermostMessage(
    message: CharSequence,
    channel: String? = null,
    username: String? = null
): MattermostMessageOut {
    return MattermostMessageOut(
        translate(message).toString(),
        channel = channel,
        username = username
    )
}

fun I18nTranslator.mattermostLink(
    title: CharSequence,
    url: String
): Link {
    val t = translate(title).toString()
    return Link(
        t,
        url
    )
}

