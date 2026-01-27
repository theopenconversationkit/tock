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

package ai.tock.genai.orchestratorclient.responses

/**
 * Represents the confidence level of an intent match.
 *
 * @property HIGH Strong confidence that the detected intent is correct.
 * @property MEDIUM Moderate confidence; the intent is likely correct but not certain.
 * @property LOW Weak confidence; the match may be incorrect.
 * @property AMBIGUOUS Multiple intents have similar scores, making the result unclear.
 */
enum class SimilarityLevel {
    HIGH,
    STRONG,
    MEDIUM,
    LOW,
    AMBIGUOUS,
}

/**
 * Suggestion for a possible intent of a sentence.
 *
 * @property intent Suggested intent name.
 * @property similarity Confidence level of the intent match.
 */
data class IntentSuggestion(
    val intent: String,
    val similarity: SimilarityLevel,
    val score: Double,
)

/**
 * Response of sentence parsing.
 *
 * @property language Detected language of the input sentence.
 * @property intent Best-matched intent.
 * @property suggestions List of alternative intent suggestions ranked by similarity.
 */
data class SentenceParsingResponse(
    val language: String,
    val intent: String,
    val similarity: SimilarityLevel,
    val score: Double,
    val suggestions: List<IntentSuggestion>,
)
