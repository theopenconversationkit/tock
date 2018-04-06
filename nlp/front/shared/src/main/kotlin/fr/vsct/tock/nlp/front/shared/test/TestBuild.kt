/*
 * Copyright (C) 2017 VSCT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.vsct.tock.nlp.front.shared.test

import fr.vsct.tock.nlp.front.shared.config.ApplicationDefinition
import org.litote.kmongo.Data
import org.litote.kmongo.Id
import java.time.Duration
import java.time.Instant
import java.util.Locale

/**
 * A build used to test the model.
 */
@Data
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
    val nbErrors: Int
)