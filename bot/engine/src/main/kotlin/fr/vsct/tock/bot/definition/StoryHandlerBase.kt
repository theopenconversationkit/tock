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
import fr.vsct.tock.translator.I18nLabelKey
import fr.vsct.tock.translator.Translator
import mu.KotlinLogging

/**
 * Base implementation of [StoryHandler].
 * Provides also a convenient implementation of [I18nKeyProvider] to support i18n.
 */
abstract class StoryHandlerBase<out T : StoryHandlerDefinition> : StoryHandler, I18nKeyProvider {

    private val logger = KotlinLogging.logger {}

    /**
     * Handle precondition like checking mandatory entities, and create [StoryHandlerDefinition].
     * If this function returns null, this implied that [BotBus.end] has been called in this function
     * (as the [StoryHandlerDefinition.handle] function is not called).
     */
    abstract fun computeStoryHandlerDefinition(bus: BotBus): T?

    final override fun handle(bus: BotBus) {
        //if not supported user interface, use unknown
        if (findStoryDefinition(bus)?.unsupportedUserInterfaces?.contains(bus.userInterfaceType) == true) {
            bus.botDefinition.unknownStory.storyHandler.handle(bus)
        } else {
            bus.i18nProvider = this
            val handler = computeStoryHandlerDefinition(bus)

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
     * Find the story definition of this handler.
     */
    open fun findStoryDefinition(bus: BotBus): StoryDefinition?
            = bus
            .botDefinition
            .stories
            .find { it.storyHandler == this }

    /**
     * Handle the action and switch the context to the underlying story definition.
     */
    fun handleAndSwitchStory(bus: BotBus) {
        findStoryDefinition(bus)
                ?.apply {
                    bus.switchStory(this)
                }

        handle(bus)
    }

    /**
     * Wait 1s by default
     */
    open val breath = 1000L

    /**
     * The namespace for [I18nKeyProvider] implementation.
     */
    protected open val i18nNamespace: String = defaultNamespace

    /**
     * Default i18n prefix.
     */
    protected fun i18nKeyPrefix(): String = javaClass.kotlin.simpleName?.replace("StoryHandler", "") ?: ""

    override fun i18nKeyFromLabel(defaultLabel: CharSequence, args: List<Any?>): I18nLabelKey {
        val prefix = i18nKeyPrefix()
        return i18nKey(
                "${prefix}_${Translator.getKeyFromDefaultLabel(defaultLabel)}",
                i18nNamespace,
                prefix,
                defaultLabel,
                args)
    }

    /**
     * Get I18nKey with specified key. Current namespace is used.
     */
    fun i18nKey(key: String, defaultLabel: CharSequence, vararg args: Any?): I18nLabelKey {
        val prefix = i18nKeyPrefix()
        return i18nKey(
                key,
                i18nNamespace,
                prefix,
                defaultLabel,
                args.toList())
    }

}