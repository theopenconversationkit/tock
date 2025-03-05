/*
 * Copyright (C) 2017/2021 e-voyageurs technologies
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

package ai.tock.bot.connector.whatsapp.cloud

import ai.tock.bot.connector.ConnectorMessage
import ai.tock.bot.connector.ConnectorType
import ai.tock.bot.connector.whatsapp.cloud.model.common.TextContent
import ai.tock.bot.connector.whatsapp.cloud.model.send.QuickReply
import ai.tock.bot.connector.whatsapp.cloud.model.send.message.WhatsAppCloudBotMessage
import ai.tock.bot.connector.whatsapp.cloud.model.send.message.WhatsAppCloudBotRecipientType
import ai.tock.bot.connector.whatsapp.cloud.model.send.message.content.*
import ai.tock.bot.connector.whatsapp.cloud.model.send.message.content.WhatsAppCloudBotBody
import ai.tock.bot.definition.IntentAware
import ai.tock.bot.definition.Parameters
import ai.tock.bot.definition.StoryHandlerDefinition
import ai.tock.bot.definition.StoryStep
import ai.tock.bot.engine.BotBus
import ai.tock.bot.engine.Bus
import ai.tock.bot.engine.I18nTranslator
import ai.tock.bot.engine.action.SendChoice
import ai.tock.shared.booleanProperty
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}
private val errorOnInvalidMessages = booleanProperty("tock_whatsapp_error_on_invalid_messages", false)

internal const val WHATS_APP_CONNECTOR_TYPE_ID = "whatsapp_cloud"
const val WHATSAPP_BUTTONS_TITLE_MAX_LENGTH = 50
const val WHATSAPP_LIST_BODY_MAX_LENGTH = 4096
const val WHATSAPP_REPLY_BUTTON_BODY_MAX_LENGTH = 1024
const val WHATSAPP_IMAGE_CAPTION_MAX_LENGTH = 1024
const val WHATSAPP_REPLY_BUTTONS_MAX_COUNT = 3
const val WHATSAPP_LIST_HEADER_MAX_LENGTH = 60
const val WHATSAPP_LIST_BUTTON_MAX_LENGTH = 20
const val WHATSAPP_SECTION_TITLE_MAX_LENGTH = 24
const val WHATSAPP_ROW_TITLE_MAX_LENGTH = 24
const val WHATSAPP_ROW_DESCRIPTION_MAX_LENGTH = 72
const val WHATSAPP_MAX_ROWS = 10
const val WHATSAPP_MAX_SECTIONS = 10

/**
 * The WhatsApp cloud connector type.
 */
val whatsAppCloudConnectorType = ConnectorType(WHATS_APP_CONNECTOR_TYPE_ID)

/**
 * Sends an WhatsApp message only if the [ConnectorType] of the current [BotBus] is [whatsAppCloudConnectorType].
 */
fun <T : Bus<T>> T.sendToWhatsAppCloud(
    messageProvider: T.() -> WhatsAppCloudBotMessage,
    delay: Long = defaultDelay(currentAnswerIndex)
): T {
    if (isCompatibleWith(whatsAppCloudConnectorType)) {
        withMessage(messageProvider(this))
        send(delay)
    }
    return this
}


/**
 * Adds a WhatsApp [ConnectorMessage] if the current connector is WhatsApp.
 * You need to call [BotBus.send] or [BotBus.end] later to send this message.
 */
fun <T : Bus<T>> T.withWhatsAppCloud(messageProvider: () -> WhatsAppCloudConnectorMessage): T {
    return withMessage(whatsAppCloudConnectorType, messageProvider)
}

/**
 * Creates a basic [Text Message](https://developers.facebook.com/docs/whatsapp/cloud-api/messages/text-messages).
 *
 * @param text the text sent
 * @param previewUrl if set to `true`, WhatsApp will render a preview of the first URL in the message's [text]
 */
fun BotBus.whatsAppCloudText(
    text: CharSequence,
    previewUrl: Boolean = false
): WhatsAppCloudBotTextMessage =
    WhatsAppCloudBotTextMessage(
        messagingProduct = "whatsapp",
        text = TextContent(translate(text).toString()),
        recipientType = WhatsAppCloudBotRecipientType.individual,
        userId = userId.id,
        previewUrl = previewUrl,
    )

