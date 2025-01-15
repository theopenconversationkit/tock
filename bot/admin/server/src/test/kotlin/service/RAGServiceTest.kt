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

package ai.tock.bot.admin.service

import ai.tock.bot.admin.AbstractTest
import ai.tock.bot.admin.BotAdminService
import ai.tock.bot.admin.answer.AnswerConfigurationType
import ai.tock.bot.admin.bot.observability.BotObservabilityConfigurationDAO
import ai.tock.bot.admin.bot.rag.BotRAGConfiguration
import ai.tock.bot.admin.bot.rag.BotRAGConfigurationDAO
import ai.tock.bot.admin.model.BotRAGConfigurationDTO
import ai.tock.bot.admin.story.StoryDefinitionConfiguration
import ai.tock.bot.admin.story.StoryDefinitionConfigurationDAO
import ai.tock.bot.definition.IntentWithoutNamespace
import ai.tock.bot.test.TFunction
import ai.tock.bot.test.TRunnable
import ai.tock.bot.test.TSupplier
import ai.tock.bot.test.TestCase
import ai.tock.genai.orchestratorclient.requests.PromptTemplate
import ai.tock.genai.orchestratorclient.responses.ProviderSettingStatusResponse
import ai.tock.genai.orchestratorclient.services.EMProviderService
import ai.tock.genai.orchestratorclient.services.LLMProviderService
import ai.tock.genai.orchestratorcore.models.em.AzureOpenAIEMSettingDTO
import ai.tock.genai.orchestratorcore.models.llm.OpenAILLMSettingDTO
import ai.tock.nlp.core.Intent
import ai.tock.shared.tockInternalInjector
import ai.tock.shared.withoutNamespace
import ai.tock.translator.I18nDAO
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.KodeinInjector
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.provider
import com.github.salomonbrys.kodein.singleton
import io.mockk.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.litote.kmongo.toId

class RAGServiceTest : AbstractTest() {

