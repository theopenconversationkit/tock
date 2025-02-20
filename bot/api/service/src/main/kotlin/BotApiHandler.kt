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

package ai.tock.bot.api.service

import ai.tock.bot.admin.bot.BotConfiguration
import ai.tock.bot.admin.story.StoryDefinitionConfigurationDAO
import ai.tock.bot.api.model.BotResponse
import ai.tock.bot.api.model.UserRequest
import ai.tock.bot.api.model.configuration.ClientConfiguration
import ai.tock.bot.api.model.message.bot.BotMessage
import ai.tock.bot.api.model.message.bot.Card
import ai.tock.bot.api.model.message.bot.Carousel
import ai.tock.bot.api.model.message.bot.CustomMessage
import ai.tock.bot.api.model.message.bot.Debug
import ai.tock.bot.api.model.message.bot.I18nText
import ai.tock.bot.api.model.message.bot.Sentence
import ai.tock.bot.connector.media.MediaAction
import ai.tock.bot.connector.media.MediaCard
import ai.tock.bot.connector.media.MediaCarousel
import ai.tock.bot.connector.media.MediaFile
import ai.tock.bot.definition.StoryDefinition
import ai.tock.bot.engine.BotBus
import ai.tock.bot.engine.action.Action
import ai.tock.bot.engine.action.SendAttachment.AttachmentType
import ai.tock.bot.engine.action.SendDebug
import ai.tock.bot.engine.action.SendSentence
import ai.tock.bot.engine.config.UploadedFilesService
import ai.tock.bot.engine.message.ActionWrappedMessage
import ai.tock.bot.engine.message.MessagesList
import ai.tock.bot.engine.user.UserTimelineDAO
import ai.tock.nlp.api.client.model.Entity
import ai.tock.nlp.api.client.model.EntityType
import ai.tock.shared.injector
import ai.tock.translator.I18nContext
import ai.tock.translator.TranslatedSequence
import ai.tock.translator.Translator
import ai.tock.translator.raw
import com.github.salomonbrys.kodein.instance

