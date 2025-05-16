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
import ai.tock.bot.admin.answer.AnswerConfigurationType.simple
import ai.tock.bot.admin.model.SummaryStorySearchRequest
import ai.tock.bot.admin.story.StoryDefinitionConfiguration
import ai.tock.bot.admin.story.StoryDefinitionConfigurationDAO
import ai.tock.bot.admin.story.StoryDefinitionConfigurationSummaryMinimumMetrics
import ai.tock.bot.admin.story.dump.StoryDefinitionConfigurationDump
import ai.tock.bot.definition.IntentWithoutNamespace
import ai.tock.nlp.front.shared.config.ApplicationDefinition
import ai.tock.shared.exception.rest.BadRequestException
import ai.tock.shared.tockInternalInjector
import ai.tock.translator.I18nDAO
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.KodeinInjector
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.provider
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.litote.kmongo.newId
import org.litote.kmongo.toId
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class BotAdminServiceTest : AbstractTest() {

    companion object {
        init {
            val i18nDAO: I18nDAO = mockk(relaxed = true)
            // IOC
            tockInternalInjector = KodeinInjector()
            val specificModule = Kodein.Module {
                bind<StoryDefinitionConfigurationDAO>() with provider { storyDefinitionDAO }
                bind<I18nDAO>() with provider { i18nDAO }

            }
            tockInternalInjector.inject(
                Kodein {
                    import(defaultModulesBinding())
                    import(specificModule)
                }
            )
        }
    }

    @Nested
    inner class SingleExistingStoryTest {

        internal fun initMocksForSingleStory(existingStory: StoryDefinitionConfiguration) {
            every { storyDefinitionDAO.getStoryDefinitionById(existingStory._id) } answers { existingStory }
            every { storyDefinitionDAO.getStoryDefinitionById(neq(existingStory._id)) } answers { null }
            every {
                storyDefinitionDAO.getStoryDefinitionByNamespaceAndBotIdAndIntent(
                    any(),
                    any(),
                    any()
                )
            } answers { null }
            every {
                storyDefinitionDAO.getStoryDefinitionByNamespaceAndBotIdAndStoryId(
                    any(),
                    any(),
                    any()
                )
            } answers { null }
            every {
                storyDefinitionDAO.getStoryDefinitionByNamespaceAndBotIdAndIntent(
                    any(),
                    any(),
                    existingStory.intent.name
                )
            } answers { existingStory }
            every {
                storyDefinitionDAO.getStoryDefinitionByNamespaceAndBotIdAndStoryId(
                    any(),
                    any(),
                    existingStory.storyId
                )
            } answers { existingStory }
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
            fun `GIVEN builtin story exists WHEN saving a configured story with same _id THEN update the story`() {

                val theNewStory =
                    aBuiltinStory.copy(_id = aBuiltinStory._id, currentType = AnswerConfigurationType.message)

                BotAdminService.saveStory(theNewStory.namespace, theNewStory, "testUser")

                verify(exactly = 0) { storyDefinitionDAO.delete(any()) }

                val slot = slot<StoryDefinitionConfiguration>()
                verify { storyDefinitionDAO.save(capture(slot)) }

                assertEquals(slot.captured.storyId, theNewStory.storyId)
                assertEquals(slot.captured.currentType, theNewStory.currentType)
                assertEquals(slot.captured._id, existingStory._id)
            }

            @Test
            fun `GIVEN builtin story exists WHEN saving a configured story with different _id THEN update the existing story`() {

                val theNewStory = aBuiltinStory.copy(currentType = AnswerConfigurationType.message, _id = newId())

                BotAdminService.saveStory(existingStory.namespace, theNewStory, "testUser")

                verify(exactly = 0) { storyDefinitionDAO.delete(existingStory) }

                val slot = slot<StoryDefinitionConfiguration>()
                verify { storyDefinitionDAO.save(capture(slot)) }

                assertEquals(slot.captured.storyId, theNewStory.storyId)
                assertEquals(slot.captured.currentType, theNewStory.currentType)
                assertEquals(slot.captured._id, existingStory._id)
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

                assertFailsWith<BadRequestException> {
                    BotAdminService.saveStory(
                        existingStory.namespace,
                        theNewStory,
                        "testUser"
                    )
                }

                verify(exactly = 0) { storyDefinitionDAO.delete(existingStory) }
                verify(exactly = 0) { storyDefinitionDAO.save(any()) }
            }

            @Test
            fun `GIVEN configured story exists WHEN saving a builtin story with same _id THEN fail with BadRequestException`() {

                val theNewStory =
                    aMessageStory.copy(_id = aMessageStory._id, currentType = AnswerConfigurationType.builtin)

                assertFailsWith<BadRequestException> {
                    BotAdminService.saveStory(
                        theNewStory.namespace,
                        theNewStory,
                        "testUser"
                    )
                }

                verify(exactly = 0) { storyDefinitionDAO.delete(any()) }
                verify(exactly = 0) { storyDefinitionDAO.save(any()) }
            }

            @Test
            fun `GIVEN configured story exists WHEN saving a builtin story with different _id THEN fail with BadRequestException`() {
                val theNewStory = aMessageStory.copy(
                    currentType = AnswerConfigurationType.builtin,
                    _id = newId(),
                    intent = IntentWithoutNamespace("test2")
                )

                assertFailsWith<BadRequestException> {
                    BotAdminService.saveStory(
                        theNewStory.namespace,
                        theNewStory,
                        "testUser"
                    )
                }

                verify(exactly = 0) { storyDefinitionDAO.delete(any()) }
                verify(exactly = 0) { storyDefinitionDAO.save(any()) }
            }

            @Test
            internal fun `GIVEN a metrics story with no step handling metric WHEN saving THEN a badRequest exception is returned`() {
                assertThrows<BadRequestException> {
                    BotAdminService.saveStory(
                        existingStory.namespace,
                        aMessageStory.copy(metricStory = true),
                        "testUser"
                    )
                }
            }

            @Test
            internal fun `GIVEN a story for a given namespace WHEN summary search on it THEN the story is returned`() {

                val mockedStoryList = listOf(StoryDefinitionConfigurationSummaryMinimumMetrics("Id".toId(),"storyId",intent= IntentWithoutNamespace("myIntent"), simple, metricStory = false))
                every {
                    storyDefinitionDAO.searchStoryDefinitionSummaries(
                        SummaryStorySearchRequest("category").toSummaryRequest()
                    )
                } returns mockedStoryList

                // When
                val storiesList = BotAdminService.searchSummaryStories(SummaryStorySearchRequest("category"))

                // Then
                assertEquals(storiesList, mockedStoryList)
            }
        }
    }

    @Test
    internal fun `GIVEN an existing story in a given namespace WHEN exporting it THEN a dump is returned`() {
        // Given
        val namespace = "namespace"
        val applicationName = "applicationName"
        val storyDefinitionId = "storyDefinitionId"

        every {
            applicationDefininitionDAO.getApplicationByNamespaceAndName(namespace, applicationName)
        } returns ApplicationDefinition(applicationName, applicationName, namespace)
        every {
            applicationConfigurationDAO.getConfigurationsByNamespaceAndNlpModel(namespace, applicationName)
        } returns listOf(aApplication)
        every {
            storyDefinitionDAO.getStoryDefinitionByNamespaceAndBotIdAndStoryId(namespace, any(), storyDefinitionId)
        } returns aBuiltinStory.toStoryDefinitionConfiguration()

        // When
        val story = BotAdminService.exportStory(namespace, applicationName, storyDefinitionId)

        // Then
        assertNotNull(story)
        assertEquals(StoryDefinitionConfigurationDump(aBuiltinStory.toStoryDefinitionConfiguration()), story)
    }

    @Test
    internal fun `GIVEN an existing story but a wrong given namespace WHEN exporting it THEN a null dump is returned`() {
        // Given
        val namespace = "namespace"
        val applicationName = "applicationName"
        val storyDefinitionId = "storyDefinitionId"

        every {
            applicationDefininitionDAO.getApplicationByNamespaceAndName(namespace, applicationName)
        } returns null
        every {
            storyDefinitionDAO.getStoryDefinitionByNamespaceAndBotIdAndStoryId(namespace, any(), storyDefinitionId)
        } returns aBuiltinStory.toStoryDefinitionConfiguration()

        // When
        val story = BotAdminService.exportStory(namespace, applicationName, storyDefinitionId)

        // Then
        assertNull(story)
    }

    @Test
    internal fun `GIVEN a non-existing story for a given namespace WHEN exporting it THEN a null dump is returned`() {
        // Given
        val namespace = "namespace"
        val applicationName = "applicationName"
        val storyDefinitionId = "storyDefinitionId"

        every {
            applicationDefininitionDAO.getApplicationByNamespaceAndName(namespace, applicationName)
        } returns ApplicationDefinition(applicationName, applicationName, namespace)
        every {
            applicationConfigurationDAO.getConfigurationsByNamespaceAndNlpModel(namespace, applicationName)
        } returns listOf(aApplication)
        every {
            storyDefinitionDAO.getStoryDefinitionByNamespaceAndBotIdAndStoryId(namespace, any(), storyDefinitionId)
        } returns null

        // When
        val story = BotAdminService.exportStory(namespace, applicationName, storyDefinitionId)

        // Then
        assertNull(story)
    }

    @Test
    internal fun `GIVEN a non-existing story for a given namespace WHEN summary search on it THEN a null dump is returned`() {

        every {
            storyDefinitionDAO.searchStoryDefinitionSummaries(
                any()
            )
        } returns emptyList()

        // When
        val storiesList = BotAdminService.searchSummaryStories(SummaryStorySearchRequest("category"))

        // Then
        assertEquals(storiesList, emptyList())
    }

}
