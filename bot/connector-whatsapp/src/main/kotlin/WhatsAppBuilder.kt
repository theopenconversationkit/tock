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
package ai.tock.bot.connector.whatsapp

import ai.tock.bot.connector.ConnectorMessage
import ai.tock.bot.connector.ConnectorType
import ai.tock.bot.connector.whatsapp.model.common.WhatsAppTextBody
import ai.tock.bot.connector.whatsapp.model.send.QuickReply
import ai.tock.bot.connector.whatsapp.model.send.WhatsAppBotAction
import ai.tock.bot.connector.whatsapp.model.send.WhatsAppBotActionButton
import ai.tock.bot.connector.whatsapp.model.send.WhatsAppBotActionButtonReply
import ai.tock.bot.connector.whatsapp.model.send.WhatsAppBotActionSection
import ai.tock.bot.connector.whatsapp.model.send.WhatsAppBotAttachment
import ai.tock.bot.connector.whatsapp.model.send.WhatsAppBotBody
import ai.tock.bot.connector.whatsapp.model.send.WhatsAppBotImageMessage
import ai.tock.bot.connector.whatsapp.model.send.WhatsAppBotInteractive
import ai.tock.bot.connector.whatsapp.model.send.WhatsAppBotInteractiveMessage
import ai.tock.bot.connector.whatsapp.model.send.WhatsAppBotInteractiveType
import ai.tock.bot.connector.whatsapp.model.send.WhatsAppBotMessage
import ai.tock.bot.connector.whatsapp.model.send.WhatsAppBotMessageInteractiveMessage
import ai.tock.bot.connector.whatsapp.model.send.WhatsAppBotRecipientType
import ai.tock.bot.connector.whatsapp.model.send.WhatsAppBotRow
import ai.tock.bot.connector.whatsapp.model.send.WhatsAppBotTextMessage
import ai.tock.bot.connector.whatsapp.model.webhook.WhatsAppComponent
import ai.tock.bot.connector.whatsapp.model.webhook.WhatsAppLanguage
import ai.tock.bot.connector.whatsapp.model.webhook.WhatsAppTemplate
import ai.tock.bot.definition.IntentAware
import ai.tock.bot.definition.Parameters
import ai.tock.bot.definition.StoryHandlerDefinition
import ai.tock.bot.definition.StoryStep
import ai.tock.bot.engine.BotBus
import ai.tock.bot.engine.Bus
import ai.tock.bot.engine.I18nTranslator
import ai.tock.bot.engine.action.SendChoice
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

internal const val WHATS_APP_CONNECTOR_TYPE_ID = "whatsapp"
private const val WHATS_APP_BUTTONS_TITLE_MAX_LENGTH = 20
private const val WHATS_APP_BUTTONS_ID_MAX_LENGTH = 256
private const val WHATS_APP_SECTION_TITLE_MAX_LENGTH = 24
private const val WHATS_APP_ROW_TITLE_MAX_LENGTH = 24
private const val WHATS_APP_ROW_ID_MAX_LENGTH = 200
private const val WHATS_APP_ROW_DESCRIPTION_MAX_LENGTH = 72
private const val WHATS_APP_MAX_ROWS = 10
private const val WHATS_APP_MAX_SECTIONS = 10

/**
 * The WhatsApp connector type.
 */
val whatsAppConnectorType = ConnectorType(WHATS_APP_CONNECTOR_TYPE_ID)

/**
 * Sends an WhatsApp message only if the [ConnectorType] of the current [BotBus] is [whatsAppConnectorType].
 */
fun <T : Bus<T>> T.sendToWhatsApp(
    messageProvider: T.() -> WhatsAppBotMessage,
    delay: Long = defaultDelay(currentAnswerIndex)
): T {
    if (isCompatibleWith(whatsAppConnectorType)) {
        withMessage(messageProvider(this))
        send(delay)
    }
    return this
}

/**
 * Sends an WhatsApp message as last bot answer, only if the [ConnectorType] of the current [BotBus] is [whatsAppConnectorType].
 */
fun <T : Bus<T>> T.endForWhatsApp(
    messageProvider: T.() -> WhatsAppBotMessage,
    delay: Long = defaultDelay(currentAnswerIndex)
): T {
    if (isCompatibleWith(whatsAppConnectorType)) {
        withMessage(messageProvider(this))
        end(delay)
    }
    return this
}

