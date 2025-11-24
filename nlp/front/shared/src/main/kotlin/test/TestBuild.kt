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

package ai.tock.nlp.front.shared.test

import ai.tock.nlp.front.shared.config.ApplicationDefinition
import org.litote.kmongo.Id
import java.time.Duration
import java.time.Instant
import java.util.Locale

/**
 * A build used to test the model.
 */
data class TestBuild(
    /**
     * The application id to test.
     */
    val applicationId: Id<ApplicationDefinition>,
    /**
     * The tested language.
     */
    val language: Locale,
    /**
     * Start of the build.
     */
    val startDate: Instant,
    /**
     * Duration of the build.
     */
    val buildModelDuration: Duration,
    /**
     * Duration of sentences tests.
     */
    val testSentencesDuration: Duration,
    /**
     * Number of sentences included in the model.
     */
    val nbSentencesInModel: Int,
    /**
     * Number of sentences tested.
     */
    val nbSentencesTested: Int,
    /**
     * Number of errors.
     */
    val nbErrors: Int,
    /**
     * Number of intent errors.
     */
    val intentErrors: Int = -1,
    /**
     * Number of entity errors
     */
    val entityErrors: Int = -1,
    /**
     * Number of sentences tested by intent.
     */
    val nbSentencesTestedByIntent: Map<String, Int> = emptyMap(),
    /**
     * Number of intent errors by intent.
     */
    val intentErrorsByIntent: Map<String, Int> = emptyMap(),
    /**
     * Number of entity errors by intent.
     */
    val entityErrorsByIntent: Map<String, Int> = emptyMap(),
)
