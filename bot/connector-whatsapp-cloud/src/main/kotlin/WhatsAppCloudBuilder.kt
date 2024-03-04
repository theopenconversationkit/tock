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
import ai.tock.bot.connector.whatsapp.cloud.model.send.manageTemplate.Card
import ai.tock.bot.connector.whatsapp.cloud.model.send.manageTemplate.LanguageCode
import ai.tock.bot.connector.whatsapp.cloud.model.send.manageTemplate.TemplateComponent
import ai.tock.bot.connector.whatsapp.cloud.model.send.message.WhatsAppCloudBotMessage
import ai.tock.bot.connector.whatsapp.cloud.model.send.message.WhatsAppCloudBotRecipientType
import ai.tock.bot.connector.whatsapp.cloud.model.send.message.content.*
import ai.tock.bot.connector.whatsapp.cloud.model.send.message.content.WhatsAppCloudBotBody
import ai.tock.bot.connector.whatsapp.cloud.model.send.manageTemplate.WhatsAppCloudTemplate
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

internal const val WHATS_APP_CONNECTOR_TYPE_ID = "whatsapp_cloud"
private const val WHATS_APP_BUTTONS_TITLE_MAX_LENGTH = 20
private const val WHATS_APP_BUTTONS_ID_MAX_LENGTH = 256
private const val WHATS_APP_SECTION_TITLE_MAX_LENGTH = 24
private const val WHATS_APP_ROW_TITLE_MAX_LENGTH = 24
private const val WHATS_APP_ROW_ID_MAX_LENGTH = 200
private const val WHATS_APP_ROW_DESCRIPTION_MAX_LENGTH = 72
private const val WHATS_APP_MAX_ROWS = 10
private const val WHATS_APP_MAX_SECTIONS = 10

/**
 * The WhatsApp cloud connector type.
 * Les methodes présente ici sont utilisé pour que le chat bot répond a l'utilisateur
 */
val whatsAppCloudConnectorType = ConnectorType(WHATS_APP_CONNECTOR_TYPE_ID)

/**
 * Sends an WhatsApp message only if the [ConnectorType] of the current [BotBus] is [whatsAppCloudConnectorType].
 */
