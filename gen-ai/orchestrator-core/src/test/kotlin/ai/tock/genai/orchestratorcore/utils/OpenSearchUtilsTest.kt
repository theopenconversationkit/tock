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

package ai.tock.genai.orchestratorcore.utils

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class OpenSearchUtilsTest {
    @Test
    fun `when namespace and botId are in uppercase`() {
        // GIVEN
        // "namespace" in uppercase
        // "botId" in uppercase
        // "indexSessionId" in uppercase

        // WHEN :
        // Launch of the normalisation
        val indexName =
            OpenSearchUtils.normalizeDocumentIndexName(
                namespace = "NAMESPACE",
                botId = "BOTID",
                indexSessionId = "123-ABC-5F",
            )

        // THEN :
        // Convert to lowercase
        assertEquals("ns-namespace-bot-botid-session-123-abc-5f", indexName)
    }

    @Test
    fun `when namespace and botId has a underscore`() {
        // GIVEN
        // "namespace" has an underscore
        // "botId" has an underscore
        // "indexSessionId" in uppercase

        // WHEN :
        // Launch of the normalisation
        val indexName =
            OpenSearchUtils.normalizeDocumentIndexName(
                namespace = "my_private_namespace",
                botId = "bot_id",
                indexSessionId = "123-ABC-5F",
            )

        // THEN :
        // Replace underscores with hyphens
        assertEquals("ns-my-private-namespace-bot-bot-id-session-123-abc-5f", indexName)
    }

    @Test
    fun `when namespace and botId has an invalid character`() {
        // GIVEN
        // "namespace" has a "*"
        // "botId" has a "?"
        // "indexSessionId" in uppercase

        // WHEN :
        // Launch of the normalisation
        val indexName =
            OpenSearchUtils.normalizeDocumentIndexName(
                namespace = "my_private*",
                botId = "bot?id",
                indexSessionId = "123-ABC-5F",
            )

        // THEN :
        // Remove invalid characters
        assertEquals("ns-my-private-bot-botid-session-123-abc-5f", indexName)
    }
}
