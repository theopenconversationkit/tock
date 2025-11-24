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

package ai.tock.bot.admin

import ai.tock.bot.admin.answer.AnswerConfigurationType
import ai.tock.bot.admin.story.StoryDefinitionConfiguration
import ai.tock.bot.admin.story.StoryDefinitionConfigurationDAO
import ai.tock.bot.definition.IntentWithoutNamespace
import ai.tock.nlp.admin.AdminService
import ai.tock.nlp.front.service.storage.FaqDefinitionDAO
import ai.tock.nlp.front.shared.config.ApplicationDefinition
import ai.tock.nlp.front.shared.config.EntityDefinition
import ai.tock.nlp.front.shared.config.FaqDefinition
import ai.tock.nlp.front.shared.config.FaqSettings
import ai.tock.nlp.front.shared.config.IntentDefinition
import ai.tock.shared.tockInternalInjector
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.KodeinInjector
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.provider
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.litote.kmongo.toId
import java.time.Instant
import kotlin.test.assertEquals

class FaqAdminServiceSettingsTest : AbstractTest() {
    private val faqDefinitionDAO: FaqDefinitionDAO = mockk(relaxed = false)

    init {
        // IOC
        tockInternalInjector = KodeinInjector()
        val specificModule =
            Kodein.Module {
                bind<StoryDefinitionConfigurationDAO>() with provider { storyDefinitionDAO }
                bind<FaqDefinitionDAO>() with provider { faqDefinitionDAO }
            }
        tockInternalInjector.inject(
            Kodein {
                import(defaultModulesBinding())
                import(specificModule)
            },
        )
    }

    private val namespace = "test"
    val applicationDefinition = ApplicationDefinition("my App", namespace = namespace)

    private fun initMock(
        intent: IntentDefinition,
        faqs: List<FaqDefinition>,
        stories: List<StoryDefinitionConfiguration>,
    ) {
        every { faqDefinitionDAO.getFaqDefinitionByBotIdAndNamespace(any(), any()) } answers { faqs }

        every { AdminService.front.getIntentById(any()) } answers { intent }

        every {
            storyDefinitionDAO.getConfiguredStoryDefinitionByNamespaceAndBotIdAndIntent(
                any(),
                any(),
                any(),
            )
        } returnsMany stories

        every { storyDefinitionDAO.save(any()) } just Runs
    }

    private fun generateIntentDefinition(intentId: String): IntentDefinition {
        return IntentDefinition(
            _id = intentId.toId(),
            name = "name-$intentId",
            namespace = "namespace-$intentId",
            applications = setOf("appId-$intentId".toId()),
            entities = emptySet<EntityDefinition>(),
        )
    }

    private fun generateStory(
        intent: IntentDefinition,
        storyId: String,
    ): StoryDefinitionConfiguration {
        return StoryDefinitionConfiguration(
            _id = storyId.toId(),
            storyId = storyId,
            botId = "bot-$storyId",
            intent = IntentWithoutNamespace(intent.name),
            currentType = AnswerConfigurationType.simple,
            answers = emptyList(),
        )
    }

    private fun generateFAQ(
        intent: IntentDefinition,
        faqId: String,
    ): FaqDefinition {
        return FaqDefinition(
            _id = faqId.toId(),
            "botId",
            namespace,
            intentId = intent._id,
            i18nId = "i18nId-$faqId".toId(),
            emptyList(), true, Instant.now(), Instant.now(),
        )
    }

    @BeforeEach
    override fun initMocks() {
        val intent0 = generateIntentDefinition("intentId0")
        val intent1 = generateIntentDefinition("intentId1")
        val intent2 = generateIntentDefinition("intentId2")
        initMock(
            intent = intent0,
            faqs = listOf(generateFAQ(intent1, "faq1"), generateFAQ(intent2, "faq2")),
            stories = listOf(generateStory(intent1, "story1"), generateStory(intent2, "story2")),
        )
    }

    @Test
    fun `GIVEN save faq settings WHEN satisfaction enabled THEN add ending rule to the story`() {
        val faqSettings =
            FaqSettings(
                applicationId = "appId".toId(),
                satisfactionEnabled = true,
                satisfactionStoryId = "mySatisfactionStoryId",
                creationDate = Instant.now(),
                updateDate = Instant.now(),
            )
        val slotStory = mutableListOf<StoryDefinitionConfiguration>()

        val faqAdminService = spyk<FaqAdminService>(recordPrivateCalls = true)
        faqAdminService.updateAllFaqStoryWithSettings(applicationDefinition, faqSettings)

        verify(exactly = 2) { storyDefinitionDAO.save(capture(slotStory)) }

        assertEquals(slotStory.size, 2)
        assertEquals(slotStory[0].features.size, 1)
        assertEquals(
            slotStory[0].features.count { feature -> feature.endWithStoryId == faqSettings.satisfactionStoryId },
            1,
        )
        assertEquals(slotStory[1].features.size, 1)
        assertEquals(
            slotStory[1].features.count { feature -> feature.endWithStoryId == faqSettings.satisfactionStoryId },
            1,
        )
    }

    @Test
    fun `GIVEN save faq settings WHEN satisfaction disabled THEN remove ending rule from the story`() {
        val faqSettings =
            FaqSettings(
                applicationId = "appId".toId(),
                satisfactionEnabled = false,
                satisfactionStoryId = null,
                creationDate = Instant.now(),
                updateDate = Instant.now(),
            )
        val slotStory = mutableListOf<StoryDefinitionConfiguration>()

        val faqAdminService = spyk<FaqAdminService>(recordPrivateCalls = true)
        faqAdminService.updateAllFaqStoryWithSettings(applicationDefinition, faqSettings)

        verify(exactly = 2) { storyDefinitionDAO.save(capture(slotStory)) }

        assertEquals(slotStory.size, 2)
        assertEquals(slotStory[0].features.size, 0)
        assertEquals(slotStory[1].features.size, 0)
    }
}
