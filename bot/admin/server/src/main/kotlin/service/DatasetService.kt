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

package ai.tock.bot.admin.service

import ai.tock.bot.admin.bot.BotApplicationConfiguration
import ai.tock.bot.admin.bot.BotApplicationConfigurationDAO
import ai.tock.bot.admin.bot.rag.BotRAGConfigurationDAO
import ai.tock.bot.admin.dataset.Dataset
import ai.tock.bot.admin.dataset.DatasetDAO
import ai.tock.bot.admin.dataset.DatasetQuestion
import ai.tock.bot.admin.dataset.DatasetRun
import ai.tock.bot.admin.dataset.DatasetRunDAO
import ai.tock.bot.admin.dataset.DatasetRunQuestionResult
import ai.tock.bot.admin.dataset.DatasetRunQuestionResultState
import ai.tock.bot.admin.dataset.DatasetRunState
import ai.tock.bot.admin.dialog.ActionReport
import ai.tock.bot.admin.dialog.DialogReport
import ai.tock.bot.admin.dialog.DialogReportDAO
import ai.tock.bot.admin.dialog.DialogReportQuery
import ai.tock.bot.admin.model.dataset.DatasetCreateRequest
import ai.tock.bot.admin.model.dataset.DatasetDTO
import ai.tock.bot.admin.model.dataset.DatasetQuestionDTO
import ai.tock.bot.admin.model.dataset.DatasetQuestionRequest
import ai.tock.bot.admin.model.dataset.DatasetRunActionDTO
import ai.tock.bot.admin.model.dataset.DatasetRunActionState
import ai.tock.bot.admin.model.dataset.DatasetRunCreateRequest
import ai.tock.bot.admin.model.dataset.DatasetRunDTO
import ai.tock.bot.admin.model.dataset.DatasetRunStatsDTO
import ai.tock.bot.admin.model.dataset.DatasetUpdateRequest
import ai.tock.bot.admin.model.genai.BotRAGConfigurationDTO
import ai.tock.bot.admin.test.TestTalkService
import ai.tock.bot.connector.ConnectorType
import ai.tock.bot.engine.action.Action
import ai.tock.bot.engine.dialog.Dialog
import ai.tock.bot.engine.event.EventType
import ai.tock.bot.engine.user.PlayerId
import ai.tock.bot.engine.user.PlayerType
import ai.tock.shared.Dice
import ai.tock.shared.injector
import ai.tock.shared.provide
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.litote.kmongo.Id
import org.litote.kmongo.toId
import java.time.Instant
import java.util.Locale

object DatasetService {
    private val datasetDAO: DatasetDAO get() = injector.provide()
    private val datasetRunDAO: DatasetRunDAO get() = injector.provide()
    private val applicationConfigurationDAO: BotApplicationConfigurationDAO get() = injector.provide()
    private val ragConfigurationDAO: BotRAGConfigurationDAO get() = injector.provide()
    private val dialogReportDAO: DialogReportDAO get() = injector.provide()
    private val objectMapper = jacksonObjectMapper()

    fun listDatasets(
        namespace: String,
        botId: String,
    ): List<DatasetDTO> =
        datasetDAO.getDatasetsByNamespaceAndBotId(namespace, botId)
            .map { dataset ->
                val runs = datasetRunDAO.getRunsByDatasetId(dataset._id)
                dataset.toDTO(
                    runs = runs,
                    includeSettingsSnapshot = false,
                )
            }

    fun getDataset(
        namespace: String,
        botId: String,
        datasetId: String,
    ): DatasetDTO {
        val dataset = getDatasetEntity(namespace, botId, datasetId)
        val runs = datasetRunDAO.getRunsByDatasetId(dataset._id)
        return dataset.toDTO(
            runs = runs,
            includeSettingsSnapshot = true,
        )
    }

