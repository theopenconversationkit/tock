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

package ai.tock.bot.admin.indicators.metric

/**
 * DAO to manage [Metric] entity
 */
interface MetricDAO {
    /**
     * Save a metric.
     * @param metric the metric to save
     */
    fun save(metric: Metric)

    /**
     * Save metrics.
     * @param metrics the metrics to save
     */
    fun saveAll(metrics: List<Metric>)

    /**
     * Find all metrics by bot id
     * @param botId the bot id
     */
    fun findAllByBotId(botId: String): List<Metric>

    /**
     * Find all metrics by filtering and grouping data
     * @param filter the [MetricFilter]
     * @param groupBy list of [MetricGroupBy]
     */
    fun filterAndGroupBy(filter: MetricFilter, groupBy: List<MetricGroupBy>): List<CustomMetric>

}
