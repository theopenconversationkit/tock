/*
 * Copyright (C) 2017/2025 SNCF Connect & Tech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ai.tock.nlp.front.storage.dynamodb

import ai.tock.nlp.front.service.intentIdByQualifiedName
import ai.tock.nlp.front.service.storage.ApplicationDefinitionRuntimeDAO
import ai.tock.nlp.front.service.storage.ClassifiedSentenceRuntimeDAO
import ai.tock.nlp.front.service.storage.EntityTypeDefinitionRuntimeDAO
import ai.tock.nlp.front.service.storage.IntentDefinitionRuntimeDAO
import ai.tock.nlp.front.service.storage.NamespaceConfigurationRuntimeDAO
import ai.tock.nlp.front.service.storage.ParseRequestLogRuntimeDAO
import ai.tock.nlp.front.shared.ApplicationRuntimeConfiguration
import ai.tock.nlp.front.shared.config.ApplicationDefinition
import ai.tock.nlp.front.shared.config.ClassifiedSentence
import ai.tock.nlp.front.shared.config.IntentDefinition
import ai.tock.nlp.front.shared.config.SentencesQuery
import ai.tock.nlp.front.shared.config.SentencesQueryResult
import ai.tock.shared.security.UserLogin
import ai.tock.storage.dynamodb.DynamoChanges
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.provider
import org.litote.kmongo.Id

/**
 * The configuration of the NLP at runtime, on DynamoDB: the exact search of the validated sentences, the inbox,
 * the applications and the intents.
 */
object DynamoApplicationRuntimeConfiguration : ApplicationRuntimeConfiguration {
    override fun getApplicationByNamespaceAndName(
        namespace: String,
        name: String,
    ): ApplicationDefinition? = ApplicationDefinitionDynamoDAO.getApplicationByNamespaceAndName(namespace, name)

    override fun getApplications(): List<ApplicationDefinition> = ApplicationDefinitionDynamoDAO.getApplications()

    override fun save(
        sentence: ClassifiedSentence,
        user: UserLogin?,
    ) {
        ClassifiedSentenceDynamoDAO.save(sentence.copy(qualifier = user))
    }

    /**
     * Only the exact search of the parser runs on DynamoDB.
     *
     * @throws UnsupportedOperationException for the other searches: the search of the Studio stays in the admin
     */
    override fun search(query: SentencesQuery): SentencesQueryResult {
        val text = query.search
        val language = query.language
        if (!query.onlyExactMatch || text == null || language == null) {
            throw UnsupportedOperationException("only the exact search of a sentence runs on DynamoDB: the search of the Studio stays in the admin")
        }
        val sentence =
            ClassifiedSentenceDynamoDAO
                .findExact(query.applicationId, language, text, query.status, query.normalizeText)
                ?.takeIf { it.status != query.notStatus }
        return SentencesQueryResult(if (sentence == null) 0 else 1, listOfNotNull(sentence))
    }

    override fun getIntentsByApplicationId(applicationId: Id<ApplicationDefinition>): List<IntentDefinition> = IntentDefinitionDynamoDAO.getIntentsByApplicationId(applicationId)

    override fun getIntentById(id: Id<IntentDefinition>): IntentDefinition? = IntentDefinitionDynamoDAO.getIntentById(id)

    override fun getIntentIdByQualifiedName(name: String): Id<IntentDefinition>? =
        intentIdByQualifiedName(name) { namespace, intentName -> IntentDefinitionDynamoDAO.getIntentByNamespaceAndName(namespace, intentName) }
}

/**
 * The runtime DAOs of the NLP stored in DynamoDB, for a serverless process. The tables are the ones of
 * tock-storage-dynamodb-core (`tock_dynamodb_table_prefix`...).
 */
val nlpDynamoModule =
    Kodein.Module(allowSilentOverride = true) {
        // the runtime reads the versions of the published configuration before it loads it
        DynamoChanges.register()
        bind<ApplicationRuntimeConfiguration>() with provider { DynamoApplicationRuntimeConfiguration }
        bind<ApplicationDefinitionRuntimeDAO>() with provider { ApplicationDefinitionDynamoDAO }
        bind<IntentDefinitionRuntimeDAO>() with provider { IntentDefinitionDynamoDAO }
        bind<EntityTypeDefinitionRuntimeDAO>() with provider { EntityTypeDefinitionDynamoDAO }
        bind<NamespaceConfigurationRuntimeDAO>() with provider { NamespaceConfigurationDynamoDAO }
        bind<ClassifiedSentenceRuntimeDAO>() with provider { ClassifiedSentenceDynamoDAO }
        bind<ParseRequestLogRuntimeDAO>() with provider { ParseRequestLogDynamoDAO }
    }
