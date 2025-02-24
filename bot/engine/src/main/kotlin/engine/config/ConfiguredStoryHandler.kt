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

package ai.tock.bot.engine.config

import ai.tock.bot.admin.answer.AnswerConfigurationType.simple
import ai.tock.bot.admin.answer.BuiltInAnswerConfiguration
import ai.tock.bot.admin.answer.ScriptAnswerConfiguration
import ai.tock.bot.admin.answer.SimpleAnswer
import ai.tock.bot.admin.answer.SimpleAnswerConfiguration
import ai.tock.bot.admin.bot.BotApplicationConfigurationKey
import ai.tock.bot.admin.indicators.metric.MetricType
import ai.tock.bot.admin.story.StoryDefinitionAnswersContainer
import ai.tock.bot.admin.story.StoryDefinitionConfiguration
import ai.tock.bot.admin.story.StoryDefinitionConfigurationStep
import ai.tock.bot.admin.story.StoryDefinitionConfigurationStep.Step
import ai.tock.bot.admin.story.StoryDefinitionStepMetric
import ai.tock.bot.connector.ConnectorFeature.CAROUSEL
import ai.tock.bot.connector.media.MediaCardDescriptor
import ai.tock.bot.connector.media.MediaCarouselDescriptor
import ai.tock.bot.definition.Intent
import ai.tock.bot.definition.StoryDefinition
import ai.tock.bot.definition.StoryHandler
import ai.tock.bot.definition.StoryHandlerBase.Companion.isEndCalled
import ai.tock.bot.definition.StoryTag
import ai.tock.bot.engine.BotBus
import ai.tock.bot.engine.BotRepository
import ai.tock.bot.engine.action.SendSentence
import ai.tock.bot.engine.action.SendSentenceWithFootnotes
import ai.tock.bot.engine.dialog.NextUserActionState
import ai.tock.bot.engine.message.ActionWrappedMessage
import ai.tock.bot.engine.message.MessagesList
import ai.tock.bot.engine.user.UserTimelineDAO
import ai.tock.nlp.api.client.model.Entity
import ai.tock.nlp.api.client.model.NlpIntentQualifier
import ai.tock.shared.injector
import com.github.salomonbrys.kodein.instance
import mu.KotlinLogging

/**
 *
 */
