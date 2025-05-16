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

package ai.tock.bot.connector.twitter

import ai.tock.bot.connector.ConnectorType
import ai.tock.bot.connector.twitter.model.AttachmentData
import ai.tock.bot.connector.twitter.model.CTA
import ai.tock.bot.connector.twitter.model.MediaCategory
import ai.tock.bot.connector.twitter.model.MessageCreate
import ai.tock.bot.connector.twitter.model.MessageData
import ai.tock.bot.connector.twitter.model.Option
import ai.tock.bot.connector.twitter.model.OptionWithoutDescription
import ai.tock.bot.connector.twitter.model.Recipient
import ai.tock.bot.connector.twitter.model.TwitterConnectorMessage
import ai.tock.bot.connector.twitter.model.TwitterPublicConnectorMessage
import ai.tock.bot.connector.twitter.model.WebUrl
import ai.tock.bot.connector.twitter.model.outcoming.DirectMessageOutcomingEvent
import ai.tock.bot.connector.twitter.model.outcoming.OutcomingEvent
import ai.tock.bot.connector.twitter.model.outcoming.Tweet
import ai.tock.bot.definition.IntentAware
import ai.tock.bot.definition.StoryHandlerDefinition
import ai.tock.bot.definition.StoryStep
import ai.tock.bot.engine.BotBus
import ai.tock.bot.engine.Bus
import ai.tock.bot.engine.I18nTranslator
import ai.tock.bot.engine.action.ActionVisibility
import ai.tock.bot.engine.action.SendChoice
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

internal const val TWITTER_CONNECTOR_TYPE_ID = "twitter"

/**
 * The Twitter connector type.
 */
val twitterConnectorType = ConnectorType(TWITTER_CONNECTOR_TYPE_ID)

const val MAX_OPTION_LABEL = 36
const val MAX_OPTION_DESCRIPTION = 72
const val MAX_METADATA = 1000

internal fun CharSequence.truncateIfLongerThan(maxCharacter: Int): String =
    if (maxCharacter >= 0 && this.length > maxCharacter) {
        if (maxCharacter > 3) this.substring(0, maxCharacter - 3) + "..."
        else this.substring(0, maxCharacter)
    } else this.toString()

/**
 * Creates a direct message with only text
 */
fun <T : Bus<T>> T.directMessage(message: CharSequence): OutcomingEvent =
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
fun <T : Bus<T>> T.directMessageWithButtons(message: CharSequence, ctas: List<CTA>): OutcomingEvent =
    OutcomingEvent(
        DirectMessageOutcomingEvent(
            MessageCreate(
                target = Recipient(userId.id),
                sourceAppId = applicationId,
                senderId = botId.id,
                messageData = MessageData(
                    translate(message).toString(),
                    ctas = if (ctas.isNotEmpty()) ctas else null
                )
            )
        )
    )

/**
 * Creates a direct message with Buttons
 * @see https://developer.twitter.com/en/docs/direct-messages/buttons/api-reference/buttons
 */
fun <T : Bus<T>> T.directMessageWithButtons(message: CharSequence, vararg ctas: CTA): OutcomingEvent =
    directMessageWithButtons(message, ctas.toList())

private fun <T : Bus<T>> T.directMessageBuidler(message: CharSequence): DirectMessageOutcomingEvent.Builder =
    DirectMessageOutcomingEvent.builder(
        target = Recipient(userId.id),
        senderId = botId.id,
        text = translate(message).toString()
    )
        .withSourceAppId(applicationId)

/**
 * Creates a direct message with quick replies
 * @see https://developer.twitter.com/en/docs/direct-messages/quick-replies/overview
 */
fun <T : Bus<T>> T.directMessageWithOptions(message: CharSequence, vararg options: Option): OutcomingEvent =
    OutcomingEvent(
        directMessageBuidler(message)
            .withOptions(*options)
            .build()
    )

/**
 * Creates a direct message with quick replies
 * @see https://developer.twitter.com/en/docs/direct-messages/quick-replies/overview
 */
fun <T : Bus<T>> T.directMessageWithOptions(message: CharSequence, vararg options: OptionWithoutDescription): OutcomingEvent =
    OutcomingEvent(
        directMessageBuidler(message)
            .withOptions(*options)
            .build()
    )

/**
 * Creates a direct message with an attachment
 * @see https://developer.twitter.com/en/docs/direct-messages/message-attachments/overview
 */
