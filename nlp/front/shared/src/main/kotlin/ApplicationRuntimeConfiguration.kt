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

import ai.tock.nlp.core.DictionaryData
import ai.tock.nlp.core.NlpEngineType
import ai.tock.nlp.core.configuration.NlpApplicationConfiguration
import ai.tock.nlp.front.shared.config.ApplicationDefinition
import ai.tock.nlp.front.shared.config.ClassifiedSentence
import ai.tock.nlp.front.shared.config.ClassifiedSentenceStatus
import ai.tock.nlp.front.shared.config.EntityDefinition
import ai.tock.nlp.front.shared.config.EntityTypeDefinition
import ai.tock.nlp.front.shared.config.FaqDefinition
import ai.tock.nlp.front.shared.config.IntentDefinition
import ai.tock.nlp.front.shared.config.SentencesQuery
import ai.tock.nlp.front.shared.config.SentencesQueryResult
import ai.tock.nlp.front.shared.namespace.NamespaceConfiguration
import ai.tock.nlp.front.shared.user.UserNamespace
import ai.tock.shared.injector
import ai.tock.shared.provide
import ai.tock.shared.provideOrDefault
import ai.tock.shared.security.UserLogin
import org.litote.kmongo.Id
import java.util.Locale

/**
 * The part of [ApplicationConfiguration] that the NLP API uses at runtime: the parsing and the configuration it caches.
 *
 * A storage that only serves the NLP API (a serverless one) binds this interface. Otherwise, [fromInjector] adapts
 * the bound [ApplicationConfiguration]: the existing storages and the projects that replace them keep working unchanged.
 */
interface ApplicationRuntimeConfiguration {
    fun getApplicationByNamespaceAndName(
        namespace: String,
        name: String,
    ): ApplicationDefinition?

    fun getApplications(): List<ApplicationDefinition>

    fun save(
        sentence: ClassifiedSentence,
        user: UserLogin? = sentence.qualifier,
    )

    fun search(query: SentencesQuery): SentencesQueryResult

    fun getIntentsByApplicationId(applicationId: Id<ApplicationDefinition>): List<IntentDefinition>

    fun getIntentById(id: Id<IntentDefinition>): IntentDefinition?

    fun getIntentIdByQualifiedName(name: String): Id<IntentDefinition>?

    companion object {
        /**
         * The bound [ApplicationRuntimeConfiguration], or the bound [ApplicationConfiguration] adapted to it.
         */
        fun fromInjector(): ApplicationRuntimeConfiguration = injector.provideOrDefault<ApplicationRuntimeConfiguration> { injector.provide<ApplicationConfiguration>().asRuntimeConfiguration() }
    }
}

/**
 * Adapts this DAO to [ApplicationRuntimeConfiguration].
 */
fun ApplicationConfiguration.asRuntimeConfiguration(): ApplicationRuntimeConfiguration = ApplicationRuntimeConfigurationAdapter(this)

private class ApplicationRuntimeConfigurationAdapter(
    private val dao: ApplicationConfiguration,
) : ApplicationRuntimeConfiguration {
    override fun getApplicationByNamespaceAndName(
        namespace: String,
        name: String,
    ): ApplicationDefinition? = dao.getApplicationByNamespaceAndName(namespace, name)

    override fun getApplications(): List<ApplicationDefinition> = dao.getApplications()

    override fun save(
        sentence: ClassifiedSentence,
        user: UserLogin?,
    ) = dao.save(sentence, user)

    override fun search(query: SentencesQuery): SentencesQueryResult = dao.search(query)

    override fun getIntentsByApplicationId(applicationId: Id<ApplicationDefinition>): List<IntentDefinition> = dao.getIntentsByApplicationId(applicationId)

    override fun getIntentById(id: Id<IntentDefinition>): IntentDefinition? = dao.getIntentById(id)

    override fun getIntentIdByQualifiedName(name: String): Id<IntentDefinition>? = dao.getIntentIdByQualifiedName(name)
}
