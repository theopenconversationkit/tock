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
package ai.tock.bot.api.client

import ai.tock.bot.api.model.context.Entity
import ai.tock.bot.api.model.message.bot.Action
import ai.tock.bot.api.model.message.bot.Attachment
import ai.tock.bot.api.model.message.bot.AttachmentType
import ai.tock.bot.api.model.message.bot.Card
import ai.tock.bot.api.model.message.bot.Carousel
import ai.tock.bot.api.model.message.bot.I18nText
import ai.tock.bot.api.model.message.bot.Suggestion
import ai.tock.bot.api.model.message.user.UserMessage
import ai.tock.bot.connector.ConnectorType
import ai.tock.bot.engine.Bus
import ai.tock.nlp.entity.Value
import ai.tock.translator.I18nLabelValue
import ai.tock.translator.RawString
import ai.tock.translator.TranslatedString

/**
 * Bus implementation for Tock Bot API mode.
 */
interface ClientBus : Bus<ClientBus> {
    /**
     * The bot definition.
     */
    val botDefinition: ClientBotDefinition

    /**
     * The entity list.
     */
    val entities: MutableList<Entity>

    /**
     * The user message.
     */
    val message: UserMessage

    /**
     * The current story.
     */
    var story: ClientStoryDefinition

    /**
     * The current step.
     */
    var step: ClientStep?

    /**
     * Handles the current request.
     */
    fun handle()

    override fun isCompatibleWith(connectorType: ConnectorType) = targetConnectorType == connectorType

    suspend fun enableStreaming()

    suspend fun disableStreaming()

    /**
     * Sends a [Card].
     */
    suspend fun send(card: Card): ClientBus

    /**
     * Sends a [Card] as last bot answer.
     */
    suspend fun end(card: Card): ClientBus

    /**
     * Sends a [Carousel].
     */
    suspend fun send(carousel: Carousel): ClientBus

    /**
     * Sends a [Carousel as last bot answer.
     */
    suspend fun end(carousel: Carousel): ClientBus

    /**
     * Sends a text with suggestions.
     */
    suspend fun send(
        i18nText: CharSequence,
        suggestions: List<Suggestion>,
        delay: Long = defaultDelay(currentAnswerIndex),
        vararg i18nArgs: Any?,
    ): ClientBus

    /**
     * Sends a text with suggestions.
     */
    suspend fun send(
        i18nText: CharSequence,
        suggestions: List<CharSequence>,
    ): ClientBus = send(i18nText, suggestions.map { Suggestion(translate(it)) })

    /**
     * Sends a text with suggestions as last bot answer.
     */
    suspend fun end(
        i18nText: CharSequence,
        suggestions: List<Suggestion>,
        delay: Long = defaultDelay(currentAnswerIndex),
        vararg i18nArgs: Any?,
    ): ClientBus

    /**
     * Sends a text with suggestions as last bot answer.
     */
    suspend fun end(
        i18nText: CharSequence,
        suggestions: List<CharSequence>,
    ): ClientBus = end(i18nText, suggestions.map { Suggestion(translate(it)) })

    /**
     * Finds the [Entity] from the specified entity role.
     */
    fun entity(role: String): Entity? = entities.find { it.role == role }

    /**
     * Returns the [Entity] text content from the specified entity role.
     */
    fun entityText(role: String): String? = entity(role)?.content

    /**
     * Returns the corresponding [Entity] [Value] from the specified entity role.
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : Value> entityValue(role: String): T? = entity(role)?.value as? T

    /**
     * Removes the entity of the specified role.
     */
    fun removeEntity(role: String): Boolean = entities.removeIf { it.role == role }

    /**
     * Remove the specified entity.
     */
    fun removeEntity(entity: Entity): Boolean = entities.remove(entity)

    override fun translate(
        text: CharSequence?,
        vararg args: Any?,
    ): I18nText {
        return if (text.isNullOrBlank()) {
            I18nText("", toBeTranslated = false)
        } else if (text is I18nLabelValue) {
            I18nText(text.defaultLabel.toString(), text.args.map { it?.toString() }, key = text.key)
        } else if (text is TranslatedString || text is RawString) {
            I18nText(text.toString(), toBeTranslated = false)
        } else {
            I18nText(text.toString(), args.map { it?.toString() })
        }
    }

    /**
     * Creates a new [Card].
     */
    fun newCard(
        title: CharSequence? = null,
        subTitle: CharSequence? = null,
        attachment: Attachment? = null,
        actions: List<Action> = emptyList(),
        delay: Long = defaultDelay(currentAnswerIndex),
    ): Card =
        Card(
            title?.let { translate(it) },
            subTitle?.let { translate(it) },
            attachment,
            actions,
            delay,
        )

    /**
     * Creates a new [Carousel].
     */
    fun newCarousel(
        cards: List<Card>,
        delay: Long = defaultDelay(currentAnswerIndex),
    ): Carousel = Carousel(cards, delay)

    /**
     * Creates a new [Carousel].
     */
    fun newCarousel(
        vararg cards: Card,
        delay: Long = defaultDelay(currentAnswerIndex),
    ): Carousel = Carousel(cards.toList(), delay)

    /**
     * Creates a new [Card].
     */
    fun newCard(
        title: CharSequence? = null,
        subTitle: CharSequence? = null,
        attachment: Attachment? = null,
        vararg actions: Action,
        delay: Long = defaultDelay(currentAnswerIndex),
    ): Card = newCard(title, subTitle, attachment, actions.toList(), delay)

    /**
     * Creates a new [Action].
     */
    fun newAction(
        title: CharSequence,
        url: String? = null,
    ): Action = Action(translate(title), url)

    /**
     * Creates a new [Attachment].
     */
    fun newAttachment(
        url: String,
        type: AttachmentType? = null,
    ): Attachment = Attachment(url, type)
}
