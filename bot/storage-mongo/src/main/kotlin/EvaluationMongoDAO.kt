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

import ai.tock.bot.admin.evaluation.Evaluation
import ai.tock.bot.admin.evaluation.EvaluationDAO
import ai.tock.bot.admin.evaluation.EvaluationSample
import ai.tock.bot.admin.evaluation.EvaluationStatus
import ai.tock.shared.ensureIndex
import ai.tock.shared.ensureUniqueIndex
import ai.tock.shared.error
import mu.KotlinLogging
import org.litote.kmongo.Id
import org.litote.kmongo.aggregate
import org.litote.kmongo.and
import org.litote.kmongo.descending
import org.litote.kmongo.eq
import org.litote.kmongo.find
import org.litote.kmongo.findOneById
import org.litote.kmongo.getCollection
import org.litote.kmongo.group
import org.litote.kmongo.limit
import org.litote.kmongo.match
import org.litote.kmongo.save
import org.litote.kmongo.skip
import org.litote.kmongo.sum

private val logger = KotlinLogging.logger {}

internal object EvaluationMongoDAO : EvaluationDAO {
    internal val col = MongoBotConfiguration.database.getCollection<Evaluation>("evaluation")

    init {
        try {
            col.ensureIndex(Evaluation::evaluationSampleId)
            col.ensureUniqueIndex(
                Evaluation::evaluationSampleId,
                Evaluation::dialogId,
                Evaluation::actionId,
            )
            col.ensureIndex(Evaluation::status)
            col.ensureIndex(descending(Evaluation::creationDate))
        } catch (e: Exception) {
            logger.error(e) { "Error creating indexes for evaluation collection" }
        }
    }

    override fun createAll(evaluations: List<Evaluation>) {
        if (evaluations.isNotEmpty()) {
            col.insertMany(evaluations)
        }
    }

    override fun findByEvaluationSampleId(
        sampleId: Id<EvaluationSample>,
        start: Int,
        size: Int,
        status: EvaluationStatus?,
    ): List<Evaluation> {
        val baseQuery = Evaluation::evaluationSampleId eq sampleId
        val query =
            if (status != null) {
                and(baseQuery, Evaluation::status eq status)
            } else {
                baseQuery
            }
        return col.find(query)
            .skip(start)
            .limit(size)
            .sort(descending(Evaluation::creationDate))
            .toList()
    }

    override fun countByEvaluationSampleId(sampleId: Id<EvaluationSample>): Long {
        return col.countDocuments(Evaluation::evaluationSampleId eq sampleId)
    }

    override fun findById(id: Id<Evaluation>): Evaluation? {
        return col.findOneById(id)
    }

    override fun update(evaluation: Evaluation): Evaluation? {
        col.save(evaluation)
        return findById(evaluation._id)
    }

    override fun countByStatus(sampleId: Id<EvaluationSample>): Map<EvaluationStatus, Long> {
        data class StatusCount(val _id: EvaluationStatus, val count: Long)

        val results =
            col.aggregate<StatusCount>(
                match(Evaluation::evaluationSampleId eq sampleId),
                group(Evaluation::status, StatusCount::count sum 1),
            ).toList()

        return EvaluationStatus.entries.associateWith { status ->
            results.find { it._id == status }?.count ?: 0L
        }
    }

    override fun deleteByEvaluationSampleId(sampleId: Id<EvaluationSample>): Long {
        return col.deleteMany(Evaluation::evaluationSampleId eq sampleId).deletedCount
    }
}
