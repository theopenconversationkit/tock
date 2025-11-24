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

package ai.tock.bot.connector.messenger

import ai.tock.bot.connector.ConnectorMessage
import ai.tock.bot.connector.ConnectorType
import ai.tock.bot.connector.messenger.model.MessengerConnectorMessage
import ai.tock.bot.connector.messenger.model.send.Attachment
import ai.tock.bot.connector.messenger.model.send.AttachmentMessage
import ai.tock.bot.connector.messenger.model.send.AttachmentType
import ai.tock.bot.connector.messenger.model.send.Button
import ai.tock.bot.connector.messenger.model.send.ButtonPayload
import ai.tock.bot.connector.messenger.model.send.CallButton
import ai.tock.bot.connector.messenger.model.send.Element
import ai.tock.bot.connector.messenger.model.send.EmailQuickReply
import ai.tock.bot.connector.messenger.model.send.GenericPayload
import ai.tock.bot.connector.messenger.model.send.LoginButton
import ai.tock.bot.connector.messenger.model.send.LogoutButton
import ai.tock.bot.connector.messenger.model.send.MediaElement
import ai.tock.bot.connector.messenger.model.send.MediaPayload
import ai.tock.bot.connector.messenger.model.send.MediaType
import ai.tock.bot.connector.messenger.model.send.PostbackButton
import ai.tock.bot.connector.messenger.model.send.QuickReply
import ai.tock.bot.connector.messenger.model.send.TextMessage
import ai.tock.bot.connector.messenger.model.send.TextQuickReply
import ai.tock.bot.connector.messenger.model.send.UrlButton
import ai.tock.bot.connector.messenger.model.send.UrlPayload
import ai.tock.bot.connector.messenger.model.send.UserAction
import ai.tock.bot.connector.messenger.model.send.UserAction.Companion.extractButtons
import ai.tock.bot.connector.messenger.model.send.UserAction.Companion.extractQuickReplies
import ai.tock.bot.definition.Intent
import ai.tock.bot.definition.IntentAware
import ai.tock.bot.definition.Parameters
import ai.tock.bot.definition.StoryStepDef
import ai.tock.bot.engine.Bus
import ai.tock.bot.engine.I18nTranslator
import ai.tock.bot.engine.action.ActionMetadata
import ai.tock.bot.engine.action.ActionNotificationType
import ai.tock.bot.engine.action.ActionPriority
import ai.tock.bot.engine.action.SendChoice
import ai.tock.bot.engine.action.SendSentence
import ai.tock.bot.engine.user.PlayerId
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

internal const val MESSENGER_CONNECTOR_TYPE_ID = "messenger"

/**
 * The Messenger connector type.
 */
val messengerConnectorType = ConnectorType(MESSENGER_CONNECTOR_TYPE_ID)

/**
 * Sends a Messenger message only if the [ConnectorType] of the current [Bus] is [messengerConnectorType].
 */
fun <T : Bus<T>> T.sendToMessenger(
    delay: Long = defaultDelay(currentAnswerIndex),
    messageProvider: T.() -> MessengerConnectorMessage,
): T {
    if (isCompatibleWith(messengerConnectorType)) {
        withMessage(messageProvider(this))
        send(delay)
    }
    return this
}

/**
 * Sends a Messenger message as last bot answer, only if the [ConnectorType] of the current [Bus] is [messengerConnectorType].
 */
fun <T : Bus<T>> T.endForMessenger(
    delay: Long = defaultDelay(currentAnswerIndex),
    messageProvider: T.() -> MessengerConnectorMessage,
): T {
    if (isCompatibleWith(messengerConnectorType)) {
        withMessage(messageProvider(this))
        end(delay)
    }
    return this
}

/**
 * Adds a Messenger [ConnectorMessage] if the current connector is Messenger.
 * You need to call [<T : Bus<T>> T.send] or [<T : Bus<T>> T.end] later to send this message.
 */
