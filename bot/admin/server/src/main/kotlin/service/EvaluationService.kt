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
import ai.tock.bot.admin.model.evaluation.ActionRefsResponse
import ai.tock.bot.admin.model.evaluation.CreateEvaluationSampleRequest
import ai.tock.bot.admin.model.evaluation.DialogsResult
import ai.tock.bot.admin.model.evaluation.EvaluationSampleDTO
import ai.tock.bot.engine.user.PlayerType
import ai.tock.shared.exception.rest.BadRequestException
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
                size = request.requestedDialogCount * 10,
            )

        val dialogResult = dialogReportDAO.search(dialogQuery)
        val totalDialogCount = dialogResult.total.toInt()

        val actionRefs = mutableListOf<ActionRef>()
        val selectedDialogs = mutableSetOf<String>()

        for (dialog in dialogResult.dialogs.shuffled()) {
            if (selectedDialogs.size >= request.requestedDialogCount) break

            val botActions = dialog.actions.filter { it.playerId.type == PlayerType.bot }
            if (botActions.isNotEmpty()) {
                selectedDialogs.add(dialog.id.toString())
                botActions.forEach { action ->
                    actionRefs.add(
                        ActionRef(
                            dialogId = dialog.id.toString(),
                            actionId = action.id.toString(),
                        ),
                    )
                }
            }
        }

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

    fun getActionRefs(
        sampleId: String,
        start: Int,
        size: Int,
        status: EvaluationStatus? = null,
    ): ActionRefsResponse {
        val sample =
            evaluationSampleDAO.findById(sampleId.toId())
                ?: throw BadRequestException("Evaluation sample not found: $sampleId")

        val evaluations =
            evaluationDAO.findByEvaluationSampleId(
                sampleId = sample._id,
                start = start,
                size = size,
                status = status,
            )

        val total =
            if (status != null) {
                evaluationDAO.countByStatus(sample._id)[status] ?: 0L
            } else {
                evaluationDAO.countByEvaluationSampleId(sample._id)
            }

        val dialogIds = evaluations.map { it.dialogId }.distinct()
        val foundDialogs =
            dialogIds.mapNotNull { dialogId ->
                dialogReportDAO.getDialog(dialogId.toId())
            }
        val foundDialogIds = foundDialogs.map { it.id.toString() }.toSet()

        val missingRefs =
            evaluations
                .filter { it.dialogId !in foundDialogIds }
                .map { ActionRef(it.dialogId, it.actionId) }

        val actionRefsWithEvaluation =
            evaluations.map { evaluation ->
                ActionRefWithEvaluation(
                    dialogId = evaluation.dialogId,
                    actionId = evaluation.actionId,
                    evaluation = evaluation,
                )
            }

        return ActionRefsResponse(
            start = start,
            end = start + evaluations.size,
            total = total,
            actionRefs = actionRefsWithEvaluation,
            dialogs =
                DialogsResult(
                    found = foundDialogs,
                    missing = missingRefs,
                ),
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
                ?: throw BadRequestException("Evaluation sample not found: $sampleId")

        if (sample.status != EvaluationSampleStatus.IN_PROGRESS) {
            throw BadRequestException("Cannot evaluate: sample status is ${sample.status}")
        }

        val evaluation =
            evaluationDAO.findById(evaluationId.toId())
                ?: throw BadRequestException("Evaluation not found: $evaluationId")

        if (evaluation.evaluationSampleId != sample._id) {
            throw BadRequestException("Evaluation $evaluationId does not belong to sample $sampleId")
        }

        if (status == EvaluationStatus.UNSET) {
            throw BadRequestException("Cannot set evaluation status to UNSET")
        }

        if (status == EvaluationStatus.UP && reason != null) {
            throw BadRequestException("Reason must be null for UP evaluation")
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
                ?: throw BadRequestException("Evaluation sample not found: $sampleId")

        if (sample.status != EvaluationSampleStatus.IN_PROGRESS) {
            throw BadRequestException("Cannot change status: sample is already ${sample.status}")
        }

        if (targetStatus == EvaluationSampleStatus.IN_PROGRESS) {
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
