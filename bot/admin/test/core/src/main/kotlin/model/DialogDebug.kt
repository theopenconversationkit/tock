/*
 * Copyright (C) 2017/2022 e-voyageurs technologies
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

package ai.tock.bot.admin.test.model

import ai.tock.bot.admin.answer.AnswerConfigurationType
import ai.tock.bot.admin.answer.TickAnswerConfiguration
import ai.tock.bot.admin.story.StoryDefinitionConfiguration
import ai.tock.bot.bean.TickAction
import ai.tock.bot.bean.TickIntent
import ai.tock.bot.engine.dialog.LastDialogState
import ai.tock.bot.engine.dialog.TickState
import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.ALWAYS)
data class DialogDebugData(val intentName: String?,
                     val story: Story?,
                     val state: State?) {
   constructor(dialog: LastDialogState? = null,
               story: StoryDefinitionConfiguration? = null) : this(
       dialog?.intentName,
       story?.let { Story(it) },
       dialog?.tickState?.let { State(it) }
   )
}

@JsonInclude(JsonInclude.Include.ALWAYS)
data class Story(val name: String,
                 val type: AnswerConfigurationType,
                 val configuration: StoryConfiguration?) {
    constructor(story: StoryDefinitionConfiguration) : this(
        story.name,
        story.currentType,
        when(story.currentType) {
            AnswerConfigurationType.tick -> StoryConfiguration(story.answers.firstOrNull() as TickAnswerConfiguration)
            else -> null
        }
    )
}
data class StoryConfiguration(val contexts: Set<String>,
                              val actions: Set<Action>,
                              val intentsContexts: Set<TickIntent>) {
    constructor(config: TickAnswerConfiguration) : this(
        config.contexts.map { it.name }.toSet(),
        config.actions.map { Action(it) }.toSet(),
        config.intentsContexts
    )
}

data class State(val targets: Set<String>,
                 val actions: Set<String>,
                 val contexts: Set<Context>) {
    constructor(state: TickState) : this(
        state.objectivesStack.toSet(),
        state.ranHandlers.toSet(),
        state.contexts.map { Context(it.key, it.value) }.toSet(),
    )
}

data class Context(val name: String,
                   val value: Any?)

data class Action(val name: String,
                  val inputContextNames: Set<String>,
                  val outputContextNames: Set<String>) {
    constructor(action: TickAction) : this(
        action.name,
        action.inputContextNames,
        action.outputContextNames
    )
}
