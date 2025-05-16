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

package ai.tock.bot.engine.dialog

import java.time.LocalDateTime
import java.time.LocalTime

class FlowAnalyticsQuery(
    val namespace: String,
    val applicationName: String = "",
    val nlpModel: String = "",
    val from: LocalDateTime = LocalDateTime.now().minusDays(7),
    val to: LocalDateTime = LocalDateTime.now()
) {
    fun formatQuery() =
        FlowAnalyticsQuery(
            namespace,
            applicationName,
            nlpModel,
            LocalDateTime.of(from.toLocalDate(), LocalTime.MIDNIGHT),
            LocalDateTime.of(to.toLocalDate(), LocalTime.MAX)
        )
}
