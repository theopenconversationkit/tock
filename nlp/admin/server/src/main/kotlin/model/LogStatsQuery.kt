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

package ai.tock.nlp.admin.model

import ai.tock.nlp.front.shared.config.ApplicationDefinition
import ai.tock.nlp.front.shared.monitoring.ParseRequestLogStatQuery

/**
 *
 */
data class LogStatsQuery(
    val intent: String?,
    val minOccurrences: Int?,
    val onlyCurrentLocale: Boolean = false,
) : ApplicationScopedQuery() {
    fun toStatQuery(application: ApplicationDefinition): ParseRequestLogStatQuery {
        return ParseRequestLogStatQuery(
            application._id,
            if (onlyCurrentLocale) currentLanguage else null,
            intent,
            minOccurrences,
        )
    }
}