    fun createDataset(
        namespace: String,
        botId: String,
        request: DatasetCreateRequest,
        userLogin: String,
    ): DatasetDTO {
        val now = Instant.now()
        val dataset =
            Dataset(
                namespace = namespace,
                botId = botId,
                name = request.name.trim(),
                description = request.description.trim(),
                questions = request.questions.toDomainQuestions(),
                createdAt = now,
                createdBy = userLogin,
            )

        return datasetDAO.save(dataset).toDTO(
            runs = emptyList(),
            includeSettingsSnapshot = false,
        )
    }

    fun createRun(
        namespace: String,
        botId: String,
        datasetId: String,
        request: DatasetRunCreateRequest,
        userLogin: String,
    ): DatasetRunDTO {
        val dataset = getDatasetEntity(namespace, botId, datasetId)
        ensureNoActiveRuns(dataset)

        val testConfiguration = resolveTestRestConfiguration(namespace, botId)
        val now = Instant.now()
        val languageTag = request.language.trim()

        val savedRun =
            datasetRunDAO.saveRun(
                DatasetRun(
                    namespace = namespace,
                    botId = botId,
                    datasetId = dataset._id,
                    state = DatasetRunState.QUEUED,
                    startTime = now,
                    startedBy = userLogin,
                    language = Locale.forLanguageTag(languageTag),
                    botApplicationConfigurationId = testConfiguration._id,
                    settingsSnapshot = buildSettingsSnapshot(namespace, botId),
                ),
            )

        datasetRunDAO.saveQuestionResults(
            dataset.questions.map { question ->
                DatasetRunQuestionResult(
                    namespace = namespace,
                    botId = botId,
                    datasetId = dataset._id,
                    runId = savedRun._id,
                    questionId = question.id,
                    userIdModifier = "dataset_${savedRun._id}_${question.id}",
                )
            },
        )

        val questionResults = datasetRunDAO.getQuestionResultsByRunId(savedRun._id)
        return savedRun.toDTO(
            includeSettingsSnapshot = false,
            stats = questionResults.toStats(),
        )
    }

    fun updateDataset(
        namespace: String,
        botId: String,
        datasetId: String,
        request: DatasetUpdateRequest,
        userLogin: String,
    ): DatasetDTO {
        val dataset = getDatasetEntity(namespace, botId, datasetId)
        ensureNoActiveRuns(dataset)

        val updated =
            dataset.copy(
                name = request.name.trim(),
                description = request.description.trim(),
                questions = request.questions.toDomainQuestions(),
                updatedAt = Instant.now(),
                updatedBy = userLogin,
            )

        return datasetDAO.save(updated).toDTO(
            runs = datasetRunDAO.getRunsByDatasetId(dataset._id),
            includeSettingsSnapshot = false,
        )
    }

    fun deleteDataset(
        namespace: String,
        botId: String,
        datasetId: String,
    ) {
        val dataset = getDatasetEntity(namespace, botId, datasetId)
        ensureNoActiveRuns(dataset)
        datasetDAO.delete(dataset._id)
    }

    fun getRun(
        namespace: String,
        botId: String,
        datasetId: String,
        runId: String,
    ): DatasetRunDTO {
        val dataset = getDatasetEntity(namespace, botId, datasetId)
        val run = getRunEntity(namespace, botId, dataset._id.toString(), runId)
        val questionResults = datasetRunDAO.getQuestionResultsByRunId(run._id)
        return run.toDTO(
            includeSettingsSnapshot = false,
            stats = questionResults.toStats(),
        )
    }

    fun cancelRun(
        namespace: String,
        botId: String,
        datasetId: String,
        runId: String,
    ): DatasetRunDTO {
        val dataset = getDatasetEntity(namespace, botId, datasetId)
        val run = getRunEntity(namespace, botId, dataset._id.toString(), runId)

        if (run.state != DatasetRunState.QUEUED && run.state != DatasetRunState.RUNNING) {
            throw DatasetError.RunStateConflict(runId, run.state)
        }

        val cancelledRun = datasetRunDAO.saveRun(run.copy(state = DatasetRunState.CANCELLED, endTime = Instant.now()))
        val updatedQuestionResults = cancelPendingQuestionResults(cancelledRun._id.toString())

        return cancelledRun.toDTO(
            includeSettingsSnapshot = false,
            stats = updatedQuestionResults.toStats(),
        )
    }

