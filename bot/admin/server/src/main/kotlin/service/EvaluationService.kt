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

import ai.tock.bot.admin.dialog.DialogReportDAO
import ai.tock.bot.admin.dialog.DialogReportQuery
import ai.tock.bot.admin.evaluation.ActionRef
import ai.tock.bot.admin.evaluation.Evaluation
import ai.tock.bot.admin.evaluation.EvaluationDAO
import ai.tock.bot.admin.evaluation.EvaluationReason
import ai.tock.bot.admin.evaluation.EvaluationSample
import ai.tock.bot.admin.evaluation.EvaluationSampleDAO
import ai.tock.bot.admin.evaluation.EvaluationSampleStatus
import ai.tock.bot.admin.evaluation.EvaluationStatus
import ai.tock.bot.admin.evaluation.EvaluationsResult
import ai.tock.bot.admin.evaluation.Evaluator
import ai.tock.bot.admin.model.evaluation.ActionRefWithEvaluation
import ai.tock.bot.admin.model.evaluation.CreateEvaluationSampleRequest
import ai.tock.bot.admin.model.evaluation.DialogEntry
import ai.tock.bot.admin.model.evaluation.EvaluationDialogsResponse
import ai.tock.bot.admin.model.evaluation.EvaluationSampleDTO
import ai.tock.shared.exception.rest.BadRequestException
import ai.tock.shared.exception.rest.UnprocessableEntityException
import ai.tock.shared.injector
import ai.tock.shared.provide
import mu.KotlinLogging
import org.litote.kmongo.toId
import java.time.Instant
import java.time.ZoneOffset

private val logger = KotlinLogging.logger {}

object EvaluationService {
    private val evaluationSampleDAO: EvaluationSampleDAO get() = injector.provide()
    private val evaluationDAO: EvaluationDAO get() = injector.provide()
    private val dialogReportDAO: DialogReportDAO get() = injector.provide()

    fun findSamplesByBotId(
        namespace: String,
        botId: String,
        status: EvaluationSampleStatus? = null,
    ): List<EvaluationSampleDTO> {
        val samples = evaluationSampleDAO.findByNamespaceAndBotId(namespace, botId, status)
        return samples.map { sample ->
            val statusCounts = evaluationDAO.countByStatus(sample._id)
            val evaluationsResult = EvaluationsResult.fromStatusCounts(statusCounts)
            EvaluationSampleDTO.from(sample, evaluationsResult)
        }
    }

    fun findSampleById(sampleId: String): EvaluationSampleDTO? {
        val sample = evaluationSampleDAO.findById(sampleId.toId()) ?: return null
        val statusCounts = evaluationDAO.countByStatus(sample._id)
        val evaluationsResult = EvaluationsResult.fromStatusCounts(statusCounts)
        return EvaluationSampleDTO.from(sample, evaluationsResult)
    }

