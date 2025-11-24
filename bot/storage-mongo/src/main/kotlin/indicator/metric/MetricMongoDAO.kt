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

package indicator.metric

import ai.tock.bot.admin.indicators.metric.CustomMetric
import ai.tock.bot.admin.indicators.metric.Metric
import ai.tock.bot.admin.indicators.metric.MetricDAO
import ai.tock.bot.admin.indicators.metric.MetricFilter
import ai.tock.bot.admin.indicators.metric.MetricGroupBy
import ai.tock.bot.mongo.MongoBotConfiguration
import org.bson.Document
import org.bson.conversions.Bson
import org.litote.kmongo.aggregate
import org.litote.kmongo.and
import org.litote.kmongo.deleteMany
import org.litote.kmongo.eq
import org.litote.kmongo.first
import org.litote.kmongo.getCollectionOfName
import org.litote.kmongo.group
import org.litote.kmongo.gte
import org.litote.kmongo.`in`
import org.litote.kmongo.lte
import org.litote.kmongo.match
import org.litote.kmongo.sum

object MetricMongoDAO : MetricDAO {
    internal val col = MongoBotConfiguration.database.getCollectionOfName<Metric>("metric")

    override fun save(metric: Metric) {
        col.insertOne(metric)
    }

    override fun saveAll(metrics: List<Metric>) {
        col.insertMany(metrics)
    }

    override fun findAllByBotId(
        namespace: String,
        botId: String,
    ): List<Metric> =
        col.find(
            and(
                Metric::namespace eq namespace,
                Metric::botId eq botId,
            ),
        ).toList()

    override fun filterAndGroupBy(
        filter: MetricFilter,
        groupBy: List<MetricGroupBy>,
    ): List<CustomMetric> {
        return col.aggregate<CustomMetric>(
            match(
                and(
                    filter.listOfNotNull(),
                ),
            ),
            group(
                id =
                    with(groupBy) {
                        if (isEmpty()) {
                            Metric::_id
                        } else {
                            Document(
                                "_id",
                                listOfNotNull(
                                    if (contains(MetricGroupBy.APPLICATION_ID)) "\$applicationId" else null,
                                    if (contains(MetricGroupBy.TYPE)) "\$type" else null,
                                    if (contains(MetricGroupBy.EMITTER_STORY_ID)) "\$emitterStoryId" else null,
                                    if (contains(MetricGroupBy.TRACKED_STORY_ID)) "\$trackedStoryId" else null,
                                    if (contains(MetricGroupBy.INDICATOR_NAME)) "\$indicatorName" else null,
                                    if (contains(MetricGroupBy.INDICATOR_VALUE_NAME)) "\$indicatorValueName" else null,
                                ).joinToString(":_:").split(":"),
                            )
                        }
                    },
                fieldAccumulators =
                    with(groupBy) {
                        listOfNotNull(
                            if (isEmpty()) CustomMetric::id first Metric::_id else null,
                            if (contains(MetricGroupBy.APPLICATION_ID)) CustomMetric::applicationId first Metric::applicationId else null,
                            if (contains(MetricGroupBy.TYPE)) CustomMetric::type first Metric::type else null,
                            if (contains(MetricGroupBy.EMITTER_STORY_ID)) CustomMetric::emitterStoryId first Metric::emitterStoryId else null,
                            if (contains(MetricGroupBy.TRACKED_STORY_ID)) CustomMetric::trackedStoryId first Metric::trackedStoryId else null,
                            if (contains(MetricGroupBy.INDICATOR_NAME)) CustomMetric::indicatorName first Metric::indicatorName else null,
                            if (contains(MetricGroupBy.INDICATOR_VALUE_NAME)) CustomMetric::indicatorValueName first Metric::indicatorValueName else null,
                            CustomMetric::count sum 1,
                        )
                    },
            ),
        ).toList()
    }

    /**
     * Create not null filters
     */
    private fun MetricFilter.listOfNotNull(): List<Bson> =
        listOfNotNull(
            namespace?.let { Metric::namespace eq it },
            botId?.let { Metric::botId eq it },
            types?.let { Metric::type `in` it },
            emitterStoryIds?.let { Metric::emitterStoryId `in` it },
            trackedStoryIds?.let { Metric::trackedStoryId `in` it },
            indicatorNames?.let { Metric::indicatorName `in` it },
            indicatorValueNames?.let { Metric::indicatorValueName `in` it },
            creationDateSince?.let { Metric::creationDate gte it },
            creationDateUntil?.let { Metric::creationDate lte it },
        )

    override fun deleteByApplicationName(
        namespace: String,
        botId: String,
    ): Boolean =
        col.deleteMany(
            Metric::namespace eq namespace,
            Metric::botId eq botId,
        ).deletedCount > 0
}