fun <T : Bus<T>> T.withMessenger(messageProvider: () -> MessengerConnectorMessage): T {
    return withMessage(messengerConnectorType, messageProvider)
}

/**
 * Adds a Messenger [ConnectorMessage] if the current connector is Messenger and the current connector is [connectorId].
 * You need to call [<T : Bus<T>> T.send] or [<T : Bus<T>> T.end] later to send this message.
 */
fun <T : Bus<T>> T.withMessenger(
    connectorId: String,
    messageProvider: () -> MessengerConnectorMessage,
): T {
    return withMessage(messengerConnectorType, connectorId, messageProvider)
}

/**
 * Creates a button template [https://developers.facebook.com/docs/messenger-platform/send-api-reference/button-template]
 */
fun I18nTranslator.buttonsTemplate(
    text: CharSequence,
    vararg actions: UserAction,
): AttachmentMessage = buttonsTemplate(text, actions.toList())

/**
 * Creates a button template [https://developers.facebook.com/docs/messenger-platform/send-api-reference/button-template]
 */
fun I18nTranslator.buttonsTemplate(
    text: CharSequence,
    actions: List<UserAction>,
): AttachmentMessage {
    val buttons = extractButtons(actions)
    if (buttons.isEmpty() || buttons.size > 4) {
        error("buttonsTemplate must have at least 1 button and at most 3")
    }

    var payloadText = translate(text).toString()
    if (payloadText.length > 640) {
        logger.warn { "text $payloadText in buttonTemplate should not exceed 640 chars." }
        payloadText = payloadText.substring(0, 637) + "..."
    }

    return AttachmentMessage(
        Attachment(
            AttachmentType.template,
            ButtonPayload(
                payloadText,
                buttons,
            ),
        ),
        extractQuickReplies(actions.toList()),
    )
}

/**
 * Creates a media template [https://developers.facebook.com/docs/messenger-platform/send-messages/template/media]
 */
fun mediaTemplate(
    mediaUrl: String,
    mediaType: MediaType = MediaType.image,
    sharable: Boolean = false,
    vararg actions: UserAction,
): AttachmentMessage = mediaTemplate(mediaUrl, mediaType, sharable, actions.toList())

/**
 * Creates a media template [https://developers.facebook.com/docs/messenger-platform/send-messages/template/media]
 */
fun mediaTemplate(
    mediaUrl: String,
    mediaType: MediaType = MediaType.image,
    sharable: Boolean = false,
    actions: List<UserAction> = emptyList(),
): AttachmentMessage {
    return AttachmentMessage(
        Attachment(
            AttachmentType.template,
            MediaPayload(
                listOf(
                    MediaElement(
                        mediaType,
                        mediaUrl,
                        extractButtons(actions).run { if (isEmpty()) null else this },
                    ),
                ),
                sharable,
            ),
        ),
        extractQuickReplies(actions),
    )
}

/**
 * Creates a [generic template](https://developers.facebook.com/docs/messenger-platform/send-messages/template/generic).
 */
fun genericTemplate(vararg elements: Element): AttachmentMessage {
    return genericTemplate(elements.toList())
}

/**
 * Creates a [generic template](https://developers.facebook.com/docs/messenger-platform/send-messages/template/generic).
 */
fun genericTemplate(
    elements: List<Element>,
    quickReplies: List<QuickReply>,
): AttachmentMessage {
    if (elements.isEmpty() || elements.size > 10) {
        error("genericTemplate must have at least 1 elements and at most 10")
    }

    return AttachmentMessage(
        Attachment(
            AttachmentType.template,
            GenericPayload(
                elements,
            ),
        ),
        quickReplies.takeUnless { it.isEmpty() },
    )
}

/**
 * Creates a [generic template](https://developers.facebook.com/docs/messenger-platform/send-messages/template/generic).
 */
fun genericTemplate(
    elements: List<Element>,
    vararg quickReplies: QuickReply,
): AttachmentMessage = genericTemplate(elements, quickReplies.toList())

