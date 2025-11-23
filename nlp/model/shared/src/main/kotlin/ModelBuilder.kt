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

package ai.tock.nlp.model

import ai.tock.nlp.core.Application
import ai.tock.nlp.core.EntityType
import ai.tock.nlp.core.Intent
import ai.tock.nlp.core.NlpEngineType
import ai.tock.nlp.core.configuration.NlpApplicationConfiguration
import ai.tock.nlp.core.sample.SampleExpression

/**
 * To manage and build nlp models.
 */
interface ModelBuilder {
    fun buildAndSaveTokenizerModel(
        context: TokenizerContext,
        expressions: List<SampleExpression>,
    )

    fun buildIntentModel(
        context: IntentContext,
        expressions: List<SampleExpression>,
    ): ModelHolder

    fun buildAndSaveIntentModel(
        context: IntentContext,
        expressions: List<SampleExpression>,
    )

    fun buildEntityModel(
        context: EntityBuildContext,
        expressions: List<SampleExpression>,
    ): ModelHolder?

    fun buildAndSaveEntityModel(
        context: EntityBuildContext,
        expressions: List<SampleExpression>,
    )

    fun isIntentModelExist(context: IntentContext): Boolean

    fun isEntityModelExist(context: EntityBuildContext): Boolean

    fun deleteOrphans(
        applicationsAndIntents: Map<Application, Set<Intent>>,
        entityTypes: List<EntityType>,
    )

    /**
     * Stores in memory the model.
     */
    fun warmupIntentModel(context: IntentContext)

    /**
     * Stores in memory the model.
     */
    fun warmupEntityModel(context: EntityCallContext)

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
