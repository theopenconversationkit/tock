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

package fr.vsct.tock.bot.connector.slack

import fr.vsct.tock.bot.connector.ConnectorMessage
import fr.vsct.tock.bot.connector.ConnectorType
import fr.vsct.tock.bot.connector.slack.model.AttachmentField
import fr.vsct.tock.bot.connector.slack.model.SlackConnectorMessage
import fr.vsct.tock.bot.connector.slack.model.SlackEmoji
import fr.vsct.tock.bot.connector.slack.model.SlackMessageAttachment
import fr.vsct.tock.bot.connector.slack.model.SlackMessageOut
import fr.vsct.tock.bot.engine.BotBus
import fr.vsct.tock.bot.engine.I18nTranslator

internal const val SLACK_CONNECTOR_TYPE_ID = "slack"

/**
 * The Slack connector type.
 */
val slackConnectorType = ConnectorType(SLACK_CONNECTOR_TYPE_ID)

/**
 * Sends a Slack message only if the [ConnectorType] of the current [BotBus] is [slackConnectorType].
 */
fun BotBus.sendToSlack(
    messageProvider: () -> SlackConnectorMessage,
    delay: Long = botDefinition.defaultDelay(currentAnswerIndex)
): BotBus {
    if (targetConnectorType == slackConnectorType) {
        withSlack(messageProvider)
        send(delay)
    }
    return this
}

/**
 * Sends a Slack message as last bot answer, only if the [ConnectorType] of the current [BotBus] is [slackConnectorType].
 */
fun BotBus.endForSlack(
    messageProvider: () -> SlackConnectorMessage,
    delay: Long = botDefinition.defaultDelay(currentAnswerIndex)
): BotBus {
    if (targetConnectorType == slackConnectorType) {
        withSlack(messageProvider)
        end(delay)
    }
    return this
}

/**
 * Adds a Slack [ConnectorMessage] if the current connector is Slack.
 * You need to call [BotBus.send] or [BotBus.end] later to send this message.
 */
fun BotBus.withSlack(messageProvider: () -> SlackConnectorMessage): BotBus {
    return withMessage(slackConnectorType, messageProvider)
}

fun I18nTranslator.textMessage(message: CharSequence): SlackMessageOut {
    return SlackMessageOut(translate(message).toString())
}

fun I18nTranslator.multiLineMessage(lines: List<CharSequence>, channel: String? = null): SlackMessageOut =
    SlackMessageOut(lines.joinToString("\n") { translate(it).toString() }, channel)

fun I18nTranslator.attachmentMessage(
    vararg fields: AttachmentField,
    fallback: String,
    color: String = "good",
    text: CharSequence? = null,
    pretext: String? = null
): SlackMessageAttachment =
    SlackMessageAttachment(fields.toList(), fallback, color, translateAndReturnBlankAsNull(text), pretext)


fun I18nTranslator.attachmentField(title: String, value: String, short: Boolean = true): AttachmentField =
    AttachmentField(translate(title).toString(), value, short)

fun emojiMessage(emoji: SlackEmoji): SlackMessageOut = SlackMessageOut(emoji.format)

fun emoji(emoji: SlackEmoji): String = emoji.format