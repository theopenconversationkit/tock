/*
 * Copyright (C) 2017/2021 e-voyageurs technologies
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
import ai.tock.bot.admin.model.BotStoryDefinitionConfiguration
import ai.tock.bot.admin.model.FaqDefinitionRequest
import ai.tock.bot.admin.story.StoryDefinitionConfiguration
import ai.tock.bot.admin.story.StoryDefinitionConfigurationDAO
import ai.tock.bot.definition.IntentWithoutNamespace
import ai.tock.nlp.admin.AdminService
import ai.tock.nlp.front.service.storage.ClassifiedSentenceDAO
import ai.tock.nlp.front.service.storage.FaqDefinitionDAO
import ai.tock.nlp.front.service.storage.IntentDefinitionDAO
import ai.tock.nlp.front.shared.config.ApplicationDefinition
import ai.tock.nlp.front.shared.config.Classification
import ai.tock.nlp.front.shared.config.ClassifiedSentence
import ai.tock.nlp.front.shared.config.ClassifiedSentenceStatus
import ai.tock.nlp.front.shared.config.FaqDefinition
import ai.tock.nlp.front.shared.config.IntentDefinition
import ai.tock.nlp.front.shared.config.SentencesQueryResult
import ai.tock.shared.security.UserLogin
import ai.tock.shared.tockInternalInjector
import ai.tock.shared.vertx.BadRequestException
import ai.tock.translator.I18nDAO
import ai.tock.translator.I18nLabel
import ai.tock.translator.I18nLocalizedLabel
import ai.tock.translator.UserInterfaceType
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.KodeinInjector
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.provider
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.spyk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.litote.kmongo.Id
import org.litote.kmongo.newId
import org.litote.kmongo.toId
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class FaqAdminServiceTest : AbstractTest() {

    companion object {
        val faqDefinitionDAO: FaqDefinitionDAO = mockk(relaxed = false)
        val sentenceDAO: ClassifiedSentenceDAO = mockk(relaxed = false)
        val intentDAO: IntentDefinitionDAO = mockk(relaxed = false)
        val i18nDAO: I18nDAO = mockk(relaxed = false)

        init {
            // IOC
            tockInternalInjector = KodeinInjector()
            val specificModule = Kodein.Module {
                bind<StoryDefinitionConfigurationDAO>() with provider { storyDefinitionDAO }
                bind<FaqDefinitionDAO>() with provider { faqDefinitionDAO }
                bind<ClassifiedSentenceDAO>() with provider { sentenceDAO }
                bind<IntentDefinitionDAO>() with provider { intentDAO }
                bind<I18nDAO>() with provider { i18nDAO }
            }
            tockInternalInjector.inject(
                Kodein {
                    import(defaultModulesBinding())
                    import(specificModule)
                }
            )
        }

        private val applicationId = newId<ApplicationDefinition>()
        private val intentId = "idIntent".toId<IntentDefinition>()
        private val faqId = "faqDefId".toId<FaqDefinition>()
        private val i18nId = "idI18n".toId<I18nLabel>()
        private val now = Instant.now().truncatedTo(ChronoUnit.MILLIS)
        private val tagList = listOf("TAG1", "TAG2")

        private const val FAQ_CATEGORY = "faq"
        private const val namespace = "test"

        private const val userLogin: UserLogin = "userLogin"

        private val faqDefinition = FaqDefinition(faqId, intentId, i18nId, tagList, true, now, now)

        val applicationDefinition = ApplicationDefinition("my App", namespace = namespace)
        val storyId = "storyId".toId<StoryDefinitionConfiguration>()

        private val firstUterrance = "FAQ utterance A"
        private val secondUterrance = "FAQ utterance B"

        private val faqDefinitionRequest = FaqDefinitionRequest(
            faqId.toString(),
            intentId.toString(),
            Locale.FRENCH,
            applicationId,
            now,
            now,
            "FAQ TITLE",
            "FAQ desciption",
            listOf(firstUterrance, secondUterrance),
            listOf("NEW TAG"),
            "The FAQ answer",
            true
        )

        private val existingIntent = IntentDefinition(
            name = "AlreadyExistingIntent",
            namespace = namespace,
            applications = setOf(applicationId),
            label = "FAQ NEW TITLE",
            description = "FAQ new description",
            category = "faq",
            _id = intentId,
            entities = emptySet()
        )
        private val theSavedIntent = IntentDefinition(
            name = "faqtitle",
            namespace = namespace,
            applications = setOf(applicationId),
            label = "FAQ TITLE",
            description = "FAQ description",
            category = "faq",
            _id = intentId,
            entities = emptySet()
        )
        val mockedI18n = I18nLabel(
            _id = i18nId,
            namespace = namespace,
            category = "faq",
            linkedSetOf(
                I18nLocalizedLabel(
                    locale = Locale.FRENCH,
                    interfaceType = UserInterfaceType.textChat,
                    label = "The FAQ answer",
                    validated = true,
                    connectorId = null,
                    alternatives = emptyList()
                )
            ),
            defaultLabel = "The FAQ answer",
            defaultLocale = Locale.FRENCH,
            version = 0
        )
    }

    @Nested
    inner class SingleExistingFaqAndStoryTest {

        internal fun initMocksForSingleStory(existingStory: StoryDefinitionConfiguration) {
            every { storyDefinitionDAO.getStoryDefinitionById(existingStory._id) } answers { existingStory }
            every { storyDefinitionDAO.getStoryDefinitionById(eq(storyId)) } returns existingStory
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
            every {
                storyDefinitionDAO.getConfiguredStoryDefinitionByNamespaceAndBotIdAndIntent(any(), any(), any())
            } answers { existingStory }

        }

        internal fun initMocksForSingleFaq(existingFaq: FaqDefinition) {
            every { faqDefinitionDAO.getFaqDefinitionById(any()) } answers { existingFaq }
            every { faqDefinitionDAO.getFaqDefinitionByIntentId(any()) } answers { existingFaq }
            every { faqDefinitionDAO.save(any()) } just Runs
        }

        internal fun initMocksForClassifiedSentences(utterances: List<String>, existingUtterances: Boolean) {
            val answers: List<ClassifiedSentence> = utterances.map {
                ClassifiedSentence(
                    it,
                    Locale.FRENCH, applicationId, now, now, ClassifiedSentenceStatus.model, Classification(
                        intentId,
                        emptyList()
                    ), null, null
                )
            }.toList()

            every { sentenceDAO.search(any()) } answers {
                SentencesQueryResult(
                    utterances.size.toLong(),
                    if (existingUtterances) answers else emptyList()
                )
            }
            every { sentenceDAO.switchSentencesStatus(any(), ClassifiedSentenceStatus.deleted) } just Runs
        }

        fun newFaqTestStory(
            storyId: String,
            type: AnswerConfigurationType,
            intentName: String,
            _id: Id<StoryDefinitionConfiguration> = newId(),
            name: String = storyId
        ): BotStoryDefinitionConfiguration {
            return BotStoryDefinitionConfiguration(
                storyId = storyId,
                botId = "testBotId",
                intent = IntentWithoutNamespace(intentName),
                currentType = type,
                namespace = namespace,
                answers = emptyList(),
                userSentenceLocale = Locale.FRANCE,
                _id = _id,
                name = name
            )
        }


        @Nested
        inner class NewFaqWithStoryWithNewUtterancesTest {

            private val existingMessageStory =
                newFaqTestStory(namespace, AnswerConfigurationType.message, theSavedIntent.name, _id = storyId)
            private val existingStory = existingMessageStory.toStoryDefinitionConfiguration()

            @BeforeEach
            internal fun initMocks() {
                //Existing Story
                initMocksForSingleStory(existingStory)
                //Existing Faq
                initMocksForSingleFaq(faqDefinition)
                //Existing ClassifiedSentences
                initMocksForClassifiedSentences(listOf(firstUterrance, secondUterrance), false)

                every { AdminService.front.getIntentById(eq(intentId)) } returns existingIntent
                //mocked here to avoid multiple implementation for I18nDao
                every { i18nDAO.save(listOf(mockedI18n)) } just Runs
                every { i18nDAO.getLabelById(i18nId) } answers { mockedI18n }
            }

            @Test
            fun `GIVEN save faq and story WHEN intent is null THEN Throw bad request`() {

                val faqAdminService = spyk<FaqAdminService>(recordPrivateCalls = true)
                every {
                    faqAdminService["createOrUpdateIntent"](
                        allAny<FaqDefinitionRequest>(),
                        allAny<ApplicationDefinition>()
                    )
                } returns null

                assertThrows<BadRequestException> {
                    faqAdminService.saveFAQ(faqDefinitionRequest, userLogin, applicationDefinition)
                }
                val botAdminService = spyk<BotAdminService>()

                every {
                    botAdminService["getBotConfigurationsByNamespaceAndBotId"](
                        any<String>(),
                        any<String>()
                    )
                } answers { aApplication }
                every {
                    botAdminService.saveStory(
                        any<String>(),
                        any<BotStoryDefinitionConfiguration>(),
                        any<UserLogin>(),
                        any<IntentDefinition>()
                    )
                } returns existingMessageStory

                val slotStory = slot<StoryDefinitionConfiguration>()

                verify(exactly = 0) {
                    botAdminService["createOrGetIntent"](
                        any<String>(),
                        any<String>(),
                        any<Id<ApplicationDefinition>>(),
                        any<String>()
                    )
                }
                verify(exactly = 0) { faqDefinitionDAO.save(any()) }
                verify(exactly = 0) { i18nDAO.save(listOf(mockedI18n)) }
                verify(exactly = 0) { storyDefinitionDAO.save(capture(slotStory)) }
            }

            @Test
            fun `GIVEN save faq WHEN and saving the same story THEN update the story`() {
                val faqAdminService = spyk<FaqAdminService>(recordPrivateCalls = true)
                val savedFaqDefinition = FaqDefinition(faqId, intentId, i18nId, listOf("NEW TAG"), true, now, now)

                every {
                    faqAdminService["createOrUpdateIntent"](
                        allAny<FaqDefinitionRequest>(),
                        allAny<ApplicationDefinition>()
                    )
                } returns theSavedIntent
                every {
                    faqAdminService["createOrUpdateStory"](
                        allAny<FaqDefinitionRequest>(),
                        allAny<IntentDefinition>(),
                        allAny<UserLogin>(),
                        allAny<I18nLabel>(),
                        allAny<ApplicationDefinition>()
                    ) as Unit
                } just Runs

                every {
                    faqAdminService["prepareCreationOrUpdatingFaqDefinition"](
                        allAny<FaqDefinitionRequest>(),
                        allAny<IntentDefinition>(),
                        allAny<I18nLabel>(),
                        allAny<FaqDefinition>()
                    ) as FaqDefinition
                } returns savedFaqDefinition

                faqAdminService.saveFAQ(faqDefinitionRequest, userLogin, applicationDefinition)

                verify(exactly = 1) { faqDefinitionDAO.save(savedFaqDefinition) }
                verify(exactly = 1) { i18nDAO.save(listOf(mockedI18n)) }

                val slotFaq = slot<FaqDefinition>()
                verify { faqDefinitionDAO.save(capture(slotFaq)) }

                assertEquals(slotFaq.captured._id, faqDefinitionRequest.id?.toId())
                assertEquals(slotFaq.captured.tags, faqDefinitionRequest.tags)
            }
        }

        @Nested
        inner class ExistingFaqWithStoryWithoutNewUtterancesTest {
            private val existingMessageStory =
                newFaqTestStory(namespace, AnswerConfigurationType.message, theSavedIntent.name, _id = storyId)
            private val existingStory = existingMessageStory.toStoryDefinitionConfiguration()

            @BeforeEach
            internal fun initMocks() {
                //Existing Story
                initMocksForSingleStory(existingStory)
                //Existing Faq
                initMocksForSingleFaq(faqDefinition)
                //Existing ClassifiedSentences
                initMocksForClassifiedSentences(listOf(firstUterrance, secondUterrance), true)

                every { AdminService.front.getIntentById(eq(intentId)) } returns existingIntent
                //mocked here to avoid multiple implementation for I18nDao
                every { i18nDAO.save(listOf(mockedI18n)) } just Runs
                every { i18nDAO.getLabelById(i18nId) } answers { mockedI18n }
            }

            @Test
            fun `GIVEN save faq and story When existing Faq THEN save the story`() {

                val faqAdminService = spyk<FaqAdminService>(recordPrivateCalls = true)
                every {
                    faqAdminService["createOrUpdateIntent"](
                        allAny<FaqDefinitionRequest>(),
                        allAny<ApplicationDefinition>()
                    )
                } returns existingIntent

                faqAdminService.saveFAQ(faqDefinitionRequest, userLogin, applicationDefinition)

                val botAdminService = spyk<BotAdminService>()
                every {
                    botAdminService["getBotConfigurationsByNamespaceAndBotId"](
                        any<String>(),
                        any<String>()
                    )
                } answers { aApplication }
                every {
                    botAdminService.saveStory(
                        any<String>(),
                        any<BotStoryDefinitionConfiguration>(),
                        any<UserLogin>(),
                        any<IntentDefinition>()
                    )
                } returns existingMessageStory

                val slotStory = slot<StoryDefinitionConfiguration>()

                verify(exactly = 0) {
                    botAdminService["createOrGetIntent"](
                        any<String>(),
                        any<String>(),
                        any<Id<ApplicationDefinition>>(),
                        any<String>()
                    )
                }
                verify(exactly = 1) { faqDefinitionDAO.save(any()) }
                verify(exactly = 1) { i18nDAO.save(listOf(mockedI18n)) }
                verify(exactly = 1) { storyDefinitionDAO.save(capture(slotStory)) }

                assertEquals(slotStory.captured.storyId, existingMessageStory.storyId)
                //story name must no be overwritten
                assertNotEquals(slotStory.captured.name, existingMessageStory.name)
                assertEquals(slotStory.captured.category, FAQ_CATEGORY)

            }

        }
    }


}