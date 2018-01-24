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

import fr.vsct.tock.nlp.api.client.model.NlpIntentEntitiesQuery
import fr.vsct.tock.nlp.api.client.model.NlpQuery
import fr.vsct.tock.nlp.api.client.model.NlpResult
import fr.vsct.tock.nlp.api.client.model.dump.ApplicationDump
import fr.vsct.tock.nlp.api.client.model.evaluation.EntityEvaluationQuery
import fr.vsct.tock.nlp.api.client.model.evaluation.EntityEvaluationResult
import fr.vsct.tock.nlp.api.client.model.merge.ValuesMergeQuery
import fr.vsct.tock.nlp.api.client.model.merge.ValuesMergeResult
import retrofit2.Response
import java.io.InputStream

/**
 * Wraps calls to the NLP stack. [TockNlpClient] is the provided implementation.
 */
interface NlpClient {

    /**
     * Analyse a sentence and returns the result.
     */
    fun parse(query: NlpQuery): Response<NlpResult>

    /**
     * Analyse a sentence and returns entities values, given a predefined intent.
     */
    fun parseIntentEntities(query: NlpIntentEntitiesQuery): Response<NlpResult>

    /**
     * Evaluate entities.
     */
    fun evaluateEntities(query: EntityEvaluationQuery): Response<EntityEvaluationResult>

    /**
     * Merge values and returns the result if found.
     */
    fun mergeValues(query: ValuesMergeQuery): Response<ValuesMergeResult>

    /**
     * Import a NLP dump (configuration and sentences of the NLP model).
     * @return true if NLP model is modified, false either
     */
    fun importNlpDump(stream: InputStream): Response<Boolean>

    /**
     * Import a NLP dump (configuration and sentences of the NLP model).
     *
     * @param dump the dump to import
     * @return true if NLP model is modified, false either
     */
    fun importNlpPlainDump(dump: ApplicationDump): Response<Boolean>

    /**
     * Check the server is up.
     */
    fun healthcheck(): Boolean
}