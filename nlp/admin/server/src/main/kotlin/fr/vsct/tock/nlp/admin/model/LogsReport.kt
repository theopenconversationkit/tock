/*
 * Copyright (C) 2017 VSCT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.vsct.tock.nlp.admin.model

import fr.vsct.tock.nlp.front.shared.monitoring.ParseRequestLogQueryResult

/**
 *
 */
data class LogsReport(
        val logs: List<LogReport>,
        val total: Long,
        val start: Long,
        val end: Long) {

    constructor(
            start: Long,
            result: ParseRequestLogQueryResult,
            applicationId: String,
            intentIdFinder: (String) -> String?) :
            this(
                    result.logs.map { LogReport(it, applicationId, intentIdFinder) },
                    result.total,
                    start,
                    start + result.logs.size
            )
}