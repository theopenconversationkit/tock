/*
 * Copyright (C) 2017/2019 e-voyageurs technologies
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

package ai.tock.bot.definition

import ai.tock.bot.definition.BotDefinition.Companion.defaultBreath
import ai.tock.bot.engine.BotBus
import ai.tock.bot.engine.action.SendSentence
import ai.tock.shared.defaultNamespace
import ai.tock.translator.I18nKeyProvider
import ai.tock.translator.I18nKeyProvider.Companion.generateKey
import ai.tock.translator.I18nLabelValue
import mu.KotlinLogging

/**
 * Base implementation of [StoryHandler].
 * Provides also a convenient implementation of [I18nKeyProvider] to support i18n.
 */
abstract class StoryHandlerBase<out T : StoryHandlerDefinition>(
    /**
     * The main intent of the story definition.
     */
    private val mainIntentName: String? = null,
    /**
     * The namespace for [I18nKeyProvider] implementation.
     */
    @Volatile
    internal var i18nNamespace: String = defaultNamespace,
    /**
     * Convenient value to wait before next answer sentence.
     */
    val breath: Long = defaultBreath
) : StoryHandler, I18nKeyProvider, IntentAware {

    companion object {
        private val logger = KotlinLogging.logger {}
        internal val SWITCH_STORY_BUS_KEY = "_tock_switch"
    }

    /**
     * Checks preconditions - if [BotBus.end] is called,
     * [StoryHandlerDefinition.handle] is not called and the handling of bot answer is over.
     */
    open fun checkPreconditions(): BotBus.() -> Any? = {}

    /**
     * Instantiates new instance of [T].
     */
    abstract fun newHandlerDefinition(bus: BotBus, data: Any? = null): T

    /**
     * Handles precondition like checking mandatory entities, and create [StoryHandlerDefinition].
     * If this function returns null, this implied that [BotBus.end] has been called in this function
     * (as the [StoryHandlerDefinition.handle] function is not called).
     */
    private fun setupHandlerDefinition(bus: BotBus): T? {
        val data = checkPreconditions().invoke(bus)?.takeUnless { it is Unit }
        return if (isEndCalled(bus)) {
            null
        } else {
            newHandlerDefinition(bus, data)
        }
    }

    /**
     * Has [BotBus.end] been already called?
     */
    private fun isEndCalled(bus: BotBus): Boolean =
        (bus.userTimeline.currentDialog ?: bus.dialog)
            .lastAction?.run { this !== bus.action && metadata.lastAnswer } ?: false

    final override fun handle(bus: BotBus) {
        //if not supported user interface, use unknown
        if (findStoryDefinition(bus)?.unsupportedUserInterfaces?.contains(bus.userInterfaceType) == true) {
            bus.botDefinition.unknownStory.storyHandler.handle(bus)
        } else {
            //set current i18n provider
            bus.i18nProvider = this

            var data = checkPreconditions().invoke(bus)?.takeUnless { it is Unit }
            if (!isEndCalled(bus)) {
                val step = bus.step
                var handler: T? = null

                //Default implementation redirect to answer if there is no current step
                // or if the [StoryStep.handle()] method of the current step returns null
                if (step != null) {
                    data = (step as? StoryDataStep<*, *>)?.checkPreconditions()?.invoke(bus)?.takeUnless { it is Unit }
                        ?: data
                    handler = newHandlerDefinition(bus, data)
                    @Suppress("UNCHECKED_CAST")
                    if (step is StoryDataStep<*, *>) {
                        (step as StoryDataStep<T, Any>).handler().invoke(handler, data)
                    } else {
                        (step as StoryStep<T>).answer().invoke(handler)
                    }
                }
                if (!isEndCalled(bus)) {
                    handler = handler ?: newHandlerDefinition(bus, data)
                    handler.handle()

                    if (!bus.connectorData.skipAnswer && bus.getBusContextValue<Boolean>(SWITCH_STORY_BUS_KEY) != true && !isEndCalled(bus)) {
                        logger.warn { "Bus.end not called for story ${bus.story.definition.id}, user ${bus.userId.id} and connector ${bus.targetConnectorType}" }
                    }
                }
            }
        }
    }

    override fun support(bus: BotBus): Double =
        if (bus.story.definition == bus.botDefinition.unknownStory) {
            0.0
        } else {
            (bus.action as? SendSentence)?.nlpStats?.nlpResult?.intentProbability ?: 1.0
        }

    /**
     * Finds the story definition of this handler.
     */
    open fun findStoryDefinition(bus: BotBus): StoryDefinition? = bus
        .botDefinition
        .stories
        .find { it.storyHandler == this }

    /**
     * Handles the action and switches the context to the underlying story definition.
     */
    fun handleAndSwitchStory(bus: BotBus) {
        findStoryDefinition(bus)
            ?.apply {
                bus.switchStory(this)
            }

        handle(bus)
    }

    /**
     * Story i18n category.
     */
    private fun i18nKeyCategory(): String = findMainIntentName() ?: i18nNamespace

    override fun i18n(defaultLabel: CharSequence, args: List<Any?>): I18nLabelValue {
        val category = i18nKeyCategory()
        return I18nLabelValue(
            generateKey(i18nNamespace, category, defaultLabel),
            i18nNamespace,
            category,
            defaultLabel,
            args
        )
    }

    /**
     * Gets an i18n label with the specified key. Current namespace is used for the categorization.
     */
    fun i18nKey(key: String, defaultLabel: CharSequence, vararg args: Any?): I18nLabelValue {
        val category = i18nKeyCategory()
        return I18nLabelValue(
            key,
            i18nNamespace,
            category,
            defaultLabel,
            args.toList()
        )
    }

    private fun findMainIntentName(): String? {
        return mainIntentName ?: this::class.simpleName?.toLowerCase()?.replace("storyhandler", "")
    }

    override fun wrappedIntent(): Intent {
        return findMainIntentName()?.let { Intent(it) } ?: error("unknown main intent name")
    }
}