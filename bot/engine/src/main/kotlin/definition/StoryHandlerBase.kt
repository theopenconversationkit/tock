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

package ai.tock.bot.definition

import ai.tock.bot.definition.BotDefinition.Companion.defaultBreath
import ai.tock.bot.engine.BotBus
import ai.tock.bot.engine.action.SendSentence
import ai.tock.bot.engine.hasCurrentSwitchStoryProcess
import ai.tock.shared.InternalTockApi
import ai.tock.shared.defaultNamespace
import ai.tock.translator.I18nKeyProvider
import ai.tock.translator.I18nKeyProvider.Companion.generateKey
import ai.tock.translator.I18nLabelValue
import ai.tock.translator.I18nLocalizedLabel
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
    @set:InternalTockApi
    override var i18nNamespace: String = defaultNamespace,
    /**
     * Convenient value to wait before next answer sentence.
     */
    val breath: Long = defaultBreath
) : I18nStoryHandler, IntentAware {

    companion object {
        private val logger = KotlinLogging.logger {}

        /**
         * Has [BotBus.end] been already called?
         */
        internal fun isEndCalled(bus: BotBus): Boolean =
            (bus.userTimeline.currentDialog ?: bus.dialog)
                .lastAction?.run { this !== bus.action && metadata.lastAnswer } ?: false
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
     * Selects step from [HandlerDef], optional data and [StoryDefinition].
     */
    fun <T : StoryHandlerDefinition> selectStepFromStoryHandlerAndData(
        def: T,
        data: Any?,
        storyDefinition: StoryDefinition?
    ): StoryStep<*>? {
        storyDefinition?.steps?.also { steps ->
            for (s in steps) {
                s as StoryStep<*>

                @Suppress("UNCHECKED_CAST") val selected = if (s is StoryDataStep<*, *, *>) {
                    (s as? StoryDataStep<in T, Any, *>)?.selectFromBusAndData()?.invoke(def, data) ?: false
                } else {
                    s.selectFromBus().invoke(def)
                }

                if (selected) {
                    return s
                }
            }
        }
        return null
    }

    final override fun handle(bus: BotBus) {
        val storyDefinition = findStoryDefinition(bus)
        // if not supported user interface, use unknown
        if (storyDefinition?.unsupportedUserInterfaces?.contains(bus.userInterfaceType) == true) {
            bus.botDefinition.unknownStory.storyHandler.handle(bus)
        } else {
            // set current i18n provider
            bus.i18nProvider = this

            val mainData = checkPreconditions().invoke(bus)?.takeUnless { it is Unit }
            if (!isEndCalled(bus)) {
                val handler: T = newHandlerDefinition(bus, mainData)

                // select steps from bus
                selectStepFromStoryHandlerAndData(handler, mainData, storyDefinition)?.also {
                    bus.step = it
                }
                val step = bus.step

                // Default implementation redirect to answer if there is no current step
                // or if the [StoryStep.handle()] method of the current step returns null
                if (step != null) {
                    @Suppress("UNCHECKED_CAST")
                    val data = (step as? StoryDataStep<T, Any, *>)?.checkPreconditions()?.invoke(handler, mainData)
                        ?.takeUnless { it is Unit }
                        ?: mainData
                    if (!isEndCalled(bus)) {
                        @Suppress("UNCHECKED_CAST")
                        if (step is StoryDataStep<*, *, *>) {
                            (step as StoryDataStep<T, Any, Any>).handler().invoke(handler, data)
                        } else {
                            (step as? StoryStep<T>)?.answer()?.invoke(handler)
                        }
                    }
                }
                if (!isEndCalled(bus)) {
                    handler.handle()

                    if (!bus.connectorData.skipAnswer &&
                        !bus.hasCurrentSwitchStoryProcess &&
                        !isEndCalled(bus)
                    ) {
                        logger.warn { "Bus.end not called for story ${(storyDefinition ?: bus.story.definition).id}, user ${bus.userId.id} and connector ${bus.targetConnectorType}" }
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
    open fun findStoryDefinition(bus: BotBus): StoryDefinition? =
        bus.botDefinition.findStoryByStoryHandler(this, bus.connectorId)

    /**
     * Handles the action and switches the context to the underlying story definition.
     */
    fun handleAndSwitchStory(bus: BotBus) {
        findStoryDefinition(bus)
            ?.apply {
                bus.switchStory(this)
            }
            ?: error("no story found for handler")

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
    override fun i18nKey(key: String, defaultLabel: CharSequence, vararg args: Any?): I18nLabelValue {
        return i18nKey(key, defaultLabel, emptySet(), *args)
    }

    /**
     * Gets an i18n label with the specified key and defaults. Current namespace is used for the categorization.
     */
    override fun i18nKey(key: String, defaultLabel: CharSequence, defaultI18n: Set<I18nLocalizedLabel>, vararg args: Any?): I18nLabelValue {
        val category = i18nKeyCategory()
        return I18nLabelValue(
            key,
            i18nNamespace,
            category,
            defaultLabel,
            args.toList(),
            defaultI18n,
        )
    }

    private fun findMainIntentName(): String? {
        return mainIntentName ?: this::class.simpleName?.lowercase()?.replace("storyhandler", "")
    }

    override fun wrappedIntent(): Intent {
        return findMainIntentName()?.let { Intent(it) } ?: error("unknown main intent name")
    }
}
