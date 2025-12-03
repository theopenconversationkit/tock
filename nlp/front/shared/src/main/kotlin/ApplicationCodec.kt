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

import ai.tock.nlp.front.shared.codec.ApplicationDump
import ai.tock.nlp.front.shared.codec.ApplicationImportConfiguration
import ai.tock.nlp.front.shared.codec.DumpType
import ai.tock.nlp.front.shared.codec.ImportReport
import ai.tock.nlp.front.shared.codec.SentencesDump
import ai.tock.nlp.front.shared.config.ApplicationDefinition
import ai.tock.nlp.front.shared.config.SentencesQuery
import org.litote.kmongo.Id
import java.util.Locale

/**
 * To import and export applications, intents, entities and sentences.
 */
interface ApplicationCodec {
    /**
     * Export application dump.
     */
    fun export(
        applicationId: Id<ApplicationDefinition>,
        dumpType: DumpType,
    ): ApplicationDump

    /**
     * Analyse the application dump and present options.
     */
    fun prepareImport(dump: ApplicationDump): ApplicationImportConfiguration

    /**
     * Import the application dump.
     */
    fun import(
        namespace: String,
        dump: ApplicationDump,
        configuration: ApplicationImportConfiguration = ApplicationImportConfiguration(),
    ): ImportReport

    /**
     * Import the sentences dump.
     */
    fun importSentences(
        namespace: String,
        dump: SentencesDump,
    ): ImportReport

    /**
     * Export sentences dump.
     *
     * @param intent if specified, only the sentences classified as this intent are exported
     * @param locale if specified, only the sentences for this locale are exported
     */
    fun exportSentences(
        applicationId: Id<ApplicationDefinition>,
        dumpType: DumpType,
        intent: String? = null,
        locale: Locale? = null,
    ): SentencesDump

    /**
     * Export sentences dump.
     */
    fun exportSentences(
        query: SentencesQuery,
        dumpType: DumpType,
    ): SentencesDump {
        return exportSentences(listOf(query), dumpType)
    }

    /**
     * Export sentences dump.
     */
    fun exportSentences(
        queries: List<SentencesQuery>,
        dumpType: DumpType,
    ): SentencesDump
}