/**
 * Adds a WhatsApp [ConnectorMessage] if the current connector is WhatsApp.
 * You need to call [BotBus.send] or [BotBus.end] later to send this message.
 */
fun <T : Bus<T>> T.withWhatsApp(messageProvider: () -> WhatsAppBotMessage): T {
    return withMessage(whatsAppConnectorType, messageProvider)
}

/**
 * Creates a [WhatsAppBotTextMessage].
 *
 * @param text the text sent
 * @param previewUrl is preview mode is used?
 */
fun BotBus.whatsAppText(
    text: CharSequence,
    previewUrl: Boolean = false
): WhatsAppBotTextMessage =
    WhatsAppBotTextMessage(
        text = WhatsAppTextBody(translate(text).toString()),
        recipientType = (connectorData.callback as? WhatsAppConnectorCallback)?.recipientType
            ?: WhatsAppBotRecipientType.individual,
        userId = userId.id,
        previewUrl = previewUrl
    )

/**
 * Creates a [WhatsAppBotImageMessage].
 */
fun BotBus.whatsAppImage(
    byteImages: ByteArray,
    contentType: String = "image/png",
    caption: CharSequence? = null
): WhatsAppBotImageMessage =
    WhatsAppBotImageMessage(
        image = WhatsAppBotAttachment(
            byteImages,
            contentType,
            caption?.let { translate(it).toString() }
        ),
        recipientType = (connectorData.callback as? WhatsAppConnectorCallback)?.recipientType
            ?: WhatsAppBotRecipientType.individual,
        userId = userId.id
    )

/**
 * Creates a [WhatsAppBotInteractiveMessage]
 */
fun BotBus.whatsAppInteractiveMessage(
    nameSpace: String,
    templateName: String,
    components: List<WhatsAppComponent>? = emptyList()
): WhatsAppBotInteractiveMessage =
    WhatsAppBotInteractiveMessage(
        template = buildTemplate(nameSpace, templateName, components),
        recipientType = (connectorData.callback as? WhatsAppConnectorCallback)?.recipientType
            ?: WhatsAppBotRecipientType.individual,
        userId = userId.id
    )

fun I18nTranslator.replyButtonMessage(
    text: CharSequence,
    vararg replies: QuickReply
) : WhatsAppBotMessageInteractiveMessage = replyButtonMessage(text, replies.toList())

fun I18nTranslator.replyButtonMessage(
    text: CharSequence,
    replies: List<QuickReply>
) : WhatsAppBotMessageInteractiveMessage = WhatsAppBotMessageInteractiveMessage(
    recipientType = WhatsAppBotRecipientType.individual,
    interactive = WhatsAppBotInteractive(
        type = WhatsAppBotInteractiveType.button,
        body = WhatsAppBotBody(translate(text).toString()),
        action = WhatsAppBotAction(
            buttons = replies.map {
                WhatsAppBotActionButton(
                    reply = WhatsAppBotActionButtonReply(
                        id = it.payload.checkLength(WHATS_APP_BUTTONS_ID_MAX_LENGTH),
                        title = translate(it.title).toString().checkLength(WHATS_APP_BUTTONS_TITLE_MAX_LENGTH),
                    )
                )
            }
        )
    )
)

fun I18nTranslator.completeListMessage(
    text: CharSequence,
    button: CharSequence,
    vararg sections: WhatsAppBotActionSection
) : WhatsAppBotMessageInteractiveMessage = completeListMessage(text, button, sections.toList())

