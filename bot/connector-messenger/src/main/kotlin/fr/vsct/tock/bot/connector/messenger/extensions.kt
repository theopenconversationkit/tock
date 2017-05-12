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

import fr.vsct.tock.bot.connector.messenger.model.send.Attachment
import fr.vsct.tock.bot.connector.messenger.model.send.AttachmentMessage
import fr.vsct.tock.bot.connector.messenger.model.send.AttachmentType
import fr.vsct.tock.bot.connector.messenger.model.send.Button
import fr.vsct.tock.bot.connector.messenger.model.send.ButtonPayload
import fr.vsct.tock.bot.connector.messenger.model.send.Element
import fr.vsct.tock.bot.connector.messenger.model.send.GenericPayload
import fr.vsct.tock.bot.connector.messenger.model.send.ListElementStyle
import fr.vsct.tock.bot.connector.messenger.model.send.ListPayload
import fr.vsct.tock.bot.connector.messenger.model.send.PostbackButton
import fr.vsct.tock.bot.connector.messenger.model.send.UrlButton
import fr.vsct.tock.bot.definition.Intent
import fr.vsct.tock.bot.definition.StoryDefinition
import fr.vsct.tock.bot.engine.BotBus
import fr.vsct.tock.bot.engine.action.SendChoice
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * Add a button template [https://developers.facebook.com/docs/messenger-platform/send-api-reference/button-template]
 */
fun BotBus.withMessengerButtons(text: String, vararg buttons: Button): BotBus {
    with(AttachmentMessage(
            Attachment(
                    AttachmentType.template,
                    ButtonPayload(
                            text, buttons.toList()
                    )
            )
    )
    )
    return this
}

fun BotBus.withMessengerList(e1: Element, e2: Element, e3: Element? = null, e4: Element? = null, topElementStyle: ListElementStyle? = null, button: Button? = null): BotBus {
    return withMessengerList(listOfNotNull(e1, e2, e3, e4), topElementStyle, button)
}

fun BotBus.withMessengerList(elements: List<Element>, topElementStyle: ListElementStyle? = null, button: Button? = null): BotBus {
    if (elements.size < 2 || elements.size > 4) {
        error("must have at least 2 elements and at most 4")
    }
    if (topElementStyle != ListElementStyle.compact
            && elements.any { it.imageUrl == null }) {
        error("imageUrl of elements may not be null with large element style")
    }

    with(AttachmentMessage(
            Attachment(
                    AttachmentType.template,
                    ListPayload(
                            elements,
                            topElementStyle,
                            if (button == null) null else listOf(button)
                    )
            )
    )
    )

    return this
}

fun BotBus.withMessengerGeneric(vararg elements: Element): BotBus {
    return withMessengerGeneric(elements.toList())
}

fun BotBus.withMessengerGeneric(elements: List<Element>): BotBus {
    if (elements.isEmpty() || elements.size > 10) {
        error("must have at least 1 elements and at most 10")
    }

    with(AttachmentMessage(
            Attachment(
                    AttachmentType.template,
                    GenericPayload(
                            elements
                    )
            )
    )
    )

    return this
}

fun BotBus.messengerGenericElement(title: String, subtitle: String? = null, imageUrl: String? = null, buttons: List<Button>? = null): Element {
    if (title.length > 80) {
        logger.warn { "title $title has more than 80 chars" }
    }
    if (subtitle?.length ?: 0 > 80) {
        logger.warn { "subtitle $subtitle has more than 80 chars" }
    }
    if (buttons?.size ?: 0 > 3) {
        error("Number of buttons > 3 : $buttons")
    }
    return Element(
            translate(title),
            imageUrl,
            translate(subtitle),
            buttons
    )
}

fun BotBus.messengerListElement(title: String, subtitle: String? = null, imageUrl: String? = null, button: Button? = null): Element {
    if (title.length > 80) {
        logger.warn { "title $title has more than 80 chars" }
    }
    if (subtitle?.length ?: 0 > 80) {
        logger.warn { "subtitle $subtitle has more than 80 chars" }
    }
    return Element(
            translate(title),
            imageUrl,
            translate(subtitle),
            if (button == null) null else listOf(button)
    )
}


fun BotBus.messengerPostback(title: String, targetStory: StoryDefinition, vararg parameters: Pair<String, String>): PostbackButton {
    if (title.length > 20) {
        logger.warn { "title $title has more than 20 chars" }
    }
    val payload = SendChoice.encodeChoiceId(targetStory, parameters.toMap())
    if (title.length > 1000) {
        logger.warn { "payload $payload has more than 1000 chars" }
    }
    return PostbackButton(payload, translate(title))
}

fun BotBus.messengerPostback(title: String, targetIntent: Intent, vararg parameters: Pair<String, String>): PostbackButton {
    if (title.length > 20) {
        logger.warn { "title $title has more than 20 chars" }
    }
    val payload = SendChoice.encodeChoiceId(targetIntent, parameters.toMap())
    if (title.length > 1000) {
        logger.warn { "payload $payload has more than 1000 chars" }
    }
    return PostbackButton(payload, translate(title))
}

fun BotBus.messengerUrl(title: String, url: String): UrlButton {
    if (title.length > 20) {
        logger.warn { "title $title has more than 20 chars" }
    }
    return UrlButton(url, title)
}

