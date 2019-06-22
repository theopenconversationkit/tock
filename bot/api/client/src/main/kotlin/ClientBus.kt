/*
 * Copyright (C) 2017/2019 VSCT
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
package fr.vsct.tock.bot.api.client

import fr.vsct.tock.bot.api.model.context.Entity
import fr.vsct.tock.bot.api.model.message.bot.Action
import fr.vsct.tock.bot.api.model.message.bot.Attachment
import fr.vsct.tock.bot.api.model.message.bot.AttachmentType
import fr.vsct.tock.bot.api.model.message.bot.Card
import fr.vsct.tock.bot.api.model.message.bot.I18nText
import fr.vsct.tock.bot.api.model.message.user.UserMessage
import fr.vsct.tock.bot.engine.Bus
import fr.vsct.tock.translator.I18nLabelValue
import fr.vsct.tock.translator.RawString
import fr.vsct.tock.translator.TranslatedString

interface ClientBus : Bus<ClientBus> {

    val botDefinition: ClientBotDefinition

    val entities: List<Entity>

    val message: UserMessage

    fun handle() {
        val story = botDefinition.stories.find { intent?.wrap(it.mainIntent) == true }
            ?: botDefinition.unknownStory
        story.handler.handle(this)
    }

    fun send(card: Card): ClientBus

    fun end(card: Card): ClientBus

    override fun translate(text: CharSequence?, vararg args: Any?): I18nText {
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
        delay: Long = defaultDelay(currentAnswerIndex)): Card =
        Card(
            title?.let { translate(it) },
            subTitle?.let { translate(it) },
            attachment,
            actions,
            delay
        )

    /**
     * Creates a new [Card].
     */
    fun newCard(
        title: CharSequence? = null,
        subTitle: CharSequence? = null,
        attachment: Attachment? = null,
        vararg actions: Action,
        delay: Long = defaultDelay(currentAnswerIndex)): Card = newCard(title, subTitle, attachment, actions.toList(), delay)

    /**
     * Creates a new [Action].
     */
    fun newAction(
        title: CharSequence,
        url: String? = null
    ): Action = Action(translate(title), url)

    /**
     * Creates a new [Attachment].
     */
    fun newAttachment(
        url: String,
        type: AttachmentType? = null
    ): Attachment = Attachment(url, type)
}