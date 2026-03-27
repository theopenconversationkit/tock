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

package ai.tock.genai.orchestratorcore.mappers

import ai.tock.genai.orchestratorcore.models.llm.AzureOpenAILLMSetting
import ai.tock.genai.orchestratorcore.models.llm.LLMProvider
import ai.tock.genai.orchestratorcore.models.llm.LLMSetting
import ai.tock.genai.orchestratorcore.models.llm.OpenAILLMSetting
import ai.tock.shared.security.key.RawSecretKey
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class LLMSettingMapperReasoningEffortTest {
    // Helper to create typed entities for toDTO tests
    // Data class generics in Kotlin are invariant, so we need unchecked casts
    @Suppress("UNCHECKED_CAST")
    private fun openAiEntity(
        temperature: String,
        model: String,
        reasoningEffort: String? = null,
    ): LLMSetting =
        OpenAILLMSetting(
            apiKey = RawSecretKey("sk-test"),
            temperature = temperature,
            reasoningEffort = reasoningEffort,
            model = model,
            baseUrl = "https://api.openai.com/v1",
        ) as LLMSetting

    @Suppress("UNCHECKED_CAST")
    private fun azureEntity(
        temperature: String,
        reasoningEffort: String? = null,
        model: String? = "gpt-5",
    ): LLMSetting =
        AzureOpenAILLMSetting(
            apiKey = RawSecretKey("azure-key"),
            temperature = temperature,
            reasoningEffort = reasoningEffort,
            apiBase = "https://endpoint.openai.azure.com",
            deploymentName = "deploy",
            apiVersion = "2025-04-01-preview",
            model = model,
        ) as LLMSetting

    // ---------------------------------------------------------------
    // toDTO() tests
    // ---------------------------------------------------------------

    @Test
    fun `toDTO should preserve reasoningEffort for OpenAI`() {
        val dto = LLMSettingMapper.toDTO(openAiEntity("0.7", "gpt-5", "high"))
        assertEquals(LLMProvider.OpenAI, dto.provider)
        assertEquals("sk-test", dto.apiKey)
        assertEquals("high", dto.reasoningEffort)
    }

    @Test
    fun `toDTO should preserve null reasoningEffort for OpenAI`() {
        val dto = LLMSettingMapper.toDTO(openAiEntity("0.7", "gpt-4o"))
        assertNull(dto.reasoningEffort)
    }

    @Test
    fun `toDTO should preserve reasoningEffort for AzureOpenAI`() {
        val dto = LLMSettingMapper.toDTO(azureEntity("0.5", "medium"))
        assertEquals("medium", dto.reasoningEffort)
        assertEquals("deploy", (dto as AzureOpenAILLMSetting).deploymentName)
    }

    @Test
    fun `toDTO should preserve null reasoningEffort for AzureOpenAI`() {
        val dto = LLMSettingMapper.toDTO(azureEntity("0.5"))
        assertNull(dto.reasoningEffort)
    }

    // ---------------------------------------------------------------
    // toEntity() tests
    // ---------------------------------------------------------------

    @Test
    fun `toEntity should preserve reasoningEffort for OpenAI`() {
        val dto =
            OpenAILLMSetting(
                apiKey = "sk-test",
                temperature = "0.7",
                reasoningEffort = "high",
                model = "gpt-5",
                baseUrl = "https://api.openai.com/v1",
            )
        val entity = LLMSettingMapper.toEntity(dto = dto, rawByForce = true)
        assertEquals("high", entity.reasoningEffort)
        assertEquals("sk-test", (entity.apiKey as RawSecretKey).secret)
    }

    @Test
    fun `toEntity should preserve null reasoningEffort for OpenAI`() {
        val dto =
            OpenAILLMSetting(
                apiKey = "sk-test",
                temperature = "0.7",
                model = "gpt-4o",
                baseUrl = "https://api.openai.com/v1",
            )
        val entity = LLMSettingMapper.toEntity(dto = dto, rawByForce = true)
        assertNull(entity.reasoningEffort)
    }

    @Test
    fun `toEntity should preserve reasoningEffort for AzureOpenAI`() {
        val dto =
            AzureOpenAILLMSetting(
                apiKey = "azure-key",
                temperature = "0.5",
                reasoningEffort = "low",
                apiBase = "https://endpoint.openai.azure.com",
                deploymentName = "deploy",
                apiVersion = "2025-04-01-preview",
                model = "gpt-5",
            )
        val entity = LLMSettingMapper.toEntity(dto = dto, rawByForce = true)
        assertEquals("low", entity.reasoningEffort)
    }

    // ---------------------------------------------------------------
    // Round-trip tests
    // ---------------------------------------------------------------

    @Test
    fun `round-trip should preserve reasoningEffort for OpenAI`() {
        val original =
            OpenAILLMSetting(
                apiKey = "sk-rt",
                temperature = "0.9",
                reasoningEffort = "high",
                model = "gpt-5",
                baseUrl = "https://api.openai.com/v1",
            )
        val entity = LLMSettingMapper.toEntity(dto = original, rawByForce = true)
        val result = LLMSettingMapper.toDTO(entity)
        assertEquals(original.reasoningEffort, result.reasoningEffort)
        assertEquals(original.apiKey, result.apiKey)
    }

    @Test
    fun `round-trip should preserve reasoningEffort for AzureOpenAI`() {
        val original =
            AzureOpenAILLMSetting(
                apiKey = "azure-rt",
                temperature = "0.5",
                reasoningEffort = "low",
                apiBase = "https://endpoint.openai.azure.com",
                deploymentName = "deploy",
                apiVersion = "2025-04-01-preview",
                model = "gpt-5",
            )
        val entity = LLMSettingMapper.toEntity(dto = original, rawByForce = true)
        val result = LLMSettingMapper.toDTO(entity)
        assertEquals(original.reasoningEffort, result.reasoningEffort)
    }

    @Test
    fun `round-trip should preserve null reasoningEffort`() {
        val original =
            OpenAILLMSetting(
                apiKey = "sk-no-re",
                temperature = "0.7",
                model = "gpt-4o",
                baseUrl = "https://api.openai.com/v1",
            )
        val entity = LLMSettingMapper.toEntity(dto = original, rawByForce = true)
        val result = LLMSettingMapper.toDTO(entity)
        assertNull(result.reasoningEffort)
    }
}
