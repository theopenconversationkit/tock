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

package ai.tock.nlp.front.service

import ai.tock.nlp.core.DictionaryRepository
import ai.tock.nlp.core.NlpCore
import ai.tock.nlp.front.service.storage.ApplicationDefinitionDAO
import ai.tock.nlp.front.service.storage.ClassifiedSentenceDAO
import ai.tock.nlp.front.service.storage.EntityTypeDefinitionDAO
import ai.tock.nlp.front.service.storage.FaqDefinitionDAO
import ai.tock.nlp.front.service.storage.IntentDefinitionDAO
import ai.tock.nlp.front.service.storage.ModelBuildTriggerDAO
import ai.tock.nlp.front.service.storage.NamespaceConfigurationDAO
import ai.tock.nlp.front.service.storage.ParseRequestLogDAO
import ai.tock.nlp.front.service.storage.UserNamespaceDAO
import ai.tock.nlp.front.shared.ApplicationConfiguration
import ai.tock.nlp.front.shared.config.ApplicationDefinition
import ai.tock.nlp.front.shared.config.Classification
import ai.tock.nlp.front.shared.config.ClassifiedSentence
import ai.tock.nlp.front.shared.config.ClassifiedSentenceStatus
import ai.tock.nlp.front.shared.config.IntentDefinition
import ai.tock.nlp.front.shared.parser.IntentQualifier
import ai.tock.nlp.front.shared.parser.ParseQuery
import ai.tock.nlp.front.shared.parser.QueryContext
import ai.tock.shared.Dice
import ai.tock.shared.Executor
import ai.tock.shared.defaultLocale
import ai.tock.shared.injector
import ai.tock.shared.name
import ai.tock.shared.tockInternalInjector
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.KodeinInjector
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.provider
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.litote.kmongo.newId
import org.litote.kmongo.toId
import java.time.Instant

/**
 *
 */
abstract class AbstractTest {

    inner class TestContext {

        val core: NlpCore = mockk(relaxed = true)
        val config: ApplicationConfiguration = mockk(relaxed = true)
        val executor: Executor = mockk(relaxed = true)
        val logDAO: ParseRequestLogDAO = mockk(relaxed = true)
        val modelBuildTriggerDAO: ModelBuildTriggerDAO = mockk(relaxed = true)
        val applicationDefinitionDAO: ApplicationDefinitionDAO = mockk(relaxed = true)
        val entityTypeDefinitionDAO: EntityTypeDefinitionDAO = mockk(relaxed = true)
        val intentDefinitionDAO: IntentDefinitionDAO = mockk(relaxed = true)
        val classifiedSentenceDAO: ClassifiedSentenceDAO = mockk(relaxed = true)
        val dictionaryRepository: DictionaryRepository = mockk(relaxed = true)
        val namespaceDAO: UserNamespaceDAO = mockk(relaxed = true)
        val faqDefinitionDAO: FaqDefinitionDAO = mockk(relaxed = true)
        val namespaceConfigurationDAO: NamespaceConfigurationDAO = mockk(relaxed = true)

        val frontTestModule = Kodein.Module {
            bind<ApplicationConfiguration>() with provider { config }
            bind<NlpCore>() with provider { core }
            bind<Executor>() with provider { executor }
            bind<ParseRequestLogDAO>() with provider { logDAO }
            bind<ModelBuildTriggerDAO>() with provider { modelBuildTriggerDAO }
            bind<ApplicationDefinitionDAO>() with provider { applicationDefinitionDAO }
            bind<EntityTypeDefinitionDAO>() with provider { entityTypeDefinitionDAO }
            bind<IntentDefinitionDAO>() with provider { intentDefinitionDAO }
            bind<FaqDefinitionDAO>() with provider { faqDefinitionDAO }
            bind<ClassifiedSentenceDAO>() with provider { classifiedSentenceDAO }
            bind<DictionaryRepository>() with provider { dictionaryRepository }
            bind<UserNamespaceDAO>() with provider { namespaceDAO }
            bind<NamespaceConfigurationDAO>() with provider { namespaceConfigurationDAO }
        }

        fun init() {
            every { config.getApplicationByNamespaceAndName(namespace, appName) } returns app
            every { config.getIntentsByApplicationId(app._id) } returns
                listOf(
                    defaultIntentDefinition,
                    intent2Definition
                )

            every { config.getIntentById(any()) } returns null
            every { config.getIntentById(defaultIntentDefinition._id) } returns defaultIntentDefinition
            every { config.getIntentById(intent2Definition._id) } returns intent2Definition

            every { config.getIntentByNamespaceAndName(any(), any()) } returns null
            every { config.getIntentByNamespaceAndName(namespace, defaultIntentDefinition.name) } returns defaultIntentDefinition
            every { config.getIntentByNamespaceAndName(namespace, intent2Definition.name) } returns intent2Definition

            tockInternalInjector = KodeinInjector()
            injector.inject(
                Kodein {
                    import(frontTestModule)
                }
            )
        }
    }

    val context = TestContext()

    val namespace = "namespace"
    val appName = "test"
    val app = ApplicationDefinition(
        appName,
        namespace = namespace,
        _id = "id".toId(),
        supportedLocales = setOf(defaultLocale)
    )

    val defaultIntentName = "$namespace:intent"
    val defaultIntentDefinition =
        IntentDefinition(defaultIntentName.name(), namespace, setOf(app._id), emptySet(), _id = newId())
    val defaultClassification = Classification(defaultIntentDefinition._id, emptyList())
    val defaultClassifiedSentence = ClassifiedSentence(
        "a",
        defaultLocale,
        "id".toId(),
        Instant.now(),
        Instant.now(),
        ClassifiedSentenceStatus.inbox,
        defaultClassification,
        1.0,
        1.0
    )

    val parseQuery = ParseQuery(listOf("a"), namespace, appName, QueryContext(defaultLocale, Dice.newId()))

    val intent2Name = "$namespace:intent2"
    val intent2Definition =
        IntentDefinition(intent2Name.name(), namespace, setOf(app._id), emptySet(), _id = newId())
    val intent2Classification = Classification(intent2Definition._id, emptyList())

    val intent2ClassifiedSentence = ClassifiedSentence(
        "text",
        defaultLocale,
        "id".toId(),
        Instant.now(),
        Instant.now(),
        ClassifiedSentenceStatus.validated,
        intent2Classification,
        1.0,
        1.0
    )
    val intentSubsetParseQuery = ParseQuery(
        listOf("text"),
        namespace,
        appName,
        QueryContext(defaultLocale, Dice.newId()),
        intentsSubset = setOf(IntentQualifier(intent2Name, 0.0))
    )

    @BeforeEach
    fun initContext() {
        context.init()
    }

    @AfterEach
    fun cleanupContext() {
        tockInternalInjector = KodeinInjector()
    }
}
