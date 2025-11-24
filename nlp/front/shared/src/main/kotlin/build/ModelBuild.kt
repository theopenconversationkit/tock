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

package ai.tock.nlp.front.shared.build

import ai.tock.nlp.front.shared.config.ApplicationDefinition
import ai.tock.nlp.front.shared.config.IntentDefinition
import org.litote.kmongo.Id
import java.time.Duration
import java.time.Instant
import java.util.Locale

/**
 * A NLP model build.
 */
data class ModelBuild(
    /**
     * The application id of the model.
     */
    val applicationId: Id<ApplicationDefinition>,
    /**
     * The language of the model.
     */
    val language: Locale,
    /**
     * Type of build.
     */
    val type: ModelBuildType,
    /**
     * The optional intent id.
     */
    val intentId: Id<IntentDefinition>?,
    /**
     * The optional entity type name.
     */
    val entityTypeName: String?,
    /**
     * Number of sentences included in the model.
     */
    val nbSentences: Int,
    /**
     * Duration of the build.
     */
    val duration: Duration,
    /**
     * Is there an error during the build?
     */
    val error: Boolean,
    /**
     * Error message if there is an error.
     */
    val errorMessage: String?,
    /**
     * Date of the build.
     */
    val date: Instant,
)
