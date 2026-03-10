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

import org.litote.kmongo.Id

interface DatasetRunDAO {
    fun saveRun(run: DatasetRun): DatasetRun

    fun claimNextQueuedRun(): DatasetRun?

    fun getRunById(id: Id<DatasetRun>): DatasetRun?

    fun getRunsByDatasetId(datasetId: Id<Dataset>): List<DatasetRun>

    fun getActiveRunsByDatasetId(datasetId: Id<Dataset>): List<DatasetRun>

    fun deleteRun(id: Id<DatasetRun>)

    fun deleteRunsByDatasetId(datasetId: Id<Dataset>)

    fun deleteRunsByNamespaceAndBotId(
        namespace: String,
        botId: String,
    )

    fun saveQuestionResult(result: DatasetRunQuestionResult): DatasetRunQuestionResult

    fun saveQuestionResults(results: List<DatasetRunQuestionResult>)

    fun getQuestionResultsByRunId(runId: Id<DatasetRun>): List<DatasetRunQuestionResult>

    fun deleteQuestionResultsByRunId(runId: Id<DatasetRun>)

    fun deleteQuestionResultsByNamespaceAndBotId(
        namespace: String,
        botId: String,
    )
}
