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

package ai.tock.bot.admin.model

import ai.tock.bot.admin.answer.AnswerConfigurationType
import ai.tock.bot.admin.answer.TickAnswerConfiguration
import ai.tock.bot.bean.TickAction
import ai.tock.bot.bean.TickContext
import ai.tock.bot.bean.TickIntent
import ai.tock.bot.bean.TickStorySettings
import ai.tock.bot.bean.unknown.TickUnknownConfiguration
import ai.tock.bot.statemachine.State

data class BotTickAnswerConfiguration(val stateMachine: State,
                                      val primaryIntents: Set<String>,
                                      val secondaryIntents: Set<String>,
                                      val triggers: Set<String>,
                                      val contexts: Set<TickContext>,
                                      val actions: Set<TickAction>,
                                      val intentsContexts: Set<TickIntent>,
                                      val unknownHandleConfiguration: TickUnknownConfiguration,
                                      val storySettings: TickStorySettings?) :
    BotAnswerConfiguration(AnswerConfigurationType.tick) {

    constructor(conf: TickAnswerConfiguration) : this(conf.stateMachine,
        conf.primaryIntents,
        conf.secondaryIntents,
        conf.triggers,
        conf.contexts,
        conf.actions,
        conf.intentsContexts,
        conf.unknownHandleConfiguration,
        conf.storySettings)

    fun toTickAnswerConfiguration() : TickAnswerConfiguration = TickAnswerConfiguration(stateMachine,
        primaryIntents, secondaryIntents, triggers, contexts, actions, intentsContexts, unknownHandleConfiguration,
        storySettings)
}
