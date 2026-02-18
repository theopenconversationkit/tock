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

package ai.tock.bot.admin.model.indicator

import ai.tock.bot.admin.indicators.Indicator
import ai.tock.bot.admin.indicators.IndicatorType
import ai.tock.bot.admin.indicators.IndicatorValue

/**
 * Map a [request][SaveIndicatorRequest] to [Indicator]
 * @param namespace the namespace
 * @param botId the application name
 * @param request [SaveIndicatorRequest]
 * @return [Indicator]
 */
fun toIndicator(
    namespace: String,
    botId: String,
    request: SaveIndicatorRequest,
): Indicator {
    return Indicator(
        type = IndicatorType.CUSTOM,
        name = request.name,
        label = request.label,
        description = request.description,
        namespace = namespace,
        botId = botId,
        dimensions = request.dimensions,
        values = request.values.map { IndicatorValue(it.name, it.label) }.toSet(),
    )
}

/**
 * Map an [indicator][Indicator] to [IndicatorResponse]
 * @param indicator indicator
 * @return [IndicatorResponse]
 */
fun toResponse(indicator: Indicator): IndicatorResponse =
    IndicatorResponse(
        id = indicator._id.toString(),
        type = indicator.type,
        name = indicator.name,
        label = indicator.label,
        description = indicator.description,
        applicationName = indicator.botId,
        dimensions = indicator.dimensions,
        values = indicator.values.map { IndicatorValueResponse(it.name, it.label) }.toSet(),
    )
