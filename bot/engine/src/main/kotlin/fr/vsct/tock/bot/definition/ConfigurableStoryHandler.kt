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

/**
 * A story handler that can be fully configured. Advanced usage only.
 */
open class ConfigurableStoryHandler<out T : StoryHandlerDefinition>(
    /**
     * The main intent of the story definition.
     */
    mainIntentName: String? = null,
    /**
     * The [HandlerDef] instantiator. Defines [StoryHandlerBase.newHandlerDefinition].
     */
    private val handlerDefInstantiator: (BotBus) -> T,
    /**
     * Check preconditions. if [BotBus.end] is called in this function,
     * [StoryHandlerDefinition.handle] is not called and the handling of bot answer is over.
     */
    private val preconditionsChecker: BotBus.() -> Unit,
    /**
     * The namespace for [I18nKeyProvider] implementation.
     */
    i18nNamespace: String = defaultNamespace,
    /**
     * Convenient value to wait before next answer sentence.
     */
    breath: Long = BotDefinition.defaultBreath
) : StoryHandlerBase<T>(mainIntentName, i18nNamespace, breath) {

    override fun newHandlerDefinition(bus: BotBus): T = handlerDefInstantiator(bus)

    override fun checkPreconditions(): BotBus.() -> Unit = preconditionsChecker
}