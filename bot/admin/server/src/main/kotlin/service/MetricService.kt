/*
 * Copyright (C) 2017/2023 e-voyageurs technologies
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

import ai.tock.bot.admin.indicators.metric.CustomMetric
import ai.tock.bot.admin.indicators.metric.MetricFilter
import ai.tock.bot.admin.indicators.metric.MetricGroupBy
import ai.tock.bot.admin.indicators.metric.MetricDAO
import ai.tock.bot.admin.model.indicator.metric.toMetricResponse
import ai.tock.shared.injector
import ai.tock.shared.provide
import com.github.salomonbrys.kodein.instance
import model.indicator.metric.CustomMetricResponse

object MetricService {

    private val metricDao: MetricDAO get() = injector.provide()

    /**
     * Retrieve all [CustomMetric] by filtering and grouping data
     * @param filter [MetricFilter] the expected indicator name
     * @param groupBy [MetricGroupBy] name of the application which the indicator is linked to
     * @return List<[CustomMetricResponse]>
     */
    fun filterAndGroupBy(filter: MetricFilter, groupBy: List<MetricGroupBy>): List<CustomMetricResponse> =
        metricDao.filterAndGroupBy(filter, groupBy).map { toMetricResponse(it) }

}