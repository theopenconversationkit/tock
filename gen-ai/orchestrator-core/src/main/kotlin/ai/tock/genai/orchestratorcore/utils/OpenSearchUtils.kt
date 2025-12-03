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

object OpenSearchUtils {
    /**
     * Normalize the document index name
     * Here, OpenSearch rules are used
     * See: https://docs.aws.amazon.com/opensearch-service/latest/developerguide/indexing.html#indexing-naming
     * @param namespace the namespace
     * @param botId the bot ID
     * @param indexSessionId the index session ID
     */
    fun normalizeDocumentIndexName(
        namespace: String,
        botId: String,
        indexSessionId: String,
    ): String {
        // Convert to lowercase
        var normalized = "ns-$namespace-bot-$botId-session-$indexSessionId".lowercase()
        // Replace underscores and space with hyphens
        normalized = normalized.replace('_', '-').replace(' ', '-')
        // Remove invalid characters
        val invalidCharacters = setOf(' ', ',', ':', '"', '*', '+', '/', '\\', '|', '?', '#', '>', '<')
        normalized = normalized.filter { it !in invalidCharacters }

        return normalized
    }
}
