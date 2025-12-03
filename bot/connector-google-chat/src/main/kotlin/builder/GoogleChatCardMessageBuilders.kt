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

package ai.tock.bot.connector.googlechat.builder

import ai.tock.bot.connector.googlechat.GoogleChatConnectorCardMessageOut
import ai.tock.bot.connector.googlechat.builder.ChatImageStyle.IMAGE
import ai.tock.bot.definition.Intent
import ai.tock.bot.definition.Parameters
import ai.tock.bot.engine.I18nTranslator
import com.google.api.services.chat.v1.model.ActionParameter
import com.google.api.services.chat.v1.model.Button
import com.google.api.services.chat.v1.model.Card
import com.google.api.services.chat.v1.model.CardHeader
import com.google.api.services.chat.v1.model.FormAction
import com.google.api.services.chat.v1.model.Image
import com.google.api.services.chat.v1.model.ImageButton
import com.google.api.services.chat.v1.model.KeyValue
import com.google.api.services.chat.v1.model.Message
import com.google.api.services.chat.v1.model.OnClick
import com.google.api.services.chat.v1.model.OpenLink
import com.google.api.services.chat.v1.model.Section
import com.google.api.services.chat.v1.model.TextButton
import com.google.api.services.chat.v1.model.TextParagraph
import com.google.api.services.chat.v1.model.WidgetMarkup

internal const val GOOGLE_CHAT_ACTION_SEND_SENTENCE = "SEND_SENTENCE"
internal const val GOOGLE_CHAT_ACTION_SEND_CHOICE = "SEND_CHOICE"
internal const val GOOGLE_CHAT_ACTION_TEXT_PARAMETER = "TEXT"
internal const val GOOGLE_CHAT_ACTION_INTENT_PARAMETER = "INTENT"

@DslMarker
annotation class CardElementMarker

@CardElementMarker
abstract class ChatCardElement(
    val i18nTranslator: I18nTranslator,
) : I18nTranslator by i18nTranslator {
    val children: MutableList<ChatCardElement> = arrayListOf()

    protected fun <T : ChatCardElement> initElement(
        element: T,
        init: T.() -> Unit,
    ): T {
        element.init()
        children.add(element)
        return element
    }

    fun hasChatButtons(): Boolean = children.any { it is ChatButtons }
}

fun I18nTranslator.card(init: ChatCard.() -> Unit): GoogleChatConnectorCardMessageOut {
    val card = ChatCard(this)
    card.init()
    return GoogleChatConnectorCardMessageOut(card)
}

class ChatCard(
    i18nTranslator: I18nTranslator,
) : ChatCardElement(i18nTranslator) {
    fun header(
        title: CharSequence,
        subtitle: CharSequence? = null,
        imageUrl: String? = null,
        imageStyle: ChatImageStyle = IMAGE,
    ) = initElement(
        ChatHeader(
            translate(title),
            subtitle?.let { translate(it) },
            imageUrl,
            imageStyle,
            i18nTranslator,
        ),
        {},
    )

    fun section(
        header: CharSequence? = null,
        init: ChatSection.() -> Unit,
    ) = initElement(
        ChatSection(
            header?.let { translate(it) },
            i18nTranslator,
        ),
        init,
    )
}

enum class ChatImageStyle {
    IMAGE,
    AVATAR,
}

class ChatHeader(
    val title: CharSequence,
    val subtitle: CharSequence?,
    val imageUrl: String?,
    val imageStyle: ChatImageStyle,
    i18nTranslator: I18nTranslator,
) : ChatCardElement(i18nTranslator)