/**
 * Creates an [attachment](https://developers.facebook.com/docs/messenger-platform/reference/send-api/#attachment).
 */
fun <T : Bus<T>> T.attachment(
    attachmentUrl: String,
    type: AttachmentType,
    vararg quickReplies: QuickReply,
): AttachmentMessage = attachment(attachmentUrl, type, quickReplies.toList())

/**
 * Creates an [attachment](https://developers.facebook.com/docs/messenger-platform/reference/send-api/#attachment).
 */
fun <T : Bus<T>> T.attachment(
    attachmentUrl: String,
    type: AttachmentType,
    quickReplies: List<QuickReply>,
): AttachmentMessage {
    return when (type) {
        AttachmentType.image -> cachedAttachment(attachmentUrl, AttachmentType.image, quickReplies = quickReplies)
        AttachmentType.audio -> cachedAttachment(attachmentUrl, AttachmentType.audio, quickReplies = quickReplies)
        AttachmentType.video -> cachedAttachment(attachmentUrl, AttachmentType.video, quickReplies = quickReplies)
        AttachmentType.file -> cachedAttachment(attachmentUrl, AttachmentType.file, quickReplies = quickReplies)
        else -> {
            error { "not supported attachment type $type" }
        }
    }
}

private fun <T : Bus<T>> T.cachedAttachment(
    attachmentUrl: String,
    type: AttachmentType,
    useCache: Boolean = MessengerConfiguration.reuseAttachmentByDefault,
    quickReplies: List<QuickReply>,
): AttachmentMessage {
    return AttachmentMessage(
        Attachment(
            type,
            UrlPayload.getUrlPayload(applicationId, attachmentUrl, useCache && !test),
        ),
        quickReplies.run { if (isEmpty()) null else this },
    )
}

/**
 * Creates an [image] as attachment (https://developers.facebook.com/docs/messenger-platform/reference/send-api/#attachment).
 */
fun <T : Bus<T>> T.image(
    imageUrl: String,
    vararg quickReplies: QuickReply,
): AttachmentMessage = image(imageUrl, quickReplies.toList())

/**
 * Creates an [image] as attachment (https://developers.facebook.com/docs/messenger-platform/reference/send-api/#attachment).
 */
fun <T : Bus<T>> T.image(
    imageUrl: String,
    quickReplies: List<QuickReply>,
): AttachmentMessage = cachedAttachment(imageUrl, AttachmentType.image, quickReplies = quickReplies)

/**
 * Creates an [audio file] as attachment (https://developers.facebook.com/docs/messenger-platform/reference/send-api/#attachment).
 */
fun <T : Bus<T>> T.audio(
    audioUrl: String,
    vararg quickReplies: QuickReply,
): AttachmentMessage = audio(audioUrl, quickReplies.toList())

/**
 * Creates an [audio file] as attachment (https://developers.facebook.com/docs/messenger-platform/reference/send-api/#attachment).
 */
fun <T : Bus<T>> T.audio(
    audioUrl: String,
    quickReplies: List<QuickReply>,
): AttachmentMessage = cachedAttachment(audioUrl, AttachmentType.audio, quickReplies = quickReplies.toList())

/**
 * Creates a [video] as attachment (https://developers.facebook.com/docs/messenger-platform/reference/send-api/#attachment).
 */
fun <T : Bus<T>> T.video(
    videoUrl: String,
    vararg quickReplies: QuickReply,
): AttachmentMessage = video(videoUrl, quickReplies.toList())

/**
 * Creates a [video] as attachment (https://developers.facebook.com/docs/messenger-platform/reference/send-api/#attachment).
 */
fun <T : Bus<T>> T.video(
    videoUrl: String,
    quickReplies: List<QuickReply>,
): AttachmentMessage = cachedAttachment(videoUrl, AttachmentType.video, quickReplies = quickReplies)

