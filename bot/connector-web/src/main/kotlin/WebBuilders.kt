/*
 * Copyright (C) 2017/2019 e-voyageurs technologies
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

package ai.tock.bot.connector.web

import ai.tock.bot.connector.ConnectorMessage
import ai.tock.bot.connector.media.MediaCard
import ai.tock.bot.connector.media.MediaCarousel
import ai.tock.bot.connector.media.MediaFile
import ai.tock.bot.connector.web.send.Button
import ai.tock.bot.connector.web.send.PostbackButton
import ai.tock.bot.connector.web.send.QuickReply
import ai.tock.bot.connector.web.send.UrlButton
import ai.tock.bot.connector.web.send.WebCard
import ai.tock.bot.connector.web.send.WebCarousel
import ai.tock.bot.connector.web.send.WebWidget
import ai.tock.bot.definition.IntentAware
import ai.tock.bot.definition.Parameters
import ai.tock.bot.definition.StoryHandlerDefinition
import ai.tock.bot.definition.StoryStep
import ai.tock.bot.engine.Bus
import ai.tock.bot.engine.I18nTranslator
import ai.tock.bot.engine.action.SendAttachment.AttachmentType
import ai.tock.bot.engine.action.SendChoice

/**
 * Adds a Web [ConnectorMessage] if the current connector is Web.
 * You need to call [BotBus.send] or [BotBus.end] later to send this message.
 */
fun <T : Bus<T>> T.withWeb(messageProvider: () -> WebConnectorMessage): T {
    return withMessage(webConnectorType, messageProvider)
}


/**
 * Creates a text with buttons.
 */
@Deprecated("Use other methods to create buttons")
fun I18nTranslator.webMessage(title: CharSequence, vararg buttons: WebButton): OldWebMessage =
    OldWebMessage(
        translate(title).toString(), buttons.toList()
    )

/**
 * Creates a text with buttons.
 */
@Deprecated("Use other methods to create buttons")
fun I18nTranslator.webMessage(title: CharSequence, buttons: List<WebButton>): OldWebMessage =
    OldWebMessage(
        translate(title).toString(), buttons
    )

/**
 * Creates a text with buttons.
 */
fun I18nTranslator.webMessage(title: CharSequence, vararg buttons: Button): WebMessage =
    WebMessage(
        translate(title).toString(), buttons.toList()
    )

/**
 * Creates a text with a list of buttons.
 */
fun I18nTranslator.webMessage(title: CharSequence, buttons: List<Button> = emptyList()): WebMessage =
    WebMessage(
        translate(title).toString(), buttons
    )

/**
 * Creates a button.
 */
@Deprecated("Use the methods of the WebBuilders to create buttons")
fun <T : Bus<T>> T.webButton(
    title: CharSequence,
    targetIntent: IntentAware? = null,
    imageUrl: String? = null,
    step: StoryStep<out StoryHandlerDefinition>? = null,
    parameters: Parameters = Parameters()
): WebButton =
    WebButton(
        translate(title).toString(),
        targetIntent?.let { i -> SendChoice.encodeChoiceId(this, i, step, parameters.toMap()) },
        imageUrl
    )

/**
 * Creates a url button
 */
fun <T : Bus<T>> T.webUrlButton(title: CharSequence, url: String): Button =
    UrlButton(
        translate(title).toString(),
        url
    )

/**
 * Creates a postback button
 */
fun <T : Bus<T>> T.webPostbackButton(
    title: CharSequence,
    targetIntent: IntentAware? = null,
    step: StoryStep<out StoryHandlerDefinition>? = null,
    parameters: Parameters = Parameters()
): Button =
    PostbackButton(
        translate(title).toString(),
        targetIntent?.let { i -> SendChoice.encodeChoiceId(this, i, step, parameters.toMap()) }
    )

/**
 * Creates a quickreply button
 */
fun <T : Bus<T>> T.webQuickReply(
    title: CharSequence,
    targetIntent: IntentAware? = null,
    step: StoryStep<out StoryHandlerDefinition>? = null,
    parameters: Parameters = Parameters(),
    imageUrl: String? = null
): Button =
    QuickReply(
        translate(title).toString(),
        targetIntent?.let { i -> SendChoice.encodeChoiceId(this, i, step, parameters.toMap()) },
        imageUrl
    )

/**
 * Creates a button from a text.
 */
fun <T : Bus<T>> T.webTextButton(text: CharSequence): WebButton =
    WebButton(translate(text).toString())

/**
 * Creates a [WebMessage] from a [WebCard].
 */

fun <T : Bus<T>> T.webCard(card: WebCard): WebMessage = WebMessage(card = card)

fun <T : Bus<T>> T.webCard(
    title: CharSequence?,
    subTitle: CharSequence?,
    buttons: List<Button> = emptyList()
): WebCard = WebCard(
    title = translate(title).toString(),
    subTitle = translate(subTitle).toString(),
    buttons = buttons
)

fun <T : Bus<T>> T.webCardWithAttachment(
    title: CharSequence?,
    subTitle: CharSequence?,
    attachmentUrl: CharSequence,
    buttons: List<Button>,
    type: AttachmentType = AttachmentType.file,
    attachementName: String = ""
): WebCard {
    return WebCard(
        title = translate(title).toString(),
        subTitle = translate(subTitle).toString(),
        file = MediaFile(
            attachmentUrl.toString(), attachementName, type
        ),
        buttons = buttons
    )
}

/**
 * Creates a [WebMessage] from a [WebCarousel].
 */

fun <T : Bus<T>> T.webCarousel(vararg cards: WebCard, buttons: List<Button> = emptyList()): WebMessage = WebMessage(carousel = WebCarousel(cards = cards.toList()), buttons = buttons)

fun <T : Bus<T>> T.webCarousel(cards: List<WebCard>, buttons: List<Button> = emptyList()): WebMessage = WebMessage(carousel = WebCarousel(cards = cards), buttons = buttons)

/**
 * Creates a [OldWebMessage] from a [MediaCard].
 */
@Deprecated("No more supported", ReplaceWith("webCard or webCardWithAttachement"))
fun <T : Bus<T>> T.webCard(card: MediaCard): OldWebMessage = OldWebMessage(card = card)

/**
 * Creates a [OldWebMessage] from a [MediaCarousel].
 */
@Deprecated("No more supported", ReplaceWith("webCarousel(vararg cards: WebCard)"))
fun <T : Bus<T>> T.webCarousel(carousel: MediaCarousel): OldWebMessage = OldWebMessage(carousel = carousel)

/**
 * Creates a custom payload
 */
fun <T : Bus<T>> T.webWidget(widget: WebWidget): WebMessage = WebMessage(widget = widget)