/**
 * Creates an [Image Message](https://developers.facebook.com/docs/whatsapp/cloud-api/messages/image-messages)
 *
 * @param id the URL of the image
 * @param caption a caption to display below the image
 */
fun BotBus.whatsAppCloudImage(
    id: String,
    link: String? = null,
    caption: CharSequence? = null,
): WhatsAppCloudBotImageMessage =
    WhatsAppCloudBotImageMessage(
        messagingProduct = "whatsapp",
        image = WhatsAppCloudBotImage(
            id = id,
            link = link,
            caption = translate(caption).toString().checkLength(WHATSAPP_IMAGE_CAPTION_MAX_LENGTH)
        ),
        recipientType = WhatsAppCloudBotRecipientType.individual,
        userId = userId.id,
    )

/**
 * Creates an [Interactive Reply Button Message](https://developers.facebook.com/docs/whatsapp/cloud-api/messages/interactive-reply-buttons-messages)
 *
 * Limitations:
 * - [text] is limited to 1024 characters
 * - each [quick reply's title][QuickReply.title] is limited to 20 characters
 * - there can be **no** [quick reply description][QuickReply.description]
 * - there can be a maximum of 3 [replies] in the message
 *
 * @param text the body of the message
 * @param replies the replies to display below the message
 * @param header an optional text header
 * @param footer an optional text footer
 */
fun I18nTranslator.whatsAppCloudReplyButtonMessage(
    text: CharSequence,
    vararg replies: QuickReply,
    header: CharSequence? = null,
    footer: CharSequence? = null,
): WhatsAppCloudBotInteractiveMessage =
    whatsAppCloudReplyButtonMessage(text, replies.toList(), header, footer)

/**
 * Creates an [Interactive Reply Button Message](https://developers.facebook.com/docs/whatsapp/cloud-api/messages/interactive-reply-buttons-messages)
 *
 * Limitations:
 * - [text] is limited to 1024 characters
 * - each [quick reply's title][QuickReply.title] is limited to 20 characters
 * - there can be **no** [quick reply description][QuickReply.description]
 * - there can be a maximum of 3 [replies] in the message
 *
 * @param text the body of the message
 * @param replies the replies to display below the message
 * @param header an optional text header
 * @param footer an optional text footer
 */
fun I18nTranslator.whatsAppCloudReplyButtonMessage(
    text: CharSequence,
    replies: List<QuickReply>,
    header: CharSequence? = null,
    footer: CharSequence? = null,
): WhatsAppCloudBotInteractiveMessage =
    whatsAppCloudReplyButtonMessage(text, footer, replies, header = header?.let {
        WhatsAppCloudBotInteractiveHeader(
            WhatsAppCloudBotHeaderType.text,
            text = translate(header).toString(),
        )
    })

internal fun I18nTranslator.whatsAppCloudReplyButtonMessage(
    text: CharSequence,
    footer: CharSequence?,
    replies: List<QuickReply>,
    header: WhatsAppCloudBotInteractiveHeader?
) = WhatsAppCloudBotInteractiveMessage(
    messagingProduct = "whatsapp",
    recipientType = WhatsAppCloudBotRecipientType.individual,
    interactive = WhatsAppCloudBotInteractive(
        type = WhatsAppCloudBotInteractiveType.button,
        header = header,
        body = WhatsAppCloudBotBody(translate(text).toString().checkLength(WHATSAPP_REPLY_BUTTON_BODY_MAX_LENGTH)),
        footer = footer?.let {
            WhatsAppCloudBotFooter(
                translate(it).toString().checkLength(WHATSAPP_LIST_HEADER_MAX_LENGTH, "message footer"),
            )
        },
        action = WhatsAppCloudBotAction(
            buttons = replies.checkCount(WHATSAPP_REPLY_BUTTONS_MAX_COUNT) { count ->
                "$count is too many buttons for a reply button message"
            }.map {
                WhatsAppCloudBotActionButton(
                    reply = WhatsAppCloudBotActionButtonReply(
                        id = it.payload,
                        title = translate(it.title).toString().checkLength(WHATSAPP_BUTTONS_TITLE_MAX_LENGTH),
                    )
                )
            }
        )
    )
)

