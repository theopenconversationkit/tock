/*
 * Copyright (C) 2017/2021 e-voyageurs technologies
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

package ai.tock.bot.mongo

import ai.tock.bot.admin.answer.AnswerConfigurationType
import ai.tock.bot.admin.bot.BotApplicationConfiguration
import ai.tock.bot.definition.DialogFlowStateTransitionType
import ai.tock.bot.engine.dialog.Dialog
import org.litote.jackson.data.JacksonData
import org.litote.kmongo.Data
import org.litote.kmongo.Id
import org.litote.kmongo.newId
import java.time.Instant
import java.time.Instant.now

/**
 *
 */
@Data(internal = true)
@JacksonData(internal = true)
internal data class DialogFlowStateCol(
    val namespace: String,
    val botId: String,
    val storyDefinitionId: String,
    val intent: String,
    val step: String?,
    val entities: Set<String>,
    val _id: Id<DialogFlowStateCol> = newId(),
    val storyType: AnswerConfigurationType? = null,
    val storyName: String = storyDefinitionId
)

@Data(internal = true)
@JacksonData(internal = true)
internal data class DialogFlowStateTransitionCol(
    val namespace: String,
    val botId: String,
    val previousStateId: Id<DialogFlowStateCol>?,
    val nextStateId: Id<DialogFlowStateCol>,
    val intent: String?,
    val step: String?,
    val newEntities: Set<String>,
    val type: DialogFlowStateTransitionType,
    val _id: Id<DialogFlowStateTransitionCol> = newId()
)

@Data(internal = true)
@JacksonData(internal = true)
internal data class DialogFlowStateTransitionStatCol(
    val applicationId: Id<BotApplicationConfiguration>,
    val transitionId: Id<DialogFlowStateTransitionCol>,
    val dialogId: Id<Dialog>,
    val text: String?,
    val date: Instant = now()
)

@Data(internal = true)
@JacksonData(internal = true)
internal data class DialogFlowAggregateResult(
    val date: String = "",
    val count: Int = 0,
    val seriesKey: String = "",
)

@Data(internal = true)
@JacksonData(internal = true)
internal data class DialogFlowAggregateSeriesResult(
    val values: List<DialogFlowAggregateResult>,
    val seriesKey: String = "",
)