    fun getRunActions(
        namespace: String,
        botId: String,
        datasetId: String,
        runId: String,
    ): List<DatasetRunActionDTO> {
        val dataset = getDatasetEntity(namespace, botId, datasetId)
        val run = getRunEntity(namespace, botId, dataset._id.toString(), runId)

        if (run.state == DatasetRunState.QUEUED || run.state == DatasetRunState.RUNNING) {
            throw DatasetError.RunNotFinished(runId, run.state)
        }

        val questionResults = datasetRunDAO.getQuestionResultsByRunId(run._id)
        val dialogsById =
            dialogReportDAO.findByDialogByIds(questionResults.mapNotNull { it.dialogId }.toSet())
                .associateBy { it.id }

        return questionResults.map { questionResult ->
            val resolvedAction = resolveRunAction(run, questionResult, dialogsById[questionResult.dialogId])
            DatasetRunActionDTO(
                datasetId = run.datasetId.toString(),
                runId = run._id.toString(),
                questionId = questionResult.questionId,
                state = resolvedAction.state,
                action = resolvedAction.action,
                retryCount = questionResult.retryCount,
            )
        }
    }

    private fun getDatasetEntity(
        namespace: String,
        botId: String,
        datasetId: String,
    ): Dataset {
        val id = datasetId.toId<Dataset>()
        val dataset = datasetDAO.getDatasetById(id) ?: throw DatasetError.DatasetNotFound(datasetId)
        if (dataset.namespace != namespace || dataset.botId != botId) {
            throw DatasetError.DatasetNotFound(datasetId)
        }
        return dataset
    }

    private fun getRunEntity(
        namespace: String,
        botId: String,
        datasetId: String,
        runId: String,
    ): DatasetRun {
        val id = runId.toId<DatasetRun>()
        val run = datasetRunDAO.getRunById(id) ?: throw DatasetError.RunNotFound(runId)
        if (run.namespace != namespace || run.botId != botId || run.datasetId.toString() != datasetId) {
            throw DatasetError.RunNotFound(runId)
        }
        return run
    }

    private fun resolveRunAction(
        run: DatasetRun,
        questionResult: DatasetRunQuestionResult,
        cachedDialog: DialogReport?,
    ): ResolvedRunAction =
        when (questionResult.state) {
            DatasetRunQuestionResultState.COMPLETED -> resolveCompletedRunAction(run, questionResult, cachedDialog)
            else -> ResolvedRunAction(DatasetRunActionState.FAILED, null)
        }

    private fun resolveCompletedRunAction(
        run: DatasetRun,
        questionResult: DatasetRunQuestionResult,
        cachedDialog: DialogReport?,
    ): ResolvedRunAction {
        cachedDialog?.let { dialog ->
            val cachedAction = resolveActionFromDialog(dialog, questionResult)
            if (cachedAction != null) {
                cacheActionReferences(questionResult, dialog.id, cachedAction.id)
                return ResolvedRunAction(DatasetRunActionState.COMPLETED, cachedAction)
            }

            if (questionResult.answerActionId != null) {
                return ResolvedRunAction(DatasetRunActionState.COMPLETED, null)
            }

            return ResolvedRunAction(DatasetRunActionState.FAILED, null)
        }

        val searchedDialog =
            findDialogForQuestionResult(run, questionResult)
                ?: return ResolvedRunAction(DatasetRunActionState.COMPLETED, null)
        val searchedAction = resolveActionFromDialog(searchedDialog, questionResult)

        cacheActionReferences(questionResult, searchedDialog.id, searchedAction?.id)

        return if (searchedAction != null) {
            ResolvedRunAction(DatasetRunActionState.COMPLETED, searchedAction)
        } else {
            ResolvedRunAction(DatasetRunActionState.FAILED, null)
        }
    }

