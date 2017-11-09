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
import fr.vsct.tock.bot.connector.messenger.model.send.Attachment
import fr.vsct.tock.bot.connector.messenger.model.send.AttachmentMessage
import fr.vsct.tock.bot.connector.messenger.model.send.AttachmentType
import fr.vsct.tock.bot.connector.messenger.model.send.Button
import fr.vsct.tock.bot.connector.messenger.model.send.ButtonPayload
import fr.vsct.tock.bot.connector.messenger.model.send.Element
import fr.vsct.tock.bot.connector.messenger.model.send.GenericPayload
import fr.vsct.tock.bot.connector.messenger.model.send.ListElementStyle
import fr.vsct.tock.bot.connector.messenger.model.send.ListPayload
import fr.vsct.tock.bot.connector.messenger.model.send.LocationQuickReply
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
import fr.vsct.tock.bot.engine.BotBus
import fr.vsct.tock.bot.engine.action.SendChoice
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

internal const val MESSENGER_CONNECTOR_TYPE_ID = "messenger"

/**
 * The messenger connector type.
 */
val messengerConnectorType = ConnectorType(MESSENGER_CONNECTOR_TYPE_ID)

fun BotBus.withMessenger(messageProvider: () -> ConnectorMessage): BotBus {
    return withMessage(messengerConnectorType, messageProvider)
}

/**
 * Add a button template [https://developers.facebook.com/docs/messenger-platform/send-api-reference/button-template]
 */
fun BotBus.buttonsTemplate(text: CharSequence, vararg actions: UserAction): AttachmentMessage {
    return AttachmentMessage(
            Attachment(
                    AttachmentType.template,
                    ButtonPayload(
                            translate(text).toString(),
                            extractButtons(actions.toList())
                    )
            ),
            extractQuickReplies(actions.toList())
    )
}

/**
 * ListTemplate does not support list with exactly one element.
 * This function generates a generic template if there is one element,
 * or a classic list element if there is between 2 and 4.
 */
fun BotBus.flexibleListTemplate(elements: List<Element>,
                                topElementStyle: ListElementStyle? = null,
                                vararg actions: UserAction): AttachmentMessage {
    return if (elements.size == 1) {
        genericTemplate(elements, *actions.filterIsInstance(QuickReply::class.java).toTypedArray())
    } else {
        listTemplate(elements, topElementStyle, *actions)
    }
}

fun BotBus.listTemplate(
        e1: Element,
        e2: Element,
        e3: Element? = null,
        e4: Element? = null,
        topElementStyle: ListElementStyle? = null,
        vararg actions: UserAction): AttachmentMessage {
    return listTemplate(listOfNotNull(e1, e2, e3, e4), topElementStyle, *actions)
}

fun BotBus.listTemplate(
        elements: List<Element>,
        topElementStyle: ListElementStyle? = null,
        vararg actions: UserAction): AttachmentMessage {
    if (elements.size < 2 || elements.size > 4) {
        error("must have at least 2 elements and at most 4")
    }
    if (topElementStyle != ListElementStyle.compact
            && elements.any { it.imageUrl == null }) {
        error("imageUrl of elements may not be null with large element style")
    }

    return AttachmentMessage(
            Attachment(
                    AttachmentType.template,
                    ListPayload(
                            elements,
                            topElementStyle,
                            extractButtons(actions.toList())
                                    .run {
                                        if (isEmpty()) null
                                        else if (size > 1) error("only one button max")
                                        else this
                                    })
            ),
            extractQuickReplies(actions.toList())
    )
}

fun BotBus.genericTemplate(vararg elements: Element): AttachmentMessage {
    return genericTemplate(elements.toList())
}

fun BotBus.genericTemplate(elements: List<Element>, vararg quickReplies: QuickReply): AttachmentMessage {
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
            quickReplies.run { if (isEmpty()) null else toList() }
    )
}

fun BotBus.attachment(attachmentUrl: String, type: AttachmentType, vararg quickReplies: QuickReply): AttachmentMessage {
    return when (type) {
        AttachmentType.image -> cachedAttachment(attachmentUrl, AttachmentType.image, quickReplies = *quickReplies)
        AttachmentType.audio -> cachedAttachment(attachmentUrl, AttachmentType.audio, quickReplies = *quickReplies)
        AttachmentType.video -> cachedAttachment(attachmentUrl, AttachmentType.video, quickReplies = *quickReplies)
        else -> {
            error { "not supported attachment type $type" }
        }
    }
}

private fun BotBus.cachedAttachment(
        attachmentUrl: String,
        type: AttachmentType,
        useCache: Boolean = MessengerConfiguration.reuseAttachmentByDefault,
        vararg quickReplies: QuickReply): AttachmentMessage {

    return AttachmentMessage(
            Attachment(
                    type,
                    UrlPayload.getUrlPayload(applicationId, attachmentUrl, useCache && !userPreferences.test)
            ),
            quickReplies.run { if (isEmpty()) null else toList() }
    )
}

