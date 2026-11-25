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

package ai.tock.nlp.front.service.storage

import ai.tock.nlp.front.shared.config.ApplicationDefinition
import ai.tock.nlp.front.shared.monitoring.ParseRequestExportLog
import ai.tock.nlp.front.shared.monitoring.ParseRequestLog
import ai.tock.nlp.front.shared.monitoring.ParseRequestLogCountQuery
import ai.tock.nlp.front.shared.monitoring.ParseRequestLogCountQueryResult
import ai.tock.nlp.front.shared.monitoring.ParseRequestLogIntentStat
import ai.tock.nlp.front.shared.monitoring.ParseRequestLogQuery
import ai.tock.nlp.front.shared.monitoring.ParseRequestLogQueryResult
import ai.tock.nlp.front.shared.monitoring.ParseRequestLogStat
import ai.tock.nlp.front.shared.monitoring.ParseRequestLogStatQuery
import ai.tock.shared.injector
import ai.tock.shared.provide
import ai.tock.shared.provideOrDefault
import org.litote.kmongo.Id
import java.util.Locale

/**
 * The part of [ParseRequestLogDAO] that the NLP API uses at runtime: the parsing and the configuration it caches.
 *
 * A storage that only serves the NLP API (a serverless one) binds this interface. Otherwise, [fromInjector] adapts
 * the bound [ParseRequestLogDAO]: the existing storages and the projects that replace them keep working unchanged.
 */
interface ParseRequestLogRuntimeDAO {
    fun save(log: ParseRequestLog)

    companion object {
        /**
         * The bound [ParseRequestLogRuntimeDAO], or the bound [ParseRequestLogDAO] adapted to it.
         */
        fun fromInjector(): ParseRequestLogRuntimeDAO = injector.provideOrDefault<ParseRequestLogRuntimeDAO> { injector.provide<ParseRequestLogDAO>().asRuntimeDAO() }
    }
}

/**
 * Adapts this DAO to [ParseRequestLogRuntimeDAO].
 */
fun ParseRequestLogDAO.asRuntimeDAO(): ParseRequestLogRuntimeDAO = ParseRequestLogRuntimeDAOAdapter(this)

private class ParseRequestLogRuntimeDAOAdapter(
    private val dao: ParseRequestLogDAO,
) : ParseRequestLogRuntimeDAO {
    override fun save(log: ParseRequestLog) = dao.save(log)
}
