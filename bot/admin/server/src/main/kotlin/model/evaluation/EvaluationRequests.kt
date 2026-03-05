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

package ai.tock.bot.admin.model.evaluation

import ai.tock.bot.admin.dialog.DialogReport
import ai.tock.bot.admin.evaluation.ActionRef
import ai.tock.bot.admin.evaluation.Evaluation
import ai.tock.bot.admin.evaluation.EvaluationReason
import ai.tock.bot.admin.evaluation.EvaluationSample
import ai.tock.bot.admin.evaluation.EvaluationSampleStatus
import ai.tock.bot.admin.evaluation.EvaluationStatus
import ai.tock.bot.admin.evaluation.EvaluationsResult
import java.time.Instant

/**
 * Request to create a new evaluation sample.
 */
data class CreateEvaluationSampleRequest(
    val name: String?,
    val description: String?,
    val dialogActivityFrom: Instant,
    val dialogActivityTo: Instant,
    val requestedDialogCount: Int,
    val allowTestDialogs: Boolean = false,
)

/**
 * Query for paginated evaluation dialogs.
 * Pagination is by dialog (dialogs are sorted by dialog id, ascending).
 * POST body follows Tock convention for search endpoints.
 *
 * @param start Offset (index of first dialog), default 0
 * @param size Number of dialogs per page, default 20
 */
data class EvaluationDialogsQuery(
    val start: Int = 0,
    val size: Int = 10,
)

/**
 * Response for paginated evaluation dialogs.
 * Contains action refs with evaluations for the requested page of dialogs,
 * plus the corresponding dialog reports.
 *
 * @param start Requested offset
 * @param size Number of action refs returned (one per evaluable action in the page)
 * @param total Total number of dialogs in the sample
 * @param actionRefs Action refs with their evaluations for the current page
 * @param dialogs Found dialog reports and missing action refs
 */
data class EvaluationDialogsResponse(
    val start: Int,
    val size: Int,
    /**
     *  exclusive. start + dialogs.size()
     */
    val end: Int,
    val total: Long,
    val actionRefs: List<ActionRefWithEvaluation>,
    val dialogs: List<DialogEntry>,
)

/**
 * An action ref with its associated evaluation.
 */
data class ActionRefWithEvaluation(
    val dialogId: String,
    val actionId: String,
    val evaluation: Evaluation,
)

data class DialogEntry(
    val dialogId: String,
    val dialog: DialogReport?,
)

/**
 * Result containing found and missing dialogs.
 */
data class DialogsResult(
    val found: List<DialogReport>,
    val missing: List<ActionRef>,
)

/**
 * Request to evaluate an action (PATCH).
 */
data class EvaluateRequest(
    val status: EvaluationStatus,
    val reason: EvaluationReason? = null,
)

/**
 * Request to change the status of an evaluation sample.
 */
data class ChangeStatusRequest(
    val targetStatus: EvaluationSampleStatus,
    val comment: String? = null,
)

/**
 * DTO for evaluation sample with computed evaluationsResult.
 */
data class EvaluationSampleDTO(
    val _id: String,
    val botId: String,
    val namespace: String,
    val name: String?,
    val description: String?,
    val dialogActivityFrom: Instant,
    val dialogActivityTo: Instant,
    val requestedDialogCount: Int,
    val dialogsCount: Int,
    val totalDialogCount: Int,
    val botActionCount: Int,
    val allowTestDialogs: Boolean,
    val status: EvaluationSampleStatus,
    val createdBy: String,
    val creationDate: Instant,
    val statusChangedBy: String,
    val statusChangeDate: Instant,
    val statusComment: String?,
    val lastUpdateDate: Instant,
    val evaluationsResult: EvaluationsResult,
) {
    companion object {
        fun from(
            sample: EvaluationSample,
            evaluationsResult: EvaluationsResult,
        ): EvaluationSampleDTO {
            return EvaluationSampleDTO(
                _id = sample._id.toString(),
                botId = sample.botId,
                namespace = sample.namespace,
                name = sample.name,
                description = sample.description,
                dialogActivityFrom = sample.dialogActivityFrom,
                dialogActivityTo = sample.dialogActivityTo,
                requestedDialogCount = sample.requestedDialogCount,
                dialogsCount = sample.dialogsCount,
                totalDialogCount = sample.totalDialogCount,
                botActionCount = sample.botActionCount,
                allowTestDialogs = sample.allowTestDialogs,
                status = sample.status,
                createdBy = sample.createdBy,
                creationDate = sample.creationDate,
                statusChangedBy = sample.statusChangedBy,
                statusChangeDate = sample.statusChangeDate,
                statusComment = sample.statusComment,
                lastUpdateDate = sample.lastUpdateDate,
                evaluationsResult = evaluationsResult,
            )
        }
    }
}