/**
 * Creates a text with quick replies.
 */
fun I18nTranslator.text(
    text: CharSequence,
    vararg quickReplies: QuickReply,
): TextMessage = text(text, quickReplies.toList())

/**
 * Creates a text with quick replies.
 */
fun I18nTranslator.text(
    text: CharSequence,
    quickReplies: List<QuickReply>,
): TextMessage = TextMessage(translate(text).toString(), quickReplies)

/**
 * Creates a [generic element](https://developers.facebook.com/docs/messenger-platform/send-messages/template/generic).
 */
fun I18nTranslator.genericElement(
    title: CharSequence,
    subtitle: CharSequence? = null,
    imageUrl: String? = null,
    buttons: List<Button>? = null,
): Element {
    val t = translate(title)
    val s = translateAndReturnBlankAsNull(subtitle)
    if (t.length > 80) {
        logger.warn { "title $t has more than 80 chars" }
    }
    if ((s?.length ?: 0) > 80) {
        logger.warn { "subtitle $s has more than 80 chars" }
    }
    if (buttons?.size ?: 0 > 3) {
        error("Number of buttons > 3 : $buttons")
    }
    return Element(t, s, imageUrl, buttons)
}

/**
 * Provides a [Log In Button](https://developers.facebook.com/docs/messenger-platform/reference/buttons/login).
 */
fun loginButton(url: String): LoginButton = LoginButton(url)

/**
 * Provides a [Log Out Button](https://developers.facebook.com/docs/messenger-platform/reference/buttons/logout).
 */
fun logoutButton(): LogoutButton = LogoutButton()

/**
 * This quick reply does not use any custom payload, but the [textToSend] will we parsed by the NLP engine.
 */
fun I18nTranslator.nlpQuickReply(
    title: CharSequence,
    textToSend: CharSequence = title,
    imageUrl: String? = null,
): QuickReply =
    TextQuickReply(
        translate(title).toString(),
        SendChoice.encodeNlpChoiceId(translate(textToSend).toString()),
        imageUrl,
    )

/**
 * Creates a [quick reply](https://developers.facebook.com/docs/messenger-platform/send-messages/quick-replies)
 * from an [I18nTranslator].
 */
@Deprecated("use notify method for standalone")
fun I18nTranslator.standaloneQuickReply(
    /**
     * The title of the quick reply.
     */
    title: CharSequence,
    /**
     * The target intent.
     */
    targetIntent: IntentAware,
    /**
     * The custom parameters.
     */
    parameters: Parameters = Parameters(),
    /**
     * The target step.
     */
    step: StoryStepDef? = null,
    /**
     * The image url of the quick reply.
     */
    imageUrl: String? = null,
    /**
     * The current step of the Bus<T>.
     */
    busStep: StoryStepDef? = null,
    /**
     * The current intent of the Bus<T>.
     */
    currentIntent: Intent? = null,
    /**
     * The app id emitter.
     */
    sourceAppId: String?,
): QuickReply =
    quickReply(title, targetIntent, imageUrl, step?.name, parameters.toMap()) { intent, s, params ->
        SendChoice.encodeChoiceId(intent, s, params, busStep?.name, currentIntent, sourceAppId = sourceAppId)
    }

/**
 * Creates a [quick reply](https://developers.facebook.com/docs/messenger-platform/send-messages/quick-replies).
 */
fun <T : Bus<T>> T.quickReply(
    title: CharSequence,
    targetIntent: IntentAware,
    parameters: Parameters,
): QuickReply = quickReply(title, targetIntent, null, stepName, parameters.toMap())

/**
 * Creates a [quick reply](https://developers.facebook.com/docs/messenger-platform/send-messages/quick-replies).
 */
fun <T : Bus<T>> T.quickReply(
    title: CharSequence,
    targetIntent: IntentAware,
    imageUrl: String? = null,
    step: StoryStepDef? = null,
    parameters: Parameters,
): QuickReply = quickReply(title, targetIntent, imageUrl, step, parameters.toMap())

