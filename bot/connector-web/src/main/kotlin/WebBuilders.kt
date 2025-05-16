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

package ai.tock.bot.connector.web

import ai.tock.bot.connector.ConnectorMessage
import ai.tock.bot.connector.media.MediaCard
import ai.tock.bot.connector.media.MediaCarousel
import ai.tock.bot.connector.web.send.Button
import ai.tock.bot.connector.web.send.ButtonStyle
import ai.tock.bot.connector.web.send.PostbackButton
import ai.tock.bot.connector.web.send.QuickReply
import ai.tock.bot.connector.web.send.UrlButton
import ai.tock.bot.connector.web.send.WebCard
import ai.tock.bot.connector.web.send.WebCarousel
import ai.tock.bot.connector.web.send.WebDeepLink
import ai.tock.bot.connector.web.send.WebImage
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
 * Creates a URL button
 *
 * @param title the text that should appear on the button
 * @param url the URL of the page that should be opened when clicking on the button
 * @param imageUrl the URL of an image file to display as an icon inside the button
 * @param target the link's target, typically either _self or _blank
 * @param style the style of the button - the specific appearance for a given style is defined by the frontend
 */
fun <T : Bus<T>> T.webUrlButton(
    title: CharSequence,
    url: String,
    imageUrl: String? = null,
    target: HrefTargetType,
    style: ButtonStyle
): Button =
    webUrlButton(title, url, imageUrl, target.name, style.name, null)

/**
 * Creates a URL button
 *
 * @param title the text that should appear on the button
 * @param url the URL of the page that should be opened when clicking on the button
 * @param imageUrl the URL of an image file to display as an icon inside the button
 * @param target the link's [target](https://developer.mozilla.org/en-US/docs/Web/HTML/Element/a#target).
 *   If [windowFeatures] is also specified, this parameter is used for the
 *   [window's target](https://developer.mozilla.org/en-US/docs/Web/API/Window/open#target) instead
 * @param style the style of the button - the specific appearance for a given style is defined by the frontend
 * @param windowFeatures if specified, the button will open the URL in a popup window configured using the
 *   [windowFeatures](https://developer.mozilla.org/en-US/docs/Web/API/Window/open#windowfeatures) argument
 */
@JvmOverloads // binary backward compatibility
fun <T : Bus<T>> T.webUrlButton(
    title: CharSequence,
    url: String,
    imageUrl: String? = null,
    target: String? = HrefTargetType._blank.name,
    style: ButtonStyle,
    windowFeatures: String? = null,
): Button =
    webUrlButton(title, url, imageUrl, target, style.name, windowFeatures)

/**
 * Creates a URL button
 *
 * @param title the text that should appear on the button
 * @param url the URL of the page that should be opened when clicking on the button
 * @param imageUrl the URL of an image file to display as an icon inside the button
 * @param target the link's target, typically either _self or _blank
 * @param style the style of the button - the specific appearance for a given style is defined by the frontend
 */
fun <T : Bus<T>> T.webUrlButton(
    title: CharSequence,
    url: String,
    imageUrl: String? = null,
    target: HrefTargetType,
    style: String? = ButtonStyle.primary.name
): Button =
    webUrlButton(title, url, imageUrl, target.name, style, null)

/**
 * Creates a URL button
 *
 * @param title the text that should appear on the button
 * @param url the URL of the page that should be opened when clicking on the button
 * @param imageUrl the URL of an image file to display as an icon inside the button
 * @param target the link's [target](https://developer.mozilla.org/en-US/docs/Web/HTML/Element/a#target).
 *   If [windowFeatures] is also specified, this parameter is used for the
 *   [window's target](https://developer.mozilla.org/en-US/docs/Web/API/Window/open#target) instead
 * @param style the style of the button - the specific appearance for a given style is defined by the frontend
 * @param windowFeatures if specified, the button will open the URL in a popup window configured using the
 *   [windowFeatures](https://developer.mozilla.org/en-US/docs/Web/API/Window/open#windowfeatures) argument
 */
@JvmOverloads // binary backward compatibility
fun <T : Bus<T>> T.webUrlButton(
    title: CharSequence,
    url: String,
    imageUrl: String? = null,
    target: String? = HrefTargetType._blank.name,
    style: String? = ButtonStyle.primary.name,
    windowFeatures: String? = null,
): Button =
    UrlButton(
        translate(title).toString(),
        url,
        imageUrl,
        target,
        style,
        windowFeatures,
    )

/**
 * Creates a postback button
 */
fun <T : Bus<T>> T.webPostbackButton(
    title: CharSequence,
    targetIntent: IntentAware,
    step: StoryStep<out StoryHandlerDefinition>? = null,
    parameters: Parameters = Parameters(),
    imageUrl: String? = null,
    style: ButtonStyle
): Button =
    webPostbackButton(title, targetIntent, step, parameters, imageUrl, style.name)

