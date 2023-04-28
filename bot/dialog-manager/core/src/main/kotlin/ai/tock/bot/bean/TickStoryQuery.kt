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

package ai.tock.bot.bean

import ai.tock.bot.bean.unknown.TickUnknownAnswerConfig
import ai.tock.bot.statemachine.State
import java.util.TreeSet

/**
 * The tick story query, corresponding on published scenario version
 *
 * @param id technical story identifier (mongodb's _id)
 * @param botId the botId
 * @param storyId functional story identifier (equals to scenario group id)
 * @param name story name
 * @param description story description
 * @param stateMachine state machine
 * @param mainIntent main intent
 * @param primaryIntents set of primary intents
 * @param secondaryIntents set of secondary intents
 * @param triggers set of triggers (events)
 * @param contexts set of contexts
 * @param actions set of tick actions [TickContext]
 * @param intentsContexts set of tick intents [TickIntent]
 * @param unknownAnswerConfigs set of unknown answer configuration [UnknownAnswerConfig]
 */
@kotlinx.serialization.Serializable
data class TickStoryQuery(
    val id: String? = null,
    val botId: String,
    val storyId: String,
    val name: String,
    val description: String,
    val stateMachine: State,
    val mainIntent: String,
    val primaryIntents: Set<String>,
    val secondaryIntents: Set<String>,
    val triggers: Set<String> = emptySet(),
    val contexts: Set<TickContext>,
    val actions: Set<TickAction>,
    val intentsContexts: Set<TickIntent> = emptySet(),
    val unknownAnswerConfigs: Set<TickUnknownAnswerConfig> = TreeSet()
)