/**
 * Create a [quick reply](https://developers.facebook.com/docs/messenger-platform/send-messages/quick-replies).
 */
fun <T : Bus<T>> T.quickReply(
    title: CharSequence,
    targetIntent: IntentAware,
    imageUrl: String? = null,
    step: StoryStepDef? = null,
    vararg parameters: Pair<String, String>,
): QuickReply = quickReply(title, targetIntent.wrappedIntent(), imageUrl, step, parameters.toMap())

/**
 * Creates a [quick reply](https://developers.facebook.com/docs/messenger-platform/send-messages/quick-replies).
 */
fun <T : Bus<T>> T.quickReply(
    title: CharSequence,
    targetIntent: IntentAware,
    imageUrl: String? = null,
    step: StoryStepDef? = null,
    parameters: Collection<Pair<String, String>>,
): QuickReply = quickReply(title, targetIntent, imageUrl, step, parameters.toMap())

/**
 * Creates a [quick reply](https://developers.facebook.com/docs/messenger-platform/send-messages/quick-replies).
 */
fun <T : Bus<T>> T.quickReply(
    title: CharSequence,
    targetIntent: IntentAware,
    imageUrl: String? = null,
    step: StoryStepDef? = null,
    parameters: Map<String, String>,
): QuickReply = quickReply(title, targetIntent, imageUrl, step?.name, parameters)

private fun <T : Bus<T>> T.quickReply(
    title: CharSequence,
    targetIntent: IntentAware,
    imageUrl: String? = null,
    step: String? = null,
    parameters: Map<String, String>,
): QuickReply =
    quickReply(title, targetIntent, imageUrl, step, parameters) { intent, s, params ->
        SendChoice.encodeChoiceId(this, intent, s, params)
    }

private fun I18nTranslator.quickReply(
    title: CharSequence,
    targetIntent: IntentAware,
    imageUrl: String? = null,
    step: String? = null,
    parameters: Map<String, String>,
    payloadEncoder: (IntentAware, String?, Map<String, String>) -> String,
): QuickReply {
    val t = translate(title)
    if (t.length > 20) {
        logger.warn { "title $t has more than 20 chars" }
    }
    val payload = payloadEncoder.invoke(targetIntent, step, parameters)
    if (payload.length > 1000) {
        logger.warn { "payload $payload has more than 1000 chars" }
    }
    return TextQuickReply(t.toString(), payload, imageUrl)
}

/**
 * Creates a [quick reply email](https://developers.facebook.com/docs/messenger-platform/send-messages/quick-replies).
 */

fun emailQuickReply(): QuickReply = EmailQuickReply()

/**
 * Creates a [postback button](https://developers.facebook.com/docs/messenger-platform/send-messages/buttons#postback).
 */
fun <T : Bus<T>> T.postbackButton(
    title: CharSequence,
    targetIntent: IntentAware,
    vararg parameters: Pair<String, String>,
): PostbackButton = postbackButton<T>(title, targetIntent, null, *parameters)

/**
 * Creates a [postback button](https://developers.facebook.com/docs/messenger-platform/send-messages/buttons#postback).
 */
fun <T : Bus<T>> T.postbackButton(
    title: CharSequence,
    targetIntent: IntentAware,
    parameters: Parameters,
): PostbackButton = postbackButton(title, targetIntent, null, parameters)

/**
 * Creates a [postback button](https://developers.facebook.com/docs/messenger-platform/send-messages/buttons#postback).
 */
fun <T : Bus<T>> T.postbackButton(
    title: CharSequence,
    targetIntent: IntentAware,
    step: StoryStepDef? = null,
    parameters: Parameters,
): PostbackButton = postbackButton(title, targetIntent, step, *parameters.toArray())

/**
 * Creates a [postback button](https://developers.facebook.com/docs/messenger-platform/send-messages/buttons#postback).
 */