/**
 * Creates a postback button
 */
fun <T : Bus<T>> T.webPostbackButton(
    title: CharSequence,
    targetIntent: IntentAware,
    step: StoryStep<out StoryHandlerDefinition>? = null,
    parameters: Parameters = Parameters(),
    imageUrl: String? = null,
    style: String? = ButtonStyle.primary.name
): Button =
    PostbackButton(
        translate(title).toString(),
        targetIntent.let { i -> SendChoice.encodeChoiceId(this, i, step, parameters.toMap()) },
        imageUrl,
        style
    )

/**
 * Creates a quickreply button
 */
@Deprecated("use other builder")
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
 * Creates a quickreply button with target intent
 */
fun <T : Bus<T>> T.webIntentQuickReply(
    title: CharSequence,
    targetIntent: IntentAware,
    step: StoryStep<out StoryHandlerDefinition>? = null,
    parameters: Parameters = Parameters(),
    imageUrl: String? = null,
    style: ButtonStyle
): Button =
    webIntentQuickReply(title, targetIntent, step, parameters, imageUrl, style.name)

/**
 * Creates a quickreply button with target intent
 */
fun <T : Bus<T>> T.webIntentQuickReply(
    title: CharSequence,
    targetIntent: IntentAware,
    step: StoryStep<out StoryHandlerDefinition>? = null,
    parameters: Parameters = Parameters(),
    imageUrl: String? = null,
    style: String? = ButtonStyle.primary.name
): Button =
    QuickReply(
        translate(title).toString(),
        SendChoice.encodeChoiceId(this, targetIntent, step, parameters.toMap()),
        imageUrl,
        null,
        style
    )

/**
 * Creates a quickreply button with target intent
 */
fun <T : Bus<T>> T.webNlpQuickReply(
    title: CharSequence,
    nlpText: String? = null,
    imageUrl: String? = null,
    style: ButtonStyle
): Button =
    webNlpQuickReply(title, nlpText, imageUrl, style.name)

/**
 * Creates a quickreply button with target intent
 */
fun <T : Bus<T>> T.webNlpQuickReply(
    title: CharSequence,
    nlpText: String? = null,
    imageUrl: String? = null,
    style: String? = ButtonStyle.primary.name
): Button =
    QuickReply(
        translate(title).toString(),
        null,
        imageUrl,
        nlpText,
        style
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
    attachementName: String = "",
    fileDescription: CharSequence? = null
): WebCard {
    return WebCard(
        title = translate(title).toString(),
        subTitle = translate(subTitle).toString(),
        file = WebMediaFile(
            attachmentUrl.toString(),
            attachementName,
            type,
            if (fileDescription != null) translate(fileDescription).toString() else null
        ),
        buttons = buttons
    )
}

/**
 * Creates a [WebMessage] from a [WebImage].
 *
 * On the frontend, this will usually be rendered as an img tag.
 *
 * Specific buttons like [webUrlButton] and [webPostbackButton] may not be supported by the frontend - in this case,
 * [webCardWithAttachment] should be considered as an alternative.
 *
 * @param imageUrl the location of the image file to display (should be one of the [image formats supported by the img tag](https://developer.mozilla.org/en-US/docs/Web/HTML/Element/img#supported_image_formats))
 * @param title a title for the image, which is also used as a file name and an alternative description when `description` is left unspecified
 * @param description the alternate description for the image
 */
fun <T : Bus<T>> T.webImage(imageUrl: String, title: CharSequence, description: CharSequence? = null): WebMessage {
    val translatedTitle = translate(title)
    return WebMessage(
        image = WebImage(
            WebMediaFile(
                imageUrl,
                translatedTitle.toString(),
                AttachmentType.image,
                if (description != null) translate(description).toString() else null
            ), translatedTitle
        )
    )
}

/**
 * Creates a [WebMessage] from a [WebCarousel].
 */

fun <T : Bus<T>> T.webCarousel(vararg cards: WebCard, buttons: List<Button> = emptyList()): WebMessage =
    WebMessage(carousel = WebCarousel(cards = cards.toList()), buttons = buttons)

fun <T : Bus<T>> T.webCarousel(cards: List<WebCard>, buttons: List<Button> = emptyList()): WebMessage =
    WebMessage(carousel = WebCarousel(cards = cards), buttons = buttons)

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
fun <T : Bus<T>> T.webWidget(widget: WebWidget, buttons: List<Button> = emptyList()): WebMessage =
    WebMessage(widget = widget, buttons = buttons)

fun <T : Bus<T>> T.webDeepLink(ref: String): WebMessage = WebMessage(deepLink = WebDeepLink(ref))
