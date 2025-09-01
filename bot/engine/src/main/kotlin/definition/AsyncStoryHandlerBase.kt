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

import ai.tock.bot.engine.AsyncBotBus
import ai.tock.bot.engine.AsyncBus
import ai.tock.shared.InternalTockApi
import ai.tock.shared.coroutines.ExperimentalTockCoroutines
import ai.tock.shared.defaultNamespace
import ai.tock.translator.I18nKeyProvider.Companion.generateKey
import ai.tock.translator.I18nLabelValue
import ai.tock.translator.I18nLocalizedLabel
import mu.KotlinLogging

/**
 * An [AsyncStoryHandler] with a base `handleAsync` implementation and i18n utilities
 */
@ExperimentalTockCoroutines
abstract class AsyncStoryHandlerBase(
    private val mainIntent: Intent?,
) : AsyncStoryHandler, I18nStoryHandler, IntentAware {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    @Volatile
    override var i18nNamespace: String = defaultNamespace
        @InternalTockApi set

    override suspend fun handle(bus: AsyncBus) {
        val baseBus = (bus as AsyncBotBus).botBus
        val storyDefinition = findStoryDefinition(bus)
        // if not supported user interface, use unknown
        if (storyDefinition?.unsupportedUserInterfaces?.contains(bus.userInterfaceType) == true) {
            baseBus.botDefinition.unknownStory.storyHandler.handle(baseBus)
        } else {
            // set current i18n provider
            baseBus.i18nProvider = this

            action(bus)

            if (!bus.isEndCalled() && !baseBus.connectorData.skipAnswer) {
                logger.warn {
                    "Bus.end not called for story ${baseBus.story.definition.id}, user ${bus.userId.id} and connector ${baseBus.targetConnectorType}"
                }
            }
        }
    }

    protected abstract suspend fun action(bus: AsyncBus)

    protected fun AsyncBus.isEndCalled() = StoryHandlerBase.isEndCalled((this as AsyncBotBus).botBus)

    /**
     * Finds the story definition of this handler.
     */
    open fun findStoryDefinition(bus: AsyncBus): StoryDefinition? =
        (bus as AsyncBotBus).botBus.botDefinition.findStoryByStoryHandler(this, bus.connectorId)

    /**
     * Story i18n category.
     */
    protected open fun i18nKeyCategory(): String = mainIntent?.name ?: i18nNamespace

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

    override fun wrappedIntent(): Intent {
        return mainIntent ?: error("unknown main intent name")
    }
}