/**
 * Creates an [Interactive Reply Button Message](https://developers.facebook.com/docs/whatsapp/cloud-api/messages/interactive-reply-buttons-messages)
 * with an image header
 *
 * Limitations:
 * - [text] is limited to 1024 characters
 * - [footer] is limited to 60 characters
 * - each [quick reply's title][QuickReply.title] is limited to 20 characters
 * - there can be **no** [quick reply description][QuickReply.description]
 * - there can be a maximum of 3 [replies] in the message
 *
 * Warning: this message type may appear after a delay (and possibly after subsequent messages),
 * due to the image upload time
 *
 * @param imgUrl the URL of the image to display in the message header
 * @param text the body of the message
 * @param replies the replies to display below the message
 * @param footer an optional text footer
 */
fun I18nTranslator.whatsAppCloudReplyImgButtonMessage(
    imgUrl: String,
    text: CharSequence,
    vararg replies: QuickReply,
    footer: CharSequence? = null,
): WhatsAppCloudBotInteractiveMessage =
    whatsAppCloudReplyImgButtonMessage(imgUrl, text, replies.toList(), footer)

/**
 * Creates an [Interactive Reply Button Message](https://developers.facebook.com/docs/whatsapp/cloud-api/messages/interactive-reply-buttons-messages)
 * with an image header
 *
 * Limitations:
 * - [text] is limited to 1024 characters
 * - [footer] is limited to 60 characters
 * - each [quick reply's title][QuickReply.title] is limited to 20 characters
 * - there can be **no** [quick reply description][QuickReply.description]
 * - there can be a maximum of 3 [replies] in the message
 *
 * Warning: this message type may appear after a delay (and possibly after subsequent messages),
 * due to the image upload time
 *
 * @param imgUrl the URL of the image to display in the message header
 * @param text the body of the message
 * @param replies the replies to display below the message
 * @param footer an optional text footer
 */
fun I18nTranslator.whatsAppCloudReplyImgButtonMessage(
    imgUrl: String,
    text: CharSequence,
    replies: List<QuickReply>,
    footer: CharSequence? = null,
): WhatsAppCloudBotInteractiveMessage = whatsAppCloudReplyButtonMessage(text, footer, replies, WhatsAppCloudBotInteractiveHeader(
    type = WhatsAppCloudBotHeaderType.image,
    image = WhatsAppCloudBotMediaImage(
        id = imgUrl
    )
))

/**
 * Creates an [Interactive Call-to-Action URL Button Message](https://developers.facebook.com/docs/whatsapp/cloud-api/messages/interactive-cta-url-messages)
 *
 * @param text the body of the message
 * @param buttonTitle the text to display on the button
 * @param url the URL to open when clicking on the button
 * @param header an optional text header
 * @param footer an optional text footer
 */
fun I18nTranslator.whatsAppCloudUrlButtonMessage(
    text: CharSequence,
    buttonTitle: CharSequence,
    url: String,
    header: CharSequence? = null,
    footer: CharSequence? = null,
): WhatsAppCloudBotInteractiveMessage = WhatsAppCloudBotInteractiveMessage(
    messagingProduct = "whatsapp",
    recipientType = WhatsAppCloudBotRecipientType.individual,
    interactive = WhatsAppCloudBotInteractive(
        type = WhatsAppCloudBotInteractiveType.cta_url,
        header = header?.let { WhatsAppCloudBotInteractiveHeader(WhatsAppCloudBotHeaderType.text, text = translate(it).toString()) },
        body = WhatsAppCloudBotBody(translate(text).toString()),
        footer = footer?.let { WhatsAppCloudBotFooter(translate(it).toString()) },
        action = WhatsAppCloudBotAction(
            name = "cta_url",
            parameters = ParametersUrl(
                displayText = translate(buttonTitle).toString(),
                url = url
            )
        )
    )
)

/**
 * Creates an [Interactive List Message](https://developers.facebook.com/docs/whatsapp/cloud-api/messages/interactive-list-messages)
 *
 * Limitations:
 * - [text] is limited to 4096 characters
 * - [button] is limited to 20 characters
 * - each [quick reply's title][QuickReply.title] is limited to 24 characters
 * - each [quick reply's description][QuickReply.description] is limited to 72 characters
 * - there can be a maximum of 10 [replies] in the message
 *
 * Length limitations apply to the localized form of each text, if applicable
 *
 * @see whatsAppCloudQuickReply
 */
