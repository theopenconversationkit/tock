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

package ai.tock.nlp.front.storage.mongo

import ai.tock.nlp.front.shared.monitoring.ParseRequestLog
import ai.tock.shared.security.TockObfuscatorService

internal class LogObfuscationService {

    fun obfuscate(log: ParseRequestLog): ParseRequestLog {
        val obfuscatedRanges = log.result?.entities
            ?.filter { it.entity.entityType.obfuscated }
            ?.map { it.toClosedRange() }
            ?: emptyList()
        return log.copy(
            query = log.query.copy(
                queries = TockObfuscatorService.obfuscate(
                    texts = log.query.queries,
                    obfuscatedRanges = log.result?.retainedQuery
                        ?.let { log.query.queries.indexOf(it) }
                        ?.takeUnless { i -> i == -1 }
                        ?.let { mapOf(it to obfuscatedRanges) }
                        ?: emptyMap()
                )
            ),
            result = log.result?.obfuscate(obfuscatedRanges)
        )
    }
}