class ChatSection(
    val header: CharSequence?,
    i18nTranslator: I18nTranslator,
) : ChatCardElement(i18nTranslator) {
    fun textParagraph(text: CharSequence) = initElement(ChatTextParagraph(translate(text), i18nTranslator), { })

    fun keyValue(
        topLabel: CharSequence? = null,
        content: CharSequence,
        bottomLabel: CharSequence? = null,
        action: (ChatButton.() -> Unit)? = null,
        contentMultiline: Boolean = false,
        iconUrl: String? = null,
        icon: ChatIcon? = null,
    ) = initElement(
        ChatKeyValue(
            topLabel?.let { translate(it) },
            translate(content),
            bottomLabel?.let { translate(it) },
            action?.let {
                val button = ChatButton.ChatTextButton("", i18nTranslator)
                button.action()
                button.buttonAction
            },
            contentMultiline,
            iconUrl,
            icon,
            i18nTranslator,
        ),
        { },
    )

    fun image(
        imageUrl: String,
        init: ChatButton.ChatIconExternalButton.() -> Unit = {},
    ) = initElement(
        ChatImage(
            initElement(ChatButton.ChatIconExternalButton(imageUrl, i18nTranslator), init),
            i18nTranslator,
        ),
        {},
    )

    fun buttons(init: ChatButtons.() -> Unit) = initElement(ChatButtons(i18nTranslator), init)
}

sealed class ChatWidget(
    i18nTranslator: I18nTranslator,
) : ChatCardElement(i18nTranslator)

class ChatTextParagraph(
    val text: CharSequence,
    i18nTranslator: I18nTranslator,
) : ChatWidget(i18nTranslator)

class ChatKeyValue(
    val topLabel: CharSequence?,
    val content: CharSequence,
    val bottomLabel: CharSequence?,
    val action: ChatButtonAction?,
    val contentMultiline: Boolean,
    val iconUrl: String?,
    val icon: ChatIcon?,
    i18nTranslator: I18nTranslator,
) : ChatButtons(i18nTranslator)

class ChatImage(
    val imageButton: ChatButton.ChatIconExternalButton,
    i18nTranslator: I18nTranslator,
) : ChatWidget(i18nTranslator)

open class ChatButtons(
    i18nTranslator: I18nTranslator,
) : ChatWidget(i18nTranslator) {
    fun textButton(
        text: CharSequence,
        init: ChatButton.() -> Unit,
    ) = initElement(
        ChatButton.ChatTextButton(
            translate(text),
            i18nTranslator,
        ),
        init,
    )

    fun nlpTextButton(text: CharSequence) =
        textButton(text) {
            nlpAction(text)
        }

    fun iconButton(
        iconUrl: String,
        init: ChatButton.() -> Unit,
    ) = initElement(ChatButton.ChatIconExternalButton(iconUrl, i18nTranslator), init)

    fun iconButton(
        icon: ChatIcon,
        init: ChatButton.() -> Unit,
    ) = initElement(ChatButton.ChatIconEmbeddedButton(icon, i18nTranslator), init)
}

enum class ChatIcon {
    AIRPLANE,
    BOOKMARK,
    BUS,
    CAR,
    CLOCK,
    CONFIRMATION_NUMBER_ICON,
    DESCRIPTION,
    DOLLAR,
    EMAIL,
    EVENT_SEAT,
    FLIGHT_ARRIVAL,
    FLIGHT_DEPARTURE,
    HOTEL,
    HOTEL_ROOM_TYPE,
    INVITE,
    MAP_PIN,
    MEMBERSHIP,
    MULTIPLE_PEOPLE,
    PERSON,
    PHONE,
    RESTAURANT_ICON,
    SHOPPING_CART,
    STAR,
    STORE,
    TICKET,
    TRAIN,
    VIDEO_CAMERA,
    VIDEO_PLAY,
}

