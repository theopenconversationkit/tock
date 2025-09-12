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

package ai.tock.bot.engine.dialog

import ai.tock.bot.definition.AsyncStoryHandler
import ai.tock.bot.definition.Intent
import ai.tock.bot.definition.StoryDefinition
import ai.tock.bot.definition.StoryHandler
import ai.tock.bot.definition.StoryStepDef
import ai.tock.bot.definition.StoryTag.CHECK_ONLY_SUB_STEPS
import ai.tock.bot.definition.StoryTag.CHECK_ONLY_SUB_STEPS_WITH_STORY_INTENT
import ai.tock.bot.engine.AsyncBotBus
import ai.tock.bot.engine.BotBus
import ai.tock.bot.engine.BotRepository
import ai.tock.bot.engine.action.Action
import ai.tock.bot.engine.action.SendChoice
import ai.tock.bot.engine.user.PlayerType
import ai.tock.bot.engine.user.UserTimeline
import ai.tock.shared.coroutines.ExperimentalTockCoroutines
import ai.tock.shared.error
import mu.KotlinLogging

/**
 * A Story is a small unit of conversation about a specific topic.
 * It is linked to at least one intent - the [starterIntent].
 */
data class Story(
    val definition: StoryDefinition,
    val starterIntent: Intent,
    internal var step: String? = null,
    val actions: MutableList<Action> = mutableListOf()
) {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    /**
     * The last action of the story.
     */
    val lastAction: Action? get() = actions.lastOrNull()

    /**
     * The last user action of the story.
     */
    val lastUserAction: Action? get() = actions.findLast { it.playerId.type == PlayerType.user }

    /**
     * The current step of the story.
     */
    val currentStep: StoryStepDef? get() = definition.steps.asSequence().mapNotNull { findStep(it) }.firstOrNull()

    /**
     * True if the story handle metrics and is not a main tracked story
     */
    val metricStory get() = definition.metricStory

    private fun findStep(step: StoryStepDef): StoryStepDef? {
        if (step.name == this.step) {
            return step
        } else {
            return step.children.asSequence().mapNotNull { findStep(it) }.firstOrNull()
        }
    }

    private fun findStep(
        steps: Collection<StoryStepDef>,
        userTimeline: UserTimeline,
        dialog: Dialog,
        action: Action,
        intent: Intent?
    ): StoryStepDef? {
        // first level
        findStepInTree(steps, userTimeline, dialog, action, intent)?.also {
            return it
        }

        // then iterate on children
        steps.forEach { s ->
            findStep(s.children, userTimeline, dialog, action, intent)?.also {
                return it
            }
        }
        return null
    }

    private fun findStepInTree(
        steps: Collection<StoryStepDef>,
        userTimeline: UserTimeline,
        dialog: Dialog,
        action: Action,
        intent: Intent?
    ): StoryStepDef? {
        // first level
        steps.forEach { s ->
            if (s.selectFromAction(userTimeline, dialog, action, intent)) {
                // check children
                findStepInTree(s.children, userTimeline, dialog, action, intent)?.also {
                    if (s.selectFromActionAndEntityStepSelection(action, intent) == true) {
                        return it
                    }
                }
                return s
            }
        }
        return null
    }

    private fun findParentStep(child: StoryStepDef): StoryStepDef? =
        definition.steps.asSequence().mapNotNull { findParentStep(it, child) }.firstOrNull()

    private fun findParentStep(current: StoryStepDef, child: StoryStepDef): StoryStepDef? =
        current.takeIf { current.children.any { child.name == it.name } }
            ?: current.children.asSequence().mapNotNull { findParentStep(it, child) }.firstOrNull()

    private fun StoryHandler.sendStartEvent(bus: BotBus): Boolean {
        // stops immediately if any startAction returns false
        return BotRepository.storyHandlerListeners.all {
            try {
                it.startAction(bus, this)
            } catch (throwable: Throwable) {
                logger.error(throwable)
                true
            }
        }
    }

    private fun StoryHandler.sendEndEvent(bus: BotBus) {
        BotRepository.storyHandlerListeners.forEach {
            try {
                it.endAction(bus, this)
            } catch (throwable: Throwable) {
                logger.error(throwable)
            }
        }
    }

    /**
     * Handles a request.
     */
    fun handle(bus: BotBus) {
        val storyHandler = definition.storyHandler
        @OptIn(ExperimentalTockCoroutines::class)
        if (storyHandler is AsyncStoryHandler) {
            // This path can only occur if this method was called from user code (TOCK always calls the suspending overload)
            error("Do not call Story.handle on an async story (${definition.id}), use handle(AsyncBotBus) instead")
        } else {
            storyHandler.withEvents(bus) {
                it.handle(bus)
            }
        }
    }

    /**
     * Handles a request using coroutines.
     */
    @ExperimentalTockCoroutines
    suspend fun handle(bus: AsyncBotBus) {
        definition.storyHandler.withEvents(bus.botBus) {
            if (it is AsyncStoryHandler) {
                it.handle(bus)
            } else {
                it.handle(bus.botBus)
            }
        }
    }

    private inline fun <T : StoryHandler> T.withEvents(bus: BotBus, op: (T) -> Unit) {
        try {
            if (sendStartEvent(bus)) {
                op(this)
            }
        } finally {
            sendEndEvent(bus)
        }
    }

    /**
     * What is the probability of this request support?
     */
    fun support(bus: BotBus): Double = definition.storyHandler.support(bus)

    /**
     * Does this story supports the action ?
     */
    fun supportAction(userTimeline: UserTimeline, dialog: Dialog, action: Action, intent: Intent): Boolean {
        if (supportIntent(intent)) {
            return true
        }

        val checkSteps = if (definition.hasTag(CHECK_ONLY_SUB_STEPS_WITH_STORY_INTENT)) {
            currentStep?.supportIntent(intent) == true ||
                    (currentStep?.children ?: definition.steps)
                        .any { it.supportIntent(intent) }
        } else {
            true
        }

        return if (checkSteps) {
            currentStep?.selectFromAction(userTimeline, dialog, action, intent) == true ||
                    (currentStep?.children
                        ?: if (definition.hasTag(CHECK_ONLY_SUB_STEPS)) emptyList() else definition.steps)
                        .any { it.selectFromAction(userTimeline, dialog, action, intent) }
        } else {
            false
        }
    }


    /**
     * Does this story supports the intent ?
     */
    fun supportIntent(intent: Intent): Boolean =
        definition.supportIntent(intent) || currentStep?.supportIntent(intent) == true

    /**
     * Set the current step form the specified action and new intent.
     */
    fun computeCurrentStep(userTimeline: UserTimeline, dialog: Dialog, action: Action, newIntent: Intent?) {
        // set current step if necessary
        var forced = false
        if (action is SendChoice && !dialog.state.hasCurrentSwitchStoryProcess) {
            action.step()?.apply {
                forced = true
                step = this
            }
        }

        // revalidate step
        val s = currentStep
        this.step = s?.name

        // check the children of the step
        if (!forced) {
            s?.children?.let { findStepInTree(it, userTimeline, dialog, action, newIntent) }?.apply {
                forced = true
                this@Story.step = name
            }
        }

        // reset the step if applicable
        if (!forced && newIntent != null &&
            (
                    (s?.intent != null && !s.supportIntent(newIntent)) ||
                            s?.selectFromActionAndEntityStepSelection(action, newIntent) == false
                    )
        ) {
            this.step = null
        }

        // check the step from the intent
        if (!forced && this.step == null) {

            if (s != null) {
                var parent: StoryStepDef? = s
                do {
                    parent = parent?.let { findParentStep(it) }
                    parent?.children?.let { findStepInTree(it, userTimeline, dialog, action, newIntent) }?.apply {
                        this@Story.step = name
                        return
                    }
                } while (parent != null)
            }

            findStep(definition.steps, userTimeline, dialog, action, newIntent)?.apply {
                this@Story.step = name
            }
        }
    }
}