fun <T : Bus<T>> T.directMessageWithAttachment(
    message: CharSequence,
    mediaCategory: MediaCategory,
    contentType: String,
    bytes: ByteArray,
    vararg options: Option
): OutcomingEvent = OutcomingEvent(
    directMessageBuidler(message)
        .withOptions(*options)
        .build(),
    AttachmentData(
        mediaCategory,
        contentType,
        bytes
    )
)

/**
 * Creates a direct message with an attachment
 * @see https://developer.twitter.com/en/docs/direct-messages/message-attachments/overview
 */
fun <T : Bus<T>> T.directMessageWithAttachment(
    message: CharSequence,
    mediaCategory: MediaCategory,
    contentType: String,
    bytes: ByteArray,
    vararg options: OptionWithoutDescription
): OutcomingEvent = OutcomingEvent(
    directMessageBuidler(message)
        .withOptions(*options)
        .build(),
    AttachmentData(
        mediaCategory,
        contentType,
        bytes
    )
)

/**
 * Creates a direct message with a gif (Max 15MB)
 * @see https://developer.twitter.com/en/docs/direct-messages/message-attachments/overview
 */
fun <T : Bus<T>> T.directMessageWithGIF(
    message: CharSequence,
    contentType: String,
    bytes: ByteArray,
    vararg options: Option
): OutcomingEvent = directMessageWithAttachment(message, MediaCategory.GIF, contentType, bytes, *options)

/**
 * Creates a direct message with a gif (Max 15MB)
 * @see https://developer.twitter.com/en/docs/direct-messages/message-attachments/overview
 */
fun <T : Bus<T>> T.directMessageWithGIF(
    message: CharSequence,
    contentType: String,
    bytes: ByteArray,
    vararg options: OptionWithoutDescription
): OutcomingEvent = directMessageWithAttachment(message, MediaCategory.GIF, contentType, bytes, *options)

/**
 * Creates a direct message with a gif (Max 15MB)
 * @see https://developer.twitter.com/en/docs/direct-messages/message-attachments/overview
 */
fun <T : Bus<T>> T.directMessageWithGIF(
    contentType: String,
    bytes: ByteArray,
    vararg options: Option
): OutcomingEvent = directMessageWithImage("", contentType, bytes, *options)

/**
 * Creates a direct message with a gif (Max 15MB)
 * @see https://developer.twitter.com/en/docs/direct-messages/message-attachments/overview
 */
fun <T : Bus<T>> T.directMessageWithGIF(
    contentType: String,
    bytes: ByteArray,
    vararg options: OptionWithoutDescription
): OutcomingEvent = directMessageWithImage("", contentType, bytes, *options)

/**
 * Creates a direct message with an image (Max 5MB)
 * @see https://developer.twitter.com/en/docs/direct-messages/message-attachments/overview
 */
fun <T : Bus<T>> T.directMessageWithImage(
    message: CharSequence,
    contentType: String,
    bytes: ByteArray,
    vararg options: Option
): OutcomingEvent = directMessageWithAttachment(message, MediaCategory.IMAGE, contentType, bytes, *options)

/**
 * Creates a direct message with an image (Max 5MB)
 * @see https://developer.twitter.com/en/docs/direct-messages/message-attachments/overview
 */
fun <T : Bus<T>> T.directMessageWithImage(
    message: CharSequence,
    contentType: String,
    bytes: ByteArray,
    vararg options: OptionWithoutDescription
): OutcomingEvent = directMessageWithAttachment(message, MediaCategory.IMAGE, contentType, bytes, *options)

/**
 * Creates a direct message with an image (Max 5MB)
 * @see https://developer.twitter.com/en/docs/direct-messages/message-attachments/overview
 */
fun <T : Bus<T>> T.directMessageWithImage(
    contentType: String,
    bytes: ByteArray,
    vararg options: Option
): OutcomingEvent = directMessageWithImage("", contentType, bytes, *options)

/**
 * Creates a direct message with an image (Max 5MB)
 * @see https://developer.twitter.com/en/docs/direct-messages/message-attachments/overview
 */
fun <T : Bus<T>> T.directMessageWithImage(
    contentType: String,
    bytes: ByteArray,
    vararg options: OptionWithoutDescription
): OutcomingEvent = directMessageWithImage("", contentType, bytes, *options)