internal class BotApiHandler(
    provider: BotApiDefinitionProvider,
    configuration: BotConfiguration,
    private val clientController: BotApiClientController = BotApiClientController(provider, configuration),
) {

    companion object {
        private const val VIEWED_STORIES_BUS_KEY = "_viewed_stories_tock_switch"
    }

    private val storyDAO: StoryDefinitionConfigurationDAO by injector.instance()
    private val userTimelineDAO: UserTimelineDAO by injector.instance()

    fun configuration(): ClientConfiguration? = clientController.configuration()

    fun send(bus: BotBus) {
        val request = bus.toUserRequest()
        clientController.send(request) { response ->
            bus.handleResponse(request, response?.botResponse)
        }
    }

    private fun BotBus.handleResponse(request: UserRequest, response: BotResponse?) {
        if (response != null) {
            //Check if there is a configuration for Ending story
            val storySetting = storyDAO.getStoryDefinitionByNamespaceAndBotIdAndStoryId(
                botDefinition.namespace,
                botDefinition.botId,
                story.definition.id
            )
            val endingStoryId = storySetting?.findEnabledEndWithStoryId(connectorId)
                .takeIf { response.context.lastResponse }

            val messages = response.messages
            if (messages.isEmpty()) {
                error("no response for $request")
            }
            messages.subList(0, messages.size - 1)
                .forEach { a ->
                    send(a)
                }
            messages.last().apply {
                send(this, response.context.lastResponse && endingStoryId == null)
            }
            // handle entity changes
            entities
                .entries
                // new collection
                .toList()
                .forEach { (role, entity) ->
                    val result = response.entities.find { it.role == role }
                    val value = entity.value
                    // remove not present
                    if (result == null) {
                        removeEntityValue(role)
                    } else if (value != null) {

                        if (result.content != value.content) {
                            changeEntityText(value.entity, result.content)
                        }
                        if (result.value != value.value) {
                            changeEntityValue(value.entity, result.value)
                        }
                    }
                }
            // handle entity add
            response.entities.forEach {
                if (entityValueDetails(it.role) == null) {
                    val entity = Entity(EntityType(it.type), it.role)
                    changeEntityText(entity, it.content)
                    changeEntityValue(entity, it.value)
                }
            }

            // switch story if new story
            if (response.storyId != request.storyId) {
                botDefinition.findStoryDefinitionById(response.storyId, request.context.applicationId)
                    .also {
                        switchStory(it)
                    }
            }
            // set step
            if (response.step != null) {
                step = story.definition.allSteps().find { it.name == response.step }
            }

            //Handle current story and switch to ending story
            if (endingStoryId != null) {

                // before switching story (Only for an ending rule), we need to save a snapshot with the current intent
                if (connectorData.saveTimeline) {
                    userTimelineDAO.save(userTimeline, botDefinition, asynchronousProcess = false)
                }

                val targetStory = botDefinition.findStoryDefinitionById(endingStoryId, connectorId)
                switchEndingStory(targetStory)
            }
        }
    }

    private fun BotBus.switchEndingStory(target: StoryDefinition) {
        step = step?.takeUnless { story.definition == target }
        setBusContextValue(VIEWED_STORIES_BUS_KEY, viewedStories + target)
        handleAndSwitchStory(target)
    }

    private val BotBus.viewedStories: Set<StoryDefinition>
        get() =
            getBusContextValue<Set<StoryDefinition>>(VIEWED_STORIES_BUS_KEY) ?: emptySet()

    private fun BotBus.send(message: BotMessage, end: Boolean = false) {
        val actions =
            when (message) {
                is Sentence -> listOf(toAction(message))
                is Card -> toActions(message)
                is CustomMessage -> listOf(toAction(message))
                is Carousel -> toActions(message)
                is Debug -> listOf(toAction(message))
                else -> error("unsupported message $message")
            }

        if (actions.isEmpty()) {
            error("no message found in $message")
        }
        val messagesList = MessagesList(actions.map { ActionWrappedMessage(it, 0) })
        val delay = botDefinition.defaultDelay(currentAnswerIndex)
        if (end) {
            end(messagesList, delay)
        } else {
            send(messagesList, delay)
        }
    }

    private fun BotBus.toAction(message: CustomMessage): Action {
        return SendSentence(
            botId,
            connectorId,
            userId,
            null,
            listOfNotNull(message.message.value).toMutableList()
        )
    }

    private fun BotBus.toAction(sentence: Sentence): Action {
        val text = translateText(sentence.text)
        if (sentence.suggestions.isNotEmpty() && text != null) {
            val message =
                underlyingConnector.addSuggestions(text, sentence.suggestions.mapNotNull { translateText(it.title) })
                    .invoke(this)
            if (message != null) {
                return SendSentence(
                    botId,
                    connectorId,
                    userId,
                    null,
                    mutableListOf(message)
                )
            }
        }
        return SendSentence(
            botId,
            connectorId,
            userId,
            text
        )
    }

    private fun BotBus.toAction(data: Debug): Action {
        return SendDebug(
            botId,
            connectorId,
            userId,
            data.text,
            data.data
        )
    }

    private fun BotBus.toActions(card: Card): List<Action> {
        val connectorMessages =
            toMediaCard(card)
                .takeIf { it.checkValidity() }
                ?.let {
                    underlyingConnector.toConnectorMessage(it).invoke(this)
                }

        return connectorMessages?.map {
            SendSentence(
                botId,
                connectorId,
                userId,
                null,
                mutableListOf(it)
            )
        } ?: emptyList()
    }

    private fun BotBus.toActions(carousel: Carousel): List<Action> {
        val connectorMessages =
            MediaCarousel(carousel.cards.map { toMediaCard(it) })
                .takeIf { it.checkValidity() }
                ?.let {
                    underlyingConnector.toConnectorMessage(it).invoke(this)
                }

        return connectorMessages?.map {
            SendSentence(
                botId,
                connectorId,
                userId,
                null,
                mutableListOf(it)
            )
        } ?: emptyList()
    }

    private fun BotBus.toMediaCard(card: Card): MediaCard =
        MediaCard(
            translateText(card.title),
            translateText(card.subTitle),
            card.attachment?.let {
                MediaFile(
                    it.url,
                    it.url,
                    it.type?.let { AttachmentType.valueOf(it.name) } ?: UploadedFilesService.attachmentType(it.url),
                    translateText(it.description)
                )
            },
            card.actions.map {
                MediaAction(
                    translateText(it.title) ?: "",
                    it.url
                )
            }
        )
}

private fun BotBus.translateText(i18n: I18nText?): TranslatedSequence? =
    when {
        i18n == null -> null
        i18n.toBeTranslated -> translate(i18n.text, i18n.args)
        else -> Translator.formatMessage(
            i18n.text,
            I18nContext(
                userLocale,
                userInterfaceType,
                targetConnectorType.id,
                contextId
            ),
            i18n.args
        ).raw
    }
