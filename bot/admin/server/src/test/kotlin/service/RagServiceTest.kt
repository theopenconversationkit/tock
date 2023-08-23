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
import ai.tock.bot.admin.answer.RagAnswerConfiguration
import ai.tock.bot.admin.bot.BotRAGConfiguration
import ai.tock.bot.admin.bot.BotRAGConfigurationDAO
import ai.tock.bot.admin.indicators.IndicatorError
import ai.tock.bot.admin.model.BotRAGConfigurationDTO
import ai.tock.bot.admin.model.Valid
import ai.tock.bot.admin.story.StoryDefinitionConfiguration
import ai.tock.bot.admin.story.StoryDefinitionConfigurationDAO
import ai.tock.bot.definition.IntentWithoutNamespace
import ai.tock.bot.definition.RagStoryDefinition
import ai.tock.bot.test.*
import ai.tock.shared.exception.rest.BadRequestException
import ai.tock.shared.tockInternalInjector
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.KodeinInjector
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.singleton
import io.mockk.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.litote.kmongo.toId
import service.IndicatorServiceTest

class RagServiceTest : AbstractTest() {

    companion object {

        private const val UNKNOWN_INTENT = "unknown"

        const val BOT_ID = "app"
        const val NAMESPACE = "namespace"
        const val ENGINE = "openai"
        const val EMBEDDING_ENGINE = "gpt4"
        const val TEMPERATURE = "0"
        const val PROMPT = "mocked prompt"
        private val PARAMETERS = mapOf("openaikey" to "value")
        private val RAG_STORY_ID = RagStoryDefinition.RAG_STORY_NAME.toId<StoryDefinitionConfiguration>()

        private val DEFAULT_RAG_CONFIG = BotRAGConfigurationDTO("ragId".toId(), NAMESPACE, BOT_ID, false, ENGINE, EMBEDDING_ENGINE, TEMPERATURE, PROMPT, PARAMETERS, null, null)
        private val DEFAULT_BOT_CONFIG = aApplication.copy(namespace = NAMESPACE, botId = BOT_ID)

        private fun saveEnableRagRequest() = DEFAULT_RAG_CONFIG.copy(enabled = true)
        private fun saveEnableRagRequestWithSavedUnknownBackupStory() = DEFAULT_RAG_CONFIG.copy(enabled = true, unknownStoryBackupId = "unknownStoryId".toId())
        private fun saveDisableRagRequestWithSavedUnknownBackupStory() = DEFAULT_RAG_CONFIG.copy(enabled = false, unknownStoryBackupId = "unknownStoryId".toId())

        init {
            tockInternalInjector = KodeinInjector()
            Kodein.Module {
                bind<BotRAGConfigurationDAO>() with singleton { ragDao }
                bind<StoryDefinitionConfigurationDAO>() with singleton { storyDao }

            }.also {
                tockInternalInjector.inject(Kodein {
                    import(defaultModulesBinding())
                    import(it)
                })
            }
        }

        private val ragDao: BotRAGConfigurationDAO = mockk(relaxed = false)
        private val storyDao: StoryDefinitionConfigurationDAO = mockk(relaxed = true)

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
            saveEnableRagRequest()
        }

        val ragNotYetExists: TRunnable = {
            every { BotAdminService.getBotConfigurationsByNamespaceAndBotId(eq(NAMESPACE), eq(BOT_ID)) } returns listOf(DEFAULT_BOT_CONFIG)
            every { storyDao.getConfiguredStoryDefinitionByNamespaceAndBotIdAndIntent(eq(NAMESPACE), eq(BOT_ID), RagStoryDefinition.OVERRIDDEN_UNKNOWN_INTENT) } returns null
            every { storyDao.getAndDeleteRagStoryDefinitionByNamespaceAndBotId(eq(NAMESPACE), eq(BOT_ID)) } just Runs
            every { ragDao.save(any()) } returns saveEnableRagRequest().toBotRAGConfiguration()
        }

