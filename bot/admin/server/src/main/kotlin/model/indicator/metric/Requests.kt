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

package ai.tock.bot.admin.model.indicator.metric

import ai.tock.bot.admin.indicators.metric.MetricFilter
import ai.tock.bot.admin.indicators.metric.MetricGroupBy

/**
 * Custom Metric Request
 */
data class Requests(
    val filter: MetricFilter? = null, val groupBy: List<MetricGroupBy>
)