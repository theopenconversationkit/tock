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

package ai.tock.bot.admin.model.dataset

import ai.tock.bot.admin.dataset.DatasetRunState
import ai.tock.bot.admin.dialog.ActionReport
import com.fasterxml.jackson.annotation.JsonInclude
import java.time.Instant

data class DatasetQuestionDTO(
    val id: String,
    val question: String,
    val groundTruth: String?,
)

data class DatasetRunStatsDTO(
    val totalQuestions: Int,
    val completedQuestions: Int,
    val failedQuestions: Int,
)

data class DatasetRunDTO(
    val id: String,
    val state: DatasetRunState,
    val startTime: Instant,
    val endTime: Instant?,
    @JsonInclude(JsonInclude.Include.NON_NULL)
    val settingsSnapshot: Map<String, Any?>? = null,
    val startedBy: String,
    val stats: DatasetRunStatsDTO,
)

data class DatasetDTO(
    val id: String,
    val name: String,
    val description: String,
    val questions: List<DatasetQuestionDTO>,
    val runs: List<DatasetRunDTO>,
    val createdAt: Instant,
    val createdBy: String,
    val updatedAt: Instant?,
    val updatedBy: String?,
)

enum class DatasetRunActionState {
    COMPLETED,
    FAILED,
}

data class DatasetRunActionDTO(
    val datasetId: String,
    val runId: String,
    val questionId: String,
    val state: DatasetRunActionState,
    @JsonInclude(JsonInclude.Include.ALWAYS)
    val action: ActionReport?,
    val retryCount: Int,
)
