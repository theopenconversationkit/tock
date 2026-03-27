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

package ai.tock.genai.orchestratorcore.models.llm

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Tests for reasoning_effort attribute across all LLMSetting implementations
 */
class LLMSettingReasoningEffortTest {
    // ---------------------------------------------------------------
    // OpenAILLMSetting
    // ---------------------------------------------------------------

    @Test
    fun `OpenAI setting should have null reasoningEffort by default`() {
        val setting =
            OpenAILLMSetting(
                apiKey = "test-key",
                temperature = "0.7",
                model = "gpt-4o",
                baseUrl = "https://api.openai.com/v1",
            )
        assertNull(setting.reasoningEffort)
        assertEquals(LLMProvider.OpenAI, setting.provider)
    }

    @Test
    fun `OpenAI setting should accept reasoningEffort low`() {
        val setting =
            OpenAILLMSetting(
                apiKey = "test-key",
                temperature = "0.7",
                reasoningEffort = "low",
                model = "gpt-5",
                baseUrl = "https://api.openai.com/v1",
            )
        assertEquals("low", setting.reasoningEffort)
    }

    @Test
    fun `OpenAI setting should accept reasoningEffort medium`() {
        val setting =
            OpenAILLMSetting(
                apiKey = "test-key",
                temperature = "0.7",
                reasoningEffort = "medium",
                model = "gpt-5",
                baseUrl = "https://api.openai.com/v1",
            )
        assertEquals("medium", setting.reasoningEffort)
    }

    @Test
    fun `OpenAI setting should accept reasoningEffort high`() {
        val setting =
            OpenAILLMSetting(
                apiKey = "test-key",
                temperature = "0.7",
                reasoningEffort = "high",
                model = "gpt-5",
                baseUrl = "https://api.openai.com/v1",
            )
        assertEquals("high", setting.reasoningEffort)
    }

    @Test
    fun `OpenAI copyWithTemperature should preserve reasoningEffort`() {
        val setting =
            OpenAILLMSetting(
                apiKey = "test-key",
                temperature = "0.7",
                reasoningEffort = "high",
                model = "gpt-5",
                baseUrl = "https://api.openai.com/v1",
            )
        val copied =
            setting.copyWithTemperature("1.0") as OpenAILLMSetting
        assertEquals("1.0", copied.temperature)
        assertEquals("high", copied.reasoningEffort)
        assertEquals("gpt-5", copied.model)
    }

    // ---------------------------------------------------------------
    // AzureOpenAILLMSetting
    // ---------------------------------------------------------------

    @Test
    fun `Azure setting should have null reasoningEffort by default`() {
        val setting =
            AzureOpenAILLMSetting(
                apiKey = "test-key",
                temperature = "0.5",
                apiBase = "https://my-endpoint.openai.azure.com",
                deploymentName = "my-deploy",
                apiVersion = "2025-04-01-preview",
                model = "gpt-5",
            )
        assertNull(setting.reasoningEffort)
        assertEquals(LLMProvider.AzureOpenAIService, setting.provider)
    }

    @Test
    fun `Azure setting should accept reasoningEffort`() {
        val setting =
            AzureOpenAILLMSetting(
                apiKey = "test-key",
                temperature = "0.5",
                reasoningEffort = "medium",
                apiBase = "https://my-endpoint.openai.azure.com",
                deploymentName = "my-deploy",
                apiVersion = "2025-04-01-preview",
                model = "gpt-5",
            )
        assertEquals("medium", setting.reasoningEffort)
    }

    @Test
    fun `Azure copyWithTemperature should preserve reasoningEffort`() {
        val setting =
            AzureOpenAILLMSetting(
                apiKey = "test-key",
                temperature = "0.5",
                reasoningEffort = "low",
                apiBase = "https://my-endpoint.openai.azure.com",
                deploymentName = "my-deploy",
                apiVersion = "2025-04-01-preview",
                model = "gpt-5",
            )
        val copied =
            setting.copyWithTemperature("1.0") as AzureOpenAILLMSetting
        assertEquals("1.0", copied.temperature)
        assertEquals("low", copied.reasoningEffort)
        assertEquals("my-deploy", copied.deploymentName)
    }

    // ---------------------------------------------------------------
    // OllamaLLMSetting — no reasoningEffort (not supported by Ollama)
    // ---------------------------------------------------------------

    @Test
    fun `Ollama setting should not have reasoningEffort`() {
        val setting =
            OllamaLLMSetting<String>(
                temperature = "0.8",
                model = "llama3",
                baseUrl = "http://localhost:11434",
            )
        assertNull(setting.reasoningEffort)
        assertEquals(LLMProvider.Ollama, setting.provider)
    }
}
