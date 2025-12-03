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

package ai.tock.bot.mongo

import ai.tock.bot.engine.action.Action
import ai.tock.bot.engine.dialog.Dialog
import ai.tock.shared.jackson.AnyValueWrapper
import org.litote.jackson.data.JacksonData
import org.litote.kmongo.Data
import org.litote.kmongo.Id
import java.time.Instant
import java.time.Instant.now

@Data(internal = true)
@JacksonData(internal = true)
internal data class ConnectorMessageColId(val actionId: Id<Action>, val dialogId: Id<Dialog>)

// @Data(internal = true)
@JacksonData(internal = true)
internal data class ConnectorMessageCol(
    val _id: ConnectorMessageColId,
    val messages: List<AnyValueWrapper?>,
    val date: Instant = now(),
)