internal class ConfiguredStoryHandler(
        private val definition: BotDefinitionWrapper,
        private val configuration: StoryDefinitionConfiguration,
        private val configurationStoryHandler: BotConfigurationStoryHandler? = null,
) : StoryHandler {

    companion object {
        private val logger = KotlinLogging.logger {}
        private const val VIEWED_STORIES_BUS_KEY = "_viewed_stories_tock_switch"
    }

    private val userTimelineDAO: UserTimelineDAO by injector.instance()

    override fun handle(bus: BotBus) {
        configurationStoryHandler?.handle(bus)
        if (isEndCalled(bus)) {
            return
        }

        configuration.mandatoryEntities.forEach { entity ->
            // fallback from "generic" entity if the role is not present
            val role = entity.role
            val entityTypeName = entity.entityTypeName
            if (role != entityTypeName &&
                    bus.entityValueDetails(role) == null &&
                    bus.hasActionEntity(entityTypeName)
            ) {
                bus.dialog.state.changeValue(
                        role,
                        bus.entityValueDetails(entityTypeName)
                                ?.let { v ->
                                    v.copy(entity = Entity(v.entity.entityType, role))
                                }
                )
                bus.removeEntityValue(entityTypeName)
            }

            if (bus.entityValueDetails(role) == null && entity.hasCurrentAnswer()) {
                // if the role is generic and there is another role in the entity list: skip
                if (role != entityTypeName || bus.entities.none { entity.entityType == it.value.value?.entity?.entityType?.name }) {
                    // else send entity question
                    entity.send(bus)
                    switchStoryIfEnding(null, bus)
                    return@handle
                }
            }
        }

        // Manage metrics
        manageMetrics(bus)

        // Manage step
        val busStep = bus.step as? Step
        busStep?.configuration
            ?.also { step ->

                if (step.hasCurrentAnswer()) {
                    step.send(bus)
                }
                val targetIntent = step.targetIntent?.name
                    ?: (bus.intent.takeIf { !step.hasCurrentAnswer() }?.wrappedIntent()?.name)
                bus.botDefinition
                    .takeIf { targetIntent != null }
                    ?.findStoryDefinition(targetIntent, bus.connectorId)
                    ?.takeUnless { it == bus.botDefinition.unknownStory }
                    ?.takeUnless { bus.viewedStories.contains(it) }
                    ?.apply {
                        bus.switchConfiguredStory(this, targetIntent ?: error("targetIntent is null??"))
                        return@handle
                    }
                if (step.hasCurrentAnswer()) {
                    switchStoryIfEnding(step, bus)
                    return@handle
                }
            }

        val configurationName = BotRepository.getConfigurationByApplicationId(BotApplicationConfigurationKey(bus))?.name
        val answerContainer =
                configurationName?.let { name -> configuration.configuredAnswers.firstOrNull { it.botConfiguration == name } }
                        ?: configuration
        removeAskAgainProcess(bus)
        answerContainer.send(bus)

        switchStoryIfEnding(null, bus)

        // Restrict next intents if defined in story settings:

        if (configuration.nextIntentsQualifiers.isNotEmpty()) {
            val nextIntentsQualifiers: MutableList<NlpIntentQualifier> = configuration.nextIntentsQualifiers.toMutableList()

            // Story steps (choices) intents are always allowed:
            configuration.steps.forEach { step ->
                val intentName: String? = step.intent?.name
                if (intentName != null) {
                    nextIntentsQualifiers.add(NlpIntentQualifier(intentName, .5))
                }
                val targetIntentName: String? = step.targetIntent?.name
                if (targetIntentName != null) {
                    nextIntentsQualifiers.add(NlpIntentQualifier(targetIntentName, .5))
                }
            }

            bus.dialog.state.nextActionState = NextUserActionState(nextIntentsQualifiers.distinctBy { it.intent }.toList())
            logger.debug { "bus.dialog.state.nextActionState  : $bus.dialog.state.nextActionState " }
            logger.debug { "NextIntentsQualifiers : ${bus.dialog.state.nextActionState} " }
        }

    }

    /**
     * Manage story and step metrics :
     * If a story is handled, save a [MetricType.STORY_HANDLED] metric
     * If a story has steps with metrics, then save [MetricType.QUESTION_ASKED] metrics for indicators
     * If a step is handled, save all its metrics as [MetricType.QUESTION_REPLIED]
     * If a step has a children with metrics, then save them as [MetricType.QUESTION_ASKED]
     */
    private fun manageMetrics(bus: BotBus) {
        val busStep = bus.step as? Step

        if(busStep == null) {
            // Save story handled metric if bot handle story and not a step
            configuration.saveMetric(
                bus.createMetric(MetricType.STORY_HANDLED)
            )

            // if story has steps with metrics then save all metrics as QuestionAsked
            saveQuestionAskedMetrics(bus, configuration.steps.flatMap { it.metrics })

        } else {
            // Save step metric if bot handle story and not a step
            busStep.configuration.metrics
                .map { bus.createMetric(MetricType.QUESTION_REPLIED, it.indicatorName, it.indicatorValueName) }
                .also {
                    if(it.isNotEmpty())
                        configuration.saveMetrics(it)
                }

            // if step has children with metrics then save all metrics as QuestionAsked
            saveQuestionAskedMetrics(bus, busStep.children.flatMap { it.metrics })
        }
    }

    /**
     * Save distinct [MetricType.QUESTION_ASKED] metrics
     */
    private fun saveQuestionAskedMetrics(bus: BotBus, metrics: List<StoryDefinitionStepMetric>) {
        metrics.map { it.indicatorName }
            .distinct()
            .map { bus.createMetric(MetricType.QUESTION_ASKED, it) }
            .also {
                if(it.isNotEmpty())
                    configuration.saveMetrics(it)
            }
    }

    /**
     * Remove the ask again process to the last story if no more ask again round available
     */
    private fun removeAskAgainProcess(bus: BotBus) {
        if (bus.dialog.stories.lastOrNull()?.definition?.hasTag(StoryTag.ASK_AGAIN) == true && bus.dialog.state.hasCurrentAskAgainProcess && bus.dialog.state.askAgainRound == 0) {
            bus.dialog.state.hasCurrentAskAgainProcess = false
        }
    }

    private fun isMissingMandatoryEntities(bus: BotBus): Boolean {
        configuration.mandatoryEntities.forEach { entity ->
            val role = entity.role
            val entityTypeName = entity.entityTypeName
            if (bus.entityValueDetails(role) == null && entity.hasCurrentAnswer()) {
                // if the role is generic and there is an other role in the entity list: skip
                if (role != entityTypeName || bus.entities.none { entity.entityType == it.value.value?.entity?.entityType?.name }) {
                    return true
                }
            }
        }
        return false
    }

    private fun switchStoryIfEnding(
            step: StoryDefinitionConfigurationStep?,
            bus: BotBus
    ) {
        if (!isMissingMandatoryEntities(bus) && bus.story.definition.steps.isEmpty() || step?.hasNoChildren == true) {
            configuration.findEnabledEndWithStoryId(bus.connectorId)
                    ?.let { bus.botDefinition.findStoryDefinitionById(it, bus.connectorId) }
                    ?.let {
                        // before switching story (Only for an ending rule), we need to save a snapshot with the current intent
                        if (bus.connectorData.saveTimeline){
                            userTimelineDAO.save(bus.userTimeline, bus.botDefinition, asynchronousProcess = false)
                        }

                        bus.switchConfiguredStory(it, it.mainIntent().name)
                    }
        }
    }

    private val BotBus.viewedStories: Set<StoryDefinition>
        get() =
            getBusContextValue<Set<StoryDefinition>>(VIEWED_STORIES_BUS_KEY) ?: emptySet()

    private fun BotBus.switchConfiguredStory(target: StoryDefinition, newIntent: String) {
        step = step?.takeUnless { story.definition == target }
        setBusContextValue(VIEWED_STORIES_BUS_KEY, viewedStories + target)
        handleAndSwitchStory(target, Intent(newIntent))
    }

    private fun StoryDefinitionAnswersContainer.send(bus: BotBus) {
        findCurrentAnswer().apply {
            when (this) {
                null -> bus.fallbackAnswer()
                is SimpleAnswerConfiguration -> bus.handleSimpleAnswer(this@send, this)
                is ScriptAnswerConfiguration -> bus.handleScriptAnswer(this@send)
                is BuiltInAnswerConfiguration ->
                    (bus.botDefinition as BotDefinitionWrapper).builtInStory(configuration.storyId)
                        .storyHandler.handle(bus)
                else -> error("type not supported for now: $this")
            }
        }
    }

    override fun support(bus: BotBus): Double = 1.0

    private fun BotBus.fallbackAnswer() =
        botDefinition.unknownStory.storyHandler.handle(this)

    private fun BotBus.handleSimpleAnswer(
        container: StoryDefinitionAnswersContainer,
        simple: SimpleAnswerConfiguration?
    ) {
        if (simple == null) {
            fallbackAnswer()
        } else {
            val isMissingMandatoryEntities = isMissingMandatoryEntities(this)
            val steps = story.definition.steps.isNotEmpty()
            val answers = fillCarousel(simple)
            answers.takeUnless { it.isEmpty() }
                ?.let {
                    it.subList(0, it.size - 1)
                        .forEach { a ->
                            send(container, a)
                        }
                    it.last().apply {
                        val currentStep = (step as? Step)?.configuration
                        val endingStoryRule = configuration.findEnabledEndWithStoryId(connectorId) != null
                        send(
                            container, this,
                            isMissingMandatoryEntities ||
                                    // No steps and no ending story
                                    (!steps && !endingStoryRule) ||
                                    // Steps not started
                                    (steps && currentStep == null) ||
                                    // Steps started with children
                                    (currentStep?.hasNoChildren == false) ||
                                    // Steps started with no children, no target intent, no ending story
                                    (currentStep?.hasNoChildren == true && currentStep?.targetIntent == null && !endingStoryRule)
                        )
                    }
                }
        }
    }

    private fun BotBus.fillCarousel(simple: SimpleAnswerConfiguration): List<SimpleAnswer> {
        val transformedAnswers = mutableListOf<SimpleAnswer>()
        logger.debug { "Configured answers: ${simple.answers}" }
        simple.answers.takeUnless { it.isEmpty() }
            ?.run {
                forEach { a ->
                    if (
                        underlyingConnector.hasFeature(CAROUSEL, targetConnectorType) &&
                        a.mediaMessage?.checkValidity() == true &&
                        a.mediaMessage is MediaCardDescriptor &&
                        a.mediaMessage.fillCarousel
                    ) {
                        val previousAnswer = transformedAnswers.lastOrNull()
                        val previousAnswerMedia = previousAnswer?.mediaMessage
                        if (previousAnswerMedia?.checkValidity() == true) {
                            when (previousAnswerMedia) {
                                is MediaCarouselDescriptor -> { // Add card to previous carousel
                                    transformedAnswers.removeLast()
                                    transformedAnswers.add(
                                        previousAnswer.copy(
                                            mediaMessage = previousAnswerMedia.copy(
                                                cards = previousAnswerMedia.cards + a.mediaMessage
                                            )
                                        )
                                    )
                                }
                                is MediaCardDescriptor -> {
                                    // Merge current and previous card to a carousel
                                    transformedAnswers.removeLast()
                                    transformedAnswers.add(
                                        previousAnswer.copy(
                                            mediaMessage = MediaCarouselDescriptor(
                                                listOf(previousAnswerMedia, a.mediaMessage)
                                            )
                                        )
                                    )
                                }
                                else -> transformedAnswers.add(a)
                            }
                        } else transformedAnswers.add(a)
                    } else transformedAnswers.add(a)
                }
            }

        logger.debug { "Transformed answers: $transformedAnswers" }
        return transformedAnswers
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
                    if (end && suggestions.isNotEmpty() && messages.isNotEmpty()) {
                        messages.take(messages.size - 1) +
                                (
                                        underlyingConnector.addSuggestions(
                                            messages.last(),
                                            suggestions
                                        ).invoke(this)
                                            ?: messages.last()
                                        )
                    } else {
                        messages
                    }
                }
                ?: listOfNotNull(
                    suggestions.takeIf { suggestions.isNotEmpty() && end }
                        ?.let { underlyingConnector.addSuggestions(label, suggestions).invoke(this) }
                )

        val actions = connectorMessages
            .map {
                SendSentence(
                    botId,
                    connectorId,
                    userId,
                    null,
                    mutableListOf(it)
                )
            }
            .takeUnless { it.isEmpty() }
            ?: listOf(
                answer.footnotes?.takeIf{ it.isNotEmpty() }
                ?.let {
                    SendSentenceWithFootnotes(
                        playerId = botId,
                        applicationId = connectorId,
                        recipientId = userId,
                        text = label,
                        footnotes = it.toMutableList()
                    )
                } ?:
                    SendSentence(
                        botId,
                        connectorId,
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
        container.storyDefinition(definition, configuration)
            ?.storyHandler
            ?.handle(this)
            ?: run {
                logger.warn { "no story definition for configured script for $container - use unknown" }
                handleSimpleAnswer(container, container.findAnswer(simple) as? SimpleAnswerConfiguration)
            }
    }

    override fun equals(other: Any?): Boolean {
        return (other as? ConfiguredStoryHandler)?.configuration == configuration
    }

    override fun hashCode(): Int = configuration.hashCode()
}
