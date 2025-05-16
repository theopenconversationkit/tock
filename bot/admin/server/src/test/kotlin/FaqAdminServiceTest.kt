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
import ai.tock.bot.admin.bot.BotApplicationConfiguration
import ai.tock.bot.admin.model.BotStoryDefinitionConfiguration
import ai.tock.bot.admin.model.FaqDefinitionRequest
import ai.tock.bot.admin.model.FaqSearchRequest
import ai.tock.bot.admin.story.StoryDefinitionConfiguration
import ai.tock.bot.admin.story.StoryDefinitionConfigurationDAO
import ai.tock.bot.connector.ConnectorType
import ai.tock.bot.definition.IntentWithoutNamespace
import ai.tock.nlp.admin.AdminService
import ai.tock.nlp.core.NlpEngineType
import ai.tock.nlp.front.service.storage.ClassifiedSentenceDAO
import ai.tock.nlp.front.service.storage.FaqDefinitionDAO
import ai.tock.nlp.front.service.storage.FaqSettingsDAO
import ai.tock.nlp.front.service.storage.IntentDefinitionDAO
import ai.tock.nlp.front.shared.config.ApplicationDefinition
import ai.tock.nlp.front.shared.config.Classification
import ai.tock.nlp.front.shared.config.ClassifiedSentence
import ai.tock.nlp.front.shared.config.ClassifiedSentenceStatus
import ai.tock.nlp.front.shared.config.ClassifiedSentenceStatus.deleted
import ai.tock.nlp.front.shared.config.EntityDefinition
import ai.tock.nlp.front.shared.config.FaqDefinition
import ai.tock.nlp.front.shared.config.FaqQueryResult
import ai.tock.nlp.front.shared.config.IntentDefinition
import ai.tock.nlp.front.shared.config.SentencesQuery
import ai.tock.nlp.front.shared.config.SentencesQueryResult
import ai.tock.shared.defaultNamespace
import ai.tock.shared.exception.rest.BadRequestException
import ai.tock.shared.security.UserLogin
import ai.tock.shared.tockInternalInjector
import ai.tock.translator.I18nDAO
import ai.tock.translator.I18nLabel
import ai.tock.translator.I18nLocalizedLabel
import ai.tock.translator.UserInterfaceType
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.KodeinInjector
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.provider
import io.mockk.CapturingSlot
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.spyk
import io.mockk.unmockkObject
import io.mockk.verify
import org.apache.commons.lang3.StringUtils
import org.junit.jupiter.api.AfterEach
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
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

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
            tockInternalInjector.inject(Kodein {
                import(defaultModulesBinding())
                import(specificModule)
            })
        }

        private val applicationId = newId<ApplicationDefinition>()
        private val botId = "botId"
        private val intentId = "idIntent".toId<IntentDefinition>()
        private val intentId2 = "idIntent2".toId<IntentDefinition>()
        private val intentId3 = "idIntent3".toId<IntentDefinition>()
        private val intentId4 = "idIntent4".toId<IntentDefinition>()
        private val faqId = "faqDefId".toId<FaqDefinition>()
        private val faqId2 = "faqDefId2".toId<FaqDefinition>()
        private val faqId3 = "faqDefId3".toId<FaqDefinition>()
        private val faqId4 = "faqDefId4".toId<FaqDefinition>()
        private val i18nId = "idI18n".toId<I18nLabel>()
        private val i18nId2 = "idI18n2".toId<I18nLabel>()
        private val i18nId3 = "idI18n3".toId<I18nLabel>()
        private val i18nId4 = "idI18n4".toId<I18nLabel>()
        private val now = Instant.now().truncatedTo(ChronoUnit.MILLIS)
        private val tagList = listOf("TAG1", "TAG2")

        private const val FAQ_CATEGORY = "faq"
        private const val namespace = "test"

        private const val userLogin: UserLogin = "userLogin"

        private val faqDefinition = FaqDefinition(faqId, botId, namespace, intentId, i18nId, tagList, true, now, now)

        val applicationDefinition =
            ApplicationDefinition(botId, namespace = namespace, supportedLocales = setOf(Locale.FRENCH))
        val storyId = "storyId".toId<StoryDefinitionConfiguration>()

        private const val firstUterrance = "FAQ utterance A"
        private const val secondUterrance = "FAQ utterance B"

        private val answer = I18nLabel(
            i18nId,
            defaultNamespace,
            " category",
            LinkedHashSet(
                listOf(
                    I18nLocalizedLabel(
                        Locale.FRENCH,
                        UserInterfaceType.textChat,
                        "label",
                        true
                    )
                )
            )
        )
        private val faqDefinitionRequest = FaqDefinitionRequest(
            faqId.toString(),
            intentId.toString(),
            Locale.FRENCH,
            botId,
            now,
            now,
            "FAQ TITLE",
            "FAQ desciption",
            listOf(firstUterrance, secondUterrance),
            listOf("NEW TAG"),
            answer,
            true,
            "faqtitle"
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

        private val existingMessageStory =
            newFaqTestStory(namespace, AnswerConfigurationType.message, theSavedIntent.name, _id = storyId)
        private val existingStory = existingMessageStory.toStoryDefinitionConfiguration()

        fun newFaqTestStory(
            storyId: String,
            type: AnswerConfigurationType,
            intentName: String,
            _id: Id<StoryDefinitionConfiguration> = newId(),
            name: String = storyId,
            botName: String = "testBotId"
        ): BotStoryDefinitionConfiguration {
            return BotStoryDefinitionConfiguration(
                storyId = storyId,
                botId = botName,
                intent = IntentWithoutNamespace(intentName),
                currentType = type,
                namespace = namespace,
                answers = emptyList(),
                userSentenceLocale = Locale.FRANCE,
                _id = _id,
                name = name
            )
        }
    }


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

    @Nested
    inner class SaveFaq {
        @Nested
        inner class SingleExistingFaqAndStory {

            internal fun initMocksForSingleFaq(existingFaq: FaqDefinition) {
                every { faqDefinitionDAO.getFaqDefinitionById(any()) } answers { existingFaq }
                every { faqDefinitionDAO.save(any()) } just Runs
                every {
                    faqDefinitionDAO.getFaqDefinitionByIntentIdAndBotIdAndNamespace(
                        eq(intentId),
                        eq(botId),
                        eq(namespace)
                    )
                } answers { existingFaq }
            }

            internal fun initMocksForClassifiedSentences(
                utterances: List<String>,
                existingUtterances: Boolean,
                applicationId: Id<ApplicationDefinition>,
                intentId: Id<IntentDefinition>
            ) {
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

            @Nested
            inner class NewFaqWithStoryWithNewUtterances {

                @BeforeEach
                internal fun initMocks() {
                    //Existing Story
                    initMocksForSingleStory(existingStory)
                    //Existing Faq
                    initMocksForSingleFaq(faqDefinition)
                    //Existing ClassifiedSentences
                    initMocksForClassifiedSentences(
                        listOf(firstUterrance, secondUterrance),
                        false,
                        applicationId,
                        intentId
                    )

                    every { AdminService.front.getIntentById(eq(intentId)) } returns existingIntent
                    every { i18nDAO.getLabelById(i18nId) } answers { mockedI18n }
                }

                @Test
                fun `GIVEN save faq and story WHEN intent is null THEN Throw bad request`() {

                    val faqAdminService = spyk<FaqAdminService>(recordPrivateCalls = true)
                    every {
                        faqAdminService["findFaqDefinitionIntent"](
                            allAny<Id<FaqDefinition>>()
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
                    } answers { listOf(aApplication) }
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
                    verify(exactly = 0) { storyDefinitionDAO.save(capture(slotStory)) }
                }

                @Test
                fun `GIVEN save faq WHEN and saving the same story THEN update the story`() {
                    val faqAdminService = spyk<FaqAdminService>(recordPrivateCalls = true)
                    val savedFaqDefinition =
                        FaqDefinition(faqId, botId, namespace, intentId, i18nId, listOf("NEW TAG"), true, now, now)

                    every {
                        faqAdminService["createOrUpdateFaqIntent"](
                            allAny<FaqDefinitionRequest>(), allAny<ApplicationDefinition>()
                        )
                    } returns theSavedIntent

                    every {
                        faqAdminService["prepareCreationOrUpdatingFaqDefinition"](
                            allAny<FaqDefinitionRequest>(),
                            allAny<ApplicationDefinition>(),
                            allAny<IntentDefinition>(),
                            allAny<FaqDefinition>()
                        ) as FaqDefinition
                    } returns savedFaqDefinition

                    faqAdminService.saveFAQ(faqDefinitionRequest, userLogin, applicationDefinition)

                    val sentenceQuerySlot = slot<SentencesQuery>()
                    verify(exactly = 1) {
                        sentenceDAO.search(capture(sentenceQuerySlot))
                    }
                    assertFalse(sentenceQuerySlot.captured.wholeNamespace)

                    verify(exactly = 1) { faqDefinitionDAO.save(savedFaqDefinition) }

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
                    initMocksForClassifiedSentences(
                        listOf(firstUterrance, secondUterrance),
                        true,
                        applicationId,
                        intentId
                    )

                    every { AdminService.front.getIntentById(eq(intentId)) } returns existingIntent
                    every { i18nDAO.getLabelById(i18nId) } answers { mockedI18n }
                }

                @Test
                fun `GIVEN save faq and story When existing Faq THEN save the story`() {

                    val faqAdminService = spyk<FaqAdminService>(recordPrivateCalls = true)
                    every {
                        faqAdminService["createOrUpdateFaqIntent"](
                            allAny<FaqDefinitionRequest>(), allAny<ApplicationDefinition>()
                        )
                    } returns existingIntent

                    faqAdminService.saveFAQ(faqDefinitionRequest, userLogin, applicationDefinition)

                    val slotStory = slot<StoryDefinitionConfiguration>()

                    verify(exactly = 1) { faqDefinitionDAO.save(any()) }
                    verify(exactly = 1) { storyDefinitionDAO.save(capture(slotStory)) }

                    val sentenceQuerySlot = slot<SentencesQuery>()
                    verify(exactly = 1) {
                        sentenceDAO.search(capture(sentenceQuerySlot))
                    }

                    assertFalse(sentenceQuerySlot.captured.wholeNamespace)

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
                        faqAdminService["createOrUpdateFaqIntent"](
                            allAny<FaqDefinitionRequest>(), allAny<ApplicationDefinition>()
                        )
                    } returns existingIntent

                    faqAdminService.saveFAQ(faqDefinitionRequestDisabledStory, userLogin, applicationDefinition)

                    val botAdminService = spyk<BotAdminService>()
                    every {
                        botAdminService["getBotConfigurationsByNamespaceAndBotId"](
                            any<String>(), any<String>()
                        )
                    } answers { listOf(aApplication) }
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

                    val sentenceQuerySlot = slot<SentencesQuery>()
                    verify(exactly = 1) {
                        sentenceDAO.search(capture(sentenceQuerySlot))
                    }

                    assertFalse(sentenceQuerySlot.captured.wholeNamespace)
                }
            }
        }

    }

    @Nested
    inner class SharedIntentFaqSave {
        /**
         * Prepare mock datas
         */
        private fun initMocksForSharedIntentFaq(existingFaq: FaqDefinition) {
            every { faqDefinitionDAO.getFaqDefinitionByIntentId(any()) } answers { existingFaq }
            every { faqDefinitionDAO.getFaqDefinitionByBotIdAndNamespace(eq(OTHER_APP_NAME), eq(defaultNamespace)) } answers { emptyList() }
            every { faqDefinitionDAO.save(any()) } just Runs
        }

        private fun initMocksForClassifiedSentences(
            utterances: List<String>,
            existingUtterances: Boolean,
            applicationId: Id<ApplicationDefinition>,
            intentId: Id<IntentDefinition>
        ) {
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

        /**
         * Some variables
         */
        private val existingMessageStory =
            newFaqTestStory(namespace, AnswerConfigurationType.message, theSavedIntent.name, _id = storyId)
        private val existingStory = existingMessageStory.toStoryDefinitionConfiguration()

        private val sharedIntent = existingIntent.copy()

        // variable for this nested class
        val OTHER_APP_NAME = "otherApp"
        val FAQ_SPECIFIC_ANSWER = I18nLabel(
            "new specific answer".toId(),
            defaultNamespace,
            " category",
            LinkedHashSet(
                listOf(
                    I18nLocalizedLabel(
                        Locale.FRENCH,
                        UserInterfaceType.textChat,
                        "label",
                        true
                    )
                )
            )
        )

        val newOtherUtterance = "new other utterance"

        @BeforeEach
        internal fun initMocks() {
            //Existing Story
            initMocksForSingleStory(existingStory)
            //Existing Faq
            initMocksForSharedIntentFaq(faqDefinition)
            //Existing ClassifiedSentences
            initMocksForClassifiedSentences(
                listOf(firstUterrance, secondUterrance),
                true,
                applicationId,
                intentId
            )

            every { AdminService.front.getIntentById(eq(intentId)) } returns existingIntent
            every { i18nDAO.getLabelById(any()) } answers { FAQ_SPECIFIC_ANSWER }
            mockkObject(BotAdminService)
        }

        @AfterEach
        fun mockkServicesDeletion(){
            unmockkObject(BotAdminService)
        }

        @Test
        fun `GIVEN save new faq on another bot application on same namespace with a shared intent WHEN saving faq same existing classified sentences should not be created THEN save the story`() {

            //MOCK
            val faqAdminService = spyk<FaqAdminService>(recordPrivateCalls = true)

            //shared intent in two app
            //GIVEN
            // new faq is created there was none before
            every {
                faqDefinitionDAO.getFaqDefinitionByIntentIdAndBotIdAndNamespace(
                    eq(intentId),
                    eq(OTHER_APP_NAME),
                    eq(namespace)
                )
            } answers { null }

            every {
                faqAdminService["createOrUpdateFaqIntent"](
                    allAny<FaqDefinitionRequest>(), allAny<ApplicationDefinition>()
                )
            } returns existingIntent.copy(applications = setOf(applicationId, OTHER_APP_NAME.toId()))

            //faq definition request already exists with same intentId
            val newFaqSharedIntent = faqDefinitionRequest.copy(
                id = faqId2.toString(),
                intentId = intentId.toString(),
                applicationName = OTHER_APP_NAME,
                answer = FAQ_SPECIFIC_ANSWER,
                // add same existing utterrances and another one
                utterances = faqDefinitionRequest.utterances.plus(newOtherUtterance)
            )

            val otherAppDefinition = applicationDefinition.copy(name = OTHER_APP_NAME)

            //WHEN
            val savedFaq = faqAdminService.saveFAQ(newFaqSharedIntent, userLogin, otherAppDefinition)

            //THEN
            val slotStory = slot<StoryDefinitionConfiguration>()

            verify(exactly = 1) { faqDefinitionDAO.save(any()) }

            val sentenceQuerySlot = slot<SentencesQuery>()
            verify(exactly = 1) {
                sentenceDAO.search(capture(sentenceQuerySlot))
            }

            assertEquals(intentId,sentenceQuerySlot.captured.intentId)

            //save only one new sentence not the shared one
            verify(exactly = 1) { BotAdminService.saveSentence(eq(newOtherUtterance), any(), any(), any(), any()) }

            val switchedClassifiedSentences = slot<List<ClassifiedSentence>>()
            verify(exactly = 1) { sentenceDAO.switchSentencesStatus(capture(switchedClassifiedSentences), eq(deleted)) }
            assertEquals(switchedClassifiedSentences.captured.size, 0)

            verify(exactly = 1) { storyDefinitionDAO.save(capture(slotStory)) }

            assertEquals(slotStory.captured.storyId, existingMessageStory.storyId)

            //story name must not be overwritten
            assertNotEquals(slotStory.captured.name, existingMessageStory.name)
            assertEquals(slotStory.captured.category, FAQ_CATEGORY)
        }


        @Test
        fun `GIVEN existing shared faq on a bot application on same namespace WHEN updating faq THEN added classified sentence should be created`() {

            //MOCK
            val faqAdminService = spyk<FaqAdminService>(recordPrivateCalls = true)

            //shared intent in two app
            //GIVEN
            val secondFaqUtterance = "second FAQ utterance"

            //faq definition request already exists with same intentId
            val existingSecondBotFaqSharedIntentRequest = faqDefinitionRequest.copy(
                id = faqId2.toString(),
                intentId = intentId.toString(),
                applicationName = OTHER_APP_NAME,
                answer = FAQ_SPECIFIC_ANSWER,
                // add same existing utterances and another one
                utterances = faqDefinitionRequest.utterances.plus(secondFaqUtterance)
            )

            // faq is already existing to be updated
            val existingFaqSharedIntentDefinition = FaqDefinition(
                faqId2,
                OTHER_APP_NAME,
                namespace,
                intentId,
                i18nId,
                existingSecondBotFaqSharedIntentRequest.tags,
                existingSecondBotFaqSharedIntentRequest.enabled,
                existingSecondBotFaqSharedIntentRequest.creationDate!!,
                existingSecondBotFaqSharedIntentRequest.updateDate!!,
            )

            every {
                faqDefinitionDAO.getFaqDefinitionByIntentIdAndBotIdAndNamespace(
                    eq(intentId),
                    eq(OTHER_APP_NAME),
                    eq(namespace)
                )
            } answers { existingFaqSharedIntentDefinition }

            every {
                faqAdminService["createOrUpdateFaqIntent"](
                    allAny<FaqDefinitionRequest>(), allAny<ApplicationDefinition>()
                )
            } returns existingIntent.copy(applications = setOf(applicationId, OTHER_APP_NAME.toId()))

            val otherAppDefinition = applicationDefinition.copy(name = OTHER_APP_NAME)

            //WHEN
            val savedFaq = faqAdminService.saveFAQ(existingSecondBotFaqSharedIntentRequest, userLogin, otherAppDefinition)

            //THEN
            // FAQ
            verify(exactly = 1) { faqDefinitionDAO.save(any()) }
            assertEquals(savedFaq.utterances.size,3)

            // Classified Sentences
            val sentenceQuerySlot = slot<SentencesQuery>()
            verify(exactly = 1) {
                sentenceDAO.search(capture(sentenceQuerySlot))
            }

                  assertEquals(intentId,sentenceQuerySlot.captured.intentId)

            // save only one new sentence not the shared one
            verify(exactly = 1) { BotAdminService.saveSentence(eq(secondFaqUtterance), any(), any(), any(), any()) }

            val switchedClassifiedSentences = slot<List<ClassifiedSentence>>()
            verify(exactly = 1) { sentenceDAO.switchSentencesStatus(capture(switchedClassifiedSentences), eq(deleted)) }
            assertEquals(switchedClassifiedSentences.captured.size, 0)

            // Story
            val slotStory = slot<StoryDefinitionConfiguration>()
            verify(exactly = 1) { storyDefinitionDAO.save(capture(slotStory)) }
            assertEquals(slotStory.captured.storyId, existingMessageStory.storyId)

            //story name must not be overwritten
            assertNotEquals(slotStory.captured.name, existingMessageStory.name)
            assertEquals(slotStory.captured.category, FAQ_CATEGORY)
        }

        @Test
        fun `GIVEN existing shared faq on a bot application on same namespace WHEN updating faq by erasing some existing one and adding another one THEN added classified sentence should be created and the other deleted`() {

            //MOCK
            val faqAdminService = spyk<FaqAdminService>(recordPrivateCalls = true)

            //shared intent in two app
            //GIVEN
            val secondFaqUtterance = "second FAQ utterance"

            //faq definition request already exists with same intentId
            val existingSecondBotFaqSharedIntentRequest = faqDefinitionRequest.copy(
                id = faqId2.toString(),
                intentId = intentId.toString(),
                applicationName = OTHER_APP_NAME,
                answer = FAQ_SPECIFIC_ANSWER,
                // add same existing utterances and another one
                utterances = faqDefinitionRequest.utterances.plus(secondFaqUtterance)
            )

            // faq is already existing to be updated
            val existingFaqSharedIntentDefinition = FaqDefinition(
                faqId2,
                OTHER_APP_NAME,
                namespace,
                intentId,
                i18nId,
                existingSecondBotFaqSharedIntentRequest.tags,
                existingSecondBotFaqSharedIntentRequest.enabled,
                existingSecondBotFaqSharedIntentRequest.creationDate!!,
                existingSecondBotFaqSharedIntentRequest.updateDate!!,
            )

            every {
                faqDefinitionDAO.getFaqDefinitionByIntentIdAndBotIdAndNamespace(
                    eq(intentId),
                    eq(OTHER_APP_NAME),
                    eq(namespace)
                )
            } answers { existingFaqSharedIntentDefinition }

            every {
                faqAdminService["createOrUpdateFaqIntent"](
                    allAny<FaqDefinitionRequest>(), allAny<ApplicationDefinition>()
                )
            } returns existingIntent.copy(applications = setOf(applicationId, OTHER_APP_NAME.toId()))

            val otherAppDefinition = applicationDefinition.copy(name = OTHER_APP_NAME)

            //WHEN
            val removingSomeUtterancesSave = existingSecondBotFaqSharedIntentRequest.copy(utterances = listOf(secondFaqUtterance))
            val savedFaq = faqAdminService.saveFAQ(removingSomeUtterancesSave, userLogin, otherAppDefinition)

            //THEN
            // FAQ
            verify(exactly = 1) { faqDefinitionDAO.save(any()) }
            assertEquals(savedFaq.utterances.size,1)

            // Classified Sentences
            val sentenceQuerySlot = slot<SentencesQuery>()
            verify(exactly = 1) {
                sentenceDAO.search(capture(sentenceQuerySlot))
            }

            assertEquals(intentId,sentenceQuerySlot.captured.intentId)

            // save only one new sentence not the shared one
            verify(exactly = 1) { BotAdminService.saveSentence(eq(secondFaqUtterance), any(), any(), any(), any()) }

            val switchedClassifiedSentences = slot<List<ClassifiedSentence>>()
            verify(exactly = 1) { sentenceDAO.switchSentencesStatus(capture(switchedClassifiedSentences), eq(deleted)) }
            assertEquals(switchedClassifiedSentences.captured.size, 0)

            // Story
            val slotStory = slot<StoryDefinitionConfiguration>()
            verify(exactly = 1) { storyDefinitionDAO.save(capture(slotStory)) }
            assertEquals(slotStory.captured.storyId, existingMessageStory.storyId)

            //story name must not be overwritten
            assertNotEquals(slotStory.captured.name, existingMessageStory.name)
            assertEquals(slotStory.captured.category, FAQ_CATEGORY)
        }
    }


    @Nested
    inner class DeleteFaq {

        private fun initDeleteFaqMock(
            mockedIntentDefinition: IntentDefinition? = existingIntent,
            mockedFaqDefinition: FaqDefinition? = faqDefinition,
            mockedStoryDefinition: StoryDefinitionConfiguration = existingStory,
            mockedApplicationDefinition: ApplicationDefinition? = applicationDefinition
        ) {
            every { applicationDefininitionDAO.getApplicationById(eq(applicationId)) } returns applicationDefinition
            every { applicationDefininitionDAO.getApplicationByNamespaceAndName(eq(namespace), eq(faqDefinition.botId)) } returns mockedApplicationDefinition

            every { faqDefinitionDAO.getFaqDefinitionById(any()) } returns mockedFaqDefinition

            every { intentDAO.getIntentById(any()) } returns mockedIntentDefinition

            justRun {
                faqDefinitionDAO.deleteFaqDefinitionById(any())
            }

            initMocksForSingleStory(mockedStoryDefinition)

            every {
                applicationConfigurationDAO.getConfigurationsByNamespaceAndBotId(
                    eq(namespace),
                    eq(mockedStoryDefinition.botId)
                )
            } returns listOf(
                BotApplicationConfiguration(
                    applicationId.toString(), mockedStoryDefinition.storyId, namespace, NlpEngineType.opennlp.name,
                    ConnectorType.rest, ConnectorType.rest
                )
            )
        }

        @Test
        fun `GIVEN delete single faq WHEN intent existing and one applicationId is found`() {
            val faqAdminService = spyk<FaqAdminService>(recordPrivateCalls = true)
            initDeleteFaqMock()

            val isDeleted = faqAdminService.deleteFaqDefinition(namespace, faqId.toString())

            assertTrue(isDeleted, "It should returns true because faq should be deleted")
            verify(exactly = 1) {
                faqDefinitionDAO.getFaqDefinitionById(any())
                faqDefinitionDAO.deleteFaqDefinitionById(eq(faqId))
                intentDAO.getIntentById(any())
                storyDefinitionDAO.delete(any())
            }
        }

        @Test
        fun `GIVEN delete single faq WHEN intent null and one applicationId is found`() {
            val faqAdminService = spyk<FaqAdminService>(recordPrivateCalls = true)
            initDeleteFaqMock(mockedIntentDefinition = null)

            val isDeleted = faqAdminService.deleteFaqDefinition(namespace, faqId.toString())

            assertFalse(isDeleted, "It should returns false because faq could not be deleted")

            verify(exactly = 1) { faqDefinitionDAO.getFaqDefinitionById(any()) }
            verify(exactly = 0) {
                faqDefinitionDAO.deleteFaqDefinitionById(eq(faqId))
                storyDefinitionDAO.delete(any())
            }
        }

        @Test
        fun `GIVEN delete single faq WHEN faq null and one applicationId is found`() {
            val faqAdminService = spyk<FaqAdminService>(recordPrivateCalls = true)
            initDeleteFaqMock(mockedFaqDefinition = null)

            val isDeleted = faqAdminService.deleteFaqDefinition(namespace, faqId.toString())
            verify(exactly = 1) { faqDefinitionDAO.getFaqDefinitionById(any()) }
            verify(exactly = 0) {
                faqDefinitionDAO.deleteFaqDefinitionById(eq(faqId))
                intentDAO.getIntentById(any())
                storyDefinitionDAO.delete(any())
            }

            assertFalse(isDeleted, "It should returns false because faq could not be deleted")
        }

        @Test
        fun `GIVEN delete single faq WHEN intent existing and one application is not found`() {
            val faqAdminService = spyk<FaqAdminService>(recordPrivateCalls = true)
            initDeleteFaqMock(mockedApplicationDefinition = null)

            assertThrows<BadRequestException> {
                faqAdminService.deleteFaqDefinition(namespace, faqId.toString())
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
            val numberOfUtterances = 1
            val expectedUtteranceLabel = "expectedLabel"

            initSearchFaqMockWithoutLabelsSearch(
                listOf(
                    createFaqQueryResult(
                        faqId = faqId2,
                        intentId = intentId2,
                        i18nId = i18nId2,
                        numberOfUtterances = 0
                    ),
                    createFaqQueryResult(numberOfUtterances = 1, utteranceText = expectedUtteranceLabel)
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
            //test name is the utterranceText+numberOfutterance
            //here 0 is the first label, see createFaqQueryResult() method
            assertEquals(
                expectedUtteranceLabel + numberOfUtterances,
                faqResult.rows.get(1).utterances[0],
                "The name of the utterance should be $expectedUtteranceLabel$numberOfUtterances"
            )

            verify(exactly = 1) {
                faqDefinitionDAO.getFaqDetailsWithCount(
                    any(), applicationDefinition, null
                )
            }
            verify(exactly = 1) {
                faqAdminService["mapI18LabelFaqAndConvertToFaqDefinitionRequest"](
                    any<List<FaqQueryResult>>(), any<ApplicationDefinition>()
                )
            }
            verify(exactly = 0) {
                faqAdminService["searchLabelsFromTockFrontDb"](
                    any<List<FaqQueryResult>>(), any<ApplicationDefinition>()
                )
            }
            verify(atLeast = 2) { i18nDAO.getLabelById(any()) }
        }

        @Test
        fun `GIVEN search faq with a query text 'Question' WHEN i18nLabels search found THEN return expected number of faq`() {
            val faqAdminService = spyk<FaqAdminService>(recordPrivateCalls = true)
            initSearchFaqMockWithLabelsSearch(
                listOf(
                    createFaqQueryResult(
                        faqId = faqId2,
                        faqName = "FAQ A",
                        intentId = intentId2,
                        i18nId = i18nId2,
                        utteranceText = "Question with answer to find",
                        numberOfUtterances = 3
                    ),
                    createFaqQueryResult(
                        faqId = faqId3,
                        intentId = intentId3,
                        i18nId = i18nId3,
                        faqName = "FAQ B",
                        numberOfUtterances = 3,
                        utteranceText = "Question with answer also expected"
                    ),
                    createFaqQueryResult(
                        faqId = faqId4,
                        i18nId = i18nId4,
                        intentId = intentId4,
                        faqName = "FAQ C",
                        numberOfUtterances = 1,
                        utteranceText = "utteranceLabelUnexpected"
                    )
                ),
                listOf<I18nLabel>(
                    mockedI18n.copy(
                        i18nId2, i18n = linkedSetOf<I18nLocalizedLabel>(
                            mockedDefaultLocalizedLabel.copy(label = "Answer to find1")
                        ), defaultLabel = "Answer to find1"
                    ),
                    mockedI18n.copy(
                        i18nId3, i18n = linkedSetOf<I18nLocalizedLabel>(
                            mockedDefaultLocalizedLabel.copy(label = "Answer also expected1")
                        ), defaultLabel = "Answer also expected1"
                    )
                )
            )

            val faqSearchRequest = createFaqSearchRequest(enabled = null, search = "Question")
            val faqResult = faqAdminService.searchFAQ(faqSearchRequest, applicationDefinition)

            assertEquals(faqResult.total, 2, "There should be two faq found when searching for a label question")
        }

        @Test
        fun `GIVEN search faq WHEN faq found THEN faq title and description equals to respectively story name and description`() {
            val faqAdminService = spyk<FaqAdminService>(recordPrivateCalls = true)

            val story1 = generateStory(generateIntentDefinition(intentId.toString()), "storyId-1")
            val story2 = generateStory(generateIntentDefinition(intentId2.toString()), "storyId-2")

            initSearchFaqMockWithoutLabelsSearch(
                listOf(
                    createFaqQueryResult(
                        faqId = faqId2, intentId = intentId2, i18nId = i18nId2, numberOfUtterances = 0
                    ),
                    createFaqQueryResult(
                        numberOfUtterances = 1, utteranceText = "testUtteranceText"
                    )
                ),
                stories = listOf(story1, story2)
            )

            val faqSearchRequest = createFaqSearchRequest(enabled = null, search = null)
            val faqResult = faqAdminService.searchFAQ(faqSearchRequest, applicationDefinition)

            val firstFaq = faqResult.rows.first { it.intentId == intentId.toString() }
            val secondFaq = faqResult.rows.first { it.intentId == intentId2.toString() }

            assertEquals(story1.name, firstFaq.title)
            assertEquals(story1.description, firstFaq.description)
            assertEquals(story2.name, secondFaq.title)
            assertEquals(story2.description, secondFaq.description)
        }

        private fun initSearchFaqMockWithoutLabelsSearch(
            faqQueryResults: List<FaqQueryResult> = listOf(createFaqQueryResult()),
            mockedI18nLabels: List<I18nLabel> = mockedI18nLabels_2Elements,
            stories: List<StoryDefinitionConfiguration> = emptyList()
        ) {
            val searchResult = Pair(faqQueryResults, faqQueryResults.size.toLong())

            every { faqDefinitionDAO.getFaqDetailsWithCount(any(), any(), any()) } returns searchResult

            every { i18nDAO.getLabelById(i18nId) } returns mockedI18nLabels[0]
            if (mockedI18nLabels.size > 1) {
                every { i18nDAO.getLabelById(i18nId2) } returns mockedI18nLabels[1]
            }

            every {
                storyDefinitionDAO.getConfiguredStoriesDefinitionByNamespaceAndBotIdAndIntent(
                    any(), any(), any()
                )
            } returns stories
        }

        private fun initSearchFaqMockWithLabelsSearch(
            faqQueryResults: List<FaqQueryResult> = listOf(createFaqQueryResult()),
            mockedI18nLabels: List<I18nLabel> = mockedI18nLabels_2Elements,
            stories: List<StoryDefinitionConfiguration> = emptyList()
        ) {

            val searchedWithLabels =
                mockedI18nLabels.flatMap { i18n -> faqQueryResults.filter { it.i18nId == i18n._id }.map { it } }

            val searchResult = Pair(searchedWithLabels, searchedWithLabels.size.toLong())

            every { i18nDAO.getLabels(namespace, any()) } returns mockedI18nLabels

            val i18nLabelId: CapturingSlot<Id<I18nLabel>> = slot()
            every { i18nDAO.getLabelById(capture(i18nLabelId)) } answers { mockedI18nLabels.firstOrNull { it._id == i18nLabelId.captured } }

            every { faqDefinitionDAO.getFaqDetailsWithCount(any(), any(), any()) } returns searchResult

            every {
                storyDefinitionDAO.getConfiguredStoriesDefinitionByNamespaceAndBotIdAndIntent(
                    any(), any(), any()
                )
            } returns stories
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
            botId: String = FaqAdminServiceTest.botId,
            enabled: Boolean = true,
            faqName: String = "Faq Name",
            numberOfUtterances: Int = 1,
            utteranceText: String = "randomText",
            classifiedSentenceStatus: ClassifiedSentenceStatus = ClassifiedSentenceStatus.validated,
            instant: Instant = now,
            intentName: String = "name-$intentId"
        ): FaqQueryResult {

            val createdIntent = IntentDefinition(
                intentName,
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
                utterances.add(createUtterance("$utteranceText$number", intentId, classifiedSentenceStatus))
            }

            return FaqQueryResult(
                faqId,
                botId,
                namespace,
                intentId,
                i18nId,
                tagList,
                enabled,
                instant,
                instant,
                utterances,
                createdIntent,
                intentName
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

        private fun generateIntentDefinition(intentId: String): IntentDefinition {
            return IntentDefinition(
                _id = intentId.toId(),
                name = "name-$intentId",
                namespace = "namespace-$intentId",
                applications = setOf("appId-$intentId".toId()),
                entities = emptySet<EntityDefinition>(),
                category = FAQ_CATEGORY
            )
        }

        private fun generateStory(intent: IntentDefinition, storyId: String): StoryDefinitionConfiguration {
            return StoryDefinitionConfiguration(
                _id = storyId.toId(),
                storyId = storyId,
                name = "name-$storyId",
                botId = "bot-$storyId",
                intent = IntentWithoutNamespace(intent.name),
                currentType = AnswerConfigurationType.simple,
                answers = emptyList(),
                category = FAQ_CATEGORY
            )
        }

    }

}
