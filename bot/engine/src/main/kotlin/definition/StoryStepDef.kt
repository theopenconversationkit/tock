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

import ai.tock.bot.admin.story.StoryDefinitionStepMetric
import ai.tock.bot.engine.action.Action
import ai.tock.bot.engine.dialog.Dialog
import ai.tock.bot.engine.user.UserTimeline

/**
 * @see StoryStep
 * @see AsyncStoryStep
 */
interface StoryStepDef {
    /**
     * The name of the step.
     */
    val name: String

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
     * Should this Step be automatically selected from the action context?
     *
     * If this method returns `false`, the step may still be selected by other mechanisms (e.g. quick replies).
     *
     * @return true if the step should be selected
     */
    fun selectFromAction(
        userTimeline: UserTimeline,
        dialog: Dialog,
        action: Action,
        intent: Intent?,
    ): Boolean = intent != null && selectFromActionAndEntityStepSelection(action, intent) ?: supportStarterIntent(intent)

    /**
     * Does this step hast to be selected from its [entityStepSelection]?
     * Returns null if there is no [entityStepSelection].
     */
    fun selectFromActionAndEntityStepSelection(
        action: Action,
        intent: Intent? = null,
    ): Boolean? =
        entityStepSelection?.let { e ->
            if (intent != null && this.intent != null && !supportStarterIntent(intent)) {
                false
            } else if (e.value == null) {
                action.hasEntity(e.entityRole)
            } else {
                action.hasEntityPredefinedValue(e.entityRole, e.value)
            }
        }

    /**
     * Does this step support this intent as starter intent?
     */
    fun supportStarterIntent(i: Intent): Boolean = intent?.wrap(i) == true || otherStarterIntents.any { it.wrap(i) }

    /**
     * Does this step support this intent?
     */
    fun supportIntent(i: Intent): Boolean = supportStarterIntent(i) || secondaryIntents.any { it.wrap(i) }

    /**
     * The optional children of the step.
     */
    val children: Set<StoryStepDef> get() = emptySet()

    /**
     * Flag indicating if it's the step has no children.
     */
    val hasNoChildren: Boolean get() = children.isEmpty()

    /**
     * If not null, entity has to be set in the current action to trigger the step.
     */
    val entityStepSelection: EntityStepSelection? get() = null

    /**
     * The step metrics.
     */
    val metrics: List<StoryDefinitionStepMetric> get() = emptyList()
}