    private fun resolveActionFromDialog(
        dialog: DialogReport,
        questionResult: DatasetRunQuestionResult,
    ): ActionReport? {
        questionResult.answerActionId?.let { answerActionId ->
            dialog.actions.firstOrNull { it.id == answerActionId }?.let { return it }
        }

        val userActionIndex = dialog.actions.indexOfFirst { it.id.toString() == questionResult.userActionId }
        if (userActionIndex == -1) {
            return null
        }

        val candidateActions =
            dialog.actions
                .drop(userActionIndex + 1)
                .takeWhile { it.playerId.type != PlayerType.user }
                .filter { it.playerId.type == PlayerType.bot && it.message.eventType.action && it.message.eventType != EventType.debug }

        return candidateActions.firstOrNull { it.message.eventType == EventType.sentenceWithFootnotes }
            ?: candidateActions.firstOrNull { it.message.eventType == EventType.sentence }
            ?: candidateActions.firstOrNull()
    }

    private fun findDialogForQuestionResult(
        run: DatasetRun,
        questionResult: DatasetRunQuestionResult,
    ): DialogReport? {
        val botApplicationConfigurationId = run.botApplicationConfigurationId ?: return null
        val playerId =
            PlayerId(
                TestTalkService.buildTestPlayerId(
                    botApplicationConfigurationId = botApplicationConfigurationId,
                    language = run.language,
                    userIdModifier = buildAttemptUserIdModifier(questionResult),
                ),
            )

        return dialogReportDAO.search(
            DialogReportQuery(
                namespace = run.namespace,
                nlpModel = run.botId,
                start = 0,
                size = 10,
                playerId = playerId,
                displayTests = true,
            ),
        ).dialogs.firstOrNull { dialog ->
            dialog.actions.any { it.id.toString() == questionResult.userActionId }
        }
    }

    private fun cacheActionReferences(
        questionResult: DatasetRunQuestionResult,
        dialogId: Id<Dialog>,
        answerActionId: Id<Action>?,
    ) {
        if (questionResult.dialogId == dialogId && questionResult.answerActionId == answerActionId) {
            return
        }

        datasetRunDAO.saveQuestionResult(
            questionResult.copy(
                dialogId = dialogId,
                answerActionId = answerActionId,
            ),
        )
    }

    private fun buildAttemptUserIdModifier(questionResult: DatasetRunQuestionResult): String =
        if (questionResult.retryCount == 0) {
            questionResult.userIdModifier
        } else {
            "${questionResult.userIdModifier}_retry${questionResult.retryCount}"
        }

    private fun ensureNoActiveRuns(dataset: Dataset) {
        if (datasetRunDAO.getActiveRunsByDatasetId(dataset._id).isNotEmpty()) {
            throw DatasetError.ActiveRunConflict(dataset._id.toString())
        }
    }

    private fun resolveTestRestConfiguration(
        namespace: String,
        botId: String,
    ): BotApplicationConfiguration {
        val candidates =
            applicationConfigurationDAO.getConfigurationsByNamespaceAndBotId(namespace, botId)
                .filter { it.connectorType == ConnectorType.rest }

        return when (candidates.size) {
            0 -> throw DatasetError.InvalidRequest("No REST test configuration found for bot $botId")
            else -> candidates.first()
        }
    }

    private fun buildSettingsSnapshot(
        namespace: String,
        botId: String,
    ): Map<String, Any?> {
        val ragConfiguration = ragConfigurationDAO.findByNamespaceAndBotId(namespace, botId) ?: return emptyMap()

        @Suppress("UNCHECKED_CAST")
        val rawSnapshot = objectMapper.convertValue(BotRAGConfigurationDTO(ragConfiguration), Map::class.java) as Map<String, Any?>
        @Suppress("UNCHECKED_CAST")
        return sanitizeSnapshot(rawSnapshot) as Map<String, Any?>
    }

