/*
 * Copyright (C) 2017 VSCT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ai.tock.bot.engine.dialog

import ai.tock.bot.definition.Intent
import ai.tock.bot.definition.StoryDefinition
import ai.tock.bot.definition.StoryHandler
import ai.tock.bot.definition.StoryStep
import ai.tock.bot.engine.BotBus
import ai.tock.bot.engine.BotRepository
import ai.tock.bot.engine.action.Action
import ai.tock.bot.engine.action.SendChoice
import ai.tock.bot.engine.user.UserTimeline
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
     * The current step of the story.
     */
    val currentStep: StoryStep<*>? get() = definition.steps.asSequence().mapNotNull { findStep(it) }.firstOrNull()

    private fun findStep(step: StoryStep<*>): StoryStep<*>? {
        if (step.name == this.step) {
            return step
        } else {
            return step.children.mapNotNull { findStep(it) }.firstOrNull()
        }
    }

    private fun StoryHandler.sendStartEvent(bus: BotBus): Boolean {
        //stops immediately if any startAction returns false
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
        definition.storyHandler.apply {
            try {
                if (sendStartEvent(bus)) {
                    handle(bus)
                }
            } finally {
                sendEndEvent(bus)
            }
        }
    }

    /**
     * What is the probability of this request support?
     */
    fun support(bus: BotBus): Double = definition.storyHandler.support(bus)

    /**
     * Set the current step form the specified action and new intent.
     */
    fun computeCurrentStep(userTimeline: UserTimeline, dialog: Dialog, action: Action, newIntent: Intent?) {
        //set current step if necessary
        var forced = false
        if (action is SendChoice) {
            action.step()?.apply {
                forced = true
                step = this
            }
        }

        //revalidate step
        val step = currentStep
        this.step = step?.name

        //check the children of the step
        if (!forced) {
            step?.children?.find { it.select(userTimeline, dialog, action, newIntent) }?.apply {
                forced = true
                this@Story.step = name
            }
        }

        //reset the step if applicable
        if (!forced && newIntent != null
            && (step?.intent != null && !step.supportIntent(newIntent))
        ) {
            this.step = null
        }

        //check the step from the intent
        if (!forced && step == null) {
            definition.steps.find { it.select(userTimeline, dialog, action, newIntent) }?.apply {
                forced = true
                this@Story.step = name
            }
        }
    }

}