fun BotBus.image(imageUrl: String, vararg quickReplies: QuickReply): AttachmentMessage {
    return cachedAttachment(imageUrl, AttachmentType.image, quickReplies = *quickReplies)
}

fun BotBus.audio(audioUrl: String, vararg quickReplies: QuickReply): AttachmentMessage {
    return cachedAttachment(audioUrl, AttachmentType.audio, quickReplies = *quickReplies)
}

fun BotBus.video(videoUrl: String, vararg quickReplies: QuickReply): AttachmentMessage {
    return cachedAttachment(videoUrl, AttachmentType.video, quickReplies = *quickReplies)
}

fun BotBus.text(text: CharSequence, vararg quickReplies: QuickReply): TextMessage {
    return TextMessage(translate(text).toString(), quickReplies.toList())
}

fun BotBus.genericElement(
        title: CharSequence,
        subtitle: CharSequence? = null,
        imageUrl: String? = null,
        buttons: List<Button>? = null): Element {
    val t = translate(title)
    val s = translateAndSetBlankAsNull(subtitle)
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

fun BotBus.listElement(
        title: CharSequence,
        subtitle: CharSequence? = null,
        imageUrl: String? = null,
        button: Button? = null): Element {
    val t = translate(title)
    val s = translateAndSetBlankAsNull(subtitle)

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

fun BotBus.locationQuickReply(): QuickReply
        = LocationQuickReply()

fun BotBus.quickReply(
        title: CharSequence,
        targetIntent: IntentAware,
        imageUrl: String? = null,
        step: StoryStep<out StoryHandlerDefinition>? = null,
        parameters: Parameters): QuickReply
        = quickReply(title, targetIntent, imageUrl, step, *parameters.toArray())


fun BotBus.quickReply(
        title: CharSequence,
        targetIntent: IntentAware,
        imageUrl: String? = null,
        step: StoryStep<out StoryHandlerDefinition>? = null,
        vararg parameters: Pair<String, String>): QuickReply
        = quickReply(title, targetIntent.wrappedIntent(), imageUrl, step, *parameters)

fun BotBus.quickReply(
        title: CharSequence,
        targetIntent: Intent,
        imageUrl: String? = null,
        step: StoryStep<out StoryHandlerDefinition>? = null,
        parameters: Parameters): QuickReply
        = quickReply(title, targetIntent, imageUrl, step, *parameters.toArray())

fun BotBus.quickReply(
        title: CharSequence,
        targetIntent: Intent,
        imageUrl: String? = null,
        step: StoryStep<out StoryHandlerDefinition>? = null,
        vararg parameters: Pair<String, String>): QuickReply {
    val t = translate(title)
    if (t.length > 20) {
        logger.warn { "title $t has more than 20 chars" }
    }
    val payload = SendChoice.encodeChoiceId(this, targetIntent, step, parameters.toMap())
    if (payload.length > 1000) {
        logger.warn { "payload $payload has more than 1000 chars" }
    }
    return TextQuickReply(t.toString(), payload, imageUrl)
}

fun BotBus.postbackButton(
        title: CharSequence,
        targetIntent: IntentAware,
        vararg parameters: Pair<String, String>)
        : PostbackButton
        = postbackButton(title, targetIntent, null, *parameters)

fun BotBus.postbackButton(
        title: CharSequence,
        targetIntent: IntentAware,
        step: StoryStep<out StoryHandlerDefinition>? = null,
        parameters: Parameters)
        : PostbackButton
        = postbackButton(title, targetIntent, step, *parameters.toArray())

fun BotBus.postbackButton(
        title: CharSequence,
        targetIntent: IntentAware,
        step: StoryStep<out StoryHandlerDefinition>? = null,
        vararg parameters: Pair<String, String>)
        : PostbackButton {
    val t = translate(title)
    if (t.length > 20) {
        logger.warn { "title $t has more than 20 chars" }
    }
    val payload = SendChoice.encodeChoiceId(this, targetIntent, step, parameters.toMap())
    if (payload.length > 1000) {
        logger.warn { "payload $payload has more than 1000 chars" }
    }
    return PostbackButton(payload, t.toString())
}

fun BotBus.urlButton(title: CharSequence, url: String): UrlButton {
    val t = translate(title)
    if (t.length > 20) {
        logger.warn { "title $t has more than 20 chars" }
    }
    return UrlButton(url, t.toString())
}

private fun BotBus.translateAndSetBlankAsNull(s: CharSequence?): String?
        = translate(s).run { if (isBlank()) null else this.toString() }
