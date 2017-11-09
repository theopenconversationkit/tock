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

/**
 * A step (the implementation is usually an enum) is a part of a [StoryDefinition].
 * Used to manage workflow in a [StoryHandler].
 */
interface StoryStep<T : StoryHandlerDefinition> {

    val name: String

    /**
     * The custom answer for this step.
     * When returning a null value,
     * it means that the step is not able to answer to the current request.
     *
     * Default implementation returns null.
     */
    fun answer(handler: T): Unit? = null

    /**
     * The main intent of the step.
     * If not null and if the current intent is equals to [intent],
     * this step will be automatically selected to be the current step.
     */
    val intent: IntentAware? get() = null

    /**
     * Same behaviour than [intent] in the rare case when the step handle more than one intent.
     */
    val otherStarterIntents: Set<IntentAware> get() = emptySet()

    /**
     * The secondary intents of this step. If detected and if the current step is this step,
     * the current step remains this step.
     */
    val secondaryIntents: Set<IntentAware> get() = emptySet()

    fun supportStarterIntent(i: Intent): Boolean
            = intent?.wrap(i) == true || otherStarterIntents.any { it.wrap(i) }

    fun supportIntent(i: Intent): Boolean
            = supportStarterIntent(i) || secondaryIntents.any { it.wrap(i) }

}