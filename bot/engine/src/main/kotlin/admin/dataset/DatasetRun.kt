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

package ai.tock.bot.admin.dataset

import ai.tock.bot.admin.bot.BotApplicationConfiguration
import ai.tock.bot.engine.action.Action
import ai.tock.bot.engine.dialog.Dialog
import org.litote.kmongo.Id
import org.litote.kmongo.newId
import java.time.Instant
import java.util.Locale

enum class DatasetRunState {
    QUEUED,
    RUNNING,
    COMPLETED,
    CANCELLED,
}

enum class DatasetRunQuestionResultState {
    PENDING,
    RUNNING,
    COMPLETED,
    FAILED,
    CANCELLED,
}

data class DatasetRun(
    val _id: Id<DatasetRun> = newId(),
    val namespace: String,
    val botId: String,
    val datasetId: Id<Dataset>,
    val state: DatasetRunState,
    val startTime: Instant,
    val endTime: Instant? = null,
    val startedBy: String,
    val language: Locale? = null,
    val botApplicationConfigurationId: Id<BotApplicationConfiguration>? = null,
    val settingsSnapshot: Map<String, Any?> = emptyMap(),
)

data class DatasetRunQuestionResult(
    val _id: Id<DatasetRunQuestionResult> = newId(),
    val namespace: String,
    val botId: String,
    val datasetId: Id<Dataset>,
    val runId: Id<DatasetRun>,
    val questionId: String,
    val state: DatasetRunQuestionResultState = DatasetRunQuestionResultState.PENDING,
    val startedAt: Instant? = null,
    val endedAt: Instant? = null,
    val userIdModifier: String,
    val userActionId: String? = null,
    val dialogId: Id<Dialog>? = null,
    val answerActionId: Id<Action>? = null,
    val retryCount: Int = 0,
    val error: String? = null,
)
