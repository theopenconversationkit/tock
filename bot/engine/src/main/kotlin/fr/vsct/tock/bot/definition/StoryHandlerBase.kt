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
abstract class StoryHandlerBase : StoryHandler, I18nKeyProvider {

    private val logger = KotlinLogging.logger {}

    /**
     * Wait 1s by default
     */
    open val breath = 1000L

    final override fun handle(bus: BotBus) {
        bus.i18nProvider = this
        action(bus)
        if (!(bus.story.lastAction?.metadata?.lastAnswer ?: false)) {
            logger.warn { "No action sent or Bus.end not called" }
        }
    }

    /**
     * The method to implement.
     */
    abstract fun action(bus: BotBus)

    /**
     * The namespace for [I18nKeyProvider] implementation.
     */
    protected open val i18nNamespace: String = defaultNamespace

    protected fun i18nKeyPrefix(): String = javaClass.kotlin.simpleName?.replace("StoryHandler", "") ?: ""

    override fun i18nKeyFromLabel(defaultLabel: String, args: List<Any?>): I18nLabelKey {
        val prefix = i18nKeyPrefix()
        return i18nKey(
                "${prefix}_${Translator.getKeyFromDefaultLabel(defaultLabel)}",
                i18nNamespace,
                prefix,
                defaultLabel,
                args)
    }

    /**
     * Shortcut method for [i18nKeyFromLabel].
     */
    fun i18n(defaultLabel: String, vararg args: Any?): I18nLabelKey {
        return i18nKeyFromLabel(defaultLabel, *args)
    }

    /**
     * Shortcut method for [i18nKeyFromLabel].
     */
    fun i18n(defaultLabel: String, arg: Any?): I18nLabelKey {
        return i18nKeyFromLabel(defaultLabel, arg)
    }

}