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

package ai.tock.nlp.core

import ai.tock.nlp.core.configuration.NlpApplicationConfiguration
import ai.tock.nlp.core.quality.TestContext
import ai.tock.nlp.core.quality.TestModelReport
import ai.tock.nlp.core.sample.SampleExpression

/**
 * The main entry point to manage NLP models.
 */
interface ModelCore {
    /**
     * Load all models in memory.
     */
    fun warmupModels(context: BuildContext)

    /**
     * Update intent model.
     */
    fun updateIntentModel(
        context: BuildContext,
        expressions: List<SampleExpression>,
    )

    /**
     * Update entity model.
     */
    fun updateEntityModelForIntent(
        context: BuildContext,
        intent: Intent,
        expressions: List<SampleExpression>,
    )

    /**
     * Update entity type model.
     */
    fun updateEntityModelForEntityType(
        context: BuildContext,
        entityType: EntityType,
        expressions: List<SampleExpression>,
    )

    /**
     * Remove models that does not match specified applications or intents.
     */
    fun deleteOrphans(
        applicationsAndIntents: Map<Application, Set<Intent>>,
        entityTypes: List<EntityType>,
    )

    /**
     * Test a model and returns a report.
     */
    fun testModel(
        context: TestContext,
        expressions: List<SampleExpression>,
    ): TestModelReport

    /**
     * Returns the current model configuration.
     */
    fun getCurrentModelConfiguration(
        applicationName: String,
        nlpEngineType: NlpEngineType,
    ): NlpApplicationConfiguration

    /**
     * Updates the model configuration for the given application name.
     */
    fun updateModelConfiguration(
        applicationName: String,
        engineType: NlpEngineType,
        configuration: NlpApplicationConfiguration,
    )
}
