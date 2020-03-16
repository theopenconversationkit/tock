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

import ai.tock.bot.engine.BotBus
import ai.tock.bot.engine.action.Action
import ai.tock.bot.engine.dialog.Dialog
import ai.tock.bot.engine.user.UserTimeline
import java.util.concurrent.ConcurrentHashMap

/**
 * step -> intent default behaviour.
 */
internal val stepToIntentRepository = ConcurrentHashMap<StoryStep<out StoryHandlerDefinition>, IntentAware>()

/**
 * Use this step when you want to set a null [StoryStep].
 */
val noStep = object : SimpleStoryStep {
    override val name: String = "_NO_STEP_"
}

/**
 * A step (the implementation is usually an enum) is a part of a [StoryDefinition].
 * Used to manage workflow in a [StoryHandler].
 */
interface StoryStep<T : StoryHandlerDefinition> {

    /**
     * The name of the step. usually automatically defined by the enum field.
     */
    val name: String

    /**
     * The custom answer for this step.
     * When returning a null value,
     * it means that the step is not able to answer to the current request.
     *
     * Default implementation returns null.
     */
    fun answer(): T.() -> Any? = { null }

    /**
     * Returns [intent] or the [StoryDefinition.mainIntent] if [intent] is null.
     */
    val baseIntent: IntentAware get() = intent ?: stepToIntentRepository[this] ?: error("no intent for $this")

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

    /**
     * Does this Step has to be selected from the Bus?
     * This method is called if [StoryHandlerBase.checkPreconditions] does not call [BotBus.end].
     * If this functions returns true, the step is selected and remaining steps are not tested.
     */
    fun selectFromBus(): BotBus.() -> Boolean = { false }

    /**
     * Does this Step has to be automatically selected?
     * if returns true, the step is selected.
     */
    fun selectFromAction(userTimeline: UserTimeline, dialog: Dialog, action: Action, intent: Intent?): Boolean =
        intent != null
            &&
            entityStepSelection?.let { e ->
                if (e.value == null) action.hasEntity(e.entityRole)
                else action.hasEntityPredefinedValue(e.entityRole, e.value)
            } ?: supportStarterIntent(intent)

    /**
     * Does this step support this intent as starter intent?
     */
    fun supportStarterIntent(i: Intent): Boolean =
        intent?.wrap(i) == true || otherStarterIntents.any { it.wrap(i) }

    /**
     * Does this step support this intent?
     */
    fun supportIntent(i: Intent): Boolean = supportStarterIntent(i) || secondaryIntents.any { it.wrap(i) }

    /**
     * The optional children of the step.
     */
    val children: Set<StoryStep<T>> get() = emptySet()

    /**
     * If not null, entity has to be set in the current action to trigger the step.
     */
    val entityStepSelection: EntityStepSelection? get() = null

}