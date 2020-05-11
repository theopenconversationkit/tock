/*
 * Copyright (C) 2017/2020 e-voyageurs technologies
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

package ai.tock.bot.engine.config

import ai.tock.bot.admin.answer.AnswerConfigurationType.simple
import ai.tock.bot.admin.answer.ScriptAnswerConfiguration
import ai.tock.bot.admin.answer.SimpleAnswer
import ai.tock.bot.admin.answer.SimpleAnswerConfiguration
import ai.tock.bot.admin.story.StoryDefinitionAnswersContainer
import ai.tock.bot.admin.story.StoryDefinitionConfiguration
import ai.tock.bot.admin.story.StoryDefinitionConfigurationStep.Step
import ai.tock.bot.definition.StoryDefinition
import ai.tock.bot.definition.StoryHandler
import ai.tock.bot.engine.BotBus
import ai.tock.bot.engine.action.SendSentence
import ai.tock.bot.engine.message.ActionWrappedMessage
import ai.tock.bot.engine.message.MessagesList
import ai.tock.nlp.api.client.model.Entity
import mu.KotlinLogging

/**
 *
 */
internal class ConfiguredStoryHandler(private val configuration: StoryDefinitionConfiguration) : StoryHandler {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override fun handle(bus: BotBus) {
        configuration.mandatoryEntities.forEach { entity ->
            //fallback from "generic" entity if the role is not present
            val role = entity.role
            val entityTypeName = entity.entityTypeName
            if (role != entityTypeName
                && bus.entityValueDetails(role) == null
                && bus.hasActionEntity(entityTypeName)) {
                bus.dialog.state.changeValue(
                    role,
                    bus.entityValueDetails(entityTypeName)
                        ?.let { v ->
                            v.copy(entity = Entity(v.entity.entityType, role))
                        }
                )
                bus.removeEntityValue(entityTypeName)
            }

            if (bus.entityValueDetails(role) == null && entity.hasCurrentAnwser()) {
                //if the role is generic and there is an other role in the entity list: skip
                if (role != entityTypeName || bus.entities.none { entity.entityType == it.value.value?.entity?.entityType?.name }) {
                    //else send entity question
                    entity.send(bus)
                    return@handle
                }
            }
        }

        (bus.step as? Step)?.configuration
            ?.also { step ->
                step.send(bus)
                val targetIntent = step.targetIntent?.name
                    ?: (bus.intent.takeIf { !step.hasCurrentAnwser() }?.wrappedIntent()?.name)
                bus.botDefinition
                    .takeIf { targetIntent != null }
                    ?.findStoryDefinition(targetIntent, bus.applicationId)
                    ?.takeUnless { it == bus.botDefinition.unknownStory }
                    ?.apply {
                        bus.switchConfiguredStory(this)
                        return@handle
                    }
                if (step.hasCurrentAnwser()) {
                    return@handle
                }
            }

        configuration.send(bus)
    }

    private fun BotBus.switchConfiguredStory(target: StoryDefinition) {
        step = step?.takeUnless { story.definition == target }
        handleAndSwitchStory(target)
    }

    private fun StoryDefinitionAnswersContainer.send(bus: BotBus) {
        findCurrentAnswer().apply {
            when (this) {
                null -> bus.fallbackAnswer()
                is SimpleAnswerConfiguration -> bus.handleSimpleAnswer(this@send, this)
                is ScriptAnswerConfiguration -> bus.handleScriptAnswer(this@send)
                else -> bus.fallbackAnswer()
            }
        }
    }

    override fun support(bus: BotBus): Double = 1.0

    private fun BotBus.fallbackAnswer() =
        botDefinition.unknownStory.storyHandler.handle(this)

    private fun BotBus.handleSimpleAnswer(container: StoryDefinitionAnswersContainer, simple: SimpleAnswerConfiguration?) {
        if (simple == null) {
            fallbackAnswer()
        } else {
            simple.answers.takeUnless { it.isEmpty() }
                ?.let {
                    it.subList(0, it.size - 1)
                        .forEach { a ->
                            send(container, a)
                        }
                    it.last().apply {
                        send(container, this,
                            // Fixes #731 end only if no targetIntent will be called after answer
                            (step as? Step)?.configuration?.targetIntent == null)
                    }
                }
        }
    }

    private fun BotBus.send(container: StoryDefinitionAnswersContainer, answer: SimpleAnswer, end: Boolean = false) {
        val label = translate(answer.key)
        val suggestions = container.findNextSteps(this, configuration).map { this.translate(it) }
        val connectorMessages =
            answer.mediaMessage
                ?.takeIf { it.checkValidity() }
                ?.let {
                    underlyingConnector.toConnectorMessage(it.toMessage(this)).invoke(this)
                }
                ?.let { messages ->
                    if (suggestions.isNotEmpty() && messages.isNotEmpty())
                        messages.take(messages.size - 1) + (underlyingConnector.addSuggestions(messages.last(), suggestions).invoke(this)
                            ?: messages.last())
                    else messages
                }
                ?: listOfNotNull(suggestions.takeIf { suggestions.isNotEmpty() && end }?.let { underlyingConnector.addSuggestions(label, suggestions).invoke(this) })


        val actions = connectorMessages
            .map {
                SendSentence(
                    botId,
                    applicationId,
                    userId,
                    null,
                    mutableListOf(it)
                )
            }
            .takeUnless { it.isEmpty() }
            ?: listOf(
                SendSentence(
                    botId,
                    applicationId,
                    userId,
                    label
                )
            )

        val messagesList = MessagesList(actions.map { ActionWrappedMessage(it, 0) })
        val delay = if (answer.delay == -1L) botDefinition.defaultDelay(currentAnswerIndex) else answer.delay
        if (end) {
            end(messagesList, delay)
        } else {
            send(messagesList, delay)
        }
    }

    private fun BotBus.handleScriptAnswer(container: StoryDefinitionAnswersContainer) {
        container.storyDefinition(botDefinition.botId)
            ?.storyHandler
            ?.handle(this)
            ?: {
                logger.warn { "no story definition for configured script for $container - use unknown" }
                handleSimpleAnswer(container, container.findAnswer(simple) as? SimpleAnswerConfiguration)
            }.invoke()
    }
}