fun I18nTranslator.whatsAppCloudListMessage(
    text: CharSequence,
    button: CharSequence,
    vararg replies: QuickReply,
    header: CharSequence? = null,
    footer: CharSequence? = null,
): WhatsAppCloudBotInteractiveMessage =
    whatsAppCloudListMessage(text, button, replies.toList(), header, footer)

/**
 * Creates an [Interactive List Message](https://developers.facebook.com/docs/whatsapp/cloud-api/messages/interactive-list-messages)
 *
 * Limitations:
 * - [text] is limited to 4096 characters
 * - [button] is limited to 20 characters
 * - [header] is limited to 60 characters
 * - [footer] is limited to 60 characters
 * - each [quick reply's title][QuickReply.title] is limited to 24 characters
 * - each [quick reply's description][QuickReply.description] is limited to 72 characters
 * - there can be a maximum of 10 [replies] in the message
 *
 * Length limitations apply to the localized form of each text, if applicable
 *
 * @see whatsAppCloudQuickReply
 */
fun I18nTranslator.whatsAppCloudListMessage(
    text: CharSequence,
    button: CharSequence,
    replies: List<QuickReply>,
    header: CharSequence? = null,
    footer: CharSequence? = null,
): WhatsAppCloudBotInteractiveMessage =
    whatsAppCloudListMessage(
        text, button,
        WhatsAppCloudBotActionSection(rows = replies.checkCount(WHATSAPP_MAX_ROWS) { count ->
            "$count is too many buttons for a list message"
        }.map {
            WhatsAppBotRow(
                id = it.payload,
                title = it.title.checkLength(WHATSAPP_ROW_TITLE_MAX_LENGTH),
                description = it.description?.checkLength(WHATSAPP_ROW_DESCRIPTION_MAX_LENGTH)
            )
        }),
        header = header,
        footer = footer,
    )

/**
 * Creates an [Interactive List Message](https://developers.facebook.com/docs/whatsapp/cloud-api/messages/interactive-list-messages)
 * divided into sections
 *
 * Limitations:
 * - [text] is limited to 4096 characters
 * - [button] is limited to 20 characters
 * - [header] is limited to 60 characters
 * - [footer] is limited to 60 characters
 * - each [quick reply's title][QuickReply.title] is limited to 24 characters
 * - each [quick reply's description][QuickReply.description] is limited to 72 characters
 * - there can be a maximum of 10 [sections] in the message
 * - there can be a maximum of 10 replies across all [sections] in the message
 *
 * Length limitations apply to the localized form of each text, if applicable
 *
 * @see whatsAppCloudListSection
 */
fun I18nTranslator.whatsAppCloudListMessage(
    text: CharSequence,
    button: CharSequence,
    vararg sections: WhatsAppCloudBotActionSection,
    header: CharSequence? = null,
    footer: CharSequence? = null,
): WhatsAppCloudBotInteractiveMessage =
    whatsAppCloudListMessage(text, button, sections.toList(), header, footer)

/**
 * Creates an [Interactive List Message](https://developers.facebook.com/docs/whatsapp/cloud-api/messages/interactive-list-messages)
 * divided into sections
 *
 * Limitations:
 * - [text] is limited to 4096 characters
 * - [button] is limited to 20 characters
 * - [header] is limited to 60 characters
 * - [footer] is limited to 60 characters
 * - each [quick reply's title][QuickReply.title] is limited to 24 characters
 * - each [quick reply's description][QuickReply.description] is limited to 72 characters
 * - there can be a maximum of 10 [sections] in the message
 * - there can be a maximum of 10 replies across all [sections] in the message
 *
 * Length limitations apply to the localized form of each text, if applicable
 *
 * @see whatsAppCloudListSection
 */
