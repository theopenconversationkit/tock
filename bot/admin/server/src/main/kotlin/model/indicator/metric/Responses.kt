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

package model.indicator.metric

/**
 * Custom Metric Response, grouped by config type
 */
data class CustomMetricGroupResponse(
    val test: List<CustomMetricResponse>,
    val prod: List<CustomMetricResponse>
)

/**
 * Custom Metric Response
 */
data class CustomMetricResponse(
    val row: Row, val count: Int
)

/**
 * Aggregation data attributes
 */
data class Row(
    val id: String?,
    val applicationId: String?,
    val type: String?,
    val emitterStoryId: String?,
    val trackedStoryId: String?,
    val indicatorName: String?,
    val indicatorValueName: String?,
)