fun <T : Bus<T>> T.postbackButton(
    title: CharSequence,
    targetIntent: IntentAware,
    step: StoryStepDef? = null,
    vararg parameters: Pair<String, String>,
): PostbackButton =
    postbackButton(
        title,
        targetIntent,
        step,
        parameters.toMap(),
    ) { intent, s, params ->
        SendChoice.encodeChoiceId(this, intent, s, params)
    }

private fun I18nTranslator.postbackButton(
    title: CharSequence,
    targetIntent: IntentAware,
    step: StoryStepDef? = null,
    parameters: Map<String, String>,
    payloadEncoder: (IntentAware, StoryStepDef?, Map<String, String>) -> String,
): PostbackButton {
    val t = translate(title)
    if (t.length > 20) {
        logger.warn { "title $t has more than 20 chars" }
    }
    val payload = payloadEncoder.invoke(targetIntent, step, parameters.toMap())
    if (payload.length > 1000) {
        logger.warn { "payload $payload has more than 1000 chars" }
    }
    return PostbackButton(payload, t.toString())
}

/**
 * This button does not use any custom payload, but the [textToSend] will we parsed by the NLP engine.
 */
fun I18nTranslator.nlpPostbackButton(
    title: CharSequence,
    textToSend: CharSequence = title,
): PostbackButton =
    PostbackButton(
        SendChoice.encodeNlpChoiceId(translate(textToSend).toString()),
        translate(title).toString(),
    )

fun I18nTranslator.callToButton(
    title: CharSequence,
    phoneNumber: String,
): CallButton {
    val t = translate(title)
    if (t.length > 20) {
        logger.warn { "title $t has more than 20 chars" }
    }
    return CallButton(t.toString(), phoneNumber)
}

/**
 * Creates an [url button](https://developers.facebook.com/docs/messenger-platform/send-messages/buttons#url).
 */
fun I18nTranslator.urlButton(
    title: CharSequence,
    url: String,
): UrlButton {
    val t = translate(title)
    if (t.length > 20) {
        logger.warn { "title $t has more than 20 chars" }
    }
    return UrlButton(url, t.toString())
}

/**
 * Used to generate a [MessengerConnectorMessage] event,
 * usually sent later by [MessengerConnector.send] or [MessengerConnector.sendOptInEvent].
 */
@Deprecated("use notify method for standalone")
fun standaloneMessengerAnswer(
    playerId: PlayerId,
    applicationId: String,
    recipientId: PlayerId,
    lastAnswer: Boolean = true,
    /** Significance deals with the notification level. */
    priority: ActionPriority = ActionPriority.normal,
    /** tag deals with type of message notification. */
    notificationType: ActionNotificationType? = null,
    messageProvider: () -> MessengerConnectorMessage,
): SendSentence =
    SendSentence(
        playerId,
        applicationId,
        recipientId,
        null,
        messages = mutableListOf(messageProvider()),
        metadata =
            ActionMetadata(
                lastAnswer,
                priority,
                notificationType,
            ),
    )

/**
 * Used to generate multiple [MessengerConnectorMessage] events,
 * usually sent later by [MessengerConnector.send] or [MessengerConnector.sendOptInEvent].
 */
@Deprecated("use notify method for standalone")
fun standaloneMessengerAnswers(
    playerId: PlayerId,
    applicationId: String,
    recipientId: PlayerId,
    /** Significance deals with the notification level. */
    priority: ActionPriority = ActionPriority.normal,
    /** tag deals with type of message notification. */
    notificationType: ActionNotificationType? = null,
    messagesProvider: () -> List<MessengerConnectorMessage>,
): List<SendSentence> =
    messagesProvider().run {
        mapIndexed { i, m ->
            standaloneMessengerAnswer(
                playerId,
                applicationId,
                recipientId,
                i == size - 1,
                priority,
                notificationType,
            ) { m }
        }
    }
