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

package ai.tock.storage.dynamodb.publication

import ai.tock.bot.admin.answer.AnswerConfigurationType
import ai.tock.bot.admin.answer.SimpleAnswer
import ai.tock.bot.admin.answer.SimpleAnswerConfiguration
import ai.tock.bot.admin.bot.BotApplicationConfiguration
import ai.tock.bot.admin.bot.BotApplicationConfigurationDAO
import ai.tock.bot.admin.bot.BotConfiguration
import ai.tock.bot.admin.story.StoryDefinitionConfiguration
import ai.tock.bot.admin.story.StoryDefinitionConfigurationDAO
import ai.tock.bot.connector.ConnectorType
import ai.tock.bot.definition.IntentWithoutNamespace
import ai.tock.bot.engine.feature.FeatureDAO
import ai.tock.bot.engine.feature.FeatureState
import ai.tock.shared.service.BotAdditionalModulesService
import ai.tock.storage.dynamodb.BotApplicationConfigurationDynamoDAO
import ai.tock.storage.dynamodb.DynamoStore
import ai.tock.storage.dynamodb.FeatureDynamoDAO
import ai.tock.storage.dynamodb.I18nDynamoDAO
import ai.tock.storage.dynamodb.PK
import ai.tock.storage.dynamodb.SK
import ai.tock.storage.dynamodb.StoryDefinitionConfigurationDynamoDAO
import ai.tock.translator.I18nDAO
import ai.tock.translator.I18nLabel
import ai.tock.translator.I18nLabelValue
import ai.tock.translator.I18nLocalizedLabel
import ai.tock.translator.UserInterfaceType.textChat
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.provider
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.litote.kmongo.Id
import org.litote.kmongo.toId
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait
import java.time.Duration
import java.time.ZonedDateTime
import java.util.Locale
import java.util.ServiceLoader
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * The publication of an admin (DAOs in memory) to DynamoDB Local.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DynamoPublicationTest {
    private val dynamo = GenericContainer("amazon/dynamodb-local:2.6.1").withExposedPorts(8000).waitingFor(Wait.forListeningPort())

    // the admin: its documents, and the listeners of its change streams
    private val applications = CopyOnWriteArrayList<BotApplicationConfiguration>()
    private val bots = CopyOnWriteArrayList<BotConfiguration>()
    private val stories = CopyOnWriteArrayList<StoryDefinitionConfiguration>()
    private val features = CopyOnWriteArrayList<Pair<String, FeatureState>>()
    private val labels = CopyOnWriteArrayList<I18nLabel>()
    private val listeners = CopyOnWriteArrayList<() -> Unit>()

    private val applicationDAO =
        mockk<BotApplicationConfigurationDAO> {
            every { getConfigurations() } answers { applications.toList() }
            every { getBotConfigurations() } answers { bots.toList() }
            every { listenChanges(any()) } answers { listeners += firstArg<() -> Unit>() }
            every { listenBotChanges(any()) } answers { listeners += firstArg<() -> Unit>() }
        }
    private val storyDAO =
        mockk<StoryDefinitionConfigurationDAO> {
            every { getStoryDefinitionsByNamespaceAndBotId(any(), any()) } answers { stories.filter { it.namespace == firstArg() && it.botId == secondArg() } }
            every { listenChanges(any()) } answers { listeners += firstArg<() -> Unit>() }
        }
    private val featureDAO =
        mockk<FeatureDAO> {
            coEvery { getFeatures(any(), any()) } answers { features.filter { it.first == firstArg<String>() }.map { it.second } }
        }
    private val i18nDAO =
        mockk<I18nDAO> {
            every { getLabels(any(), any()) } answers { labels.filter { it.namespace == firstArg() } }
            every { listenI18n(any()) } answers {
                val listener = firstArg<(Id<I18nLabel>) -> Unit>()
                listeners += { listener("x".toId()) }
            }
        }

    private val businessRules = CopyOnWriteArrayList<ai.tock.bot.admin.bot.businessrules.BotBusinessRulesConfiguration>()
    private val indicators = CopyOnWriteArrayList<ai.tock.bot.admin.indicators.Indicator>()
    private val businessRulesDAO =
        mockk<ai.tock.bot.admin.bot.businessrules.BotBusinessRulesConfigurationDAO> {
            every { findByNamespaceAndBotId(any(), any()) } answers { businessRules.firstOrNull { it.namespace == firstArg() && it.botId == secondArg() } }
            every { listenChanges(any()) } answers { listeners += firstArg<() -> Unit>() }
        }
    private val indicatorDAO =
        mockk<ai.tock.bot.admin.indicators.IndicatorDAO> {
            every { findAllByBotId(any(), any()) } answers { indicators.filter { it.namespace == firstArg() && it.botId == secondArg() } }
        }

    private val publication =
        DynamoPublication(
            applicationDAO,
            storyDAO,
            featureDAO,
            i18nDAO,
            settings = BotSettingsSources(businessRules = businessRulesDAO),
            indicators = indicatorDAO,
            interval = Duration.ofHours(1),
            debounce = Duration.ofMillis(100),
        )

    @BeforeAll
    fun start() {
        dynamo.start()
        System.setProperty("tock_dynamodb_endpoint", "http://${dynamo.host}:${dynamo.getMappedPort(8000)}")
        System.setProperty("aws.accessKeyId", "x")
        System.setProperty("aws.secretAccessKey", "x")
        // the features read by the runtime are not cached: the test reads them right after their publication
        System.setProperty("tock_dynamodb_feature_cache_ttl_in_ms", "0")
    }

    @BeforeEach
    @AfterEach
    fun cleanup() {
        publication.stop()
        listOf(applications, bots, stories, features, labels, listeners, businessRules, indicators).forEach { it.clear() }
        DynamoStore.client.listTables().tableNames().forEach { table ->
            DynamoStore.client.scanPaginator { it.tableName(table) }.items().forEach { item ->
                DynamoStore.client.deleteItem { it.tableName(table).key(mapOf(PK to item.getValue(PK), SK to item.getValue(SK))) }
            }
        }
    }

    private fun application(
        applicationId: String = "whatsapp",
        path: String? = "/$applicationId",
    ) = BotApplicationConfiguration(
        applicationId = applicationId,
        botId = "bot",
        namespace = "app",
        nlpModel = "bot",
        connectorType = ConnectorType("test"),
        path = path,
    )

    private fun story(
        storyId: String,
        intent: String = storyId,
        label: String = "hello",
    ) = StoryDefinitionConfiguration(
        storyId = storyId,
        botId = "bot",
        intent = IntentWithoutNamespace(intent),
        currentType = AnswerConfigurationType.simple,
        answers = listOf(SimpleAnswerConfiguration(listOf(SimpleAnswer(I18nLabelValue("key_$storyId", "app", "test", label))))),
        namespace = "app",
        userSentenceLocale = Locale.FRENCH,
    )

    private fun label(
        id: String,
        text: String,
    ) = I18nLabel(
        _id = id.toId(),
        namespace = "app",
        category = "test",
        i18n = linkedSetOf(I18nLocalizedLabel(Locale.ENGLISH, textChat, text, true)),
        defaultLabel = text,
    )

    private fun feature(
        name: String,
        enabled: Boolean,
        startDate: ZonedDateTime? = null,
    ) = "bot" to FeatureState("category", name, enabled, startDate, null, null)

    private fun givenAnAdmin() {
        applications += application()
        bots += BotConfiguration("bot", "bot", "app", "bot")
        stories += story("greetings")
        stories += story("goodbye")
        features += feature("enabled", true, ZonedDateTime.now().minusDays(1))
        features += feature("disabled", false)
        labels += label("app_test_hello", "hello")
    }

    private fun runtimeStories() = StoryDefinitionConfigurationDynamoDAO.getStoryDefinitionsByNamespaceAndBotId("app", "bot")

    @Test
    fun `GIVEN an admin WHEN it is published THEN the runtime reads its configuration, and a second publication writes nothing`() {
        givenAnAdmin()

        val report = publication.publishAll()
        assertEquals(
            mapOf(DynamoPublication.Kind.STORY to 2, DynamoPublication.Kind.FEATURE to 2),
            report.written.filterKeys {
                it in
                    setOf(DynamoPublication.Kind.STORY, DynamoPublication.Kind.FEATURE)
            },
        )
        assertEquals(emptyMap(), report.failed)

        assertEquals(applications.toList(), BotApplicationConfigurationDynamoDAO.getConfigurations())
        assertEquals(bots.toList(), BotApplicationConfigurationDynamoDAO.getBotConfigurations())
        assertEquals(stories.toSet(), runtimeStories().toSet())
        assertEquals(labels.toList(), I18nDynamoDAO.getLabels("app"))
        runBlocking {
            assertTrue(FeatureDynamoDAO.isEnabled("bot", "app", "category", "enabled"))
            assertFalse(FeatureDynamoDAO.isEnabled("bot", "app", "category", "disabled", default = true))
        }

        val second = publication.publishAll()
        assertTrue(second.isEmpty, "nothing changed: $second")
    }

    @Test
    fun `GIVEN a published admin WHEN items change or are deleted in the admin THEN the runtime follows, and keeps its own items`() {
        givenAnAdmin()
        publication.publishAll()
        // the runtime creates its own items: a built-in story, a label, a feature
        StoryDefinitionConfigurationDynamoDAO.createBuiltInStoriesIfNotExist(listOf(story("builtin")))
        I18nDynamoDAO.save(label("app_test_runtime", "runtime"))
        runBlocking { FeatureDynamoDAO.isEnabled("bot", "app", "category", "registered", default = true) }

        stories.removeIf { it.storyId == "goodbye" }
        stories.replaceAll { if (it.storyId == "greetings") it.copy(name = "renamed") else it }
        labels.clear()
        features.removeIf { it.second.name == "disabled" }
        val report = publication.publishAll()
        assertEquals(1, report.deleted[DynamoPublication.Kind.STORY])
        assertEquals(1, report.written[DynamoPublication.Kind.STORY])

        assertEquals(setOf("greetings" to "renamed", "builtin" to "builtin"), runtimeStories().map { it.storyId to it.name }.toSet())
        assertEquals(listOf("app_test_runtime"), I18nDynamoDAO.getLabels("app").map { it._id.toString() })
        assertEquals(
            setOf("enabled", "registered"),
            runBlocking { FeatureDynamoDAO.getFeatures("bot", "app") }.map { it.name }.toSet(),
        )
    }

    @Test
    fun `GIVEN items of the runtime with the unique keys of items of the admin WHEN the admin is published THEN the items of the admin replace them`() {
        // installed by the serverless runtime before the publication: other ids, same intent and same application
        StoryDefinitionConfigurationDynamoDAO.createBuiltInStoriesIfNotExist(listOf(story("greetings")))
        BotApplicationConfigurationDynamoDAO.save(application())
        givenAnAdmin()

        val report = publication.publishAll()
        assertEquals(emptyMap(), report.failed)
        assertEquals(stories.toSet(), runtimeStories().toSet())
        assertEquals(applications.toList(), BotApplicationConfigurationDynamoDAO.getConfigurations())
    }

    @Test
    fun `GIVEN a started publication WHEN the admin reports a change THEN the change is published`() {
        givenAnAdmin()
        publication.start()
        waitUntil { runtimeStories().size == 2 }

        stories += story("third")
        listeners.forEach { it() }
        waitUntil { runtimeStories().size == 3 }
    }

    @Test
    fun `GIVEN the publication enabled WHEN the modules of the admin are injected THEN the publication starts`() {
        givenAnAdmin()
        System.setProperty("tock_dynamodb_publication_enabled", "true")
        try {
            val service = ServiceLoader.load(BotAdditionalModulesService::class.java).filterIsInstance<DynamoPublicationModulesService>().single()
            Kodein {
                bind<BotApplicationConfigurationDAO>() with provider { applicationDAO }
                bind<StoryDefinitionConfigurationDAO>() with provider { storyDAO }
                bind<FeatureDAO>() with provider { featureDAO }
                bind<I18nDAO>() with provider { i18nDAO }
                service.customModules().forEach { import(it) }
            }
            val started = assertNotNull(DynamoPublicationModulesService.publication)
            try {
                waitUntil { runtimeStories().size == 2 }
            } finally {
                started.stop()
            }
        } finally {
            System.clearProperty("tock_dynamodb_publication_enabled")
        }
    }

    @Test
    fun `GIVEN the publication disabled WHEN the modules are injected THEN nothing is added`() {
        assertNull(System.getProperty("tock_dynamodb_publication_enabled"))
        assertEquals(emptySet(), DynamoPublicationModulesService().customModules())
    }

    @Test
    fun `GIVEN bot settings and indicators in the admin WHEN they are published THEN the runtime reads them, and their deletion follows`() {
        givenAnAdmin()
        val rules =
            ai.tock.bot.admin.bot.businessrules
                .BotBusinessRulesConfiguration(org.litote.kmongo.newId(), "app", "bot", coveredTopics = listOf("trains"))
        businessRules += rules
        val indicator =
            ai.tock.bot.admin.indicators.Indicator(
                name = "satisfaction",
                label = "Satisfaction",
                namespace = "app",
                botId = "bot",
                values =
                    setOf(
                        ai.tock.bot.admin.indicators
                            .IndicatorValue("yes", "Yes"),
                    ),
            )
        indicators += indicator

        publication.publishAll()
        assertEquals(
            rules,
            ai.tock.storage.dynamodb.BotBusinessRulesConfigurationDynamoDAO
                .findByNamespaceAndBotId("app", "bot"),
        )
        assertEquals(
            indicator,
            ai.tock.storage.dynamodb.IndicatorDynamoDAO
                .findByNameAndBotId("satisfaction", "app", "bot"),
        )
        assertTrue(publication.publishAll().isEmpty)

        businessRules.clear()
        indicators.clear()
        publication.publishAll()
        assertNull(
            ai.tock.storage.dynamodb.BotBusinessRulesConfigurationDynamoDAO
                .findByNamespaceAndBotId("app", "bot"),
        )
        assertNull(
            ai.tock.storage.dynamodb.IndicatorDynamoDAO
                .findByNameAndBotId("satisfaction", "app", "bot"),
        )
    }

    private fun waitUntil(condition: () -> Boolean) {
        val deadline = System.currentTimeMillis() + 10_000
        while (!condition()) {
            check(System.currentTimeMillis() < deadline) { "timeout" }
            Thread.sleep(50)
        }
    }
}