@JvmName("whatsAppCloudListMessageWithSections")
fun I18nTranslator.whatsAppCloudListMessage(
    text: CharSequence,
    button: CharSequence,
    sections: List<WhatsAppCloudBotActionSection>,
    header: CharSequence? = null,
    footer: CharSequence? = null,
): WhatsAppCloudBotInteractiveMessage {
    return WhatsAppCloudBotInteractiveMessage(
        messagingProduct = "whatsapp",
        recipientType = WhatsAppCloudBotRecipientType.individual,
        interactive = WhatsAppCloudBotInteractive(
            type = WhatsAppCloudBotInteractiveType.list,
            header = header?.let {
                WhatsAppCloudBotInteractiveHeader(
                    WhatsAppCloudBotHeaderType.text,
                    text = translate(it).toString().checkLength(WHATSAPP_LIST_HEADER_MAX_LENGTH, "list message header")
                )
            },
            footer = footer?.let {
                WhatsAppCloudBotFooter(
                    translate(it).toString().checkLength(WHATSAPP_LIST_HEADER_MAX_LENGTH, "list message footer")
                )
            },
            body = WhatsAppCloudBotBody(translate(text).toString().checkLength(WHATSAPP_LIST_BODY_MAX_LENGTH, "list message body")),
            action = WhatsAppCloudBotAction(
                button = translate(button).toString().checkLength(WHATSAPP_LIST_BUTTON_MAX_LENGTH, "list message button text"),
                sections = sections.checkCount(WHATSAPP_MAX_SECTIONS) { count ->
                    "$count is too many sections for a list message"
                },
            )
        )
    ).also {
        if ((it.interactive.action?.sections?.flatMap { s -> s.rows ?: listOf() }?.count() ?: 0) > WHATSAPP_MAX_ROWS) {
            error("a list message is limited to $WHATSAPP_MAX_ROWS rows across all sections.")
        }
    }
}

/**
 * Creates a section for an interactive list message
 *
 * Limitations:
 * - [title] is limited to 24 characters
 * - each [quick reply's title][QuickReply.title] is limited to 24 characters
 * - each [quick reply's description][QuickReply.description] is limited to 72 characters
 * - there can be a maximum of 10 [rows] in the section
 * - there can be a maximum of 10 [rows] across all sections in a message
 *
 * @see whatsAppCloudListMessage
 * @see whatsAppCloudQuickReply
 */
fun I18nTranslator.whatsAppCloudListSection(title: CharSequence, vararg rows: QuickReply) = whatsAppCloudListSection(title, listOf(*rows))

/**
 * Creates a section for an interactive list message
 *
 * Limitations:
 * - [title] is limited to 24 characters
 * - each [quick reply's title][QuickReply.title] is limited to 24 characters
 * - each [quick reply's description][QuickReply.description] is limited to 72 characters
 * - there can be a maximum of 10 [rows] in the section
 * - there can be a maximum of 10 [rows] across all sections in a message
 *
 * @see whatsAppCloudListMessage
 * @see whatsAppCloudQuickReply
 */
fun I18nTranslator.whatsAppCloudListSection(title: CharSequence, rows: List<QuickReply>) = WhatsAppCloudBotActionSection(
    title = translate(title).toString().checkLength(WHATSAPP_SECTION_TITLE_MAX_LENGTH),
    rows = rows.checkCount(WHATSAPP_MAX_ROWS) { count ->
        "$count is too many rows for a list section"
    }.map { qr -> WhatsAppBotRow(
        id = qr.payload,
        title = translate(qr.title).toString().checkLength(WHATSAPP_ROW_TITLE_MAX_LENGTH),
        description = translate(qr.description).toString().checkLength(WHATSAPP_ROW_DESCRIPTION_MAX_LENGTH)
    ) }
)


/**
 * Creates a [Location Request Message](https://developers.facebook.com/docs/whatsapp/cloud-api/guides/send-messages/location-request-messages)
 *
 * Limitations:
 * - [text] is limited to 1024 characters
 */
fun I18nTranslator.whatsAppCloudReplyLocationMessage(
    text: CharSequence
): WhatsAppCloudBotInteractiveMessage = WhatsAppCloudBotInteractiveMessage(
    messagingProduct = "whatsapp",
    recipientType = WhatsAppCloudBotRecipientType.individual,
    interactive = WhatsAppCloudBotInteractive(
        type = WhatsAppCloudBotInteractiveType.location_request_message,
        body = WhatsAppCloudBotBody(translate(text).toString().checkLength(WHATSAPP_REPLY_BUTTON_BODY_MAX_LENGTH)),
        action = WhatsAppCloudBotAction(
            name = "send_location"
        )
    )
)