        val captureRagAndStoryToSave: TRunnable = {
            every { storyDao.save(capture(storySlot)) } returns Unit
            every { ragDao.save(capture(slot)) } returns saveEnableRagRequest().toBotRAGConfiguration()
        }

        val callServiceSave: TFunction<SaveFnEntry?, Unit> = {
            Assertions.assertNotNull(it)
            RagService.saveRag(it!!)
        }

        val daoExistByFnIsCalledOnce: TRunnable = {
            verify(exactly = 1) { storyDao.getConfiguredStoryDefinitionByNamespaceAndBotIdAndIntent(eq(NAMESPACE), eq(BOT_ID), RagStoryDefinition.OVERRIDDEN_UNKNOWN_INTENT) }
        }

        val daoGetAndDeletedRagByFnNotCalled: TRunnable = {
            verify(exactly = 0) { storyDao.getAndDeleteRagStoryDefinitionByNamespaceAndBotId(eq(NAMESPACE), eq(BOT_ID)) }
        }

        val daoSaveByFnIsCalledOnce: TRunnable = {
            verify(exactly = 1) { storyDao.save(any()) }
            verify(exactly = 1) { ragDao.save(eq(saveEnableRagRequest().toBotRAGConfiguration())) }
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
            Assertions.assertEquals(EMBEDDING_ENGINE, captured.embeddingEngine)
            Assertions.assertEquals(TEMPERATURE, captured.temperature)
            Assertions.assertEquals(PROMPT, captured.prompt)
            Assertions.assertEquals(null, captured.unknownStoryBackupId)
            Assertions.assertEquals(null, captured.noAnswerRedirection)
        }

        val checkStoryToPersist: TRunnable = {
            Assertions.assertTrue(storySlot.isCaptured)
            val captured = storySlot.captured
            Assertions.assertNotNull(captured)
            Assertions.assertNotNull(captured._id)
            Assertions.assertEquals(BOT_ID, captured.botId)
            Assertions.assertEquals(true, captured.features[0].enabled)
            Assertions.assertEquals(AnswerConfigurationType.rag, captured.currentType)
            Assertions.assertEquals(NAMESPACE, captured.namespace)
            Assertions.assertEquals(EMBEDDING_ENGINE, (captured.answers[0] as RagAnswerConfiguration).embeddingEngine)
            Assertions.assertEquals(TEMPERATURE, (captured.answers[0] as RagAnswerConfiguration).temperature)
            Assertions.assertEquals(PROMPT, (captured.answers[0] as RagAnswerConfiguration).prompt)
            Assertions.assertEquals(null, (captured.answers[0] as RagAnswerConfiguration).noAnswerRedirection)
        }

