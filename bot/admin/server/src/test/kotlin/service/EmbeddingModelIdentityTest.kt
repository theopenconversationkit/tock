/*
 * Copyright (C) 2017/2026 SNCF Connect & Tech
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

import ai.tock.genai.orchestratorcore.models.em.AzureOpenAIEMSetting
import ai.tock.genai.orchestratorcore.models.em.OllamaEMSetting
import ai.tock.genai.orchestratorcore.models.em.OpenAIEMSetting
import ai.tock.shared.security.key.RawSecretKey
import ai.tock.shared.security.key.SecretKey
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class EmbeddingModelIdentityTest {
    private fun openAI(model: String) = OpenAIEMSetting<SecretKey>(RawSecretKey("k"), model, "https://example.org")

    private fun ollama(model: String) = OllamaEMSetting<SecretKey>(model, "https://example.org")

    private fun azure(model: String?) = AzureOpenAIEMSetting<SecretKey>(RawSecretKey("k"), "https://example.org", "deployment-x", "2024-01-01", model)

    @Test fun `openai and ollama normalize to their model, trimmed`() {
        assertEquals("text-embedding-3-small", EmbeddingModelIdentity.normalized(openAI("  text-embedding-3-small  ")))
        assertEquals("nomic", EmbeddingModelIdentity.normalized(ollama("nomic")))
    }

    @Test fun `ollama strips a trailing latest tag`() {
        assertEquals("nomic", EmbeddingModelIdentity.normalized(ollama("nomic:latest")))
        // A non-latest tag is kept.
        assertEquals("nomic:v1.5", EmbeddingModelIdentity.normalized(ollama("nomic:v1.5")))
    }

    @Test fun `azure uses the model and never the deployment name`() {
        assertEquals("ada-002", EmbeddingModelIdentity.normalized(azure("ada-002")))
        assertNull(EmbeddingModelIdentity.normalized(azure(null)))
        assertNull(EmbeddingModelIdentity.normalized(azure("   ")))
    }

    @Test fun `coherence compares normalized models only, ignoring provider`() {
        // Same normalized model across providers is a MATCH; provider is not part of the comparison.
        assertEquals(EmbeddingCoherence.MATCH, EmbeddingModelIdentity.coherence("m", "m"))
        assertEquals(EmbeddingCoherence.MISMATCH, EmbeddingModelIdentity.coherence("m", "other"))
    }

    @Test fun `coherence is unknown and never blocking when either side is missing`() {
        assertEquals(EmbeddingCoherence.UNKNOWN, EmbeddingModelIdentity.coherence(null, "m"))
        assertEquals(EmbeddingCoherence.UNKNOWN, EmbeddingModelIdentity.coherence("m", null))
        assertEquals(EmbeddingCoherence.UNKNOWN, EmbeddingModelIdentity.coherence("", "m"))
    }
}
