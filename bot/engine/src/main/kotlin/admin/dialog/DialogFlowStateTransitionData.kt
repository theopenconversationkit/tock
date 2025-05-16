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

package ai.tock.bot.admin.dialog

import ai.tock.bot.definition.DialogFlowStateTransitionType
import org.litote.kmongo.Id

data class DialogFlowStateTransitionData(
    val previousStateId: Id<DialogFlowStateData>?,
    val nextStateId: Id<DialogFlowStateData>,
    val intent: String?,
    val step: String?,
    val newEntities: Set<String>,
    val type: DialogFlowStateTransitionType,
    val count: Long = 0
)