        TestCase<SaveFnEntry, Unit>("Save valid Rag Configuration that does not exist yet")
                .given("An application name and a valid request", entry)
                .and(
                        "Rag Config not exist with request name or label and the given application name",
                        ragNotYetExists
                )
                .and("The rag config and story definition to persist in database is captured", captureRagAndStoryToSave)
                .`when`("RagService's save method is called", callServiceSave)
                .then("The dao's getConfiguredStoryDefinitionByNamespaceAndBotIdAndIntent must be called exactly once", daoExistByFnIsCalledOnce)
                .and("The dao's getAndDeleteRagStoryDefinitionByNamespaceAndBotId must not be called", daoGetAndDeletedRagByFnNotCalled)
                .and("The dao's saveEnableRagRequest must be called exactly once", daoSaveByFnIsCalledOnce)
                .and("The dao's to find current unknown story must be not called", findCurrentUnknownFnNotCalled)
                .and(
                        """
                - BotRAGConfiguration to persist must be not null
                - BotRAGConfiguration to persist must have a not null id
                - BotRAGConfiguration is enabled
            """.trimIndent(), checkRagConfigToPersist
                )
                .and("""
                    - Story rag to persist must be not null
                    - Story rag to persist must have a not null id
                    - Story rag to persist must have expected $NAMESPACE
                    - Story rag to persist must have expected $BOT_ID
                    - Story rag to persist must be rag type
                """.trimIndent(), checkStoryToPersist)
                .run()
    }

    @Test
    fun `Save rag configuration enabled when unknown story already exists`() {

        val currentUnknownStory = StoryDefinitionConfiguration(storyId = "unknown", botId = BOT_ID, intent = IntentWithoutNamespace(UNKNOWN_INTENT), AnswerConfigurationType.simple, emptyList(), namespace = NAMESPACE, _id = saveEnableRagRequestWithSavedUnknownBackupStory().unknownStoryBackupId!!)

        val entry: TSupplier<SaveFnEntry> = {
            saveEnableRagRequestWithSavedUnknownBackupStory()
        }

        val currentStoryUnknownExists: TRunnable = {
            every { BotAdminService.getBotConfigurationsByNamespaceAndBotId(eq(NAMESPACE), eq(BOT_ID)) } returns listOf(DEFAULT_BOT_CONFIG)
            every { storyDao.getConfiguredStoryDefinitionByNamespaceAndBotIdAndIntent(eq(NAMESPACE), eq(BOT_ID), eq(UNKNOWN_INTENT)) } returns currentUnknownStory
            every { storyDao.getAndDeleteRagStoryDefinitionByNamespaceAndBotId(eq(NAMESPACE), eq(BOT_ID)) } just Runs
            every { ragDao.save(any()) } returns saveEnableRagRequestWithSavedUnknownBackupStory().toBotRAGConfiguration()
        }

        val captureRagAndStoryToSave: TRunnable = {
            every { storyDao.save(capture(storySlot)) } returns Unit
            every { ragDao.save(capture(slot)) } returns saveEnableRagRequestWithSavedUnknownBackupStory().toBotRAGConfiguration()
        }

        val callServiceSave: TFunction<SaveFnEntry?, Unit> = {
            Assertions.assertNotNull(it)
            RagService.saveRag(it!!)
        }

        val storyDaoExistByFnIsCalledOnce: TRunnable = {
            verify(exactly = 1) { storyDao.getConfiguredStoryDefinitionByNamespaceAndBotIdAndIntent(eq(NAMESPACE), eq(BOT_ID), UNKNOWN_INTENT) }
        }

        val storyDaoSaveFnIsCalledTwiceSpecifically: TRunnable = {
            verify(atLeast = 1) { storyDao.save(eq(currentUnknownStory.copy(intent = IntentWithoutNamespace("unknownBackup")))) }
            verify(exactly = 2) { storyDao.save(any()) }
        }

        val daoGetAndDeletedRagByFnNotCalled: TRunnable = {
            verify(exactly = 0) { storyDao.getAndDeleteRagStoryDefinitionByNamespaceAndBotId(eq(NAMESPACE), eq(BOT_ID)) }
        }

        val daoSaveByFnIsCalledOnce: TRunnable = {
            verify(exactly = 1) { ragDao.save(eq(saveEnableRagRequestWithSavedUnknownBackupStory().toBotRAGConfiguration())) }
        }

        val findCurrentUnknownFnNotCalledBecauseRagIsActive: TRunnable = {
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
            Assertions.assertEquals(EMBEDDING_ENGINE, captured.embeddingEngine)
            Assertions.assertEquals(TEMPERATURE, captured.temperature)
            Assertions.assertEquals(PROMPT, captured.prompt)
            Assertions.assertEquals(saveEnableRagRequestWithSavedUnknownBackupStory().unknownStoryBackupId, captured.unknownStoryBackupId)
            Assertions.assertEquals(null, captured.noAnswerRedirection)
        }

        val checkStoryToPersist: TRunnable = {
            Assertions.assertTrue(storySlot.isCaptured)
            val captured = storySlot.captured
            Assertions.assertNotNull(captured)
            Assertions.assertNotNull(captured._id)
            Assertions.assertEquals(BOT_ID, captured.botId)
            Assertions.assertEquals(true, captured.features[0].enabled)
            Assertions.assertEquals(AnswerConfigurationType.rag, captured.currentType)
            Assertions.assertEquals(NAMESPACE, captured.namespace)
            Assertions.assertEquals(EMBEDDING_ENGINE, (captured.answers[0] as RagAnswerConfiguration).embeddingEngine)
            Assertions.assertEquals(TEMPERATURE, (captured.answers[0] as RagAnswerConfiguration).temperature)
            Assertions.assertEquals(PROMPT, (captured.answers[0] as RagAnswerConfiguration).prompt)
            Assertions.assertEquals(null, (captured.answers[0] as RagAnswerConfiguration).noAnswerRedirection)
        }

        TestCase<SaveFnEntry, Unit>("Save valid Rag Configuration that does not exist yet")
                .given("An application name and a valid request", entry)
                .and(
                        "Rag Config not exist but current story with unknown intent is present",
                        currentStoryUnknownExists
                )
                .and("The rag config and story definition to persist in database is captured", captureRagAndStoryToSave)
                .`when`("RagService's save method is called", callServiceSave)
                .then("The dao's getConfiguredStoryDefinitionByNamespaceAndBotIdAndIntent must be called exactly once", storyDaoExistByFnIsCalledOnce)
                .and("The dao's getAndDeleteRagStoryDefinitionByNamespaceAndBotId must not be not called", daoGetAndDeletedRagByFnNotCalled)
                .and("The dao's saveEnableRagRequest must be called exactly once", daoSaveByFnIsCalledOnce)
                .and("The dao's saveStory must be called exactly twice specifically about each save", storyDaoSaveFnIsCalledTwiceSpecifically)
                .and("The dao's to find current unknown story must be not called since rag config is active", findCurrentUnknownFnNotCalledBecauseRagIsActive)
                .and(
                        """
                - BotRAGConfiguration to persist must be not null
                - BotRAGConfiguration to persist must have a not null id
                - BotRAGConfiguration is enabled
            """.trimIndent(), checkRagConfigToPersist
                )
                .and("""
                    - Story rag to persist must be not null
                    - Story rag to persist must have a not null id
                    - Story rag to persist must have expected $NAMESPACE
                    - Story rag to persist must have expected $BOT_ID
                    - Story rag to persist must be rag type
                """.trimIndent(), checkStoryToPersist)
                .run()
    }

    @Test
    fun `Save rag configuration disabled when unknown story already exists in configuration and current rag story active`() {

        val currentUnknownStory = StoryDefinitionConfiguration(storyId = "unknown", botId = BOT_ID, intent = IntentWithoutNamespace("unknownBackup"), AnswerConfigurationType.simple, emptyList(), namespace = NAMESPACE, _id = saveEnableRagRequestWithSavedUnknownBackupStory().unknownStoryBackupId!!)
        val currentRagStory = StoryDefinitionConfiguration(storyId = RagStoryDefinition.RAG_STORY_NAME, botId = BOT_ID, intent = IntentWithoutNamespace(UNKNOWN_INTENT), AnswerConfigurationType.rag, emptyList(), namespace = NAMESPACE, _id = RAG_STORY_ID)

        val currentActiveRagConfig = DEFAULT_RAG_CONFIG.toBotRAGConfiguration().copy(unknownStoryBackupId = currentUnknownStory._id, enabled = true)

        val entry: TSupplier<SaveFnEntry> = {
            saveDisableRagRequestWithSavedUnknownBackupStory()
        }

        val previousStoryWithUnknownIntent: TRunnable = {
            every { BotAdminService.getBotConfigurationsByNamespaceAndBotId(eq(NAMESPACE), eq(BOT_ID)) } returns listOf(DEFAULT_BOT_CONFIG)
            every { storyDao.getConfiguredStoryDefinitionByNamespaceAndBotIdAndIntent(eq(NAMESPACE), eq(BOT_ID), eq(UNKNOWN_INTENT)) } returns currentRagStory


            every { ragDao.findByNamespaceAndBotId(eq(NAMESPACE), eq(BOT_ID)) } returns currentActiveRagConfig
            every { storyDao.getStoryDefinitionById(eq(currentUnknownStory._id)) } returns currentUnknownStory
            every { storyDao.save(eq(currentUnknownStory.copy(intent = IntentWithoutNamespace(UNKNOWN_INTENT)))) } just Runs

            every { storyDao.getAndDeleteRagStoryDefinitionByNamespaceAndBotId(eq(NAMESPACE), eq(BOT_ID)) } just Runs
            every { ragDao.save(any()) } returns saveDisableRagRequestWithSavedUnknownBackupStory().toBotRAGConfiguration()
        }

        val captureRagAndStoryToSave: TRunnable = {
            every { storyDao.save(capture(storySlot)) } returns Unit
            every { ragDao.save(capture(slot)) } returns saveDisableRagRequestWithSavedUnknownBackupStory().toBotRAGConfiguration()
        }

        val callServiceSave: TFunction<SaveFnEntry?, Unit> = {
            Assertions.assertNotNull(it)
            RagService.saveRag(it!!)
        }

        val ragDaoFindByFnIsCalledOnce: TRunnable = {
            verify(exactly = 1) { ragDao.findByNamespaceAndBotId(eq(NAMESPACE), eq(BOT_ID)) }
        }

        val storyDaoExistByFnIsCalledOnce: TRunnable = {
            verify(exactly = 1) { storyDao.getConfiguredStoryDefinitionByNamespaceAndBotIdAndIntent(eq(NAMESPACE), eq(BOT_ID), UNKNOWN_INTENT) }
        }

        val storyDaoGetAndSaveFnPreviousUnknownStoryBack: TRunnable = {
            verify(atLeast = 1) { storyDao.getStoryDefinitionById(eq(currentUnknownStory._id)) }
            verify(atLeast = 1) { storyDao.save(eq(currentUnknownStory.copy(intent = IntentWithoutNamespace(UNKNOWN_INTENT)))) }
        }

        val daoGetAndDeletedRagByFnCalledOnce: TRunnable = {
            verify(exactly = 1) { storyDao.getAndDeleteRagStoryDefinitionByNamespaceAndBotId(any(), any()) }
        }

        val daoSaveByFnIsCalledOnce: TRunnable = {
            verify(exactly = 1) { ragDao.save(eq(saveDisableRagRequestWithSavedUnknownBackupStory().toBotRAGConfiguration())) }
        }

        val checkRagConfigToPersist: TRunnable = {
            Assertions.assertTrue(slot.isCaptured)
            val captured = slot.captured
            Assertions.assertNotNull(captured)
            Assertions.assertNotNull(captured._id)
            Assertions.assertEquals(BOT_ID, captured.botId)
            Assertions.assertEquals(false, captured.enabled)
            Assertions.assertEquals(NAMESPACE, captured.namespace)
            Assertions.assertEquals(EMBEDDING_ENGINE, captured.embeddingEngine)
            Assertions.assertEquals(TEMPERATURE, captured.temperature)
            Assertions.assertEquals(PROMPT, captured.prompt)
            Assertions.assertEquals(saveEnableRagRequestWithSavedUnknownBackupStory().unknownStoryBackupId, captured.unknownStoryBackupId)
            Assertions.assertEquals(null, captured.noAnswerRedirection)
        }

        val checkStoryToPersist: TRunnable = {
            Assertions.assertTrue(storySlot.isCaptured)
            val captured = storySlot.captured
            Assertions.assertNotNull(captured)
            Assertions.assertNotNull(captured._id)
            Assertions.assertEquals(BOT_ID, captured.botId)
            Assertions.assertEquals(NAMESPACE, captured.namespace)
            Assertions.assertEquals(AnswerConfigurationType.simple, captured.currentType)
            Assertions.assertEquals(currentUnknownStory.storyId, captured.storyId)

        }

        val checkStoryBackupChangeIntentToUnknown: TRunnable = {
            val captured = storySlot.captured
            Assertions.assertNotEquals(currentUnknownStory.intent, captured.intent)
            Assertions.assertEquals(UNKNOWN_INTENT,captured.intent.name)
        }

        TestCase<SaveFnEntry, Unit>("Save valid Rag Configuration disabled and put back old previous saved unknown Story")
                .given("A valid request", entry)
                .and(
                        "Rag Config exists and previous story with unknown intent is present",
                        previousStoryWithUnknownIntent
                )
                .and("The rag config and story definition to persist in database is captured", captureRagAndStoryToSave)
                .`when`("RagService's save method is called", callServiceSave)
                .then("The dao's getConfiguredStoryDefinitionByNamespaceAndBotIdAndIntent must be called exactly once", storyDaoExistByFnIsCalledOnce)
                .and("The dao's getAndDeleteRagStoryDefinitionByNamespaceAndBotId must be called to delete the current RagStory", daoGetAndDeletedRagByFnCalledOnce)
                .and("The dao's saveEnableRagRequest must be called exactly once", daoSaveByFnIsCalledOnce)
                .and("The dao's rag Config must be called once to get the current config", ragDaoFindByFnIsCalledOnce)
                .and("The dao's saveStory must be called once", storyDaoGetAndSaveFnPreviousUnknownStoryBack)
                .and(
                        """
                - BotRAGConfiguration to persist must be not null
                - BotRAGConfiguration to persist must have a not null id
                - BotRAGConfiguration is disabled
            """.trimIndent(), checkRagConfigToPersist
                )
                .and("""
                    - Story backup to persist must be not null
                    - Story backup to persist must have a not null id
                    - Story backup to persist must have expected $NAMESPACE
                    - Story backup to persist must have expected $BOT_ID
                    - Story backup to persist must be simple type
                """.trimIndent(), checkStoryToPersist)
                .and("""
                    - Story backup to persist must be back to Intent Unknown
                """.trimIndent(), checkStoryBackupChangeIntentToUnknown)
                .run()
    }

    @Test
    fun `Save rag configuration and no bot configuration is defined throws a Bad Request Exception`() {

        val entry: TSupplier<SaveFnEntry> = {
            saveEnableRagRequest()
        }

        val previousStoryWithUnknownIntent: TRunnable = {
            every { BotAdminService.getBotConfigurationsByNamespaceAndBotId(eq(NAMESPACE), eq(BOT_ID)) } returns emptyList()
        }

        val callServiceSave: TFunction<SaveFnEntry?, BadRequestException> = {
            Assertions.assertNotNull(it)
            assertThrows {
                RagService.saveRag(it!!)
            }

        }

        val serviceCalledOnlyOnce: TRunnable = {
            verify(exactly = 1) { BotAdminService.getBotConfigurationsByNamespaceAndBotId(eq(NAMESPACE),eq(BOT_ID)) }
        }

        val checkError: TConsumer<BadRequestException?> = {
            Assertions.assertNotNull(it)
            Assertions.assertTrue(it?.httpResponseBody?.errors?.firstOrNull()?.message?.contains("No bot configuration is defined yet")!!)
        }

        TestCase<SaveFnEntry, BadRequestException>("Save valid Rag Configuration enabled and bot configuration is not defined yet")
                .given("A valid request", entry)
                .and(
                        "Rag Config exists and previous story with unknown intent is present",
                        previousStoryWithUnknownIntent
                )
                .`when`("RagService's save method is called", callServiceSave)
                .then("The BotAdminServce getConfiguredStoryDefinitionByNamespaceAndBotIdAndIntent must be called exactly once", serviceCalledOnlyOnce)
                .and(
                        """
                - Error is not null
                - Error is type BadRequestException
            """.trimIndent(), checkError
                )
                .run()
    }
}


typealias SaveFnEntry = BotRAGConfigurationDTO