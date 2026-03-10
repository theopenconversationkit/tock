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

import ai.tock.bot.admin.dataset.Dataset
import ai.tock.bot.admin.dataset.DatasetDAO
import ai.tock.bot.admin.dataset.DatasetRun
import ai.tock.bot.admin.dataset.DatasetRunDAO
import ai.tock.bot.admin.dataset.DatasetRunQuestionResult
import ai.tock.bot.admin.dataset.DatasetRunState
import ai.tock.shared.ensureIndex
import ai.tock.shared.ensureUniqueIndex
import ai.tock.shared.error
import com.mongodb.client.model.FindOneAndUpdateOptions
import com.mongodb.client.model.ReturnDocument
import com.mongodb.client.model.Sorts.ascending
import com.mongodb.client.model.Updates.set
import mu.KotlinLogging
import org.litote.kmongo.Id
import org.litote.kmongo.ascendingSort
import org.litote.kmongo.deleteMany
import org.litote.kmongo.deleteOneById
import org.litote.kmongo.descendingSort
import org.litote.kmongo.eq
import org.litote.kmongo.find
import org.litote.kmongo.findOneById
import org.litote.kmongo.getCollection
import org.litote.kmongo.`in`
import org.litote.kmongo.save

internal object DatasetMongoDAO : DatasetDAO, DatasetRunDAO {
    private val logger = KotlinLogging.logger {}

    private val datasetCol = MongoBotConfiguration.database.getCollection<Dataset>("dataset")
    private val datasetRunCol = MongoBotConfiguration.database.getCollection<DatasetRun>("dataset_run")
    private val datasetRunQuestionResultCol =
        MongoBotConfiguration.database.getCollection<DatasetRunQuestionResult>("dataset_run_question_result")

    init {
        try {
            datasetCol.ensureIndex(Dataset::namespace, Dataset::botId)
            datasetCol.ensureIndex(Dataset::namespace, Dataset::botId, Dataset::name)

            datasetRunCol.ensureIndex(DatasetRun::namespace, DatasetRun::botId)
            datasetRunCol.ensureIndex(DatasetRun::datasetId)
            datasetRunCol.ensureIndex(DatasetRun::datasetId, DatasetRun::state)
            datasetRunCol.ensureIndex(DatasetRun::datasetId, DatasetRun::startTime)

            datasetRunQuestionResultCol.ensureIndex(DatasetRunQuestionResult::namespace, DatasetRunQuestionResult::botId)
            datasetRunQuestionResultCol.ensureIndex(DatasetRunQuestionResult::datasetId, DatasetRunQuestionResult::runId)
            datasetRunQuestionResultCol.ensureUniqueIndex(
                DatasetRunQuestionResult::runId,
                DatasetRunQuestionResult::questionId,
            )
        } catch (e: Exception) {
            logger.error(e)
        }
    }

    override fun save(dataset: Dataset): Dataset {
        datasetCol.save(dataset)
        return dataset
    }

    override fun getDatasetById(id: Id<Dataset>): Dataset? {
        return datasetCol.findOneById(id)
    }

    override fun getDatasetsByNamespaceAndBotId(
        namespace: String,
        botId: String,
    ): List<Dataset> {
        return datasetCol.find(
            Dataset::namespace eq namespace,
            Dataset::botId eq botId,
        ).ascendingSort(Dataset::name).toList()
    }

    override fun delete(id: Id<Dataset>) {
        datasetCol.deleteOneById(id)
        deleteRunsByDatasetId(id)
    }

    override fun deleteByNamespaceAndBotId(
        namespace: String,
        botId: String,
    ) {
        datasetCol.deleteMany(
            Dataset::namespace eq namespace,
            Dataset::botId eq botId,
        )
        deleteRunsByNamespaceAndBotId(namespace, botId)
    }

    override fun saveRun(run: DatasetRun): DatasetRun {
        datasetRunCol.save(run)
        return run
    }

    override fun claimNextQueuedRun(): DatasetRun? {
        return datasetRunCol.findOneAndUpdate(
            DatasetRun::state eq DatasetRunState.QUEUED,
            set(DatasetRun::state.name, DatasetRunState.RUNNING),
            FindOneAndUpdateOptions()
                .sort(ascending(DatasetRun::startTime.name))
                .returnDocument(ReturnDocument.AFTER),
        )
    }

    override fun getRunById(id: Id<DatasetRun>): DatasetRun? {
        return datasetRunCol.findOneById(id)
    }

    override fun getRunsByDatasetId(datasetId: Id<Dataset>): List<DatasetRun> {
        return datasetRunCol.find(DatasetRun::datasetId eq datasetId)
            .descendingSort(DatasetRun::startTime)
            .toList()
    }

    override fun getActiveRunsByDatasetId(datasetId: Id<Dataset>): List<DatasetRun> {
        return datasetRunCol.find(
            DatasetRun::datasetId eq datasetId,
            DatasetRun::state `in` listOf(DatasetRunState.QUEUED, DatasetRunState.RUNNING),
        ).toList()
    }

    override fun deleteRun(id: Id<DatasetRun>) {
        datasetRunCol.deleteOneById(id)
        deleteQuestionResultsByRunId(id)
    }

    override fun deleteRunsByDatasetId(datasetId: Id<Dataset>) {
        val runIds = datasetRunCol.find(DatasetRun::datasetId eq datasetId).toList().map { it._id }
        datasetRunCol.deleteMany(DatasetRun::datasetId eq datasetId)
        if (runIds.isNotEmpty()) {
            datasetRunQuestionResultCol.deleteMany(DatasetRunQuestionResult::runId `in` runIds)
        }
    }

    override fun deleteRunsByNamespaceAndBotId(
        namespace: String,
        botId: String,
    ) {
        val runIds =
            datasetRunCol.find(
                DatasetRun::namespace eq namespace,
                DatasetRun::botId eq botId,
            ).toList().map { it._id }

        datasetRunCol.deleteMany(
            DatasetRun::namespace eq namespace,
            DatasetRun::botId eq botId,
        )

        if (runIds.isNotEmpty()) {
            datasetRunQuestionResultCol.deleteMany(DatasetRunQuestionResult::runId `in` runIds)
        }
    }

    override fun saveQuestionResult(result: DatasetRunQuestionResult): DatasetRunQuestionResult {
        datasetRunQuestionResultCol.save(result)
        return result
    }

    override fun saveQuestionResults(results: List<DatasetRunQuestionResult>) {
        if (results.isEmpty()) {
            return
        }
        results.forEach { datasetRunQuestionResultCol.save(it) }
    }

    override fun getQuestionResultsByRunId(runId: Id<DatasetRun>): List<DatasetRunQuestionResult> {
        return datasetRunQuestionResultCol.find(DatasetRunQuestionResult::runId eq runId).toList()
    }

    override fun deleteQuestionResultsByRunId(runId: Id<DatasetRun>) {
        datasetRunQuestionResultCol.deleteMany(DatasetRunQuestionResult::runId eq runId)
    }

    override fun deleteQuestionResultsByNamespaceAndBotId(
        namespace: String,
        botId: String,
    ) {
        datasetRunQuestionResultCol.deleteMany(
            DatasetRunQuestionResult::namespace eq namespace,
            DatasetRunQuestionResult::botId eq botId,
        )
    }
}
