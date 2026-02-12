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

/**
 * DAO for individual evaluations.
 */
interface EvaluationDAO {
    fun createAll(evaluations: List<Evaluation>)

    fun findByEvaluationSampleId(
        sampleId: Id<EvaluationSample>,
        start: Int = 0,
        size: Int = 20,
        status: EvaluationStatus? = null,
    ): List<Evaluation>

    fun countByEvaluationSampleId(sampleId: Id<EvaluationSample>): Long

    fun findById(id: Id<Evaluation>): Evaluation?

    fun update(evaluation: Evaluation): Evaluation?

    fun countByStatus(sampleId: Id<EvaluationSample>): Map<EvaluationStatus, Long>

    fun deleteByEvaluationSampleId(sampleId: Id<EvaluationSample>): Long
}
