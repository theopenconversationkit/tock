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
import ai.tock.nlp.front.shared.config.IntentDefinition
import ai.tock.nlp.front.shared.monitoring.ParseRequestLog
import ai.tock.nlp.front.shared.parser.ParseQuery
import ai.tock.nlp.front.shared.parser.ParseResult
import org.litote.kmongo.Id
import java.time.Instant

/**
 *
 */
data class LogReport(
    val sentence: SentenceReport?,
    val intent: String,
    val dialogId: String,
    val request: ParseQuery,
    val response: ParseResult?,
    val durationInMS: Long,
    val error: Boolean,
    val date: Instant,
) {
    constructor(log: ParseRequestLog, applicationId: Id<ApplicationDefinition>, intentIdFinder: (String) -> Id<IntentDefinition>?) :
        this(
            if (log.result == null) {
                null
            } else {
                SentenceReport(
                    log.result!!,
                    log.query.context.language,
                    applicationId,
                    intentIdFinder.invoke(log.result!!.intent),
                )
            },
            log.result?.intent ?: "",
            log.query.context.dialogId,
            log.query,
            log.result,
            log.durationInMS,
            log.error,
            log.date,
        )
}
