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

package ai.tock.nlp.api.client

import ai.tock.nlp.api.client.model.NlpLogCount
import ai.tock.nlp.api.client.model.NlpLogCountQuery
import ai.tock.nlp.api.client.model.NlpQuery
import ai.tock.nlp.api.client.model.NlpResult
import ai.tock.nlp.api.client.model.dump.ApplicationDefinition
import ai.tock.nlp.api.client.model.dump.ApplicationDump
import ai.tock.nlp.api.client.model.dump.IntentDefinition
import ai.tock.nlp.api.client.model.dump.SentencesDump
import ai.tock.nlp.api.client.model.evaluation.EntityEvaluationQuery
import ai.tock.nlp.api.client.model.evaluation.EntityEvaluationResult
import ai.tock.nlp.api.client.model.merge.ValuesMergeQuery
import ai.tock.nlp.api.client.model.merge.ValuesMergeResult
import ai.tock.nlp.api.client.model.monitoring.MarkAsUnknownQuery
import java.io.InputStream
import java.util.Locale

/**
 * Wraps calls to the NLP stack. [TockNlpClient] is the provided implementation.
 */
interface NlpClient {

    /**
     * Analyses a sentence and returns the result.
     */
    fun parse(query: NlpQuery): NlpResult?

    /**
     * Evaluates entities.
     */
    fun evaluateEntities(query: EntityEvaluationQuery): EntityEvaluationResult?

    /**
     * Merges values and returns the result if found.
     */
    fun mergeValues(query: ValuesMergeQuery): ValuesMergeResult?

    /**
     * Informs the nlp model that a sentence has not been understood.
     */
    fun markAsUnknown(query: MarkAsUnknownQuery)

    /**
     * Returns the list of intents of a model.
     *
     * @namespace Application Namespace
     * @name Application Name
     *
     * @return List of IntentDefinition
     */
    fun getIntentsByNamespaceAndName(namespace: String, name: String): List<IntentDefinition>?

    /**
     * Returns an application for the specified namespace and name.
     *
     * @namespace Application Namespace
     * @name Application Name
     *
     * @return the application if any
     */
    fun getApplicationByNamespaceAndName(namespace: String, name: String): ApplicationDefinition?

    /**
     * Creates an application if it does not exists.
     * @return the new application, null if it already exists.
     */
    fun createApplication(namespace: String, name: String, locale: Locale): ApplicationDefinition?

    /**
     * Imports a NLP dump (configuration and sentences of the NLP model).
     *
     * @return true if NLP model is modified, false either
     */
    fun importNlpDump(stream: InputStream): Boolean

    /**
     * Imports a NLP dump (configuration and sentences of the NLP model).
     *
     * @param dump the dump to import
     * @return true if NLP model is modified, false either
     */
    fun importNlpPlainDump(dump: ApplicationDump): Boolean

    /**
     * Imports a NLP sentences dump (only validated sentences) - format is simpler than [ApplicationDump].
     *
     * @return true if NLP model is modified, false either
     */
    fun importNlpSentencesDump(stream: InputStream): Boolean

    /**
     * Imports a NLP sentences dump (only validated sentences) - format is simpler than [ApplicationDump].
     *
     * @param dump the dump to import
     * @return true if NLP model is modified, false either
     */
    fun importNlpPlainSentencesDump(dump: SentencesDump): Boolean

    /**
     * Analyses a sentence and returns the result.
     */
    fun logsCount(query: NlpLogCountQuery): List<NlpLogCount>?

    /**
     * Checks the server is up.
     */
    fun healthcheck(): Boolean
}