/**
 * Creates a direct message with a video (Max 15MB)
 * @see https://developer.twitter.com/en/docs/direct-messages/message-attachments/overview
 */
fun <T : Bus<T>> T.directMessageWithVideo(
    message: CharSequence,
    contentType: String,
    bytes: ByteArray,
    vararg options: Option
): OutcomingEvent = directMessageWithAttachment(message, MediaCategory.VIDEO, contentType, bytes, *options)

/**
 * Creates a direct message with a video (Max 15MB)
 * @see https://developer.twitter.com/en/docs/direct-messages/message-attachments/overview
 */
fun <T : Bus<T>> T.directMessageWithVideo(
    message: CharSequence,
    contentType: String,
    bytes: ByteArray,
    vararg options: OptionWithoutDescription
): OutcomingEvent = directMessageWithAttachment(message, MediaCategory.VIDEO, contentType, bytes, *options)

/**
 * Creates a direct message with a video (Max 15MB)
 * @see https://developer.twitter.com/en/docs/direct-messages/message-attachments/overview
 */
fun <T : Bus<T>> T.directMessageWithVideo(
    contentType: String,
    bytes: ByteArray,
    vararg options: Option
): OutcomingEvent = directMessageWithVideo("", contentType, bytes, *options)

/**
 * Creates a direct message with a video (Max 15MB)
 * @see https://developer.twitter.com/en/docs/direct-messages/message-attachments/overview
 */
fun <T : Bus<T>> T.directMessageWithVideo(
    contentType: String,
    bytes: ByteArray,
    vararg options: OptionWithoutDescription
): OutcomingEvent = directMessageWithVideo("", contentType, bytes, *options)

/**
 * Creates a url button
 * @see https://developer.twitter.com/en/docs/direct-messages/buttons/api-reference/buttons
 */
fun <T : Bus<T>> T.webUrl(
    label: CharSequence,
    url: CharSequence
): WebUrl {
    val l = translate(label)
    if (l.length > MAX_OPTION_LABEL) {
        logger.warn { "label $l has more than $MAX_OPTION_LABEL chars, it will be truncated" }
        return WebUrl(l.truncateIfLongerThan(MAX_OPTION_LABEL), url.toString())
    }
    return WebUrl(l.toString(), url.toString())
}

/**
 * Creates an Option Quick Reply
 * @see https://developer.twitter.com/en/docs/direct-messages/quick-replies/overview
 */
fun <T : Bus<T>> T.option(
    label: CharSequence,
    description: CharSequence,
    targetIntent: IntentAware,
    step: StoryStep<out StoryHandlerDefinition>? = null,
    vararg parameters: Pair<String, String>
): Option =
    option(label, description, targetIntent.wrappedIntent(), step, parameters.toMap())

/**
 * Creates an Option Quick Reply without description
 * @see https://developer.twitter.com/en/docs/direct-messages/quick-replies/overview
 */
fun <T : Bus<T>> T.option(
    label: CharSequence,
    targetIntent: IntentAware,
    step: StoryStep<out StoryHandlerDefinition>? = null,
    vararg parameters: Pair<String, String>
): OptionWithoutDescription =
    option(label, targetIntent.wrappedIntent(), step, parameters.toMap())

/**
 * Creates an Option Quick Reply
 * @see https://developer.twitter.com/en/docs/direct-messages/quick-replies/overview
 */