sealed class ChatButton(
    i18nTranslator: I18nTranslator,
) : ChatCardElement(i18nTranslator) {
    lateinit var buttonAction: ChatButtonAction

    fun link(linkUrl: String) {
        buttonAction = ChatButtonAction.ChatLink(linkUrl)
    }

    fun action(
        action: String,
        parameters: Map<String, String> = emptyMap(),
    ) {
        buttonAction = ChatButtonAction.ChatAction(action, parameters)
    }

    fun nlpAction(text: CharSequence) = action(GOOGLE_CHAT_ACTION_SEND_SENTENCE, mapOf(GOOGLE_CHAT_ACTION_TEXT_PARAMETER to text.toString()))

    fun choiceAction(
        intent: Intent,
        parameters: Parameters = Parameters.EMPTY,
    ) = action(
        GOOGLE_CHAT_ACTION_SEND_CHOICE,
        mapOf(GOOGLE_CHAT_ACTION_INTENT_PARAMETER to intent.name) + parameters.toMap(),
    )

    class ChatTextButton(
        val text: CharSequence,
        i18nTranslator: I18nTranslator,
    ) : ChatButton(i18nTranslator)

    class ChatIconExternalButton(
        val iconUrl: String,
        i18nTranslator: I18nTranslator,
    ) : ChatButton(i18nTranslator)

    class ChatIconEmbeddedButton(
        val icon: ChatIcon,
        i18nTranslator: I18nTranslator,
    ) : ChatButton(i18nTranslator)
}

sealed class ChatButtonAction {
    class ChatLink(
        val link: String,
    ) : ChatButtonAction()

    class ChatAction(
        val action: String,
        val parameters: Map<String, String>,
    ) : ChatButtonAction()
}

fun ChatCard.toCardMessage(): Message =
    Message().setCards(
        mutableListOf(
            Card()
                .setHeader(children.mapNotNull { it as? ChatHeader }.firstOrNull()?.toCardHeader())
                .setSections(children.mapNotNull { it as? ChatSection }.map { it.toSection() }),
        ),
    )

private fun ChatHeader.toCardHeader() =
    CardHeader()
        .setTitle(title.toString())
        .setSubtitle(subtitle?.toString())
        .setImageUrl(imageUrl)
        .setImageStyle(imageStyle.name)

private fun ChatSection.toSection(): Section =
    Section()
        .setHeader(header?.toString())
        .setWidgets(children.mapNotNull { it as? ChatWidget }.map { it.toWidget() })

private fun ChatWidget.toWidget(): WidgetMarkup =
    when (this) {
        is ChatTextParagraph -> {
            WidgetMarkup().setTextParagraph(TextParagraph().setText(text.toString()))
        }

        is ChatImage -> {
            WidgetMarkup().setImage(
                Image().setImageUrl(imageButton.iconUrl).setOnClick(imageButton.buttonAction.toOnClick()),
            )
        }

        is ChatKeyValue -> {
            WidgetMarkup().setKeyValue(
                KeyValue()
                    .setTopLabel(topLabel?.toString())
                    .setContent(content.toString())
                    .setBottomLabel(bottomLabel?.toString())
                    .setOnClick(action?.toOnClick())
                    .setContentMultiline(contentMultiline)
                    .setIconUrl(iconUrl)
                    .setIcon(icon?.name)
                    .setButton(children.mapNotNull { it as? ChatButton }.lastOrNull()?.toButton()),
            )
        }

        is ChatButtons -> {
            WidgetMarkup().setButtons(children.mapNotNull { it as? ChatButton }.map { it.toButton() })
        }
    }

private fun ChatButton.toButton(): Button =
    when (this) {
        is ChatButton.ChatTextButton -> {
            Button().setTextButton(
                TextButton().setText(text.toString()).setOnClick(buttonAction.toOnClick()),
            )
        }

        is ChatButton.ChatIconExternalButton -> {
            Button().setImageButton(
                ImageButton().setIconUrl(iconUrl).setOnClick(buttonAction.toOnClick()),
            )
        }

        is ChatButton.ChatIconEmbeddedButton -> {
            Button().setImageButton(
                ImageButton().setIcon(icon.name).setOnClick(buttonAction.toOnClick()),
            )
        }
    }

private fun ChatButtonAction.toOnClick(): OnClick =
    when (this) {
        is ChatButtonAction.ChatLink -> {
            OnClick().setOpenLink(OpenLink().setUrl(link))
        }

        is ChatButtonAction.ChatAction -> {
            OnClick().setAction(
                FormAction()
                    .setActionMethodName(action)
                    .setParameters(parameters.map { ActionParameter().setKey(it.key).setValue(it.value) }),
            )
        }
    }
