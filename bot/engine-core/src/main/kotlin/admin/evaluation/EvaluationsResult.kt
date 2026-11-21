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

/**
 * Computed evaluation statistics for an evaluation sample.
 * This is calculated on-the-fly and not persisted in the database.
 */
data class EvaluationsResult(
    val total: Int,
    val evaluated: Int,
    val remaining: Int,
    val positiveCount: Int,
    val negativeCount: Int,
) {
    companion object {
        fun fromStatusCounts(counts: Map<EvaluationStatus, Long>): EvaluationsResult {
            val unsetCount = counts[EvaluationStatus.UNSET] ?: 0L
            val upCount = counts[EvaluationStatus.UP] ?: 0L
            val downCount = counts[EvaluationStatus.DOWN] ?: 0L
            val total = (unsetCount + upCount + downCount).toInt()
            val evaluated = (upCount + downCount).toInt()

            return EvaluationsResult(
                total = total,
                evaluated = evaluated,
                remaining = unsetCount.toInt(),
                positiveCount = upCount.toInt(),
                negativeCount = downCount.toInt(),
            )
        }
    }
}
