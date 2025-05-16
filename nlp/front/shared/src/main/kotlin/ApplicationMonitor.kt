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

package ai.tock.nlp.front.shared

import ai.tock.nlp.front.shared.config.ApplicationDefinition
import ai.tock.nlp.front.shared.monitoring.ParseRequestExportLog
import ai.tock.nlp.front.shared.monitoring.ParseRequestLogIntentStat
import ai.tock.nlp.front.shared.monitoring.ParseRequestLogQuery
import ai.tock.nlp.front.shared.monitoring.ParseRequestLogQueryResult
import ai.tock.nlp.front.shared.monitoring.ParseRequestLogStat
import ai.tock.nlp.front.shared.monitoring.ParseRequestLogStatQuery
import ai.tock.nlp.front.shared.monitoring.ParseRequestLogCountQuery
import ai.tock.nlp.front.shared.monitoring.ParseRequestLogCountQueryResult
import ai.tock.nlp.front.shared.monitoring.UserActionLog
import ai.tock.nlp.front.shared.monitoring.UserActionLogQuery
import ai.tock.nlp.front.shared.monitoring.UserActionLogQueryResult
import org.litote.kmongo.Id
import java.util.Locale

/**
 *
 */
interface ApplicationMonitor {

    fun search(query: ParseRequestLogQuery): ParseRequestLogQueryResult

    fun search(query: ParseRequestLogCountQuery): ParseRequestLogCountQueryResult

    fun stats(query: ParseRequestLogStatQuery): List<ParseRequestLogStat>

    fun intentStats(query: ParseRequestLogStatQuery): List<ParseRequestLogIntentStat>

    fun export(applicationId: Id<ApplicationDefinition>, language: Locale): List<ParseRequestExportLog>

    fun save(log: UserActionLog)

    fun search(query: UserActionLogQuery): UserActionLogQueryResult
}