fun I18nTranslator.completeListMessage(
    text: CharSequence,
    button: CharSequence,
    sections: List<WhatsAppBotActionSection>,
) : WhatsAppBotMessageInteractiveMessage = WhatsAppBotMessageInteractiveMessage(
    recipientType = WhatsAppBotRecipientType.individual,
    interactive = WhatsAppBotInteractive(
        type = WhatsAppBotInteractiveType.list,
        body = WhatsAppBotBody(translate(text).toString()),
        action = WhatsAppBotAction(
            button = translate(button).toString().checkLength(WHATS_APP_BUTTONS_TITLE_MAX_LENGTH),
            sections = sections.map {
                WhatsAppBotActionSection(
                    title = translate(it.title).toString().checkLength(WHATS_APP_SECTION_TITLE_MAX_LENGTH),
                    rows = it.rows?.map { row ->
                        WhatsAppBotRow(
                            id = row.id.checkLength(WHATS_APP_ROW_ID_MAX_LENGTH),
                            title = translate(row.title).toString().checkLength(WHATS_APP_ROW_TITLE_MAX_LENGTH),
                            description = translate(row.description).toString().checkLength(WHATS_APP_ROW_DESCRIPTION_MAX_LENGTH)
                        )
                    }
                )
            }
        )
    )
).also {
    if ((it.interactive.action?.sections?.flatMap { s -> s.rows ?: listOf() }?.count() ?: 0) > WHATS_APP_MAX_ROWS) {
        error("a list message is limited to $WHATS_APP_MAX_ROWS rows across all sections.")
    }
    if ((it.interactive.action?.sections?.count() ?: 0) > WHATS_APP_MAX_SECTIONS) {
        error("sections count in list message should not exceed $WHATS_APP_MAX_SECTIONS.")
    }
    if ((it.interactive.action?.button?.count() ?: 0) > WHATS_APP_BUTTONS_TITLE_MAX_LENGTH) {
        error("button text ${it.interactive.action?.button} should not exceed $WHATS_APP_BUTTONS_TITLE_MAX_LENGTH chars.")
    }
}

fun I18nTranslator.listMessage(
    text: CharSequence,
    button: CharSequence,
    vararg replies: QuickReply
) : WhatsAppBotMessageInteractiveMessage =
        listMessage(text, button, replies.toList())

fun I18nTranslator.listMessage(
    text: CharSequence,
    button: CharSequence,
    replies: List<QuickReply>
) : WhatsAppBotMessageInteractiveMessage =
    completeListMessage(
        text, button, WhatsAppBotActionSection(rows = replies.map {
            WhatsAppBotRow(
                id = it.payload,
                title = it.title,
                description = it.subTitle
            )
        })
    )

fun I18nTranslator.nlpQuickReply(
    title: CharSequence,
    subTitle: CharSequence? = null,
    textToSend: CharSequence = title,
) : QuickReply = QuickReply(
        translate(title).toString(),
        SendChoice.encodeNlpChoiceId(translate(textToSend).toString()),
        translate(subTitle).toString(),
)

fun <T: Bus<T>> T.quickReply(
    title: CharSequence,
    subTitle: CharSequence? = null,
    targetIntent: IntentAware,
    parameters: Parameters
): QuickReply =
    quickReply(title, subTitle, targetIntent, stepName, parameters.toMap())

fun <T: Bus<T>> T.quickReply(
    title: CharSequence,
    subTitle: CharSequence? = null,
    targetIntent: IntentAware,
    step: StoryStep<out StoryHandlerDefinition>? = null,
    vararg parameters: Pair<String, String>
) : QuickReply = quickReply(title, subTitle, targetIntent.wrappedIntent(), step?.name, parameters.toMap())

fun <T: Bus<T>> T.quickReply(
    title: CharSequence,
    subTitle: CharSequence? = null,
    targetIntent: IntentAware,
    step: String? = null,
    parameters: Map<String, String> = mapOf()
) : QuickReply =
    quickReply(title, subTitle, targetIntent, step, parameters) { intent, s, params ->
        SendChoice.encodeChoiceId(intent, s, params, null, null, sourceAppId = null)
    }

private fun I18nTranslator.quickReply(
    title: CharSequence,
    subTitle: CharSequence? = null,
    targetIntent: IntentAware,
    step: String? = null,
    parameters: Map<String, String>,
    payloadEncoder: (IntentAware, String?, Map<String, String>) -> String
) : QuickReply = QuickReply(
        translate(title).toString(),
        payloadEncoder.invoke(targetIntent, step, parameters),
        translate(subTitle).toString()
)

private fun buildTemplate(
    nameSpace: String,
    templateName: String,
    components: List<WhatsAppComponent>?
): WhatsAppTemplate {
    return WhatsAppTemplate(
        namespace = nameSpace,
        name = templateName,
        language = WhatsAppLanguage(
            code = "fr",
            policy = "deterministic"
        ),
        components = components
    )
}

private fun String.checkLength(maxLength: Int) : String {
    if (maxLength > 0 && this.length > maxLength) {
        error("text $this should not exceed $maxLength chars.")
    }
    return this
}
