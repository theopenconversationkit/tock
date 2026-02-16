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
 * An individual evaluation of a bot action.
 * Created with status UNSET when the evaluation sample is created.
 */
data class Evaluation(
    val _id: Id<Evaluation> = newId(),
    val evaluationSampleId: Id<EvaluationSample>,
    val dialogId: String,
    val actionId: String,
    val status: EvaluationStatus = EvaluationStatus.UNSET,
    val reason: EvaluationReason? = null,
    val evaluator: Evaluator? = null,
    val evaluationDate: Instant? = null,
    val creationDate: Instant = Instant.now(),
    val lastUpdateDate: Instant = Instant.now(),
)
