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
import fr.vsct.tock.bot.connector.slack.model.SlackEmoji
import fr.vsct.tock.bot.connector.slack.model.SlackMessageAttachment
import fr.vsct.tock.bot.connector.slack.model.SlackMessageOut
import fr.vsct.tock.bot.engine.BotBus

internal const val SLACK_CONNECTOR_TYPE_ID = "slack"

val slackConnectorType = ConnectorType(SLACK_CONNECTOR_TYPE_ID)


fun BotBus.withSlack(messageProvider: () -> ConnectorMessage): BotBus {
    return withMessage(slackConnectorType, messageProvider)
}

fun BotBus.textMessage(message: String): SlackMessageOut {
    return SlackMessageOut(message)
}

fun BotBus.multiLineMessage(lines: List<String>, channel: String? = null): SlackMessageOut
        = SlackMessageOut(lines.joinToString("\n"), channel)

fun BotBus.attachmentMessage(vararg fields: AttachmentField, fallback: String, color: String = "good", text: String? = null, pretext: String? = null): SlackMessageAttachment
        = SlackMessageAttachment(fields.toList(), fallback, color, text, pretext)


fun BotBus.attachmentField(title: String, value: String, short: Boolean = true): AttachmentField
        = AttachmentField(title, value, short)

fun BotBus.emojiMessage(emoji: SlackEmoji): SlackMessageOut = SlackMessageOut(emoji.format)

fun BotBus.emoji(emoji: SlackEmoji): String = emoji.format