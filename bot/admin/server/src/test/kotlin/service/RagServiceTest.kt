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
import ai.tock.bot.admin.bot.BotRAGConfiguration
import ai.tock.bot.admin.bot.BotRAGConfigurationDAO
import ai.tock.bot.admin.model.BotRAGConfigurationDTO
import ai.tock.bot.admin.story.StoryDefinitionConfiguration
import ai.tock.bot.admin.story.StoryDefinitionConfigurationDAO
import ai.tock.bot.definition.IntentWithoutNamespace
import ai.tock.bot.test.TConsumer
import ai.tock.bot.test.TFunction
import ai.tock.bot.test.TRunnable
import ai.tock.bot.test.TSupplier
import ai.tock.bot.test.TestCase
import ai.tock.nlp.core.Intent
import ai.tock.shared.exception.rest.BadRequestException
import ai.tock.shared.tockInternalInjector
import ai.tock.shared.withoutNamespace
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.KodeinInjector
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.singleton
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.litote.kmongo.toId

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

        private val DEFAULT_RAG_CONFIG = BotRAGConfigurationDTO(
            id = "ragId",
            namespace = NAMESPACE,
            botId = BOT_ID,
            enabled = false,
            ENGINE,
            EMBEDDING_ENGINE,
            TEMPERATURE,
            PROMPT,
            PARAMETERS,
            "",
            null
        )

        private val DEFAULT_BOT_CONFIG = aApplication.copy(namespace = NAMESPACE, botId = BOT_ID)

        private fun getRAGConfigurationDTO(enabled: Boolean) = DEFAULT_RAG_CONFIG.copy(enabled = enabled)

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
            getRAGConfigurationDTO(true)
        }

        val ragNotYetExists: TRunnable = {
            every { BotAdminService.getBotConfigurationsByNamespaceAndBotId(eq(NAMESPACE), eq(BOT_ID)) } returns listOf(
                DEFAULT_BOT_CONFIG
            )
            every { ragDao.save(any()) } returns getRAGConfigurationDTO(true).toBotRAGConfiguration()
        }

        val captureRagAndStoryToSave: TRunnable = {
            every { storyDao.save(capture(storySlot)) } returns Unit
            every { ragDao.save(capture(slot)) } returns getRAGConfigurationDTO(true).toBotRAGConfiguration()
        }

        val callServiceSave: TFunction<SaveFnEntry?, Unit> = {
            Assertions.assertNotNull(it)
            RagService.saveRag(it!!)
        }

        val daoSaveByFnIsCalledOnce: TRunnable = {
            verify(exactly = 1) { storyDao.save(any()) }
            verify(exactly = 1) { ragDao.save(eq(getRAGConfigurationDTO(true).toBotRAGConfiguration())) }
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
            Assertions.assertEquals(null, captured.noAnswerStoryId)
        }


        TestCase<SaveFnEntry, Unit>("Save valid Rag Configuration that does not exist yet").given(
                "An application name and a valid request",
                entry
            ).and(
                "Rag Config not exist with request name or label and the given application name", ragNotYetExists
            ).and("The rag config in database is captured", captureRagAndStoryToSave)
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
        // RAG Configuration
        val ragConfigurationDTO = getRAGConfigurationDTO(true)
        val query: TSupplier<SaveFnEntry> = { ragConfigurationDTO }

        // WHEN
        // save RAG Configuration
        val callService: TFunction<SaveFnEntry?, Unit> = {
            Assertions.assertNotNull(it)
            RagService.saveRag(it!!)
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

        TestCase<SaveFnEntry, Unit>("Save valid Rag Configuration")
            .given(
                "No story exists for unknown intent",
                noStoryExistsForUnknownIntent
            )
            .and("Rag Configuration is valid", query)
            .and("Bot configuration exists", mocks)
            .`when`("RagService's save method is called", callService)
            .then(
                """
                - no story is saved
                - rag configuration is saved
            """.trimIndent(), checks)
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
        // RAG Configuration (enabled)
        val ragConfigurationDTO = getRAGConfigurationDTO(true)
        val query: TSupplier<SaveFnEntry> = { ragConfigurationDTO }

        // WHEN
        // save RAG Configuration
        val callService: TFunction<SaveFnEntry?, Unit> = {
            Assertions.assertNotNull(it)
            RagService.saveRag(it!!)
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

        TestCase<SaveFnEntry, Unit>("Save enabled Rag Configuration")
            .given(
                "Unknown story exists",
                noStoryExistsForUnknownIntent
            ).and("Rag Configuration is valid", query)
            .and("Bot configuration exists", mocks)
            .`when`("RagService's save method is called", callService)
            .then(
                """
                - unknown story is saved
                - unknown story is disabled
                - rag configuration is saved
            """.trimIndent(), checks)
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
        // RAG Configuration (disabled)
        val ragConfigurationDTO = getRAGConfigurationDTO(false)
        val query: TSupplier<SaveFnEntry> = { ragConfigurationDTO }

        // WHEN
        // save RAG Configuration
        val callService: TFunction<SaveFnEntry?, Unit> = {
            Assertions.assertNotNull(it)
            RagService.saveRag(it!!)
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

        TestCase<SaveFnEntry, Unit>("Save disabled Rag Configuration")
            .given(
                "Unknown story exists",
                unknownStoryExists
            ).and("Rag Configuration is valid", query)
            .and("Bot configuration exists", mocks)
            .`when`("RagService's save method is called", callService)
            .then(
                """
                - unknown story is saved
                - unknown story is enabled
                - rag configuration is saved
            """.trimIndent(), checks)
            .run()
    }

}


typealias SaveFnEntry = BotRAGConfigurationDTO