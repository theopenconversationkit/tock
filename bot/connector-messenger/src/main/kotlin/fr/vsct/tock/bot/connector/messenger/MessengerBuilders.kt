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

package fr.vsct.tock.bot.connector.messenger

import fr.vsct.tock.bot.connector.ConnectorMessage
import fr.vsct.tock.bot.connector.ConnectorType
import fr.vsct.tock.bot.connector.messenger.model.MessengerConnectorMessage
import fr.vsct.tock.bot.connector.messenger.model.send.Attachment
import fr.vsct.tock.bot.connector.messenger.model.send.AttachmentMessage
import fr.vsct.tock.bot.connector.messenger.model.send.AttachmentType
import fr.vsct.tock.bot.connector.messenger.model.send.Button
import fr.vsct.tock.bot.connector.messenger.model.send.ButtonPayload
import fr.vsct.tock.bot.connector.messenger.model.send.CallButton
import fr.vsct.tock.bot.connector.messenger.model.send.Element
import fr.vsct.tock.bot.connector.messenger.model.send.EmailQuickReply
import fr.vsct.tock.bot.connector.messenger.model.send.GenericPayload
import fr.vsct.tock.bot.connector.messenger.model.send.ListElementStyle
import fr.vsct.tock.bot.connector.messenger.model.send.ListPayload
import fr.vsct.tock.bot.connector.messenger.model.send.LocationQuickReply
import fr.vsct.tock.bot.connector.messenger.model.send.LoginButton
import fr.vsct.tock.bot.connector.messenger.model.send.LogoutButton
import fr.vsct.tock.bot.connector.messenger.model.send.MediaElement
import fr.vsct.tock.bot.connector.messenger.model.send.MediaPayload
import fr.vsct.tock.bot.connector.messenger.model.send.MediaType
import fr.vsct.tock.bot.connector.messenger.model.send.PostbackButton
import fr.vsct.tock.bot.connector.messenger.model.send.QuickReply
import fr.vsct.tock.bot.connector.messenger.model.send.TextMessage
import fr.vsct.tock.bot.connector.messenger.model.send.TextQuickReply
import fr.vsct.tock.bot.connector.messenger.model.send.UrlButton
import fr.vsct.tock.bot.connector.messenger.model.send.UrlPayload
import fr.vsct.tock.bot.connector.messenger.model.send.UserAction
import fr.vsct.tock.bot.connector.messenger.model.send.UserAction.Companion.extractButtons
import fr.vsct.tock.bot.connector.messenger.model.send.UserAction.Companion.extractQuickReplies
import fr.vsct.tock.bot.definition.Intent
import fr.vsct.tock.bot.definition.IntentAware
import fr.vsct.tock.bot.definition.Parameters
import fr.vsct.tock.bot.definition.StoryHandlerDefinition
import fr.vsct.tock.bot.definition.StoryStep
import fr.vsct.tock.bot.engine.Bus
import fr.vsct.tock.bot.engine.I18nTranslator
import fr.vsct.tock.bot.engine.action.ActionMetadata
import fr.vsct.tock.bot.engine.action.ActionNotificationType
import fr.vsct.tock.bot.engine.action.ActionPriority
import fr.vsct.tock.bot.engine.action.SendChoice
import fr.vsct.tock.bot.engine.action.SendSentence
import fr.vsct.tock.bot.engine.user.PlayerId
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
    messageProvider: T.() -> MessengerConnectorMessage
): T {
    if (targetConnectorType == messengerConnectorType) {
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
    messageProvider: T.() -> MessengerConnectorMessage
): T {
    if (targetConnectorType == messengerConnectorType) {
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
 * Creates a button template [https://developers.facebook.com/docs/messenger-platform/send-api-reference/button-template]
 */
fun I18nTranslator.buttonsTemplate(text: CharSequence, vararg actions: UserAction): AttachmentMessage =
    buttonsTemplate(text, actions.toList())

/**
 * Creates a button template [https://developers.facebook.com/docs/messenger-platform/send-api-reference/button-template]
 */
fun I18nTranslator.buttonsTemplate(text: CharSequence, actions: List<UserAction> = emptyList()): AttachmentMessage {
    return AttachmentMessage(
        Attachment(
            AttachmentType.template,
            ButtonPayload(
                translate(text).toString(),
                extractButtons(actions)
            )
        ),
        extractQuickReplies(actions.toList())
    )
}

/**
 * Creates a media template [https://developers.facebook.com/docs/messenger-platform/send-messages/template/media]
 */
fun mediaTemplate(
    mediaUrl: String,
    mediaType: MediaType = MediaType.image,
    sharable: Boolean = false,
    vararg actions: UserAction
): AttachmentMessage = mediaTemplate(mediaUrl, mediaType, sharable, actions.toList())

/**
 * Creates a media template [https://developers.facebook.com/docs/messenger-platform/send-messages/template/media]
 */
fun mediaTemplate(
    mediaUrl: String,
    mediaType: MediaType = MediaType.image,
    sharable: Boolean = false,
    actions: List<UserAction> = emptyList()
): AttachmentMessage {
    return AttachmentMessage(
        Attachment(
            AttachmentType.template,
            MediaPayload(
                listOf(
                    MediaElement(
                        mediaType,
                        mediaUrl,
                        extractButtons(actions).run { if (isEmpty()) null else this })
                ),
                sharable
            )
        ),
        extractQuickReplies(actions)
    )
}

/**
 * ListTemplate does not support list with exactly one element.
 * This function generates a generic template if there is one element,
 * or a classic list element if there is between 2 and 4.
 */
@Deprecated("messenger lists are deprecated")
fun flexibleListTemplate(
    elements: List<Element>,
    topElementStyle: ListElementStyle? = null,
    vararg actions: UserAction
): AttachmentMessage =
    flexibleListTemplate(elements, topElementStyle, actions.toList())

/**
 * ListTemplate does not support list with exactly one element.
 * This function generates a generic template if there is one element,
 * or a classic list element if there is between 2 and 4.
 */
@Deprecated("messenger lists are deprecated")
fun flexibleListTemplate(
    elements: List<Element>,
    topElementStyle: ListElementStyle? = null,
    actions: List<UserAction> = emptyList()
): AttachmentMessage {
    return if (elements.size == 1) {
        genericTemplate(elements, *actions.filterIsInstance(QuickReply::class.java).toTypedArray())
    } else {
        listTemplate(elements.take(4), topElementStyle, actions)
    }
}

/**
 * Creates a [list template](https://developers.facebook.com/docs/messenger-platform/send-messages/template/list).
 */
@Deprecated("messenger lists are deprecated")
fun listTemplate(
    e1: Element,
    e2: Element,
    e3: Element? = null,
    e4: Element? = null,
    topElementStyle: ListElementStyle? = null,
    vararg actions: UserAction
): AttachmentMessage =
    listTemplate(e1, e2, e3, e4, topElementStyle, actions.toList())

/**
 * Creates a [list template](https://developers.facebook.com/docs/messenger-platform/send-messages/template/list).
 */
@Deprecated("messenger lists are deprecated")
fun listTemplate(
    e1: Element,
    e2: Element,
    e3: Element? = null,
    e4: Element? = null,
    topElementStyle: ListElementStyle? = null,
    actions: List<UserAction> = emptyList()
): AttachmentMessage {
    return listTemplate(listOfNotNull(e1, e2, e3, e4), topElementStyle, actions)
}

/**
 * Creates a [list template](https://developers.facebook.com/docs/messenger-platform/send-messages/template/list).
 */
@Deprecated("messenger lists are deprecated")
fun listTemplate(
    elements: List<Element>,
    topElementStyle: ListElementStyle? = null,
    vararg actions: UserAction
): AttachmentMessage = listTemplate(elements, topElementStyle, actions.toList())

/**
 * Creates a [list template](https://developers.facebook.com/docs/messenger-platform/send-messages/template/list).
 */
@Deprecated("messenger lists are deprecated")
fun listTemplate(
    elements: List<Element>,
    topElementStyle: ListElementStyle? = null,
    actions: List<UserAction> = emptyList()
): AttachmentMessage {
    if (elements.size < 2 || elements.size > 4) {
        error("must have at least 2 elements and at most 4")
    }
    if (topElementStyle != ListElementStyle.compact
        && elements.any { it.imageUrl == null }
    ) {
        error("imageUrl of elements may not be null with large element style")
    }

    return AttachmentMessage(
        Attachment(
            AttachmentType.template,
            ListPayload(
                elements,
                topElementStyle,
                extractButtons(actions)
                    .run {
                        if (isEmpty()) null
                        else if (size > 1) error("only one button max")
                        else this
                    })
        ),
        extractQuickReplies(actions)
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
fun genericTemplate(elements: List<Element>, quickReplies: List<QuickReply>): AttachmentMessage {
    if (elements.isEmpty() || elements.size > 10) {
        error("must have at least 1 elements and at most 10")
    }

    return AttachmentMessage(
        Attachment(
            AttachmentType.template,
            GenericPayload(
                elements
            )
        ),
        quickReplies.takeUnless { it.isEmpty() }
    )
}

/**
 * Creates a [generic template](https://developers.facebook.com/docs/messenger-platform/send-messages/template/generic).
 */
fun genericTemplate(elements: List<Element>, vararg quickReplies: QuickReply): AttachmentMessage =
    genericTemplate(elements, quickReplies.toList())

/**
 * Creates an [attachment](https://developers.facebook.com/docs/messenger-platform/reference/send-api/#attachment).
 */
fun <T : Bus<T>> T.attachment(
    attachmentUrl: String,
    type: AttachmentType,
    vararg quickReplies: QuickReply
): AttachmentMessage =
    attachment(attachmentUrl, type, quickReplies.toList())

/**
 * Creates an [attachment](https://developers.facebook.com/docs/messenger-platform/reference/send-api/#attachment).
 */
fun <T : Bus<T>> T.attachment(
    attachmentUrl: String,
    type: AttachmentType,
    quickReplies: List<QuickReply>
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
    quickReplies: List<QuickReply>
): AttachmentMessage {

    return AttachmentMessage(
        Attachment(
            type,
            UrlPayload.getUrlPayload(applicationId, attachmentUrl, useCache && !test)
        ),
        quickReplies.run { if (isEmpty()) null else this }
    )
}

/**
 * Creates an [image] as attachment (https://developers.facebook.com/docs/messenger-platform/reference/send-api/#attachment).
 */
fun <T : Bus<T>> T.image(imageUrl: String, vararg quickReplies: QuickReply): AttachmentMessage =
    image(imageUrl, quickReplies.toList())

/**
 * Creates an [image] as attachment (https://developers.facebook.com/docs/messenger-platform/reference/send-api/#attachment).
 */
fun <T : Bus<T>> T.image(imageUrl: String, quickReplies: List<QuickReply>): AttachmentMessage =
    cachedAttachment(imageUrl, AttachmentType.image, quickReplies = quickReplies)

/**
 * Creates an [audio file] as attachment (https://developers.facebook.com/docs/messenger-platform/reference/send-api/#attachment).
 */
fun <T : Bus<T>> T.audio(audioUrl: String, vararg quickReplies: QuickReply): AttachmentMessage =
    audio(audioUrl, quickReplies.toList())

/**
 * Creates an [audio file] as attachment (https://developers.facebook.com/docs/messenger-platform/reference/send-api/#attachment).
 */
fun <T : Bus<T>> T.audio(audioUrl: String, quickReplies: List<QuickReply>): AttachmentMessage =
    cachedAttachment(audioUrl, AttachmentType.audio, quickReplies = quickReplies.toList())

/**
 * Creates a [video] as attachment (https://developers.facebook.com/docs/messenger-platform/reference/send-api/#attachment).
 */
fun <T : Bus<T>> T.video(videoUrl: String, vararg quickReplies: QuickReply): AttachmentMessage =
    video(videoUrl, quickReplies.toList())

/**
 * Creates a [video] as attachment (https://developers.facebook.com/docs/messenger-platform/reference/send-api/#attachment).
 */
fun <T : Bus<T>> T.video(videoUrl: String, quickReplies: List<QuickReply>): AttachmentMessage =
    cachedAttachment(videoUrl, AttachmentType.video, quickReplies = quickReplies)

/**
 * Creates a text with quick replies.
 */
fun I18nTranslator.text(text: CharSequence, vararg quickReplies: QuickReply): TextMessage =
    text(text, quickReplies.toList())

/**
 * Creates a text with quick replies.
 */
fun I18nTranslator.text(text: CharSequence, quickReplies: List<QuickReply>): TextMessage =
    TextMessage(translate(text).toString(), quickReplies)

/**
 * Creates a [generic element](https://developers.facebook.com/docs/messenger-platform/send-messages/template/generic).
 */
fun I18nTranslator.genericElement(
    title: CharSequence,
    subtitle: CharSequence? = null,
    imageUrl: String? = null,
    buttons: List<Button>? = null
): Element {
    val t = translate(title)
    val s = translateAndReturnBlankAsNull(subtitle)
    if (t.length > 80) {
        logger.warn { "title $t has more than 80 chars" }
    }
    if (s?.length ?: 0 > 80) {
        logger.warn { "subtitle $s has more than 80 chars" }
    }
    if (buttons?.size ?: 0 > 3) {
        error("Number of buttons > 3 : $buttons")
    }
    return Element(
        t.toString(),
        imageUrl,
        s,
        buttons
    )
}

/**
 * Creates a [list element](https://developers.facebook.com/docs/messenger-platform/send-messages/template/list).
 */
@Deprecated("messenger lists are deprecated")
fun I18nTranslator.listElement(
    title: CharSequence,
    subtitle: CharSequence? = null,
    imageUrl: String? = null,
    button: Button? = null
): Element {
    val t = translate(title)
    val s = translateAndReturnBlankAsNull(subtitle)

    if (t.length > 80) {
        logger.warn { "title $t has more than 80 chars" }
    }
    if (s?.length ?: 0 > 80) {
        logger.warn { "subtitle $s has more than 80 chars" }
    }
    return Element(
        t.toString(),
        imageUrl,
        s,
        if (button == null) null else listOf(button)
    )
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
 * Creates a [location quick reply](https://developers.facebook.com/docs/messenger-platform/send-messages/quick-replies#location).
 */
@Deprecated("location QR is deprecated")
fun locationQuickReply(): QuickReply = LocationQuickReply()

/**
 * This quick reply does not use any custom payload, but the [textToSend] will we parsed by the NLP engine.
 */
fun I18nTranslator.nlpQuickReply(
    title: CharSequence,
    textToSend: CharSequence = title,
    imageUrl: String? = null
): QuickReply =
    TextQuickReply(
        translate(title).toString(),
        SendChoice.encodeNlpChoiceId(translate(textToSend).toString()),
        imageUrl
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
    step: StoryStep<out StoryHandlerDefinition>? = null,
    /**
     * The image url of the quick reply.
     */
    imageUrl: String? = null,
    /**
     * The current step of the Bus<T>.
     */
    busStep: StoryStep<out StoryHandlerDefinition>? = null,
    /**
     * The current intent of the Bus<T>.
     */
    currentIntent: Intent? = null
): QuickReply =
    quickReply(title, targetIntent, imageUrl, step?.name, parameters.toMap()) { intent, s, params ->
        SendChoice.encodeChoiceId(intent, s, params, busStep?.name, currentIntent)
    }

/**
 * Creates a [quick reply](https://developers.facebook.com/docs/messenger-platform/send-messages/quick-replies).
 */
fun <T : Bus<T>> T.quickReply(
    title: CharSequence,
    targetIntent: IntentAware,
    parameters: Parameters
): QuickReply =
    quickReply(title, targetIntent, null, stepName, parameters.toMap())

/**
 * Creates a [quick reply](https://developers.facebook.com/docs/messenger-platform/send-messages/quick-replies).
 */
fun <T : Bus<T>> T.quickReply(
    title: CharSequence,
    targetIntent: IntentAware,
    imageUrl: String? = null,
    step: StoryStep<out StoryHandlerDefinition>? = null,
    parameters: Parameters
): QuickReply =
    quickReply(title, targetIntent, imageUrl, step, parameters.toMap())

/**
 * Create a [quick reply](https://developers.facebook.com/docs/messenger-platform/send-messages/quick-replies).
 */
fun <T : Bus<T>> T.quickReply(
    title: CharSequence,
    targetIntent: IntentAware,
    imageUrl: String? = null,
    step: StoryStep<out StoryHandlerDefinition>? = null,
    vararg parameters: Pair<String, String>
): QuickReply =
    quickReply(title, targetIntent.wrappedIntent(), imageUrl, step, parameters.toMap())

/**
 * Creates a [quick reply](https://developers.facebook.com/docs/messenger-platform/send-messages/quick-replies).
 */
fun <T : Bus<T>> T.quickReply(
    title: CharSequence,
    targetIntent: IntentAware,
    imageUrl: String? = null,
    step: StoryStep<out StoryHandlerDefinition>? = null,
    parameters: Collection<Pair<String, String>>
): QuickReply =
    quickReply(title, targetIntent, imageUrl, step, parameters.toMap())

/**
 * Creates a [quick reply](https://developers.facebook.com/docs/messenger-platform/send-messages/quick-replies).
 */
fun <T : Bus<T>> T.quickReply(
    title: CharSequence,
    targetIntent: IntentAware,
    imageUrl: String? = null,
    step: StoryStep<out StoryHandlerDefinition>? = null,
    parameters: Map<String, String>
): QuickReply =
    quickReply(title, targetIntent, imageUrl, step?.name, parameters)

private fun <T : Bus<T>> T.quickReply(
    title: CharSequence,
    targetIntent: IntentAware,
    imageUrl: String? = null,
    step: String? = null,
    parameters: Map<String, String>
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
    payloadEncoder: (IntentAware, String?, Map<String, String>) -> String
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
 * from an [I18nTranslator].
 */
@Deprecated("use notify method for standalone")
fun I18nTranslator.standalonePostbackButton(
    /**
     * The title of the button.
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
    step: StoryStep<out StoryHandlerDefinition>? = null,
    /**
     * The current step of the Bus<T>.
     */
    busStep: StoryStep<out StoryHandlerDefinition>? = null,
    /**
     * The current intent of the Bus<T>.
     */
    currentIntent: Intent? = null
): PostbackButton =
    postbackButton(
        title,
        targetIntent,
        step,
        parameters.toMap()
    ) { intent, s, params ->
        SendChoice.encodeChoiceId(intent, s, params, busStep, currentIntent)
    }

/**
 * Creates a [postback button](https://developers.facebook.com/docs/messenger-platform/send-messages/buttons#postback).
 */
fun <T : Bus<T>> T.postbackButton(
    title: CharSequence,
    targetIntent: IntentAware,
    vararg parameters: Pair<String, String>
): PostbackButton =
    postbackButton<T>(title, targetIntent, null, *parameters)

/**
 * Creates a [postback button](https://developers.facebook.com/docs/messenger-platform/send-messages/buttons#postback).
 */
fun <T : Bus<T>> T.postbackButton(
    title: CharSequence,
    targetIntent: IntentAware,
    parameters: Parameters
): PostbackButton =
    postbackButton(title, targetIntent, null, parameters)

/**
 * Creates a [postback button](https://developers.facebook.com/docs/messenger-platform/send-messages/buttons#postback).
 */
fun <T : Bus<T>> T.postbackButton(
    title: CharSequence,
    targetIntent: IntentAware,
    step: StoryStep<out StoryHandlerDefinition>? = null,
    parameters: Parameters
): PostbackButton =
    postbackButton(title, targetIntent, step, *parameters.toArray())

/**
 * Creates a [postback button](https://developers.facebook.com/docs/messenger-platform/send-messages/buttons#postback).
 */
fun <T : Bus<T>> T.postbackButton(
    title: CharSequence,
    targetIntent: IntentAware,
    step: StoryStep<out StoryHandlerDefinition>? = null,
    vararg parameters: Pair<String, String>
): PostbackButton =
    postbackButton(
        title,
        targetIntent,
        step,
        parameters.toMap()
    ) { intent, s, params ->
        SendChoice.encodeChoiceId(this, intent, s, params)
    }

private fun I18nTranslator.postbackButton(
    title: CharSequence,
    targetIntent: IntentAware,
    step: StoryStep<out StoryHandlerDefinition>? = null,
    parameters: Map<String, String>,
    payloadEncoder: (IntentAware, StoryStep<out StoryHandlerDefinition>?, Map<String, String>) -> String
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
    textToSend: CharSequence = title
): PostbackButton =
    PostbackButton(
        SendChoice.encodeNlpChoiceId(translate(textToSend).toString()),
        translate(title).toString()
    )

fun I18nTranslator.callToButton(
    title: CharSequence,
    phoneNumber: String
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
fun I18nTranslator.urlButton(title: CharSequence, url: String): UrlButton {
    val t = translate(title)
    if (t.length > 20) {
        logger.warn { "title $t has more than 20 chars" }
    }
    return UrlButton(url, t.toString())
}

/**
 * Used to generate a text only event,
 * usually sent later by [MessengerConnector.send] or [MessengerConnector.sendOptInEvent].
 */
@Deprecated("use notify method for standalone")
fun standaloneMessengerAnswer(
    playerId: PlayerId,
    applicationId: String,
    recipientId: PlayerId,
    text: String,
    lastAnswer: Boolean = true,
    /** Significance deals with the notification level. */
    priority: ActionPriority = ActionPriority.normal,
    /** tag deals with type of message notification. */
    notificationType: ActionNotificationType? = null
): SendSentence =
    SendSentence(
        playerId,
        applicationId,
        recipientId,
        text,
        metadata = ActionMetadata(
            lastAnswer,
            priority,
            notificationType
        )
    )

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
    messageProvider: () -> MessengerConnectorMessage
): SendSentence =
    SendSentence(
        playerId,
        applicationId,
        recipientId,
        null,
        messages = mutableListOf(messageProvider()),
        metadata = ActionMetadata(
            lastAnswer,
            priority,
            notificationType
        )
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
    messagesProvider: () -> List<MessengerConnectorMessage>
): List<SendSentence> =
    messagesProvider().run {
        mapIndexed { i, m ->
            standaloneMessengerAnswer(
                playerId,
                applicationId,
                recipientId,
                i == size - 1,
                priority,
                notificationType
            ) { m }
        }
    }