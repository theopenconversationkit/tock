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

package ai.tock.llm.orchestrator.core.utils

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class OpenSearchUtilsTest {
    @Test
    fun `when namespace and botId are in uppercase`() {

        // GIVEN
        // "namespace" in uppercase
        // "botId" in uppercase

        // WHEN :
        // Launch of the normalisation
        val indexName = OpenSearchUtils.normalizeDocumentIndexName(
            namespace = "NAMESPACE", botId = "BOTID"
        )

        // THEN :
        // Convert to lowercase
        assertEquals("ns-namespace-bot-botid", indexName)
    }

    @Test
    fun `when namespace and botId has a underscore`() {

        // GIVEN
        // "namespace" has a underscore
        // "botId" has a underscore

        // WHEN :
        // Launch of the normalisation
        val indexName = OpenSearchUtils.normalizeDocumentIndexName(
            namespace = "my_private_namespace", botId = "bot_id"
        )

        // THEN :
        // Replace underscores with hyphens
        assertEquals("ns-my-private-namespace-bot-bot-id", indexName)
    }

    @Test
    fun `when namespace and botId has an invalid character`() {

        // GIVEN
        // "namespace" has a "*"
        // "botId" has a "?"

        // WHEN :
        // Launch of the normalisation
        val indexName = OpenSearchUtils.normalizeDocumentIndexName(
            namespace = "my_private*", botId = "bot?id"
        )

        // THEN :
        // Remove invalid characters
        assertEquals("ns-my-private-bot-botid", indexName)
    }
}