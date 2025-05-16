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

package ai.tock.bot.admin.model

import ai.tock.bot.admin.user.AnalyticsQuery
import ai.tock.bot.admin.user.UserReportQuery
import ai.tock.nlp.admin.model.PaginatedQuery
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZonedDateTime

/**
 *
 */
data class UserSearchQuery(
    val name: String?,
    val from: ZonedDateTime?,
    val to: ZonedDateTime?,
    val flags: Set<String> = emptySet(),
    val displayTests: Boolean = false
) : PaginatedQuery() {

    fun toUserReportQuery(): UserReportQuery {
        return UserReportQuery(
            namespace,
            applicationName,
            currentLanguage,
            start,
            size,
            name,
            from,
            to,
            flags.map { it to null }.toMap(),
            displayTests
        )
    }

    fun toUserAnalyticsQuery(): AnalyticsQuery {
        return AnalyticsQuery(
            namespace,
            applicationName,
            currentLanguage,
            LocalDateTime.of(from?.toLocalDate(), LocalTime.MIDNIGHT),
            LocalDateTime.of(to?.toLocalDate(), LocalTime.MAX)
        )
    }
}
