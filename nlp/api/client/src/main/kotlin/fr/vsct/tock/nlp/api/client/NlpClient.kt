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

package fr.vsct.tock.nlp.api.client

import fr.vsct.tock.nlp.api.client.model.NlpQuery
import fr.vsct.tock.nlp.api.client.model.NlpResult
import fr.vsct.tock.nlp.api.client.model.dump.ApplicationDefinition
import fr.vsct.tock.nlp.api.client.model.dump.ApplicationDump
import fr.vsct.tock.nlp.api.client.model.dump.IntentDefinition
import fr.vsct.tock.nlp.api.client.model.dump.SentencesDump
import fr.vsct.tock.nlp.api.client.model.evaluation.EntityEvaluationQuery
import fr.vsct.tock.nlp.api.client.model.evaluation.EntityEvaluationResult
import fr.vsct.tock.nlp.api.client.model.merge.ValuesMergeQuery
import fr.vsct.tock.nlp.api.client.model.merge.ValuesMergeResult
import java.io.InputStream
import java.util.Locale

/**
 * Wraps calls to the NLP stack. [TockNlpClient] is the provided implementation.
 */
interface NlpClient {

    /**
     * Analyse a sentence and returns the result.
     */
    fun parse(query: NlpQuery): NlpResult?

    /**
     * Evaluate entities.
     */
    fun evaluateEntities(query: EntityEvaluationQuery): EntityEvaluationResult?

    /**
     * Merge values and returns the result if found.
     */
    fun mergeValues(query: ValuesMergeQuery): ValuesMergeResult?

    /**
     * Export list of IntentDefinition
     *
     * @namespace Application Namespace
     * @name Application Name
     *
     * @return List of IntentDefinition
     */
    fun getIntentsByNamespaceAndName(namespace: String, name: String): List<IntentDefinition>?

    /**
     * Create an application if it does not exists.
     * @return the new application, null if it already exists.
     */
    fun createApplication(namespace: String, name: String, locale: Locale): ApplicationDefinition?

    /**
     * Import a NLP dump (configuration and sentences of the NLP model).
     *
     * @return true if NLP model is modified, false either
     */
    fun importNlpDump(stream: InputStream): Boolean

    /**
     * Import a NLP dump (configuration and sentences of the NLP model).
     *
     * @param dump the dump to import
     * @return true if NLP model is modified, false either
     */
    fun importNlpPlainDump(dump: ApplicationDump): Boolean

    /**
     * Import a NLP sentences dump (only validated sentences) - format is simpler than [ApplicationDump].
     *
     * @return true if NLP model is modified, false either
     */
    fun importNlpSentencesDump(stream: InputStream): Boolean

    /**
     * Import a NLP sentences dump (only validated sentences) - format is simpler than [ApplicationDump].
     *
     * @param dump the dump to import
     * @return true if NLP model is modified, false either
     */
    fun importNlpPlainSentencesDump(dump: SentencesDump): Boolean

    /**
     * Check the server is up.
     */
    fun healthcheck(): Boolean
}