/**
 * Creates a quick reply for a message
 *
 * @see whatsAppCloudListMessage
 * @see whatsAppCloudListSection
 * @see whatsAppCloudReplyButtonMessage
 * @see whatsAppCloudReplyImgButtonMessage
 */
fun <T : Bus<T>> T.whatsAppCloudQuickReply(
    title: CharSequence,
    targetIntent: IntentAware,
    parameters: Parameters
): QuickReply =
    whatsAppCloudQuickReply(title, targetIntent, stepName, parameters.toMap())

/**
 * Creates a quick reply for a message
 *
 * @see whatsAppCloudListMessage
 * @see whatsAppCloudListSection
 * @see whatsAppCloudReplyButtonMessage
 * @see whatsAppCloudReplyImgButtonMessage
 */
fun <T : Bus<T>> T.whatsAppCloudQuickReply(
    title: CharSequence,
    targetIntent: IntentAware,
    step: StoryStep<out StoryHandlerDefinition>? = null,
    vararg parameters: Pair<String, String>
): QuickReply = whatsAppCloudQuickReply(title, targetIntent.wrappedIntent(), step?.name, parameters.toMap())

@Deprecated("Use step object directly instead of its name")
fun <T : Bus<T>> T.whatsAppCloudQuickReply(
    title: CharSequence,
    targetIntent: IntentAware,
    step: String?,
    parameters: Map<String, String> = mapOf()
): QuickReply =
    whatsAppCloudQuickReply(title, null, targetIntent, step, parameters) { intent, s, params ->
        SendChoice.encodeChoiceId(intent, s, params, null, null, sourceAppId = null)
    }

/**
 * Creates a quick reply for a message
 *
 * @see whatsAppCloudListMessage
 * @see whatsAppCloudListSection
 * @see whatsAppCloudReplyButtonMessage
 * @see whatsAppCloudReplyImgButtonMessage
 */
fun <T : Bus<T>> T.whatsAppCloudQuickReply(
    title: CharSequence,
    targetIntent: IntentAware,
    step: StoryStep<*>? = null,
    parameters: Map<String, String> = mapOf()
): QuickReply =
    whatsAppCloudQuickReply(title, null, targetIntent, step?.name, parameters) { intent, s, params ->
        SendChoice.encodeChoiceId(intent, s, params, null, null, sourceAppId = null)
    }

@Deprecated("Use step object directly instead of its name", level = DeprecationLevel.HIDDEN)
fun <T : Bus<T>> T.whatsAppCloudQuickReply(
    title: CharSequence,
    subTitle: CharSequence? = null,
    targetIntent: IntentAware,
    step: String?,
    parameters: Map<String, String> = mapOf()
): QuickReply =
    whatsAppCloudQuickReply(title,subTitle, targetIntent, step, parameters) { intent, s, params ->
        SendChoice.encodeChoiceId(intent, s, params, null, null, sourceAppId = null)
    }

/**
 * Creates a quick reply for a message
 *
 * @see whatsAppCloudListMessage
 * @see whatsAppCloudListSection
 * @see whatsAppCloudReplyButtonMessage
 * @see whatsAppCloudReplyImgButtonMessage
 */
fun <T : Bus<T>> T.whatsAppCloudQuickReply(
    title: CharSequence,
    subTitle: CharSequence? = null,
    targetIntent: IntentAware,
    step: StoryStep<*>? = null,
    parameters: Map<String, String> = mapOf()
): QuickReply =
    whatsAppCloudQuickReply(title,subTitle, targetIntent, step?.name, parameters) { intent, s, params ->
        SendChoice.encodeChoiceId(intent, s, params, null, null, sourceAppId = null)
    }

private inline fun I18nTranslator.whatsAppCloudQuickReply(
    title: CharSequence,
    subTitle: CharSequence? = null,
    targetIntent: IntentAware,
    step: String? = null,
    parameters: Map<String, String>,
    encodePayload: (IntentAware, String?, Map<String, String>) -> String
): QuickReply = QuickReply(
    translate(title).toString(),
    encodePayload(targetIntent, step, parameters),
    translate(subTitle).toString()
)

/**
 * This quick reply does not use any custom payload, but the [textToSend] will we parsed by the NLP engine.
 */
