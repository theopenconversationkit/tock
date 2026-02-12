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

import ai.tock.bot.admin.evaluation.EvaluationSample
import ai.tock.bot.admin.evaluation.EvaluationSampleDAO
import ai.tock.bot.admin.evaluation.EvaluationSampleStatus
import ai.tock.shared.ensureIndex
import ai.tock.shared.error
import mu.KotlinLogging
import org.litote.kmongo.Id
import org.litote.kmongo.and
import org.litote.kmongo.combine
import org.litote.kmongo.deleteOneById
import org.litote.kmongo.descending
import org.litote.kmongo.eq
import org.litote.kmongo.findOneById
import org.litote.kmongo.getCollection
import org.litote.kmongo.save
import org.litote.kmongo.setValue
import org.litote.kmongo.updateOneById
import java.time.Instant

private val logger = KotlinLogging.logger {}

internal object EvaluationSampleMongoDAO : EvaluationSampleDAO {
    internal val col = MongoBotConfiguration.database.getCollection<EvaluationSample>("evaluation_sample")

    init {
        try {
            col.ensureIndex(EvaluationSample::botId, EvaluationSample::namespace, EvaluationSample::status)
            col.ensureIndex(descending(EvaluationSample::creationDate))
            col.ensureIndex(EvaluationSample::status)
        } catch (e: Exception) {
            logger.error(e) { "Error creating indexes for evaluation_sample collection" }
        }
    }

    override fun findByNamespaceAndBotId(
        namespace: String,
        botId: String,
        status: EvaluationSampleStatus?,
    ): List<EvaluationSample> {
        val baseQuery =
            and(
                EvaluationSample::namespace eq namespace,
                EvaluationSample::botId eq botId,
            )
        val query =
            if (status != null) {
                and(baseQuery, EvaluationSample::status eq status)
            } else {
                baseQuery
            }
        return col.find(query)
            .sort(descending(EvaluationSample::creationDate))
            .toList()
    }

    override fun findById(id: Id<EvaluationSample>): EvaluationSample? {
        return col.findOneById(id)
    }

    override fun save(sample: EvaluationSample): EvaluationSample {
        col.save(sample)
        return sample
    }

    override fun updateStatus(
        id: Id<EvaluationSample>,
        status: EvaluationSampleStatus,
        changedBy: String,
        comment: String?,
    ): EvaluationSample? {
        val now = Instant.now()
        col.updateOneById(
            id,
            combine(
                setValue(EvaluationSample::status, status),
                setValue(EvaluationSample::statusChangedBy, changedBy),
                setValue(EvaluationSample::statusChangeDate, now),
                setValue(EvaluationSample::statusComment, comment),
                setValue(EvaluationSample::lastUpdateDate, now),
            ),
        )
        return findById(id)
    }

    override fun delete(id: Id<EvaluationSample>): Boolean {
        return col.deleteOneById(id).deletedCount > 0
    }
}
