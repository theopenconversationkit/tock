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

package ai.tock.bot.admin.evaluation

import org.litote.kmongo.Id
import org.litote.kmongo.newId
import java.time.Instant

/**
 * An evaluation sample containing a set of bot actions to evaluate.
 */
data class EvaluationSample(
    val _id: Id<EvaluationSample> = newId(),
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
    val actionRefs: List<ActionRef>,
    val status: EvaluationSampleStatus = EvaluationSampleStatus.IN_PROGRESS,
    val createdBy: String,
    val creationDate: Instant = Instant.now(),
    val statusChangedBy: String,
    val statusChangeDate: Instant = Instant.now(),
    val statusComment: String? = null,
    val lastUpdateDate: Instant = Instant.now(),
)
