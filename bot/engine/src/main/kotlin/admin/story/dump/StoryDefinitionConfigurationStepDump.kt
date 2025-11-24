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

package ai.tock.bot.admin.story.dump

import ai.tock.bot.admin.answer.AnswerConfigurationType
import ai.tock.bot.admin.story.StoryDefinitionConfigurationStep
import ai.tock.bot.definition.EntityStepSelection
import ai.tock.bot.definition.IntentWithoutNamespace
import ai.tock.translator.I18nKeyProvider
import ai.tock.translator.I18nLabelValue

data class StoryDefinitionConfigurationStepDump(
    /**
     * The name of the step.
     */
    val name: String,
    /**
     * The intent used to reach the step - mandatory if an answer is set, or if there is a [targetIntent].
     */
    val intent: IntentWithoutNamespace?,
    /**
     * The optional intent to switch to when the step is reached.
     */
    val targetIntent: IntentWithoutNamespace?,
    /**
     * The answers available.
     */
    val answers: List<AnswerConfigurationDump>,
    /**
     * The type of answer configuration.
     */
    val currentType: AnswerConfigurationType,
    /**
     * The user sentence sample.
     */
    val userSentenceLabel: I18nLabelValue? = null,
    /**
     * The children of the steps
     */
    val children: List<StoryDefinitionConfigurationStepDump> = emptyList(),
    /**
     * The level of the step.
     */
    val level: Int = 0,
    /**
     * Entity selection.
     */
    val entity: EntityStepSelection? = null,
) {
    constructor(def: StoryDefinitionConfigurationStep, namespace: String, category: String) :
        this(
            def.name,
            def.intent,
            def.targetIntent,
            AnswerConfigurationDump.toDump(def.answers),
            def.currentType,
            def.userSentenceLabel
                ?: I18nLabelValue(
                    I18nKeyProvider.generateKey(namespace, category, def.userSentence),
                    namespace,
                    category,
                    def.userSentenceLabel?.defaultLabel ?: def.userSentence,
                ),
            def.children.map { StoryDefinitionConfigurationStepDump(it, namespace, category) },
            def.level,
            def.entity,
        )

    fun toStep(controller: StoryDefinitionConfigurationDumpController): StoryDefinitionConfigurationStep =
        StoryDefinitionConfigurationStep(
            name.takeUnless { it.isBlank() } ?: "${intent?.name}_$level",
            controller.checkIntent(intent),
            controller.checkIntent(targetIntent),
            answers.map { it.toAnswer(currentType, controller) },
            currentType,
            "",
            userSentenceLabel?.withNamespace(controller.targetNamespace),
            children.map { it.toStep(controller) },
            level,
            entity,
        )
}
