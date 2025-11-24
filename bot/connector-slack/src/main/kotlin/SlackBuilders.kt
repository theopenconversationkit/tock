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

package ai.tock.bot.connector.slack

import ai.tock.bot.connector.ConnectorMessage
import ai.tock.bot.connector.ConnectorType
import ai.tock.bot.connector.slack.model.AttachmentField
import ai.tock.bot.connector.slack.model.Button
import ai.tock.bot.connector.slack.model.SlackConnectorMessage
import ai.tock.bot.connector.slack.model.SlackEmoji
import ai.tock.bot.connector.slack.model.SlackMessageAttachment
import ai.tock.bot.connector.slack.model.SlackMessageOut
import ai.tock.bot.definition.IntentAware
import ai.tock.bot.definition.Parameters
import ai.tock.bot.definition.StoryStepDef
import ai.tock.bot.engine.Bus
import ai.tock.bot.engine.I18nTranslator
import ai.tock.bot.engine.action.SendChoice

internal const val SLACK_CONNECTOR_TYPE_ID = "slack"

/**
 * The Slack connector type.
 */
val slackConnectorType = ConnectorType(SLACK_CONNECTOR_TYPE_ID)

/**
 * Sends a Slack message only if the [ConnectorType] of the current [BotBus] is [slackConnectorType].
 */
fun <T : Bus<T>> T.sendToSlack(
    delay: Long = defaultDelay(currentAnswerIndex),
    messageProvider: T.() -> SlackConnectorMessage,
): T {
    if (targetConnectorType == slackConnectorType) {
        withMessage(messageProvider(this))
        send(delay)
    }
    return this
}

/**
 * Sends a Slack message as last bot answer, only if the [ConnectorType] of the current [BotBus] is [slackConnectorType].
 */
fun <T : Bus<T>> T.endForSlack(
    delay: Long = defaultDelay(currentAnswerIndex),
    messageProvider: T.() -> SlackConnectorMessage,
): T {
    if (targetConnectorType == slackConnectorType) {
        withMessage(messageProvider(this))
        end(delay)
    }
    return this
}

/**
 * Adds a Slack [ConnectorMessage] if the current connector is Slack.
 * You need to call [BotBus.send] or [BotBus.end] later to send this message.
 */
fun <T : Bus<T>> T.withSlack(messageProvider: () -> SlackConnectorMessage): T {
    return withMessage(slackConnectorType, messageProvider)
}

fun I18nTranslator.textMessage(message: CharSequence): SlackMessageOut {
    return SlackMessageOut(translate(message).toString())
}

fun I18nTranslator.multiLineMessage(
    lines: List<CharSequence>,
    channel: String? = null,
): SlackMessageOut = SlackMessageOut(lines.joinToString("\n") { translate(it).toString() }, channel)

fun I18nTranslator.slackMessage(
    message: CharSequence,
    vararg attachments: SlackMessageAttachment,
): SlackMessageOut {
    return slackMessage(message, null, attachments = *attachments)
}

fun I18nTranslator.slackMessage(
    message: CharSequence,
    channel: String? = null,
    vararg attachments: SlackMessageAttachment,
): SlackMessageOut {
    return SlackMessageOut(translate(message).toString(), channel, attachments.toList())
}

fun I18nTranslator.slackAttachment(
    text: CharSequence? = null,
    vararg buttons: Button,
): SlackMessageAttachment = slackAttachment(text, buttons.toList())

fun I18nTranslator.slackAttachment(vararg buttons: Button): SlackMessageAttachment = slackAttachment(buttons = buttons.toList())

fun I18nTranslator.slackAttachment(
    text: CharSequence? = null,
    buttons: List<Button> = emptyList(),
    color: String = "good",
    pretext: String? = null,
    fallback: String = translate(text).toString(),
    vararg fields: AttachmentField,
): SlackMessageAttachment = SlackMessageAttachment(buttons, fields.toList(), fallback, color, translateAndReturnBlankAsNull(text)?.toString(), pretext)

fun I18nTranslator.attachmentField(
    title: String,
    value: String,
    short: Boolean = true,
): AttachmentField = AttachmentField(translate(title).toString(), value, short)

fun emojiMessage(emoji: SlackEmoji): SlackMessageOut = SlackMessageOut(emoji.format)

fun emoji(emoji: SlackEmoji): String = emoji.format

/**
 * Creates Slack button: https://api.slack.com/reference/messaging/block-elements#button
 */
fun <T : Bus<T>> T.slackButton(
    title: CharSequence,
    targetIntent: IntentAware? = null,
    parameters: Parameters = Parameters(),
    name: String = "default",
): Button = slackButton(title, targetIntent, null, parameters, name)

/**
 * Creates a Slack button: https://api.slack.com/reference/messaging/block-elements#button
 */
fun <T : Bus<T>> T.slackButton(
    title: CharSequence,
    targetIntent: IntentAware?,
    step: StoryStepDef? = null,
    parameters: Parameters = Parameters(),
    name: String = "default",
): Button = slackButton(title, targetIntent, step, *parameters.toArray(), name = name)

/**
 * Creates a Slack button: https://api.slack.com/reference/messaging/block-elements#button
 */
fun <T : Bus<T>> T.slackButton(
    title: CharSequence,
    targetIntent: IntentAware?,
    step: StoryStepDef? = null,
    vararg parameters: Pair<String, String>,
    name: String = "default",
): Button {
    val t = translate(title).toString()
    return Button(
        name,
        t,
        if (targetIntent == null) {
            SendChoice.encodeNlpChoiceId(t)
        } else {
            SendChoice.encodeChoiceId(this, targetIntent, step, parameters.toMap())
        },
    )
}
