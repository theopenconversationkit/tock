/*
 * Copyright (C) 2019 VSCT
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

package fr.vsct.tock.bot.connector.twitter

import fr.vsct.tock.bot.connector.ConnectorType
import fr.vsct.tock.bot.connector.twitter.model.CTA
import fr.vsct.tock.bot.connector.twitter.model.MessageCreate
import fr.vsct.tock.bot.connector.twitter.model.MessageData
import fr.vsct.tock.bot.connector.twitter.model.Option
import fr.vsct.tock.bot.connector.twitter.model.Options
import fr.vsct.tock.bot.connector.twitter.model.Recipient
import fr.vsct.tock.bot.connector.twitter.model.TwitterConnectorMessage
import fr.vsct.tock.bot.connector.twitter.model.WebUrl
import fr.vsct.tock.bot.connector.twitter.model.outcoming.DirectMessageOutcomingEvent
import fr.vsct.tock.bot.connector.twitter.model.outcoming.OutcomingEvent
import fr.vsct.tock.bot.definition.IntentAware
import fr.vsct.tock.bot.definition.StoryHandlerDefinition
import fr.vsct.tock.bot.definition.StoryStep
import fr.vsct.tock.bot.engine.BotBus
import fr.vsct.tock.bot.engine.action.SendChoice
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

internal const val TWITTER_CONNECTOR_TYPE_ID = "twitter"

/**
 * The Twitter connector type.
 */
val twitterConnectorType = ConnectorType(TWITTER_CONNECTOR_TYPE_ID)


/**
 * Creates a direct message with only text
 */
fun BotBus.directMessage(message: CharSequence): OutcomingEvent =
    OutcomingEvent(
        DirectMessageOutcomingEvent(
            MessageCreate(
                target = Recipient(userId.id),
                sourceAppId = applicationId,
                senderId = botId.id,
                messageData = MessageData(translate(message).toString())
            )
        )
    )

/**
 * Creates a direct message with Buttons
 * @see https://developer.twitter.com/en/docs/direct-messages/buttons/api-reference/buttons
 */
fun BotBus.directMessageWithButtons(message: CharSequence, ctas: List<CTA>): OutcomingEvent =
    OutcomingEvent(
        DirectMessageOutcomingEvent(
            MessageCreate(
                target = Recipient(userId.id),
                sourceAppId = applicationId,
                senderId = botId.id,
                messageData = MessageData(translate(message).toString(), ctas = ctas)
            )
        )
    )

/**
 * Creates a direct message with Buttons
 * @see https://developer.twitter.com/en/docs/direct-messages/buttons/api-reference/buttons
 */
fun BotBus.directMessageWithButtons(message: CharSequence, vararg ctas: CTA): OutcomingEvent =
    OutcomingEvent(
        DirectMessageOutcomingEvent(
            MessageCreate(
                target = Recipient(userId.id),
                sourceAppId = applicationId,
                senderId = botId.id,
                messageData = MessageData(translate(message).toString(), ctas = ctas.toList())
            )
        )
    )

/**
 * Creates a direct message with quick replies
 * @see https://developer.twitter.com/en/docs/direct-messages/quick-replies/overview
 */
fun BotBus.directMessageWithOptions(message: CharSequence, options: List<Option>): OutcomingEvent =
    OutcomingEvent(
        DirectMessageOutcomingEvent(
            MessageCreate(
                target = Recipient(userId.id),
                sourceAppId = applicationId,
                senderId = botId.id,
                messageData = MessageData(translate(message).toString(), quickReply = Options(options))
            )
        )
    )

/**
 * Creates a direct message with quick replies
 * @see https://developer.twitter.com/en/docs/direct-messages/quick-replies/overview
 */
fun BotBus.directMessageWithOptions(message: CharSequence, vararg options: Option): OutcomingEvent = OutcomingEvent(
    DirectMessageOutcomingEvent(
        MessageCreate(
            target = Recipient(userId.id),
            sourceAppId = applicationId,
            senderId = botId.id,
            messageData = MessageData(translate(message).toString(), quickReply = Options(options.toList()))
        )
    )
)

/**
 * Creates a url button
 * @see https://developer.twitter.com/en/docs/direct-messages/buttons/api-reference/buttons
 */
fun BotBus.webUrl(
    label: CharSequence,
    url: CharSequence
): WebUrl {
    val l = translate(label)
    if (l.length > 36) {
        logger.warn { "label $l has more than 36 chars" }
    }
    return WebUrl(l.toString(), url.toString())
}

/**
 * Creates an Option Quick Reply
 * @see https://developer.twitter.com/en/docs/direct-messages/quick-replies/overview
 */
fun BotBus.option(
    label: CharSequence,
    description: CharSequence,
    targetIntent: IntentAware,
    step: StoryStep<out StoryHandlerDefinition>? = null,
    vararg parameters: Pair<String, String>
): Option =
    option(label, description, targetIntent.wrappedIntent(), step, parameters.toMap())

/**
 * Creates an Option Quick Reply
 * @see https://developer.twitter.com/en/docs/direct-messages/quick-replies/overview
 */
fun BotBus.option(
    label: CharSequence,
    description: CharSequence,
    targetIntent: IntentAware,
    step: StoryStep<out StoryHandlerDefinition>? = null,
    parameters: Map<String, String>
): Option =
    option(label, description, targetIntent, step, parameters) { intent, s, params ->
        SendChoice.encodeChoiceId(this, intent, s, params)
    }

/**
 * Creates an Option Quick Reply
 * @see https://developer.twitter.com/en/docs/direct-messages/quick-replies/overview
 */
private fun BotBus.option(
    label: CharSequence,
    description: CharSequence,
    targetIntent: IntentAware,
    step: StoryStep<out StoryHandlerDefinition>? = null,
    parameters: Map<String, String>,
    metadataEncoder: (IntentAware, StoryStep<out StoryHandlerDefinition>?, Map<String, String>) -> String
): Option {
    val l = translate(label)
    if (l.length > 36) {
        logger.warn { "label $l has more than 36 chars" }
    }
    val d = translate(description)
    if (d.length > 72) {
        logger.warn { "label $d has more than 72 chars" }
    }
    val metadata = metadataEncoder.invoke(targetIntent, step, parameters)
    if (metadata.length > 1000) {
        logger.warn { "payload $metadata has more than 1000 chars" }
    }
    return Option(l.toString(), d.toString(), metadata)
}

/**
 * Adds a Twitter [ConnectorMessage] if the current connector is Twitter.
 * You need to call [BotBus.send] or [BotBus.end] later to send this message.
 */
fun BotBus.withTwitter(messageProvider: () -> TwitterConnectorMessage): BotBus {
    return withMessage(twitterConnectorType, messageProvider)
}