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
package ai.tock.nlp.systemone

import ai.tock.nlp.core.Intent
import ai.tock.nlp.core.IntentClassification
import ai.tock.nlp.model.IntentContext
import ai.tock.nlp.model.service.engine.IntentClassifier
import ai.tock.nlp.systemone.SystemOneClient.ChoiceQuestion
import ai.tock.nlp.systemone.SystemOneClient.RunRequest

/**
 * Classifies intents by sending a single Clef "choice" question whose criteria are the
 * application's current intents (qualified name -> [Intent.description]). When no description
 * has been set in TOCK Studio, `null` is sent as the criteria value - Clef's documented
 * convention for "no detail needed" for a choice option.
 *
 * Clef is intent-only: see [SystemOneEntityClassifier] for the entity side.
 */
internal class SystemOneIntentClassifier(
    private val conf: SystemOneConfiguration,
    private val intentQuestionInstructions: String = INTENT_QUESTION_INSTRUCTIONS,
) : IntentClassifier {
    override fun classifyIntent(
        context: IntentContext,
        text: String,
        tokens: Array<String>,
    ): IntentClassification {
        val intents = context.application.intents
        if (intents.isEmpty()) {
            return emptyClassification()
        }
        // Clef's "choice" question requires at least 2 criteria - with a single configured
        // intent there is nothing to disambiguate, so skip the API call entirely.
        if (intents.size == 1) {
            return singleIntentClassification(intents.first())
        }
        check(intents.size <= MAX_CRITERIA) {
            "Clef only supports up to $MAX_CRITERIA choice criteria per question, " +
                "but this application has ${intents.size} intents"
        }

        val criteria = intents.associate { intent -> intent.name to intent.description?.takeIf { it.isNotBlank() } }

        val result =
            SystemOneClientProvider
                .getClient(conf)
                .run(
                    RunRequest(
                        model = conf.model,
                        state = text,
                        questions =
                            mapOf(
                                INTENT_QUESTION_ID to
                                    ChoiceQuestion(
                                        instructions = intentQuestionInstructions,
                                        criteria = criteria,
                                    ),
                            ),
                    ),
                )

        val probabilities =
            result.answers[INTENT_QUESTION_ID]?.probabilities
                ?: error("Clef response did not contain an answer for question \"$INTENT_QUESTION_ID\"")

        val rankedProbabilities = probabilities.entries.sortedByDescending { it.value }

        return object : IntentClassification {
            var probability = 0.0
            val iterator = rankedProbabilities.iterator()

            override fun probability(): Double = probability

            override fun hasNext(): Boolean = iterator.hasNext()

            override fun next(): Intent =
                iterator.next().let { (name, proba) ->
                    probability = proba
                    context.application.getIntent(name) ?: Intent.UNKNOWN_INTENT
                }
        }
    }

    private fun singleIntentClassification(intent: Intent): IntentClassification =
        object : IntentClassification {
            var consumed = false

            override fun probability(): Double = 1.0

            override fun hasNext(): Boolean = !consumed

            override fun next(): Intent {
                consumed = true
                return intent
            }
        }

    private fun emptyClassification(): IntentClassification =
        object : IntentClassification {
            override fun probability(): Double = 0.0

            override fun hasNext(): Boolean = false

            override fun next(): Intent = Intent.UNKNOWN_INTENT
        }

    companion object {
        internal const val INTENT_QUESTION_ID = "intent"
        internal const val INTENT_QUESTION_INSTRUCTIONS_PROPERTY = "intent_question_instructions"
        internal const val INTENT_QUESTION_INSTRUCTIONS = "Select the intent that best matches the user message."

        /** Clef's `choice` question type accepts between 2 and 255 criteria. */
        internal const val MAX_CRITERIA = 255
    }
}