    fun createEvaluationSample(
        namespace: String,
        botId: String,
        request: CreateEvaluationSampleRequest,
        createdBy: String,
    ): EvaluationSampleDTO {
        logger.info { "Creating evaluation sample for bot $botId in namespace $namespace" }

        val dialogQuery =
            DialogReportQuery(
                namespace = namespace,
                nlpModel = botId,
                dialogActivityFrom = request.dialogActivityFrom.atZone(ZoneOffset.UTC),
                dialogActivityTo = request.dialogActivityTo.atZone(ZoneOffset.UTC),
                displayTests = request.allowTestDialogs,
                withAnnotations = false,
                start = 0L,
                size = request.requestedDialogCount,
                random = true,
                evaluableActionsOnly = true,
            )

        val dialogResult = dialogReportDAO.search(dialogQuery)
        val totalDialogCount = dialogResult.total.toInt()

        val actionRefs =
            dialogResult.dialogs.flatMap { dialog ->
                dialog.actions.map { action ->
                    ActionRef(
                        dialogId = dialog.id,
                        actionId = action.id,
                    )
                }
            }

        if (actionRefs.isEmpty()) {
            throw UnprocessableEntityException(
                errorCode = 4221,
                message = "No dialog matching query",
            )
        }

        val selectedDialogs = actionRefs.map { it.dialogId }.distinct()
        val now = Instant.now()
        val sample =
            EvaluationSample(
                botId = botId,
                namespace = namespace,
                name = request.name,
                description = request.description,
                dialogActivityFrom = request.dialogActivityFrom,
                dialogActivityTo = request.dialogActivityTo,
                requestedDialogCount = request.requestedDialogCount,
                dialogsCount = selectedDialogs.size,
                totalDialogCount = totalDialogCount,
                botActionCount = actionRefs.size,
                allowTestDialogs = request.allowTestDialogs,
                actionRefs = actionRefs,
                status = EvaluationSampleStatus.IN_PROGRESS,
                createdBy = createdBy,
                creationDate = now,
                statusChangedBy = createdBy,
                statusChangeDate = now,
                lastUpdateDate = now,
            )

        val savedSample = evaluationSampleDAO.save(sample)

        val evaluations =
            actionRefs.map { ref ->
                Evaluation(
                    evaluationSampleId = savedSample._id,
                    dialogId = ref.dialogId,
                    actionId = ref.actionId,
                    status = EvaluationStatus.UNSET,
                    creationDate = now,
                    lastUpdateDate = now,
                )
            }
        evaluationDAO.createAll(evaluations)

        logger.info { "Created evaluation sample ${savedSample._id} with ${actionRefs.size} bot actions from ${selectedDialogs.size} dialogs" }

        val evaluationsResult =
            EvaluationsResult(
                total = actionRefs.size,
                evaluated = 0,
                remaining = actionRefs.size,
                positiveCount = 0,
                negativeCount = 0,
            )

        return EvaluationSampleDTO.from(savedSample, evaluationsResult)
    }

    /**
     * Returns paginated evaluation dialogs for a sample.
     * Dialogs are sorted by dialog id (ascending). Each page contains action refs with their evaluations.
     */
    fun getEvaluationDialogs(
        sampleId: String,
        start: Int,
        size: Int,
    ): EvaluationDialogsResponse {
        val sample =
            evaluationSampleDAO.findById(sampleId.toId())
                ?: run {
                    logger.error { "Evaluation sample not found for getEvaluationDialogs: sampleId=$sampleId" }
                    throw BadRequestException("Evaluation sample not found: $sampleId")
                }
        val groupedEvaluations =
            evaluationDAO.findGroupedEvaluationsBySampleId(
                sampleId = sample._id,
                start = start,
                size = size,
            )

        val pageDialogIds = groupedEvaluations.map { it._id }
        val foundDialogs = dialogReportDAO.findByDialogByIds(pageDialogIds.toSet())
        val dialogsById = foundDialogs.associateBy { it.id }
        val evaluations = groupedEvaluations.flatMap { it.evaluations }
        val actionRefsWithEvaluation =
            evaluations.map { eval ->
                ActionRefWithEvaluation(
                    dialogId = eval.dialogId.toString(),
                    actionId = eval.actionId.toString(),
                    evaluation = eval,
                )
            }

        val dialogsOrdered =
            pageDialogIds.map { dialogId ->
                DialogEntry(
                    dialogId = dialogId.toString(),
                    dialog = dialogsById[dialogId],
                )
            }

        return EvaluationDialogsResponse(
            start = start,
            size = actionRefsWithEvaluation.size,
            end = start + dialogsOrdered.size,
            total = sample.dialogsCount.toLong(),
            actionRefs = actionRefsWithEvaluation,
            dialogs = dialogsOrdered,
        )
    }