    private fun sanitizeSnapshot(value: Any?): Any? =
        when (value) {
            is Map<*, *> ->
                value.entries
                    .filter { it.key != "apiKey" }
                    .associate { (key, nestedValue) -> key.toString() to sanitizeSnapshot(nestedValue) }
            is Iterable<*> -> value.map { sanitizeSnapshot(it) }
            else -> value
        }

    private fun cancelPendingQuestionResults(runId: String): List<DatasetRunQuestionResult> {
        val results = datasetRunDAO.getQuestionResultsByRunId(runId.toId<DatasetRun>())
        val updatedResults =
            results.map { result ->
                when (result.state) {
                    DatasetRunQuestionResultState.PENDING,
                    DatasetRunQuestionResultState.RUNNING,
                    -> result.copy(state = DatasetRunQuestionResultState.CANCELLED, endedAt = Instant.now())

                    else -> result
                }
            }

        datasetRunDAO.saveQuestionResults(updatedResults)
        return updatedResults
    }

    private fun List<DatasetQuestionRequest>.toDomainQuestions(): List<DatasetQuestion> =
        map { question ->
            DatasetQuestion(
                id = question.id?.trim()?.takeIf(String::isNotEmpty) ?: Dice.newId(),
                question = question.question.trim(),
                groundTruth = question.groundTruth?.trim()?.takeIf(String::isNotEmpty),
            )
        }

    private fun Dataset.toDTO(
        runs: List<DatasetRun>,
        includeSettingsSnapshot: Boolean,
    ): DatasetDTO =
        DatasetDTO(
            id = _id.toString(),
            name = name,
            description = description,
            questions = questions.map { it.toDTO() },
            runs =
                runs.map { run ->
                    run.toDTO(
                        includeSettingsSnapshot = includeSettingsSnapshot,
                        stats = datasetRunDAO.getQuestionResultsByRunId(run._id).toStats(),
                    )
                },
            createdAt = createdAt,
            createdBy = createdBy,
            updatedAt = updatedAt,
            updatedBy = updatedBy,
        )

    private fun DatasetQuestion.toDTO(): DatasetQuestionDTO =
        DatasetQuestionDTO(
            id = id,
            question = question,
            groundTruth = groundTruth,
        )

    private fun DatasetRun.toDTO(
        includeSettingsSnapshot: Boolean,
        stats: DatasetRunStatsDTO,
    ): DatasetRunDTO =
        DatasetRunDTO(
            id = _id.toString(),
            state = state,
            startTime = startTime,
            endTime = endTime,
            settingsSnapshot = settingsSnapshot.takeIf { includeSettingsSnapshot },
            startedBy = startedBy,
            stats = stats,
        )

    private fun List<DatasetRunQuestionResult>.toStats(): DatasetRunStatsDTO =
        DatasetRunStatsDTO(
            totalQuestions = size,
            completedQuestions = count { it.state == DatasetRunQuestionResultState.COMPLETED },
            failedQuestions = count { it.state == DatasetRunQuestionResultState.FAILED },
        )
}

private data class ResolvedRunAction(
    val state: DatasetRunActionState,
    val action: ActionReport?,
)

sealed class DatasetError(message: String) : RuntimeException(message) {
    class DatasetNotFound(datasetId: String) : DatasetError("Dataset $datasetId not found")

    class RunNotFound(runId: String) : DatasetError("Run $runId not found")

    class ActiveRunConflict(datasetId: String) :
        DatasetError("Dataset $datasetId cannot be modified while a run is QUEUED or RUNNING")

    class RunStateConflict(runId: String, state: DatasetRunState) :
        DatasetError("Run $runId is already $state")

    class RunNotFinished(runId: String, state: DatasetRunState) :
        DatasetError("Run $runId is not yet finished, current state is $state")

    class InvalidRequest(message: String) : DatasetError(message)
}