fun <T : Bus<T>> T.option(
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
 * Creates an Option Quick Reply without description
 * @see https://developer.twitter.com/en/docs/direct-messages/quick-replies/overview
 */
fun <T : Bus<T>> T.option(
    label: CharSequence,
    targetIntent: IntentAware,
    step: StoryStep<out StoryHandlerDefinition>? = null,
    parameters: Map<String, String>
): OptionWithoutDescription =
    option(label, targetIntent, step, parameters) { intent, s, params ->
        SendChoice.encodeChoiceId(this, intent, s, params)
    }

/**
 * Creates an Option Quick Reply
 * @see https://developer.twitter.com/en/docs/direct-messages/quick-replies/overview
 */
private fun <T : Bus<T>> T.option(
    label: CharSequence,
    description: CharSequence,
    targetIntent: IntentAware,
    step: StoryStep<out StoryHandlerDefinition>? = null,
    parameters: Map<String, String>,
    metadataEncoder: (IntentAware, StoryStep<out StoryHandlerDefinition>?, Map<String, String>) -> String
): Option = Option.of(translate(label).toString(), translate(description).toString(), metadataEncoder.invoke(targetIntent, step, parameters))

/**
 * Creates an Option Quick Reply without description
 * @see https://developer.twitter.com/en/docs/direct-messages/quick-replies/overview
 */
private fun <T : Bus<T>> T.option(
    label: CharSequence,
    targetIntent: IntentAware,
    step: StoryStep<out StoryHandlerDefinition>? = null,
    parameters: Map<String, String>,
    metadataEncoder: (IntentAware, StoryStep<out StoryHandlerDefinition>?, Map<String, String>) -> String
): OptionWithoutDescription = OptionWithoutDescription.of(translate(label).toString(), metadataEncoder.invoke(targetIntent, step, parameters))

/**
 * Creates a NLP Option Quick Reply without description
 * @see https://developer.twitter.com/en/docs/direct-messages/quick-replies/overview
 */
fun I18nTranslator.nlpOption(label: CharSequence): OptionWithoutDescription {
    val l = translate(label).toString()
    return OptionWithoutDescription.of(l, SendChoice.encodeNlpChoiceId(l))
}

/**
 * Creates a NLP Option Quick Reply without description
 * @see https://developer.twitter.com/en/docs/direct-messages/quick-replies/overview
 */
fun I18nTranslator.nlpOption(label: CharSequence, description: CharSequence): Option {
    val l = translate(label).toString()
    val d = translate(description).toString()
    return Option.of(l, SendChoice.encodeNlpChoiceId(l), d)
}

/**
 * Adds a Twitter [ConnectorMessage] if the current connector is Twitter and the interface is not public.
 * You need to call [BotBus.send] or [BotBus.end] later to send this message.
 */
fun BotBus.withTwitter(messageProvider: () -> TwitterConnectorMessage): BotBus {
    withVisibility(actionVisibility(this))
    return if (actionVisibility(this) != ActionVisibility.PUBLIC) {
        withMessage(twitterConnectorType, messageProvider)
    } else {
        this
    }
}

/**
 * Adds a Twitter [ConnectorMessage] if the current connector is Twitter and the current connector is [connectorId].
 * You need to call [<T : Bus<T>> T.send] or [<T : Bus<T>> T.end] later to send this message.
 */
fun BotBus.withTwitter(connectorId: String, messageProvider: () -> TwitterConnectorMessage): BotBus {
    withVisibility(actionVisibility(this))
    return if (actionVisibility(this) != ActionVisibility.PUBLIC) {
        withMessage(twitterConnectorType, connectorId, messageProvider)
    } else {
        this
    }
}

/**
 * Adds a Twitter [ConnectorMessage] if the current connector is Twitter and the interface is public.
 * You need to call [BotBus.send] or [BotBus.end] later to send this message.
 */
fun BotBus.withPublicTwitter(messageProvider: () -> TwitterPublicConnectorMessage): BotBus {
    withVisibility(actionVisibility(this))
    return if (actionVisibility(this) == ActionVisibility.PUBLIC) {
        withMessage(twitterConnectorType, messageProvider)
    } else {
        this
    }
}

/**
 * End the conversation only if the visibility is public
 */
fun BotBus.endIfPublicTwitter() {
    if (targetConnectorType == twitterConnectorType &&
        actionVisibility(this) == ActionVisibility.PUBLIC
    ) {
        end()
    }
}

private fun actionVisibility(botBus: BotBus) = botBus.action.metadata.visibility

/**
 * Create a tweet
 * @see https://developer.twitter.com/en/docs/tweets/post-and-engage/overview
 */
fun BotBus.tweet(message: CharSequence): Tweet {
    return Tweet(translate(message).toString())
}

/**
 * Create a tweet with a link for DM to the account listened
 * @see https://developer.twitter.com/en/docs/tweets/post-and-engage/overview
 * @see https://developer.twitter.com/en/docs/direct-messages/welcome-messages/guides/deeplinking-to-welcome-message
 */
fun BotBus.tweetWithInviteForDM(message: CharSequence, welcomeMessageID: String? = null, defaultMessage: String? = null): Tweet {
    return Tweet(translate(message).toString(), botId.id, welcomeMessageID, defaultMessage)
}
