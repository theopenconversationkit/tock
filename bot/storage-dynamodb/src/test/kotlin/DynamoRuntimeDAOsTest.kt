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

package ai.tock.storage.dynamodb

import ai.tock.bot.admin.answer.AnswerConfigurationType
import ai.tock.bot.admin.answer.SimpleAnswer
import ai.tock.bot.admin.answer.SimpleAnswerConfiguration
import ai.tock.bot.admin.story.StoryDefinitionConfiguration
import ai.tock.bot.connector.whatsapp.cloud.database.model.PayloadWhatsAppCloud
import ai.tock.bot.definition.IntentWithoutNamespace
import ai.tock.translator.I18nLabel
import ai.tock.translator.I18nLabelFilter
import ai.tock.translator.I18nLabelStat
import ai.tock.translator.I18nLabelValue
import ai.tock.translator.I18nLocalizedLabel
import ai.tock.translator.UserInterfaceType.textChat
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.litote.kmongo.newId
import org.litote.kmongo.toId
import java.time.Instant
import java.time.ZonedDateTime
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * The runtime DAOs of the stories, the features, the labels and the WhatsApp payloads, on DynamoDB Local.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DynamoRuntimeDAOsTest {
    @BeforeAll
    fun start() {
        DynamoLocal.start()
    }

    private fun unique() = UUID.randomUUID().toString().take(8)

    // ---------------------------------------------------------------- stories

    private fun story(
        botId: String,
        storyId: String,
        intent: String = storyId,
        label: String = "hello",
    ) = StoryDefinitionConfiguration(
        storyId = storyId,
        botId = botId,
        intent = IntentWithoutNamespace(intent),
        currentType = AnswerConfigurationType.simple,
        answers = listOf(SimpleAnswerConfiguration(listOf(SimpleAnswer(I18nLabelValue("key_$storyId", "app", "test", label))))),
        namespace = "app",
        userSentenceLocale = Locale.FRENCH,
    )

    @Test
    fun `GIVEN published stories WHEN the runtime reads them THEN the answers are back`() {
        val botId = "bot_${unique()}"
        val greetings = story(botId, "greetings")
        StoryDefinitionConfigurationDynamoDAO.save(greetings)
        StoryDefinitionConfigurationDynamoDAO.save(story(botId, "goodbye"))
        StoryDefinitionConfigurationDynamoDAO.save(story("other_$botId", "greetings"))

        val stories = StoryDefinitionConfigurationDynamoDAO.getStoryDefinitionsByNamespaceAndBotId("app", botId)
        assertEquals(setOf("greetings", "goodbye"), stories.map { it.storyId }.toSet())
        assertEquals(greetings, StoryDefinitionConfigurationDynamoDAO.getStoryDefinitionByNamespaceAndBotIdAndStoryId("app", botId, "greetings"))
    }

    @Test
    fun `GIVEN a story WHEN another story of the bot takes its intent THEN it is refused, until the intent is free`() {
        val botId = "bot_${unique()}"
        val greetings = story(botId, "greetings", intent = "hello")
        StoryDefinitionConfigurationDynamoDAO.save(greetings)
        assertFailsWith<IllegalArgumentException> { StoryDefinitionConfigurationDynamoDAO.save(story(botId, "other", intent = "hello")) }

        // the story changes its intent: the old one is free
        StoryDefinitionConfigurationDynamoDAO.save(greetings.copy(intent = IntentWithoutNamespace("hi")))
        StoryDefinitionConfigurationDynamoDAO.save(story(botId, "other", intent = "hello"))
        assertEquals(2, StoryDefinitionConfigurationDynamoDAO.getStoryDefinitionsByNamespaceAndBotId("app", botId).size)

        StoryDefinitionConfigurationDynamoDAO.delete(greetings)
        StoryDefinitionConfigurationDynamoDAO.save(story(botId, "third", intent = "hi"))
        assertEquals(setOf("other", "third"), StoryDefinitionConfigurationDynamoDAO.getStoryDefinitionsByNamespaceAndBotId("app", botId).map { it.storyId }.toSet())
    }

    @Test
    fun `GIVEN a configured story WHEN the built-in stories are created THEN it is kept`() {
        val botId = "bot_${unique()}"
        val configured = story(botId, "greetings", label = "configured")
        StoryDefinitionConfigurationDynamoDAO.save(configured)

        StoryDefinitionConfigurationDynamoDAO.createBuiltInStoriesIfNotExist(
            listOf(story(botId, "greetings", label = "built-in"), story(botId, "goodbye")),
        )
        val stories = StoryDefinitionConfigurationDynamoDAO.getStoryDefinitionsByNamespaceAndBotId("app", botId).associateBy { it.storyId }
        assertEquals(configured, stories["greetings"])
        assertNotNull(stories["goodbye"])
    }

    // ---------------------------------------------------------------- features

    @Test
    fun `GIVEN an unknown feature WHEN it is checked THEN the default is returned and the feature is registered`() {
        runBlocking {
            val botId = "bot_${unique()}"
            assertTrue(FeatureDynamoDAO.isEnabled(botId, "app", "category", "name", default = true))
            assertEquals(listOf(true), FeatureDynamoDAO.getFeatures(botId, "app").map { it.enabled })
            // the registered feature is kept
            assertTrue(FeatureDynamoDAO.isEnabled(botId, "app", "category", "name", default = false))
        }
    }

    @Test
    fun `GIVEN a feature and its connector variant WHEN they are checked THEN the variant of the connector wins`() {
        runBlocking {
            val botId = "bot_${unique()}"
            FeatureDynamoDAO.disable(botId, "app", "category", "name")
            FeatureDynamoDAO.enable(botId, "app", "category", "name", null, null, applicationId = "whatsapp")
            // a feature whose name starts with the same name is another feature
            FeatureDynamoDAO.enable(botId, "app", "category", "name2", null, null)

            assertFalse(FeatureDynamoDAO.isEnabled(botId, "app", "category", "name"))
            assertTrue(FeatureDynamoDAO.isEnabled(botId, "app", "category", "name", applicationId = "whatsapp"))
            assertFalse(FeatureDynamoDAO.isEnabled(botId, "app", "category", "name", applicationId = "messenger"))
            assertEquals(
                setOf(Triple("name", null, false), Triple("name", "whatsapp", true), Triple("name2", null, true)),
                FeatureDynamoDAO.getFeatures(botId, "app").map { Triple(it.name, it.applicationId, it.enabled) }.toSet(),
            )

            // the writes of the process invalidate its cache
            FeatureDynamoDAO.deleteFeature(botId, "app", "category", "name", applicationId = "whatsapp")
            assertFalse(FeatureDynamoDAO.isEnabled(botId, "app", "category", "name", applicationId = "whatsapp"))
        }
    }

    @Test
    fun `GIVEN features with dates and a graduation WHEN they are checked THEN the dates and the users are taken into account`() {
        runBlocking {
            val botId = "bot_${unique()}"
            val now = ZonedDateTime.now()
            FeatureDynamoDAO.enable(botId, "app", "category", "past", now.minusDays(2), now.minusDays(1))
            FeatureDynamoDAO.enable(botId, "app", "category", "current", now.minusDays(1), now.plusDays(1))
            FeatureDynamoDAO.enable(botId, "app", "category", "nobody", null, null, graduation = 0)

            assertFalse(FeatureDynamoDAO.isEnabled(botId, "app", "category", "past"))
            assertTrue(FeatureDynamoDAO.isEnabled(botId, "app", "category", "current"))
            assertFalse(FeatureDynamoDAO.isEnabled(botId, "app", "category", "nobody", userId = "user"))
            assertTrue(FeatureDynamoDAO.isEnabled(botId, "app", "category", "nobody"))
            val current = FeatureDynamoDAO.getFeatures(botId, "app").first { it.name == "current" }
            assertEquals(now.minusDays(1).toInstant(), current.startDate?.toInstant())
        }
    }

    // ---------------------------------------------------------------- labels

    private fun label(
        namespace: String,
        text: String = "hello",
        validated: Boolean = true,
        category: String = "test",
    ) = I18nLabel(
        _id = "${namespace}_${unique()}".toId(),
        namespace = namespace,
        category = category,
        i18n = linkedSetOf(I18nLocalizedLabel(Locale.FRENCH, textChat, "bonjour", true), I18nLocalizedLabel(Locale.ENGLISH, textChat, text, validated)),
    )

    @Test
    fun `GIVEN saved labels WHEN they are read by id and by namespace THEN they are back with their default label`() {
        val namespace = "ns_${unique()}"
        val label = label(namespace)
        I18nDynamoDAO.save(label)
        I18nDynamoDAO.save(label("other_$namespace"))

        val stored = assertNotNull(I18nDynamoDAO.getLabelById(label._id))
        assertEquals("hello", stored.defaultLabel)
        // the localized labels are sorted by language
        assertEquals(listOf(Locale.ENGLISH, Locale.FRENCH), stored.i18n.map { it.locale })
        assertEquals(listOf(stored), I18nDynamoDAO.getLabels(namespace))

        // the label moves to another namespace
        I18nDynamoDAO.save(stored.copy(namespace = "moved_$namespace"))
        assertEquals(emptyList(), I18nDynamoDAO.getLabels(namespace))
        assertEquals(listOf(label._id), I18nDynamoDAO.getLabels("moved_$namespace").map { it._id })

        I18nDynamoDAO.deleteByNamespaceAndId("moved_$namespace", label._id)
        assertNull(I18nDynamoDAO.getLabelById(label._id))
        assertEquals(emptyList(), I18nDynamoDAO.getLabels("moved_$namespace"))
    }

    @Test
    fun `GIVEN existing labels WHEN labels are saved if they do not exist THEN only the new ones are stored`() {
        val namespace = "ns_${unique()}"
        val existing = label(namespace, text = "existing")
        I18nDynamoDAO.save(existing)

        I18nDynamoDAO.saveIfNotExist(listOf(existing.copy(i18n = linkedSetOf(I18nLocalizedLabel(Locale.ENGLISH, textChat, "replaced", true))), label(namespace, text = "new")))
        assertEquals(setOf("existing", "new"), I18nDynamoDAO.getLabels(namespace).map { it.defaultLabel }.toSet())
    }

    @Test
    fun `GIVEN labels WHEN they are filtered THEN the filters of the Mongo DAO apply`() {
        val namespace = "ns_${unique()}"
        val hello = label(namespace, text = "Hello world")
        val notValidated = label(namespace, text = "goodbye", validated = false, category = "other")
        I18nDynamoDAO.save(hello)
        I18nDynamoDAO.save(notValidated)

        assertEquals(listOf(hello._id), I18nDynamoDAO.getLabels(namespace, I18nLabelFilter(label = "WORLD")).map { it._id })
        assertEquals(listOf(notValidated._id), I18nDynamoDAO.getLabels(namespace, I18nLabelFilter(category = "other")).map { it._id })
        // both have a validated french label: none is "not validated"
        assertEquals(2, I18nDynamoDAO.getLabels(namespace, I18nLabelFilter(state = ai.tock.translator.I18nLabelStateFilter.VALIDATED)).size)

        // used since yesterday: hello is filtered out of the labels not used since yesterday
        I18nDynamoDAO.incrementLabelStat(I18nLabelStat(hello._id, namespace, Locale.ENGLISH, textChat, null))
        assertEquals(
            listOf(notValidated._id),
            I18nDynamoDAO.getLabels(namespace, I18nLabelFilter(notUsedSince = Instant.now().minusSeconds(86_400))).map { it._id },
        )
    }

    @Test
    fun `GIVEN label uses WHEN the statistics are incremented THEN they add up`() {
        val namespace = "ns_${unique()}"
        val id = newId<I18nLabel>()
        repeat(3) { I18nDynamoDAO.incrementLabelStat(I18nLabelStat(id, namespace, Locale.ENGLISH, textChat, "whatsapp", count = 2)) }
        I18nDynamoDAO.incrementLabelStat(I18nLabelStat(id, namespace, Locale.FRENCH, textChat, null))

        val stats = I18nDynamoDAO.getLabelStats(namespace).associateBy { it.locale }
        assertEquals(6, stats[Locale.ENGLISH]?.count)
        assertEquals("whatsapp", stats[Locale.ENGLISH]?.connectorId)
        assertEquals(1, stats[Locale.FRENCH]?.count)
        assertEquals(setOf(id), I18nDynamoDAO.getLabelIdsFromStats(namespace, Instant.now().minusSeconds(60)))
    }

    @Test
    fun `GIVEN used alternatives WHEN they are read and deleted THEN only the ones of the context are concerned`() {
        val label = label("ns_${unique()}")
        val localized = label.i18n.first()
        I18nDynamoDAO.addAlternativeIndex(label, localized, 0, "user1")
        I18nDynamoDAO.addAlternativeIndex(label, localized, 2, "user1")
        I18nDynamoDAO.addAlternativeIndex(label, localized, 1, "user2")

        assertEquals(setOf(0, 2), I18nDynamoDAO.getAlternativeIndexes(label, localized, "user1"))
        I18nDynamoDAO.deleteAlternativeIndexes(label, localized, "user1")
        assertEquals(emptySet(), I18nDynamoDAO.getAlternativeIndexes(label, localized, "user1"))
        assertEquals(setOf(1), I18nDynamoDAO.getAlternativeIndexes(label, localized, "user2"))
    }

    // ---------------------------------------------------------------- WhatsApp payloads

    @Test
    fun `GIVEN a stored payload WHEN it is read THEN it is back, and a short payload is its own id`() {
        val id = UUID.randomUUID().toString()
        PayloadWhatsAppCloudDynamoDAO.save(PayloadWhatsAppCloud(id, "a long payload", Date()))
        assertEquals("a long payload", PayloadWhatsAppCloudDynamoDAO.getPayloadById(id))
        assertEquals("short", PayloadWhatsAppCloudDynamoDAO.getPayloadById("short"))
        assertNull(PayloadWhatsAppCloudDynamoDAO.getPayloadById(UUID.randomUUID().toString()))

        // expired, not yet removed by the TTL
        val expired = UUID.randomUUID().toString()
        PayloadWhatsAppCloudDynamoDAO.save(PayloadWhatsAppCloud(expired, "expired", Date.from(Instant.now().minusSeconds(11L * 24 * 3600))))
        assertNull(PayloadWhatsAppCloudDynamoDAO.getPayloadById(expired))
    }
}
