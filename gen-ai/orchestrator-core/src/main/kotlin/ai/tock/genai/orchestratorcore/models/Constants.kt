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

package ai.tock.genai.orchestratorcore.models

object Constants {
    const val OPEN_AI = "OpenAI"
    const val AZURE_OPEN_AI_SERVICE = "AzureOpenAIService"
    const val OLLAMA = "Ollama"

    const val LANGFUSE = "Langfuse"

    const val OPEN_SEARCH = "OpenSearch"
    const val PG_VECTOR = "PGVector"

    const val BLOOMZ_COMPRESSOR = "BloomzRerank"

    private const val GEN_AI="GenAI"
    private const val GEN_AI_RAG="$GEN_AI/RAG"
    private const val GEN_AI_COMPLETION="$GEN_AI/COMPLETION"

    const val GEN_AI_RAG_QUESTION_CONDENSING="$GEN_AI_RAG/questionCondensing"
    const val GEN_AI_RAG_QUESTION_ANSWERING="$GEN_AI_RAG/questionAnswering"
    const val GEN_AI_RAG_EMBEDDING_QUESTION="$GEN_AI_RAG/embeddingQuestion"

    const val GEN_AI_COMPLETION_SENTENCE_GENERATION="$GEN_AI_COMPLETION/sentenceGeneration"

    const val GEN_AI_VECTOR_STORE="$GEN_AI/VECTOR_STORE"
    const val GEN_AI_OBSERVABILITY="$GEN_AI/OBSERVABILITY"
}
