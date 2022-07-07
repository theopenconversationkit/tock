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
import ai.tock.bot.admin.model.FaqSearchRequest
import ai.tock.bot.admin.story.StoryDefinitionConfiguration
import ai.tock.bot.admin.story.StoryDefinitionConfigurationDAO
import ai.tock.bot.definition.IntentWithoutNamespace
import ai.tock.nlp.admin.AdminService
import ai.tock.nlp.front.service.storage.ClassifiedSentenceDAO
import ai.tock.nlp.front.service.storage.FaqDefinitionDAO
import ai.tock.nlp.front.service.storage.FaqSettingsDAO
import ai.tock.nlp.front.service.storage.IntentDefinitionDAO
import ai.tock.nlp.front.shared.config.ApplicationDefinition
import ai.tock.nlp.front.shared.config.Classification
import ai.tock.nlp.front.shared.config.ClassifiedSentence
import ai.tock.nlp.front.shared.config.ClassifiedSentenceStatus
import ai.tock.nlp.front.shared.config.FaqDefinition
import ai.tock.nlp.front.shared.config.FaqQueryResult
import ai.tock.nlp.front.shared.config.FaqSettingsQuery
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
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.slot
import io.mockk.spyk
import io.mockk.verify
import org.apache.commons.lang3.StringUtils
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.litote.kmongo.Id
import org.litote.kmongo.newId
import org.litote.kmongo.toId
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class FaqAdminServiceTest : AbstractTest() {
    companion object {
        val faqDefinitionDAO: FaqDefinitionDAO = mockk(relaxed = false)
        val sentenceDAO: ClassifiedSentenceDAO = mockk(relaxed = false)
        val intentDAO: IntentDefinitionDAO = mockk(relaxed = false)
        val i18nDAO: I18nDAO = mockk(relaxed = false)
        val faqSettingsDAO: FaqSettingsDAO = mockk(relaxed = true)

        init {
            // IOC
            tockInternalInjector = KodeinInjector()
            val specificModule = Kodein.Module {
                bind<StoryDefinitionConfigurationDAO>() with provider { storyDefinitionDAO }
                bind<FaqDefinitionDAO>() with provider { faqDefinitionDAO }
                bind<ClassifiedSentenceDAO>() with provider { sentenceDAO }
                bind<IntentDefinitionDAO>() with provider { intentDAO }
                bind<I18nDAO>() with provider { i18nDAO }
                bind<FaqSettingsDAO>() with provider { faqSettingsDAO }
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
        private val intentId2 = "idIntent2".toId<IntentDefinition>()
        private val faqId = "faqDefId".toId<FaqDefinition>()
        private val faqId2 = "faqDefId2".toId<FaqDefinition>()
        private val i18nId = "idI18n".toId<I18nLabel>()
        private val i18nId2 = "idI18n2".toId<I18nLabel>()
        private val now = Instant.now().truncatedTo(ChronoUnit.MILLIS)
        private val tagList = listOf("TAG1", "TAG2")

        private const val FAQ_CATEGORY = "faq"
        private const val namespace = "test"

        private const val userLogin: UserLogin = "userLogin"

        private val faqDefinition = FaqDefinition(faqId, applicationId, intentId, i18nId, tagList, true, now, now)

        val applicationDefinition =
            ApplicationDefinition("my App", namespace = namespace, supportedLocales = setOf(Locale.FRENCH))
        val storyId = "storyId".toId<StoryDefinitionConfiguration>()

        private val firstUterrance = "FAQ utterance A"
        private val secondUterrance = "FAQ utterance B"

        private val faqDefinitionRequest = FaqDefinitionRequest(
            faqId.toString(), intentId.toString(), Locale.FRENCH, applicationId,
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

        private val mockedDefaultLocalizedLabel: I18nLocalizedLabel = I18nLocalizedLabel(
            locale = Locale.FRENCH,
            interfaceType = UserInterfaceType.textChat,
            label = "The FAQ answer",
            validated = true,
            connectorId = null,
            alternatives = emptyList()
        )
        val mockedI18n = I18nLabel(
            _id = i18nId, namespace = namespace, category = "faq", linkedSetOf(
                mockedDefaultLocalizedLabel
            ), defaultLabel = "The FAQ answer", defaultLocale = Locale.FRENCH, version = 0
        )

        val mockedI18nLabels_1Element = listOf<I18nLabel>(mockedI18n)
        val mockedI18nLabels_2Elements = listOf<I18nLabel>(
            mockedI18n, mockedI18n.copy(
                i18nId2, i18n = linkedSetOf<I18nLocalizedLabel>(
                    mockedDefaultLocalizedLabel.copy(label = "Another Faq answer")
                ), defaultLabel = "Another Faq answer"
            )
        )

    }

    @Nested
    inner class SaveFaq {
        @Nested
        inner class SingleExistingFaqAndStory {

            internal fun initMocksForSingleStory(existingStory: StoryDefinitionConfiguration) {
                every { storyDefinitionDAO.getStoryDefinitionById(existingStory._id) } answers { existingStory }
                every { storyDefinitionDAO.getStoryDefinitionById(eq(storyId)) } returns existingStory
                every { storyDefinitionDAO.getStoryDefinitionById(neq(existingStory._id)) } answers { null }
                every {
                    storyDefinitionDAO.getStoryDefinitionByNamespaceAndBotIdAndIntent(
                        any(), any(), any()
                    )
                } answers { null }
                every {
                    storyDefinitionDAO.getStoryDefinitionByNamespaceAndBotIdAndStoryId(
                        any(), any(), any()
                    )
                } answers { null }
                every {
                    storyDefinitionDAO.getStoryDefinitionByNamespaceAndBotIdAndIntent(
                        any(), any(), existingStory.intent.name
                    )
                } answers { existingStory }
                every {
                    storyDefinitionDAO.getStoryDefinitionByNamespaceAndBotIdAndStoryId(
                        any(), any(), existingStory.storyId
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
                        it, Locale.FRENCH, applicationId, now, now, ClassifiedSentenceStatus.model, Classification(
                            intentId, emptyList()
                        ), null, null
                    )
                }.toList()

                every { sentenceDAO.search(any()) } answers {
                    SentencesQueryResult(
                        utterances.size.toLong(), if (existingUtterances) answers else emptyList()
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
            inner class NewFaqWithStoryWithNewUtterances {

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
                            allAny<FaqDefinitionRequest>(), allAny<ApplicationDefinition>()
                        )
                    } returns null

                    assertThrows<BadRequestException> {
                        faqAdminService.saveFAQ(faqDefinitionRequest, userLogin, applicationDefinition)
                    }
                    val botAdminService = spyk<BotAdminService>()

                    every {
                        botAdminService["getBotConfigurationsByNamespaceAndBotId"](
                            any<String>(), any<String>()
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
                            any<String>(), any<String>(), any<Id<ApplicationDefinition>>(), any<String>()
                        )
                    }
                    verify(exactly = 0) { faqDefinitionDAO.save(any()) }
                    verify(exactly = 0) { i18nDAO.save(listOf(mockedI18n)) }
                    verify(exactly = 0) { storyDefinitionDAO.save(capture(slotStory)) }
                }

                @Test
                fun `GIVEN save faq WHEN and saving the same story THEN update the story`() {
                    val faqAdminService = spyk<FaqAdminService>(recordPrivateCalls = true)
                    val savedFaqDefinition =
                        FaqDefinition(faqId, applicationId, intentId, i18nId, listOf("NEW TAG"), true, now, now)

                    every {
                        faqAdminService["createOrUpdateIntent"](
                            allAny<FaqDefinitionRequest>(), allAny<ApplicationDefinition>()
                        )
                    } returns theSavedIntent
                    every {
                        faqAdminService["createOrUpdateStory"](
                            allAny<FaqDefinitionRequest>(),
                            allAny<IntentDefinition>(),
                            allAny<UserLogin>(),
                            allAny<I18nLabel>(),
                            allAny<ApplicationDefinition>(),
                            allAny<FaqSettingsQuery>()
                        ) as Unit
                    } just Runs

                    every {
                        faqAdminService["prepareCreationOrUpdatingFaqDefinition"](
                            allAny<FaqDefinitionRequest>(),
                            allAny<ApplicationDefinition>(),
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
            inner class ExistingFaqWithStoryWithoutNewUtterances {
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
                            allAny<FaqDefinitionRequest>(), allAny<ApplicationDefinition>()
                        )
                    } returns existingIntent

                    faqAdminService.saveFAQ(faqDefinitionRequest, userLogin, applicationDefinition)

                    val botAdminService = spyk<BotAdminService>()
                    every {
                        botAdminService["getBotConfigurationsByNamespaceAndBotId"](
                            any<String>(), any<String>()
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
                            any<String>(), any<String>(), any<Id<ApplicationDefinition>>(), any<String>()
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

                @Test
                fun `GIVEN save faq and story When existing Faq THEN save and disable the story`() {

                    val faqDefinitionRequestDisabledStory = faqDefinitionRequest.copy(enabled = false)
                    val faqAdminService = spyk<FaqAdminService>(recordPrivateCalls = true)
                    every {
                        faqAdminService["createOrUpdateIntent"](
                            allAny<FaqDefinitionRequest>(), allAny<ApplicationDefinition>()
                        )
                    } returns existingIntent

                    faqAdminService.saveFAQ(faqDefinitionRequestDisabledStory, userLogin, applicationDefinition)

                    val botAdminService = spyk<BotAdminService>()
                    every {
                        botAdminService["getBotConfigurationsByNamespaceAndBotId"](
                            any<String>(), any<String>()
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
                            any<String>(), any<String>(), any<Id<ApplicationDefinition>>(), any<String>()
                        )
                    }
                    verify(exactly = 1) { faqDefinitionDAO.save(any()) }
                    verify(exactly = 1) { i18nDAO.save(listOf(mockedI18n)) }
                    verify(exactly = 1) { storyDefinitionDAO.save(capture(slotStory)) }

                    assertEquals(slotStory.captured.storyId, existingMessageStory.storyId)
                    //story name must no be overwritten
                    assertNotEquals(slotStory.captured.name, existingMessageStory.name)
                    assertEquals(
                        slotStory.captured.features.get(0).enabled,
                        faqDefinitionRequestDisabledStory.enabled,
                        "Should be equals to false, considered as disabled"
                    )
                    assertEquals(slotStory.captured.category, FAQ_CATEGORY)
                }

            }
        }

        @Nested
        inner class FaqWithIntentName {

            private val newFaqQuery = FaqDefinitionRequest(
                null,
                null,
                Locale.FRENCH,
                applicationId,
                now,
                now,
                "NEW FAQ TITLE",
                "NEW FAQ DESCRIPTION",
                listOf("NEW FAQ QUESTION"),
                emptyList(),
                "NEW FAQ ANSWER",
                true,
                intentName = "new_faq_intent"
            )

            private val existingFaqQuery = newFaqQuery.copy(
                id = "myFaq", intentName = null
            )

            private val newIntentDefinition = IntentDefinition(
                name = "myIntentName",
                namespace = namespace,
                applications = setOf(applicationId),
                label = "my Intent Label",
                description = "my Intent Description",
                category = "faq",
                entities = emptySet()
            )

            private val existingIntentDefinition = newIntentDefinition.copy(
                _id = "myIntent".toId<IntentDefinition>(),
            )

            private val newFaqDefinition = FaqDefinition(
                applicationId = applicationId,
                intentId = "myIntent".toId<IntentDefinition>(),
                i18nId = "myI18n".toId<I18nLabel>(),
                tags = emptyList(),
                enabled = true,
                creationDate = Instant.now(),
                updateDate = Instant.now(),
            )

            private val existingFaqDefinition = newFaqDefinition.copy(
                _id = "myFaq".toId<FaqDefinition>()
            )

            @Test
            fun `GIVEN create faq WHEN intent name is null THEN Throw IllegalArgumentException`() {
                val faqAdminService = spyk<FaqAdminService>(recordPrivateCalls = true)
                assertThrows<BadRequestException>() {
                    faqAdminService.saveFAQ(
                        newFaqQuery.copy(id = null, intentName = null), userLogin, applicationDefinition
                    )
                }
            }

            @Test
            fun `GIVEN update faq WHEN intent name is not null THEN save`() {
                val faqAdminService = spyk<FaqAdminService>(recordPrivateCalls = true)
                initSaveFaqMock(faqAdminService)

                faqAdminService.saveFAQ(newFaqQuery, userLogin, applicationDefinition)

                verify(exactly = 1) { faqAdminService["getIntentName"](any<FaqDefinitionRequest>()) }
                verify(exactly = 0) { faqAdminService["findFaqDefinitionIntent"](any<Id<FaqDefinition>>()) }
            }

            @Test
            fun `GIVEN update faq WHEN intent name is null THEN save`() {
                val faqAdminService = spyk<FaqAdminService>(recordPrivateCalls = true)
                initSaveFaqMock(faqAdminService)

                faqAdminService.saveFAQ(existingFaqQuery, userLogin, applicationDefinition)

                verify(exactly = 1) { faqAdminService["getIntentName"](any<FaqDefinitionRequest>()) }
                verify(exactly = 1) { faqAdminService["findFaqDefinitionIntent"](any<Id<FaqDefinition>>()) }
            }

            private fun initSaveFaqMock(
                faqAdminService: FaqAdminService,
                mockedIntentDefinition: IntentDefinition = existingIntentDefinition,
                mockedI18nLabel: I18nLabel = mockedI18n,
                mockedFaqDefinition: FaqDefinition = existingFaqDefinition
            ) {
                every {
                    faqAdminService["findFaqDefinitionIntent"](any<Id<FaqDefinition>>())
                } returns mockedIntentDefinition

                justRun {
                    faqAdminService["createOrUpdateUtterances"](
                        any<FaqDefinitionRequest>(), any<Id<IntentDefinition>>(), any<UserLogin>()
                    )
                }

                every {
                    faqAdminService["manageI18nLabelUpdate"](
                        any<FaqDefinitionRequest>(), any<String>(), any<FaqDefinition>()
                    )
                } returns mockedI18nLabel

                every { faqDefinitionDAO.getFaqDefinitionByIntentId(any()) } returns mockedFaqDefinition

                justRun { faqDefinitionDAO.save(any()) }

                justRun {
                    faqAdminService["createOrUpdateStory"](
                        any<FaqDefinitionRequest>(),
                        any<IntentDefinition>(),
                        any<UserLogin>(),
                        any<I18nLabel>(),
                        any<ApplicationDefinition>(),
                        any<FaqSettingsQuery>()
                    )
                }
            }
        }

    }

    /**
     * Tests according to faq search
     */
    @Nested
    inner class SearchFaq {
        @Test
        fun `GIVEN no classifiedSentences associated to a faq THEN should return MISSING_UTTERANCE as search`() {
            val faqAdminService = spyk<FaqAdminService>(recordPrivateCalls = true)
            initSearchFaqMockWithoutLabelsSearch(
                faqAdminService, listOf(
                    createFaqQueryResult(
                        faqId = faqId2, intentId = intentId2, i18nId = i18nId2, numberOfUtterances = 0
                    ), createFaqQueryResult()
                )
            )

            val faqSearchRequest = createFaqSearchRequest(enabled = null, search = null)
            val faqResult = faqAdminService.searchFAQ(faqSearchRequest, applicationDefinition)

            assertEquals(2, faqResult.rows.size, "The expected size of faq Result is not the one expected (2)")
            assertEquals(
                FaqAdminService.MISSING_UTTERANCE,
                faqResult.rows.first().utterances[0],
                "The name of the utterance should be 'MISSING_UTTERANCE'"
            )
            assertEquals(
                "randomText 1",
                faqResult.rows.get(1).utterances[0],
                "The name of the utterance should be 'randomText 1'"
            )

            verify(exactly = 1) {
                faqDefinitionDAO.getFaqDetailsWithCount(
                    any(), applicationDefinition._id.toString(), null
                )
            }
            verify(exactly = 0) {
                faqAdminService["searchFromTockBotDbWithFoundTextLabels"](
                    any<List<FaqQueryResult>>(), any<ApplicationDefinition>(), any<List<I18nLabel>>()
                )
            }
            verify(exactly = 1) {
                faqAdminService["searchLabelsFromTockFrontDb"](
                    any<List<FaqQueryResult>>(), any<ApplicationDefinition>()
                )
            }

            verify(atLeast = 2) { i18nDAO.getLabelById(any()) }

        }

        private fun initSearchFaqMockWithoutLabelsSearch(
            faqAdminService: FaqAdminService,
            faqQueryResults: List<FaqQueryResult> = listOf(createFaqQueryResult()),
//            mockedIntentDefinition: IntentDefinition = existingIntentDefinition,
            mockedI18nLabels: List<I18nLabel> = mockedI18nLabels_2Elements,
//            mockedFaqDefinition: FaqDefinition = existingFaqDefinition
        ) {
            every {
                faqAdminService["findPredicatesFrom18nLabels"](any<ApplicationDefinition>(), any<String>())
            } returns mockedI18nLabels

            val searchResult = Pair(faqQueryResults, faqQueryResults.size.toLong())

            every { faqDefinitionDAO.getFaqDetailsWithCount(any(), any(), any()) } returns searchResult

            every {
                faqAdminService["searchFromTockBotDbWithFoundTextLabels"](
                    any<List<FaqQueryResult>>(), any<ApplicationDefinition>(), mockedI18nLabels
                )
            } returns emptySet<FaqDefinitionRequest>()

            every { i18nDAO.getLabelById(i18nId) } returns mockedI18nLabels[0]
            if (mockedI18nLabels.size > 1) {
                every { i18nDAO.getLabelById(i18nId2) } returns mockedI18nLabels[1]
            }

        }

    }


    private fun createFaqSearchRequest(
        enabled: Boolean? = null,
        search: String? = null,
        tags: List<String> = emptyList(),
        start: Int = 0,
        size: Int = 10
    ): FaqSearchRequest {
        return FaqSearchRequest(tags, search, enabled, userLogin, null)
    }

    /**
     * Create data For Faq Search with associated data for collections in FaqDefinition, IntentDefinition, ClassifiedSentences
     * With default data for each parameter
     * @return a Pair <listOf FaqQueryResult and the count>
     */
    private fun createFaqQueryResult(
        faqId: Id<FaqDefinition> = FaqAdminServiceTest.faqId,
        intentId: Id<IntentDefinition> = FaqAdminServiceTest.intentId,
        i18nId: Id<I18nLabel> = FaqAdminServiceTest.i18nId,
        tagList: List<String> = emptyList(),
        applicationId: Id<ApplicationDefinition> = FaqAdminServiceTest.applicationId,
        enabled: Boolean = true,
        faqName: String = "Faq Name",
        numberOfUtterances: Int = 1,
        utteranceText: String = "randomText",
        classifiedSentenceStatus: ClassifiedSentenceStatus = ClassifiedSentenceStatus.validated,
        instant: Instant = now
    ): FaqQueryResult {

        val createdIntent = IntentDefinition(
            faqName,
            namespace,
            setOf(applicationId),
            emptySet(),
            label = StringUtils.lowerCase(faqName),
            category = FAQ_CATEGORY,
            _id = intentId
        )

        //create utterance according to the number of utterance
        val utterances = ArrayList<ClassifiedSentence>()
        for (number: Int in 1..numberOfUtterances) {
            utterances.add(createUtterance("$utteranceText $number", intentId, classifiedSentenceStatus))
        }

        return FaqQueryResult(
            faqId, applicationId, intentId, i18nId, tagList, enabled, instant, instant, utterances, createdIntent
        )
    }

    private fun createUtterance(text: String, intentId: Id<IntentDefinition>, status: ClassifiedSentenceStatus) =
        ClassifiedSentence(
            text = text,
            language = Locale.FRENCH,
            applicationId = applicationId,
            creationDate = Instant.now(),
            updateDate = Instant.now(),
            status = status,
            classification = Classification(intentId, emptyList()),
            lastIntentProbability = 1.0,
            lastEntityProbability = 1.0,
            qualifier = userLogin
        )

}