fun <T : Bus<T>> T.sendToWhatsApp(
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
fun <T : Bus<T>> T.withWhatsAppCloud(messageProvider: () -> WhatsAppCloudBotMessage): T {
    return withMessage(whatsAppCloudConnectorType, messageProvider)
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
): WhatsAppCloudBotTextMessage =
        WhatsAppCloudBotTextMessage(
                messagingProduct = "whatsapp",
                text = TextContent(translate(text).toString()),
                recipientType = WhatsAppCloudBotRecipientType.individual,
                userId = userId.id,
        )

fun I18nTranslator.replyButtonMessage(
        text: CharSequence,
        vararg replies: QuickReply
) : WhatsAppCloudBotInteractiveMessage = replyButtonMessage(text, replies.toList())

fun I18nTranslator.replyButtonMessage(
        text: CharSequence,
        replies: List<QuickReply>
) : WhatsAppCloudBotInteractiveMessage = WhatsAppCloudBotInteractiveMessage(
        messagingProduct = "whatsapp",
        recipientType = WhatsAppCloudBotRecipientType.individual,
        interactive = WhatsAppCloudBotInteractive(
                type = WhatsAppCloudBotInteractiveType.button,
                body = WhatsAppCloudBotBody(translate(text).toString()),
                action = WhatsAppCloudBotAction(
                        buttons = replies.map {
                            WhatsAppCloudBotActionButton(
                                    reply = WhatsAppCloudBotActionButtonReply(
                                            id = it.payload.checkLength(WHATS_APP_BUTTONS_ID_MAX_LENGTH),
                                            title = translate(it.title).toString().checkLength(WHATS_APP_BUTTONS_TITLE_MAX_LENGTH),
                                    )
                            )
                        }
                )
        )
)

fun I18nTranslator.urlButtonMessage(
        text: CharSequence,
        textButton: String,
        url: String
) : WhatsAppCloudBotInteractiveMessage = WhatsAppCloudBotInteractiveMessage(
        messagingProduct = "whatsapp",
        recipientType = WhatsAppCloudBotRecipientType.individual,
        interactive = WhatsAppCloudBotInteractive(
                type = WhatsAppCloudBotInteractiveType.cta_url,
                body = WhatsAppCloudBotBody(translate(text).toString()),
                action = WhatsAppCloudBotAction(
                        name = "cta_url",
                        parameters = ParametersUrl(
                                displayText = textButton,
                                url = url
                        )
                )
        )
)

fun I18nTranslator.listMessage(
        text: CharSequence,
        button: CharSequence,
        vararg replies: QuickReply
) : WhatsAppCloudBotInteractiveMessage =
        listMessage(text, button, replies.toList())

fun I18nTranslator.listMessage(
        text: CharSequence,
        button: CharSequence,
        replies: List<QuickReply>
) : WhatsAppCloudBotInteractiveMessage =
        completeListMessage(
                text, button, WhatsAppCloudBotActionSection(rows = replies.map {
            WhatsAppBotRow(
                    id = it.payload,
                    title = it.title,
                    description = it.subTitle
            )
        })
        )

fun I18nTranslator.completeListMessage(
        text: CharSequence,
        button: CharSequence,
        vararg sections: WhatsAppCloudBotActionSection
) : WhatsAppCloudBotInteractiveMessage = completeListMessage(text, button, sections.toList())

fun I18nTranslator.completeListMessage(
        text: CharSequence,
        button: CharSequence,
        sections: List<WhatsAppCloudBotActionSection>,
) : WhatsAppCloudBotInteractiveMessage = WhatsAppCloudBotInteractiveMessage(
        messagingProduct = "whatsapp",
        recipientType = WhatsAppCloudBotRecipientType.individual,
        interactive =  WhatsAppCloudBotInteractive(
                type = WhatsAppCloudBotInteractiveType.list,
                body = WhatsAppCloudBotBody(translate(text).toString()),
                action = WhatsAppCloudBotAction(
                        button = translate(button).toString().checkLength(WHATS_APP_BUTTONS_TITLE_MAX_LENGTH),
                        sections = sections.map {
                            WhatsAppCloudBotActionSection(
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



/*fun I18nTranslator.replyLocationMessage(
        text: CharSequence
) : WhatsAppCloudBotInteractiveMessage = replyLocationMessage(text)*/

fun I18nTranslator.replyLocationMessage(
        text: CharSequence
) : WhatsAppCloudBotInteractiveMessage = WhatsAppCloudBotInteractiveMessage(
        messagingProduct = "whatsapp",
        recipientType = WhatsAppCloudBotRecipientType.individual,
        interactive = WhatsAppCloudBotInteractive(
                type = WhatsAppCloudBotInteractiveType.location_request_message,
                body = WhatsAppCloudBotBody(translate(text).toString()),
                action = WhatsAppCloudBotAction(
                        name = "send_location"
                )
        )
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


fun I18nTranslator.buildTemplateMessageCarousel(
        templateName: String,
        components : List<Component.Card>,
        languageCode: String
): WhatsAppCloudBotTemplateMessage = WhatsAppCloudBotTemplateMessage(
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

fun I18nTranslator.buildTemplateMessage(
        templateName: String,
        languageCode: String,
        components: List<Component>
):  WhatsAppCloudBotTemplateMessage = WhatsAppCloudBotTemplateMessage(
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

fun <T : Bus<T>> T.phoneButton(
        textButton: String,
        number: String
): WhatsAppCloudBotTemplateMessage =  buildTemplateMessage(
               templateName = "text_button_phonnumber",
                languageCode = "fr",
                listOf(
                        bodyTemplate(listOf(TextParameter(
                                type = ParameterType.TEXT,
                                text = textButton))),
                        buttonTemplate("0", ButtonSubType.URL.name,
                                listOf(PayloadParameter(
                                        type = ParameterType.TEXT,
                                        payload = "toto",number)
                                ))
                )
        )



fun <T : Bus<T>> T.cardCarousel(
        cardIndex: Int,
        title: String,
        subTitle: String,
        detailButton: Component.Button,
        perturbationButton: Component.Button,
        imageHeader: Component.Header
): Component.Card = Component.Card(
        cardIndex = cardIndex,
        components = listOf(
                imageHeader,
                Component.Body(
                        type = ComponentType.BODY,
                        parameters = listOf(
                                TextParameter(
                                        type = ParameterType.TEXT,
                                        text = title
                                ),
                                TextParameter(
                                        type = ParameterType.TEXT,
                                        text = subTitle
                                )


                        )
                ),
                detailButton,
                perturbationButton
        )
)

fun <T : Bus<T>> T.bodyTemplate(
        parameters: List<TextParameter>
):Component.Body = Component.Body(
        type = ComponentType.BODY,
        parameters = parameters
)

fun <T : Bus<T>> T.TextParameterTemplate(
        textButton: String,
        typeParameter:String
):TextParameter = TextParameter(
        type = ParameterType.valueOf(typeParameter),
        text = textButton,
)

fun buttonTemplate(
        index: String,
        subType: String,
        parameters: List<PayloadParameter>
):Component.Button = Component.Button(
        type = ComponentType.BUTTON,
        subType = ButtonSubType.valueOf(subType),
        index = index,
        parameters = parameters
)

fun <T : Bus<T>> T.urlButtonTemplate(
        textButton: String,
        languageCode: String,
):WhatsAppCloudBotTemplateMessage = WhatsAppCloudBotTemplateMessage(
        messagingProduct = "whatsapp",
        recipientType = WhatsAppCloudBotRecipientType.individual,
        template = WhatsAppCloudBotTemplate(
                name = "url_button_template",
                language = Language(
                        code = languageCode,
                ),
                components = listOf(
                        buttonTemplate("0", "URL",
                                listOf(payloadParameterTemplate(textButton, "TEXT")))
                )
        )
)

fun payloadParameterTemplate(
        textButton: String,
        typeParameter:String
):PayloadParameter = PayloadParameter(
        type = ParameterType.valueOf(typeParameter),
        payload = "payload",
        text = textButton,
)



fun headerTemplate(
        typeParameter:String,
        imageId: String
):Component.Header = Component.Header(
    type = ComponentType.HEADER,
    parameters = listOf(HeaderParameter.Image(
            type = ParameterType.valueOf(typeParameter),
            image = ImageId(
                        id = imageId
                    )
            )
    )
)


fun <T : Bus<T>> T.buildTemplate(
        nameTemplate: String,
        category: String,
        components: List<TemplateComponent>
): WhatsAppCloudTemplate {
    return WhatsAppCloudTemplate(
        name = nameTemplate,
        language = LanguageCode.fr,
        category = category,
        components = components
    )
}

private fun String.checkLength(maxLength: Int) : String {
//    if (maxLength > 0 && this.length > maxLength) {
//        error("text $this should not exceed $maxLength chars.")
//    }
    return this
}



