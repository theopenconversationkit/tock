/*
 * Copyright (C) 2017/2019 e-voyageurs technologies
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
import ai.tock.bot.admin.bot.BotApplicationConfiguration
import ai.tock.bot.admin.bot.BotApplicationConfigurationDAO
import ai.tock.bot.admin.dialog.DialogReportDAO
import ai.tock.bot.admin.model.BotAnswerConfiguration
import ai.tock.bot.admin.model.BotStoryDefinitionConfiguration
import ai.tock.bot.admin.story.StoryDefinitionConfiguration
import ai.tock.bot.admin.story.StoryDefinitionConfigurationDAO
import ai.tock.bot.admin.user.UserReportDAO
import ai.tock.bot.connector.ConnectorType
import ai.tock.bot.definition.IntentWithoutNamespace
import ai.tock.bot.engine.feature.FeatureDAO
import ai.tock.nlp.front.shared.*
import ai.tock.nlp.front.shared.codec.alexa.AlexaCodec
import ai.tock.shared.tockInternalInjector
import ai.tock.shared.vertx.BadRequestException
import com.github.salomonbrys.kodein.*
import io.mockk.*
import org.junit.jupiter.api.*
import org.litote.kmongo.Id
import org.litote.kmongo.newId
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals

class BotAdminServiceTest {

    companion object {


        fun BotStoryDefinitionConfiguration.toStoryDefinitionConfiguration(): StoryDefinitionConfiguration {
            return StoryDefinitionConfiguration(
                    storyId = storyId,
                    botId = botId,
                    intent = intent,
                    currentType = currentType,
                    namespace = namespace,
                    answers = emptyList(),
                    userSentenceLocale = userSentenceLocale,
                    _id = _id
            )
        }

        fun newTestStory(storyId: String, type: AnswerConfigurationType, _id: Id<StoryDefinitionConfiguration> = newId(), name: String = storyId): BotStoryDefinitionConfiguration {
            return BotStoryDefinitionConfiguration(
                    storyId = storyId,
                    botId = "testBotId",
                    intent = IntentWithoutNamespace("testIntent"),
                    currentType = type,
                    namespace = "testNamespace",
                    answers = emptyList<BotAnswerConfiguration>(),
                    userSentenceLocale = Locale.FRANCE,
                    _id = _id,
                    name = name
            )
        }

        val aApplication = BotApplicationConfiguration(
                applicationId = "testApplicationId",
                botId = "testBotId",
                namespace = "testNamespace",
                nlpModel = "testNlpModel",
                connectorType = ConnectorType.rest
        )

        val aBuiltinStory = newTestStory("testBuiltinStory", AnswerConfigurationType.builtin)
        val aMessageStory = newTestStory("testMessageStory", AnswerConfigurationType.message)

        val storyDefinitionDAO: StoryDefinitionConfigurationDAO = mockk(relaxed = false)
        val applicationConfigurationDAO: BotApplicationConfigurationDAO = mockk(relaxed = false)

        init {
            // IOC
            tockInternalInjector = KodeinInjector()
            val module = Kodein.Module {
                bind<StoryDefinitionConfigurationDAO>() with provider { storyDefinitionDAO }
                bind<ApplicationConfiguration>() with provider { mockk<ApplicationConfiguration>(relaxed = true) }
                bind<UserReportDAO>() with provider { mockk<UserReportDAO>(relaxed = true) }
                bind<DialogReportDAO>() with provider { mockk<DialogReportDAO>(relaxed = true) }
                bind<BotApplicationConfigurationDAO>() with provider { applicationConfigurationDAO }
                bind<FeatureDAO>() with provider { mockk<FeatureDAO>(relaxed = true) }
                bind<Parser>() with provider { mockk<Parser>(relaxed = true) }
                bind<ModelUpdater>() with provider { mockk<ModelUpdater>(relaxed = true) }
                bind<ApplicationCodec>() with provider { mockk<ApplicationCodec>(relaxed = true) }
                bind<AlexaCodec>() with provider { mockk<AlexaCodec>(relaxed = true) }
                bind<ApplicationMonitor>() with provider { mockk<ApplicationMonitor>(relaxed = true) }
                bind<ModelTester>() with provider { mockk<ModelTester>(relaxed = true) }
            }
            tockInternalInjector.inject(Kodein {
                import(module)
            })
        }
    }

    @BeforeEach
    internal fun initMocks() {
        every { applicationConfigurationDAO.getConfigurationsByNamespaceAndBotId(any(), any()) } answers { listOf(aApplication) }
        every { storyDefinitionDAO.delete(any()) } answers { null }
        every { storyDefinitionDAO.save(any()) } answers { null }
    }

    @AfterEach
    internal fun clearMocks() {
        clearAllMocks()
    }

    @Nested
    inner class SingleExistingStoryTest {

        internal fun initMocksForSingleStory(existingStory: StoryDefinitionConfiguration) {
            every { storyDefinitionDAO.getStoryDefinitionById(existingStory._id) } answers { existingStory }
            every { storyDefinitionDAO.getStoryDefinitionById(neq(existingStory._id)) } answers { null }
            every { storyDefinitionDAO.getStoryDefinitionByNamespaceAndBotIdAndTypeAndIntent(any(), any(), existingStory.currentType, any()) } answers { existingStory }
            every { storyDefinitionDAO.getStoryDefinitionByNamespaceAndBotIdAndTypeAndStoryId(any(), any(), existingStory.currentType, any()) } answers { existingStory }
            every { storyDefinitionDAO.getStoryDefinitionByNamespaceAndBotIdAndTypeAndIntent(any(), any(), neq(existingStory.currentType), any()) } answers { null }
            every { storyDefinitionDAO.getStoryDefinitionByNamespaceAndBotIdAndTypeAndStoryId(any(), any(), neq(existingStory.currentType), any()) } answers { null }
        }

        @Nested
        inner class ExistingBuiltinStoryTest {

            val existingStory = aBuiltinStory.toStoryDefinitionConfiguration()

            @BeforeEach
            internal fun initMocks() {
                initMocksForSingleStory(existingStory)
            }

            @Test
            fun `GIVEN builtin story exists WHEN saving the same story THEN update the story`() {

                val theNewStory = aBuiltinStory.copy(name = "otherName")

                BotAdminService.saveStory(existingStory.namespace, theNewStory, "testUser")

                verify(exactly = 0) { storyDefinitionDAO.delete(existingStory) }

                val slot = slot<StoryDefinitionConfiguration>()
                verify { storyDefinitionDAO.save(capture(slot)) }

                assertEquals(slot.captured.storyId, theNewStory.storyId)
                assertNotEquals(slot.captured.name, existingStory.name)
                assertEquals(slot.captured.name, theNewStory.name)
                assertEquals(slot.captured._id, existingStory._id)
            }

            @Test
            fun `GIVEN builtin story exists WHEN saving the same story with different _id THEN update the story`() {

                val theNewStory = aBuiltinStory.copy(_id = newId(), name = "otherName")

                BotAdminService.saveStory(existingStory.namespace, theNewStory, "testUser")

                verify(exactly = 0) { storyDefinitionDAO.delete(existingStory) }

                val slot = slot<StoryDefinitionConfiguration>()
                verify { storyDefinitionDAO.save(capture(slot)) }

                assertEquals(slot.captured.storyId, theNewStory.storyId)
                assertNotEquals(slot.captured.name, existingStory.name)
                assertEquals(slot.captured.name, theNewStory.name)
                assertEquals(slot.captured._id, existingStory._id)
            }

            @Test
            fun `GIVEN builtin story exists WHEN saving a configured story with same _id THEN fail with BadRequestException`() {

                val theNewStory = aBuiltinStory.copy(currentType = AnswerConfigurationType.message)

                assertFailsWith<BadRequestException> { BotAdminService.saveStory(theNewStory.namespace, theNewStory, "testUser") }

                verify(exactly = 0) { storyDefinitionDAO.delete(any()) }
                verify(exactly = 0) { storyDefinitionDAO.save(any()) }
            }

            @Test
            fun `GIVEN builtin story exists WHEN saving a configured story with different _id THEN add the story`() {

                val theNewStory = aBuiltinStory.copy(currentType = AnswerConfigurationType.message, _id = newId())

                BotAdminService.saveStory(existingStory.namespace, theNewStory, "testUser")

                verify(exactly = 0) { storyDefinitionDAO.delete(existingStory) }

                val slot = slot<StoryDefinitionConfiguration>()
                verify { storyDefinitionDAO.save(capture(slot)) }

                assertEquals(slot.captured.storyId, theNewStory.storyId)
                assertEquals(slot.captured.currentType, theNewStory.currentType)
                assertNotEquals(slot.captured._id, existingStory._id)
            }
        }

        @Nested
        inner class ExistingConfiguredStoryTest {

            val existingStory = aMessageStory.toStoryDefinitionConfiguration()

            @BeforeEach
            internal fun initMocks() {
                initMocksForSingleStory(existingStory)
            }

            @Test
            fun `GIVEN configured story exists WHEN saving the same story THEN update the story`() {

                val theNewStory = aMessageStory.copy(name = "otherName")

                BotAdminService.saveStory(existingStory.namespace, theNewStory, "testUser")

                verify(exactly = 0) { storyDefinitionDAO.delete(existingStory) }

                val slot = slot<StoryDefinitionConfiguration>()
                verify { storyDefinitionDAO.save(capture(slot)) }

                assertEquals(slot.captured.storyId, theNewStory.storyId)
                assertNotEquals(slot.captured.name, existingStory.name)
                assertEquals(slot.captured.name, theNewStory.name)
                assertEquals(slot.captured._id, existingStory._id)
            }

            @Test
            fun `GIVEN configured story exists WHEN saving the same story with different _id THEN fail with BadRequestException`() {

                val theNewStory = aMessageStory.copy(_id = newId(), name = "otherName")

                assertFailsWith<BadRequestException> { BotAdminService.saveStory(existingStory.namespace, theNewStory, "testUser") }

                verify(exactly = 0) { storyDefinitionDAO.delete(existingStory) }
                verify(exactly = 0) { storyDefinitionDAO.save(any()) }
            }

            @Test
            fun `GIVEN configured story exists WHEN saving a builtin story with same _id THEN fail with BadRequestException`() {

                val theNewStory = aMessageStory.copy(currentType = AnswerConfigurationType.builtin)

                assertFailsWith<BadRequestException> { BotAdminService.saveStory(theNewStory.namespace, theNewStory, "testUser") }

                verify(exactly = 0) { storyDefinitionDAO.delete(any()) }
                verify(exactly = 0) { storyDefinitionDAO.save(any()) }
            }

            @Test
            fun `GIVEN configured story exists WHEN saving a builtin story with different _id THEN add the story`() {

                val theNewStory = aMessageStory.copy(currentType = AnswerConfigurationType.builtin, _id = newId())

                BotAdminService.saveStory(existingStory.namespace, theNewStory, "testUser")

                verify(exactly = 0) { storyDefinitionDAO.delete(existingStory) }

                val slot = slot<StoryDefinitionConfiguration>()
                verify { storyDefinitionDAO.save(capture(slot)) }

                assertEquals(slot.captured.storyId, theNewStory.storyId)
                assertEquals(slot.captured.currentType, theNewStory.currentType)
                assertNotEquals(slot.captured._id, existingStory._id)
            }
        }
    }

    @Nested
    inner class BothExistingStoriesTest {

        // Bot builtin and configured stories already exist

        val existingBuiltinStory = aBuiltinStory.toStoryDefinitionConfiguration()

        val existingMessageStory = aMessageStory.toStoryDefinitionConfiguration()

        @BeforeEach
        internal fun initMocks() {
            every { storyDefinitionDAO.getStoryDefinitionById(existingBuiltinStory._id) } answers { existingBuiltinStory }
            every { storyDefinitionDAO.getStoryDefinitionById(existingMessageStory._id) } answers { existingMessageStory }
            every { storyDefinitionDAO.getStoryDefinitionById(and(neq(existingBuiltinStory._id), neq(existingMessageStory._id))) } answers { null }
            every { storyDefinitionDAO.getStoryDefinitionByNamespaceAndBotIdAndTypeAndIntent(any(), any(), AnswerConfigurationType.builtin, any()) } answers { existingBuiltinStory }
            every { storyDefinitionDAO.getStoryDefinitionByNamespaceAndBotIdAndTypeAndStoryId(any(), any(), AnswerConfigurationType.builtin, any()) } answers { existingBuiltinStory }
            every { storyDefinitionDAO.getStoryDefinitionByNamespaceAndBotIdAndTypeAndIntent(any(), any(), AnswerConfigurationType.message, any()) } answers { existingMessageStory }
            every { storyDefinitionDAO.getStoryDefinitionByNamespaceAndBotIdAndTypeAndStoryId(any(), any(), AnswerConfigurationType.message, any()) } answers { existingMessageStory }
        }

        @Nested
        inner class SaveBuiltinStoryTest {

            @Test
            fun `GIVEN builtin and configured story exist WHEN saving the same builtin story THEN update the builtin story`() {

                val theNewStory = aBuiltinStory.copy(name = "otherName")

                BotAdminService.saveStory(theNewStory.namespace, theNewStory, "testUser")

                verify(exactly = 0) { storyDefinitionDAO.delete(existingBuiltinStory) }
                verify(exactly = 0) { storyDefinitionDAO.delete(existingMessageStory) }

                val slot = slot<StoryDefinitionConfiguration>()
                verify { storyDefinitionDAO.save(capture(slot)) }

                assertEquals(slot.captured.storyId, theNewStory.storyId)
                assertEquals(slot.captured.currentType, theNewStory.currentType)
                assertEquals(slot.captured.name, theNewStory.name)
            }

            @Test
            fun `GIVEN builtin and configured story exist WHEN saving the same builtin story with different _id THEN update the builtin story`() {

                val theNewStory = aBuiltinStory.copy(_id = newId(), name = "otherName")

                BotAdminService.saveStory(theNewStory.namespace, theNewStory, "testUser")

                verify(exactly = 0) { storyDefinitionDAO.delete(existingBuiltinStory) }
                verify(exactly = 0) { storyDefinitionDAO.delete(existingMessageStory) }

                val slot = slot<StoryDefinitionConfiguration>()
                verify { storyDefinitionDAO.save(capture(slot)) }

                assertEquals(slot.captured.storyId, theNewStory.storyId)
                assertEquals(slot.captured.currentType, theNewStory.currentType)
                assertEquals(slot.captured.name, theNewStory.name)
            }
        }

        @Nested
        inner class SaveConfiguredStoryTest {

            @Test
            fun `GIVEN builtin and configured story exist WHEN saving the same configured story THEN update the configured story`() {

                val theNewStory = aMessageStory.copy(name = "otherName")

                BotAdminService.saveStory(theNewStory.namespace, theNewStory, "testUser")

                verify(exactly = 0) { storyDefinitionDAO.delete(existingBuiltinStory) }
                verify(exactly = 0) { storyDefinitionDAO.delete(existingMessageStory) }

                val slot = slot<StoryDefinitionConfiguration>()
                verify { storyDefinitionDAO.save(capture(slot)) }

                assertEquals(slot.captured.storyId, theNewStory.storyId)
                assertEquals(slot.captured.currentType, theNewStory.currentType)
                assertEquals(slot.captured.name, theNewStory.name)
                assertEquals(slot.captured._id, aMessageStory._id)
            }

            @Test
            fun `GIVEN builtin and configured story exist WHEN saving the same configured story with different _id THEN fail with BadRequestException`() {

                val theNewStory = aMessageStory.copy(_id = newId(), name = "otherName")

                assertFailsWith<BadRequestException> { BotAdminService.saveStory(theNewStory.namespace, theNewStory, "testUser") }

                verify(exactly = 0) { storyDefinitionDAO.delete(existingMessageStory) }
                verify(exactly = 0) { storyDefinitionDAO.save(any()) }
            }
        }
    }
}