    fun evaluate(
        sampleId: String,
        evaluationId: String,
        status: EvaluationStatus,
        reason: EvaluationReason?,
        evaluatorId: String,
    ): Evaluation? {
        val sample =
            evaluationSampleDAO.findById(sampleId.toId())
                ?: run {
                    logger.error { "Evaluation sample not found: sampleId=$sampleId, evaluationId=$evaluationId" }
                    throw BadRequestException("Evaluation sample not found: $sampleId")
                }

        if (sample.status != EvaluationSampleStatus.IN_PROGRESS) {
            logger.error { "Cannot evaluate: sample status is ${sample.status}, sampleId=$sampleId, evaluationId=$evaluationId" }
            throw BadRequestException("Cannot evaluate: sample status is ${sample.status}")
        }

        val evaluation =
            evaluationDAO.findById(evaluationId.toId())
                ?: run {
                    logger.error { "Evaluation not found: sampleId=$sampleId, evaluationId=$evaluationId" }
                    throw BadRequestException("Evaluation not found: $evaluationId")
                }

        if (evaluation.evaluationSampleId != sample._id) {
            logger.error { "Evaluation does not belong to sample: sampleId=$sampleId, evaluationId=$evaluationId" }
            throw BadRequestException("Evaluation $evaluationId does not belong to sample $sampleId")
        }

        if (status == EvaluationStatus.UNSET) {
            logger.error { "Cannot set evaluation status to UNSET: sampleId=$sampleId, evaluationId=$evaluationId" }
            throw BadRequestException("Cannot set evaluation status to UNSET")
        }

        if (status == EvaluationStatus.UP && reason != null) {
            logger.error { "Reason must be null for UP evaluation: sampleId=$sampleId, evaluationId=$evaluationId" }
            throw BadRequestException("Reason must be null for UP evaluation")
        }

        if (status == EvaluationStatus.DOWN && reason == null) {
            logger.error { "Reason is required for DOWN evaluation: sampleId=$sampleId, evaluationId=$evaluationId" }
            throw BadRequestException("Reason is required for DOWN evaluation")
        }

        val now = Instant.now()
        val updated =
            evaluation.copy(
                status = status,
                reason = reason,
                evaluator = Evaluator(evaluatorId),
                evaluationDate = now,
                lastUpdateDate = now,
            )

        return evaluationDAO.update(updated)
    }

    fun changeStatus(
        sampleId: String,
        targetStatus: EvaluationSampleStatus,
        changedBy: String,
        comment: String?,
    ): EvaluationSampleDTO? {
        val sample =
            evaluationSampleDAO.findById(sampleId.toId())
                ?: run {
                    logger.error { "Evaluation sample not found for changeStatus: sampleId=$sampleId" }
                    throw BadRequestException("Evaluation sample not found: $sampleId")
                }

        if (sample.status != EvaluationSampleStatus.IN_PROGRESS) {
            logger.error { "Cannot change status: sample is already ${sample.status}, sampleId=$sampleId" }
            throw BadRequestException("Cannot change status: sample is already ${sample.status}")
        }

        if (targetStatus == EvaluationSampleStatus.IN_PROGRESS) {
            logger.error { "Cannot change status to IN_PROGRESS: sampleId=$sampleId" }
            throw BadRequestException("Cannot change status to IN_PROGRESS")
        }

        val updatedSample =
            evaluationSampleDAO.updateStatus(
                id = sample._id,
                status = targetStatus,
                changedBy = changedBy,
                comment = comment,
            ) ?: return null

        val statusCounts = evaluationDAO.countByStatus(updatedSample._id)
        val evaluationsResult = EvaluationsResult.fromStatusCounts(statusCounts)

        return EvaluationSampleDTO.from(updatedSample, evaluationsResult)
    }

    fun deleteSample(sampleId: String): Boolean {
        val id = sampleId.toId<EvaluationSample>()
        evaluationDAO.deleteByEvaluationSampleId(id)
        return evaluationSampleDAO.delete(id)
    }
}
