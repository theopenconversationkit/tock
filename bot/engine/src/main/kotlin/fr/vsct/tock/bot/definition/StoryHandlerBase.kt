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

package fr.vsct.tock.bot.definition

import fr.vsct.tock.bot.engine.BotBus
import fr.vsct.tock.shared.defaultNamespace
import fr.vsct.tock.translator.I18nKeyProvider
import fr.vsct.tock.translator.I18nKeyProvider.Companion.generateKey
import fr.vsct.tock.translator.I18nLabelValue
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
     * Will be modified when bot owner is known.
     */
    @Volatile
    internal var i18nNamespace: String = defaultNamespace,
    /**
     * Convenient value to wait before next answer sentence. 1s by default.
     */
    val breath: Long = 1000L
) : StoryHandler, I18nKeyProvider, IntentAware {

    private val logger = KotlinLogging.logger {}

    /**
     * Checks preconditions - if [BotBus.end] is called,
     * [StoryHandlerDefinition.handle] is not called and the handling of bot answer is over.
     */
    open fun checkPreconditions(): BotBus.() -> Unit = {}

    /**
     * Instantiates new instance of [T].
     */
    abstract fun newHandlerDefinition(bus: BotBus): T

    /**
     * Handles precondition like checking mandatory entities, and create [StoryHandlerDefinition].
     * If this function returns null, this implied that [BotBus.end] has been called in this function
     * (as the [StoryHandlerDefinition.handle] function is not called).
     */
    open fun setupHandlerDefinition(bus: BotBus): T? {
        checkPreconditions().invoke(bus)
        return if (isEndCalled(bus)) {
            null
        } else {
            newHandlerDefinition(bus)
        }
    }

    /**
     * Has [BotBus.end] been already called?
     */
    private fun isEndCalled(bus: BotBus): Boolean =
        bus.dialog.allActions().lastOrNull()?.run { this !== bus.action && metadata.lastAnswer } ?: false

    final override fun handle(bus: BotBus) {
        //if not supported user interface, use unknown
        if (findStoryDefinition(bus)?.unsupportedUserInterfaces?.contains(bus.userInterfaceType) == true) {
            bus.botDefinition.unknownStory.storyHandler.handle(bus)
        } else {
            //set current i18n provider
            bus.i18nProvider = this
            val handler = setupHandlerDefinition(bus)

            if (handler == null) {
                logger.debug { "end called in computeStoryContext - skip action" }
            } else {
                handler.handle()
            }

            if (bus.story.lastAction?.metadata?.lastAnswer != true) {
                logger.warn { "Bus.end not called" }
            }
        }
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
     * Gets I18nKey with specified key. Current namespace is used.
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