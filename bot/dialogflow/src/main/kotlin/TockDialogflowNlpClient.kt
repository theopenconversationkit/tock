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

package ai.tock.nlp.dialogflow

import ai.tock.nlp.api.client.NlpClient
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
import ai.tock.shared.property
import java.io.InputStream
import java.util.Locale

internal class TockDialogflowNlpClient : NlpClient {
    private val projectId = property("dialogflow_project_id", "please set a google project id")

    override fun parse(query: NlpQuery): NlpResult? {
        return DialogflowService.detectIntentText(
            projectId,
            query.queries.firstOrNull() ?: "",
            query.context.dialogId,
            query.context.language.toString(),
        )?.let {
            DialogflowTockMapper().toNlpResult(it, query.namespace)
        }
    }

    override fun healthcheck(): Boolean {
        return true
    }

    override fun evaluateEntities(query: EntityEvaluationQuery): EntityEvaluationResult? = null

    override fun mergeValues(query: ValuesMergeQuery): ValuesMergeResult? = null

    override fun markAsUnknown(query: MarkAsUnknownQuery) = Unit

    override fun getIntentsByNamespaceAndName(
        namespace: String,
        name: String,
    ): List<IntentDefinition>? = null

    override fun getApplicationByNamespaceAndName(
        namespace: String,
        name: String,
    ): ApplicationDefinition? = null

    override fun createApplication(
        namespace: String,
        name: String,
        locale: Locale,
    ): ApplicationDefinition? = null

    override fun importNlpDump(stream: InputStream): Boolean = false

    override fun importNlpPlainDump(dump: ApplicationDump): Boolean = false

    override fun importNlpSentencesDump(stream: InputStream): Boolean = false

    override fun importNlpPlainSentencesDump(dump: SentencesDump): Boolean = false

    override fun logsCount(query: NlpLogCountQuery): List<NlpLogCount>? = null
}