fun I18nTranslator.whatsAppCloudNlpQuickReply(
    title: CharSequence,
    textToSend: CharSequence = title,
): QuickReply = QuickReply(
    translate(title).toString(),
    SendChoice.encodeNlpChoiceId(translate(textToSend).toString()),
)

@Deprecated("renamed", ReplaceWith("whatsAppBuildCloudTemplateMessage(templateName, languageCode, components)"))
fun I18nTranslator.whatsAppBuildCloudTemplateMessage(
    templateName: String,
    languageCode: String,
    components: List<Component>
) = whatsAppCloudTemplateMessage(templateName, languageCode, components)

/**
 * Creates a [Template Message](https://developers.facebook.com/docs/whatsapp/cloud-api/guides/send-message-templates)
 *
 * Note: this type of message incurs additional costs for each use
 */
fun I18nTranslator.whatsAppCloudTemplateMessage(
    templateName: String,
    languageCode: String,
    components: List<Component>
): WhatsAppCloudBotTemplateMessage {
    return WhatsAppCloudBotTemplateMessage(
        messagingProduct = "whatsapp",
        recipientType = WhatsAppCloudBotRecipientType.individual,
        template = WhatsAppCloudBotTemplate(
            name = templateName,
            language = Language(
                code = languageCode,
            ),
            components = components
        )
    )
}

@Deprecated("renamed", ReplaceWith("whatsAppCloudTemplateMessageCarousel(templateName, components, languageCode)"))
fun I18nTranslator.whatsAppCloudBuildTemplateMessageCarousel(
    templateName: String,
    components: List<Component.Card>,
    languageCode: String
) = whatsAppCloudTemplateMessageCarousel(templateName, components, languageCode)

/**
 * Creates a [Media Card Carousel Template](https://developers.facebook.com/docs/whatsapp/cloud-api/guides/send-message-templates/media-card-carousel-templates)
 *
 * Note: this type of message incurs additional costs for each use
 */
fun I18nTranslator.whatsAppCloudTemplateMessageCarousel(
    templateName: String,
    components: List<Component.Card>,
    languageCode: String
): WhatsAppCloudBotTemplateMessage {
    return WhatsAppCloudBotTemplateMessage(
        messagingProduct = "whatsapp",
        recipientType = WhatsAppCloudBotRecipientType.individual,
        template = WhatsAppCloudBotTemplate(
            name = templateName,
            language = Language(
                code = languageCode,
            ),
            components = listOf(
                Component.Carousel(
                    type = ComponentType.CAROUSEL,
                    cards = components
                )
            )
        )
    )
}

fun <T : Bus<T>> T.whatsAppCloudCardCarousel(cardIndex: Int, components: List<Component>): Component.Card {
    return whatsAppCloudTemplateCard(
        cardIndex, components
    )
}

/**
 * Creates a Media Card for a Carousel Template
 *
 * @param cardIndex the index of the card within the carousel
 * @param components the list of components making up the card
 * @see whatsAppCloudTemplateMessageCarousel
 * @see whatsAppCloudTemplateBody
 */
fun <T : Bus<T>> T.whatsAppCloudTemplateCard(
    cardIndex: Int,
    components: List<Component>
): Component.Card = Component.Card(
    cardIndex = cardIndex,
    components = components
)

@Deprecated("renamed", ReplaceWith("whatsAppCloudTemplateCardBody(templateName, components, languageCode)"))
fun <T : Bus<T>> T.whatsAppCloudBodyTemplate(
    parameters: List<TextParameter>
) = whatsAppCloudTemplateBody(parameters)

/**
 * Creates a body component for a template
 *
 * @see whatsAppCloudTemplateCard
 */
fun <T : Bus<T>> T.whatsAppCloudTemplateBody(
    parameters: List<TextParameter>
): Component.Body = Component.Body(
    type = ComponentType.BODY,
    parameters = parameters
)

@Deprecated("use whatsAppCloudTextParameterTemplate(textButton) instead")
fun <T : Bus<T>> T.whatsAppCloudTextParameterTemplate(
    typeParameter: CharSequence?,
    textButton: CharSequence?
): TextParameter = TextParameter(
    type = ParameterType.valueOf((typeParameter).toString()),
    text = translate(textButton).toString(),
)