    companion object {

        private const val UNKNOWN_INTENT = "unknown"

        const val BOT_ID = "app"
        const val NAMESPACE = "namespace"
        const val PROVIDER = "OpenAI"
        const val MODEL = "gpt4"
        const val TEMPERATURE = "0"
        const val PROMPT = "mocked prompt"
        const val INDEX_SESSION_ID = "1010101"

        private val DEFAULT_RAG_CONFIG = BotRAGConfigurationDTO(
             id = "ragId",
            namespace = NAMESPACE,
            botId = BOT_ID,
            enabled = false,
            questionCondensingLlmSetting = OpenAILLMSettingDTO(
                apiKey = "apikey",
                model = MODEL,
                temperature = TEMPERATURE,
                baseUrl = "https://api.openai.com/v1"
            ),
            questionCondensingPrompt = PromptTemplate(template = PROMPT),
            questionAnsweringLlmSetting = OpenAILLMSettingDTO(
                apiKey = "apikey",
                model = MODEL,
                temperature = TEMPERATURE,
                baseUrl = "https://api.openai.com/v1"
            ),
            questionAnsweringPrompt = PromptTemplate(template = PROMPT),
            emSetting = AzureOpenAIEMSettingDTO(
                apiKey = "apiKey",
                apiVersion = "apiVersion",
                deploymentName = "deployment",
                model = "model",
                apiBase = "url"
            ),
            noAnswerSentence = "No answer sentence",
            documentsRequired = true,
            debugEnabled = false,
            maxDocumentsRetrieved = 2,
            maxMessagesFromHistory = 2,
        )

        private val DEFAULT_BOT_CONFIG = aApplication.copy(namespace = NAMESPACE, botId = BOT_ID)

        private fun getRAGConfigurationDTO(enabled: Boolean, indexSessionId: String? = null) =
            DEFAULT_RAG_CONFIG.copy(enabled = enabled, indexSessionId = indexSessionId)

        init {
            tockInternalInjector = KodeinInjector()
            Kodein.Module {
                bind<BotRAGConfigurationDAO>() with singleton { ragDao }
                bind<StoryDefinitionConfigurationDAO>() with singleton { storyDao }
                bind<LLMProviderService>() with singleton { llmProviderService }
                bind<EMProviderService>() with singleton { emProviderService }
                bind<I18nDAO>() with singleton { i18nDAO }
                bind<BotObservabilityConfigurationDAO>() with provider { botObservabilityConfigurationDAO }

            }.also {
                tockInternalInjector.inject(Kodein {
                    import(defaultModulesBinding())
                    import(it)
                })
            }
        }

        private val ragDao: BotRAGConfigurationDAO = mockk(relaxed = false)
        private val storyDao: StoryDefinitionConfigurationDAO = mockk(relaxed = true)

        private val llmProviderService: LLMProviderService = mockk(relaxed = false)
        private val emProviderService: EMProviderService = mockk(relaxed = false)

        private val i18nDAO: I18nDAO = mockk(relaxed = true)
        private val botObservabilityConfigurationDAO: BotObservabilityConfigurationDAO = mockk(relaxed = true)

        private val slot = slot<BotRAGConfiguration>()
        private val storySlot = slot<StoryDefinitionConfiguration>()
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `Save rag configuration enabled when it does not exists`() {

        val entry: TSupplier<SaveFnEntry> = {
            getRAGConfigurationDTO(true, INDEX_SESSION_ID)
        }

        val ragNotYetExists: TRunnable = {
            every { BotAdminService.getBotConfigurationsByNamespaceAndBotId(eq(NAMESPACE), eq(BOT_ID)) } returns listOf(
                DEFAULT_BOT_CONFIG
            )
            every { ragDao.save(any()) } returns getRAGConfigurationDTO(true, INDEX_SESSION_ID).toBotRAGConfiguration()
        }

        val checkLlmAndEmSetting: TRunnable = {
            every { llmProviderService.checkSetting(any()) } returns ProviderSettingStatusResponse(valid = true)
            every { emProviderService.checkSetting(any()) } returns ProviderSettingStatusResponse(valid = true)
        }

        val captureRagAndStoryToSave: TRunnable = {
            every { storyDao.save(capture(storySlot)) } returns Unit
            every { ragDao.save(capture(slot)) } returns getRAGConfigurationDTO(
                true,
                INDEX_SESSION_ID
            ).toBotRAGConfiguration()
        }

        val callServiceSave: TFunction<SaveFnEntry?, Unit> = {
            Assertions.assertNotNull(it)
            RAGService.saveRag(it!!)
        }

        val daoSaveByFnIsCalledOnce: TRunnable = {
            verify(exactly = 1) { storyDao.save(any()) }
            verify(exactly = 1) {
                ragDao.save(
                    eq(
                        getRAGConfigurationDTO(
                            true,
                            INDEX_SESSION_ID
                        ).toBotRAGConfiguration()
                    )
                )
            }
        }

        val findCurrentUnknownFnNotCalled: TRunnable = {
            verify(exactly = 0) { ragDao.findByNamespaceAndBotId(any(), any()) }
            verify(atLeast = 0) { storyDao.getStoryDefinitionById(any()) }
        }

        val checkRagConfigToPersist: TRunnable = {
            Assertions.assertTrue(slot.isCaptured)
            val captured = slot.captured
            Assertions.assertNotNull(captured)
            Assertions.assertNotNull(captured._id)
            Assertions.assertEquals(BOT_ID, captured.botId)
            Assertions.assertEquals(true, captured.enabled)
            Assertions.assertEquals(NAMESPACE, captured.namespace)
            Assertions.assertEquals(PROVIDER, captured.questionAnsweringLlmSetting!!.provider.name)
            Assertions.assertEquals(TEMPERATURE, captured.questionAnsweringLlmSetting!!.temperature)
            Assertions.assertEquals(PROMPT, captured.questionAnsweringPrompt!!.template)
            Assertions.assertEquals(null, captured.noAnswerStoryId)
        }


        TestCase<SaveFnEntry, Unit>("Save valid RAG Configuration that does not exist yet").given(
            "An application name and a valid request",
            entry
        ).and(
            "Rag Config not exist with request name or label and the given application name", ragNotYetExists
        ).and("The rag config in database is captured", captureRagAndStoryToSave)
            .and("The LLM and EM setting are valid", checkLlmAndEmSetting)
            .`when`("RagService's save method is called", callServiceSave)
            .then("The dao's saveEnableRagRequest must be called exactly once", daoSaveByFnIsCalledOnce)
            .and("The dao's to find current unknown story must be not called", findCurrentUnknownFnNotCalled).and(
                """
                - BotRAGConfiguration to persist must be not null
                - BotRAGConfiguration to persist must have a not null id
                - BotRAGConfiguration is enabled
            """.trimIndent(), checkRagConfigToPersist
            ).run()
    }

    @Test
    fun `Save rag configuration when unknown story does not exists`() {

        // GIVEN
        // No story exists for unknown intent
        val noStoryExistsForUnknownIntent: TRunnable = {
            every {
                storyDao.getStoryDefinitionByNamespaceAndBotIdAndIntent(
                    eq(NAMESPACE), eq(BOT_ID), eq(UNKNOWN_INTENT)
                )
            } returns null
        }
        val checkLlmAndEmSetting: TRunnable = {
            every { llmProviderService.checkSetting(any()) } returns ProviderSettingStatusResponse(valid = true)
            every { emProviderService.checkSetting(any()) } returns ProviderSettingStatusResponse(valid = true)
        }

        // RAG Configuration
        val ragConfigurationDTO = getRAGConfigurationDTO(true, INDEX_SESSION_ID)
        val query: TSupplier<SaveFnEntry> = { ragConfigurationDTO }

        // WHEN
        // save RAG Configuration
        val callService: TFunction<SaveFnEntry?, Unit> = {
            Assertions.assertNotNull(it)
            RAGService.saveRag(it!!)
        }

        // WITH
        val mocks: TRunnable = {
            every { BotAdminService.getBotConfigurationsByNamespaceAndBotId(eq(NAMESPACE), eq(BOT_ID)) } returns listOf(
                DEFAULT_BOT_CONFIG
            )
            every {
                ragDao.save(
                    query.invoke().toBotRAGConfiguration()
                )
            } returns ragConfigurationDTO.toBotRAGConfiguration()
        }

        // THEN
        val checks: TRunnable = {
            // check existence of unknown story
            verify(exactly = 1) {
                storyDao.getStoryDefinitionByNamespaceAndBotIdAndIntent(
                    eq(NAMESPACE), eq(BOT_ID), UNKNOWN_INTENT
                )
            }
            // no story is saved
            verify(exactly = 0) { storyDao.save(any()) }
            // rag configuration is saved
            verify(exactly = 1) { ragDao.save(eq(ragConfigurationDTO.toBotRAGConfiguration())) }
        }

        TestCase<SaveFnEntry, Unit>("Save valid RAG Configuration")
            .given(
                "No story exists for unknown intent",
                noStoryExistsForUnknownIntent
            )
            .and("Rag Configuration is valid", query)
            .and("The LLM and EM setting are valid", checkLlmAndEmSetting)
            .and("Bot configuration exists", mocks)
            .`when`("RagService's save method is called", callService)
            .then(
                """
                - no story is saved
                - rag configuration is saved
            """.trimIndent(), checks
            )
            .run()

    }

    @Test
    fun `Save enabled rag configuration when unknown story exists`() {

        // GIVEN
        // Story exists for unknown intent
        val unknownStory = StoryDefinitionConfiguration(
            _id = "aaa".toId(),
            namespace = NAMESPACE,
            storyId = "unknown_story_id",
            botId = BOT_ID,
            intent = IntentWithoutNamespace(Intent.UNKNOWN_INTENT_NAME.withoutNamespace()),
            currentType = AnswerConfigurationType.simple,
            answers = emptyList()
        )
        val noStoryExistsForUnknownIntent: TRunnable = {
            every {
                storyDao.getStoryDefinitionByNamespaceAndBotIdAndIntent(
                    eq(NAMESPACE), eq(BOT_ID), eq(UNKNOWN_INTENT)
                )
            } returns unknownStory
        }
        val checkLlmAndEmSetting: TRunnable = {
            every { llmProviderService.checkSetting(any()) } returns ProviderSettingStatusResponse(valid = true)
            every { emProviderService.checkSetting(any()) } returns ProviderSettingStatusResponse(valid = true)
        }

        // RAG Configuration (enabled)
        val ragConfigurationDTO = getRAGConfigurationDTO(true, INDEX_SESSION_ID)
        val query: TSupplier<SaveFnEntry> = { ragConfigurationDTO }

        // WHEN
        // save RAG Configuration
        val callService: TFunction<SaveFnEntry?, Unit> = {
            Assertions.assertNotNull(it)
            RAGService.saveRag(it!!)
        }

        // WITH
        val mocks: TRunnable = {
            every { BotAdminService.getBotConfigurationsByNamespaceAndBotId(eq(NAMESPACE), eq(BOT_ID)) } returns listOf(
                DEFAULT_BOT_CONFIG
            )
            every {
                ragDao.save(
                    query.invoke().toBotRAGConfiguration()
                )
            } returns ragConfigurationDTO.toBotRAGConfiguration()
        }

        // THEN
        val checks: TRunnable = {
            // check existence of unknown story
            verify(exactly = 1) {
                storyDao.getStoryDefinitionByNamespaceAndBotIdAndIntent(
                    eq(NAMESPACE), eq(BOT_ID), UNKNOWN_INTENT
                )
            }
            // unknown story is saved
            verify(exactly = 1) { storyDao.save(capture(storySlot)) }
            // unknown story is disabled
            Assertions.assertTrue(storySlot.isCaptured)
            Assertions.assertNotNull(storySlot.captured.features.find { it.enabled == !ragConfigurationDTO.enabled })

            // rag configuration is saved
            verify(exactly = 1) { ragDao.save(eq(ragConfigurationDTO.toBotRAGConfiguration())) }
        }

        TestCase<SaveFnEntry, Unit>("Save enabled RAG Configuration")
            .given(
                "Unknown story exists",
                noStoryExistsForUnknownIntent
            ).and("Rag Configuration is valid", query)
            .and("The LLM and EM setting are valid", checkLlmAndEmSetting)
            .and("Bot configuration exists", mocks)
            .`when`("RagService's save method is called", callService)
            .then(
                """
                - unknown story is saved
                - unknown story is disabled
                - rag configuration is saved
            """.trimIndent(), checks
            )
            .run()
    }

    @Test
    fun `Save disabled rag configuration when unknown story exists`() {

        // GIVEN
        // Story exists for unknown intent
        val unknownStory = StoryDefinitionConfiguration(
            _id = "aaa".toId(),
            namespace = NAMESPACE,
            storyId = "unknown_story_id",
            botId = BOT_ID,
            intent = IntentWithoutNamespace(Intent.UNKNOWN_INTENT_NAME.withoutNamespace()),
            currentType = AnswerConfigurationType.simple,
            answers = emptyList()
        )
        val unknownStoryExists: TRunnable = {
            every {
                storyDao.getStoryDefinitionByNamespaceAndBotIdAndIntent(
                    eq(NAMESPACE), eq(BOT_ID), eq(UNKNOWN_INTENT)
                )
            } returns unknownStory
        }
        val checkLlmAndEmSetting: TRunnable = {
            every { llmProviderService.checkSetting(any()) } returns ProviderSettingStatusResponse(valid = true)
            every { emProviderService.checkSetting(any()) } returns ProviderSettingStatusResponse(valid = true)
        }
        // RAG Configuration (disabled)
        val ragConfigurationDTO = getRAGConfigurationDTO(false)
        val query: TSupplier<SaveFnEntry> = { ragConfigurationDTO }

        // WHEN
        // save RAG Configuration
        val callService: TFunction<SaveFnEntry?, Unit> = {
            Assertions.assertNotNull(it)
            RAGService.saveRag(it!!)
        }

        // WITH
        val mocks: TRunnable = {
            every { BotAdminService.getBotConfigurationsByNamespaceAndBotId(eq(NAMESPACE), eq(BOT_ID)) } returns listOf(
                DEFAULT_BOT_CONFIG
            )
            every {
                ragDao.save(
                    query.invoke().toBotRAGConfiguration()
                )
            } returns ragConfigurationDTO.toBotRAGConfiguration()
        }

        // THEN
        val checks: TRunnable = {
            // check existence of unknown story
            verify(exactly = 1) {
                storyDao.getStoryDefinitionByNamespaceAndBotIdAndIntent(
                    eq(NAMESPACE), eq(BOT_ID), UNKNOWN_INTENT
                )
            }
            // unknown story is saved
            verify(exactly = 1) { storyDao.save(capture(storySlot)) }
            // unknown story is enabled
            Assertions.assertTrue(storySlot.isCaptured)
            Assertions.assertNotNull(storySlot.captured.features.find { it.enabled == !ragConfigurationDTO.enabled })

            // rag configuration is saved
            verify(exactly = 1) { ragDao.save(eq(ragConfigurationDTO.toBotRAGConfiguration())) }
        }

        TestCase<SaveFnEntry, Unit>("Save disabled RAG Configuration")
            .given(
                "Unknown story exists",
                unknownStoryExists
            ).and("Rag Configuration is valid", query)
            .and("The LLM and EM setting are valid", checkLlmAndEmSetting)
            .and("Bot configuration exists", mocks)
            .`when`("RagService's save method is called", callService)
            .then(
                """
                - unknown story is saved
                - unknown story is enabled
                - rag configuration is saved
            """.trimIndent(), checks
            )
            .run()
    }

}


typealias SaveFnEntry = BotRAGConfigurationDTO