fun <T : Bus<T>> T.whatsAppCloudTextParameterTemplate(
    buttonTitle: CharSequence?
): TextParameter = TextParameter(
    type = ParameterType.TEXT,
    text = translate(buttonTitle).toString(),
)

fun whatsAppCloudButtonTemplate(
    index: Int,
    subType: ButtonSubType,
    parameters: List<PayloadParameter>
): Component.Button = Component.Button(
    type = ComponentType.BUTTON,
    subType = subType,
    index = index.toString(),
    parameters = parameters
)

fun <T : Bus<T>> T.whatsAppCloudPostbackButton(
    index: Int,
    textButton: String,
    payload: String?
): Component.Button = whatsAppCloudButtonTemplate(
    index, ButtonSubType.QUICK_REPLY, listOf(
        whatsAppCloudPayloadParameterTemplate(textButton, payload, ParameterType.PAYLOAD)
    )
)

@Deprecated("Use variant with Int index")
fun <T : Bus<T>> T.whatsAppCloudPostbackButton(
    index: String,
    title: CharSequence,
    targetIntent: IntentAware,
    step: StoryStep<out StoryHandlerDefinition>? = null,
    parameters: Parameters = Parameters()
) = whatsAppCloudPostbackButton(index.toInt(), title, targetIntent, step, parameters)

fun <T : Bus<T>> T.whatsAppCloudPostbackButton(
    index: Int,
    title: CharSequence,
    targetIntent: IntentAware,
    step: StoryStep<out StoryHandlerDefinition>? = null,
    parameters: Parameters = Parameters()
): Component.Button = whatsAppCloudPostbackButton(
    index = index,
    textButton = translate(title).toString(),
    // Add an index parameter to ensure that all buttons in the list have unique ids
    targetIntent.let { i -> SendChoice.encodeChoiceId(this, i, step, parameters.toMap() + (index.toString() to index.toString())) }
)

fun <T : Bus<T>> T.whatsAppCloudNLPPostbackButton(
    index: Int,
    title: CharSequence,
    textToSend: CharSequence = title,
): Component.Button = whatsAppCloudPostbackButton(
    index = index,
    textButton = translate(title).toString(),
    payload = SendChoice.encodeNlpChoiceId(translate(textToSend).toString()),
)

fun <T : Bus<T>> T.whatsAppCloudUrlButton(
    index: Int,
    textButton: String,
): Component.Button = whatsAppCloudButtonTemplate(
    index, ButtonSubType.URL, listOf(
        whatsAppCloudPayloadParameterTemplate(textButton, null, ParameterType.TEXT)
    )
)

fun whatsAppCloudPayloadParameterTemplate(
    textButton: String,
    payload: String?,
    typeParameter: ParameterType,
): PayloadParameter = PayloadParameter(
    type = typeParameter,
    payload = payload,
    text = textButton,
)


@Deprecated("renamed", ReplaceWith("whatsAppCloudTemplateImageHeader(imageId)"))
fun whatsAppCloudHeaderTemplate(
    typeParameter: String,
    imageId: String
) = whatsAppCloudTemplateImageHeader(imageId)

fun whatsAppCloudTemplateImageHeader(
    imageId: String
): Component.Header = Component.Header(
    type = ComponentType.HEADER,
    parameters = listOf(
        HeaderParameter.Image(
            type = ParameterType.IMAGE,
            image = ImageId(
                id = imageId
            )
        )
    )
)

private inline fun <T> List<T>.checkCount(maxCount: Int, errorMessage: (Int) -> String): List<T> {
    return if (this.size > maxCount) {
        val msg = errorMessage(this.size) + " (max: $maxCount)"
        if (errorOnInvalidMessages) {
            throw IllegalArgumentException(msg)
        } else {
            logger.warn(msg)
            this.take(maxCount)
        }
    } else {
        this
    }
}

private fun String.checkLength(maxLength: Int, textType: String = "text"): String {
    return if (maxLength > 0 && this.length > maxLength) {
        val msg = "$textType \"$this\" should not exceed $maxLength chars."
        if (errorOnInvalidMessages) {
            throw IllegalArgumentException(msg)
        } else {
            logger.warn(msg)
            this.take(maxLength - 1) + "…"
        }
    } else {
        this
    }
}
