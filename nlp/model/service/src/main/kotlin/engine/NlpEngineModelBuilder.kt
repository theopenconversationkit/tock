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

package ai.tock.nlp.model.service.engine

import ai.tock.nlp.core.configuration.NlpApplicationConfiguration
import ai.tock.nlp.core.configuration.NlpModelConfiguration
import ai.tock.nlp.core.sample.SampleExpression
import ai.tock.nlp.model.EntityBuildContext
import ai.tock.nlp.model.IntentContext
import ai.tock.nlp.model.TokenizerContext

/**
 * Model builder to implement for nlp engines.
 */
interface NlpEngineModelBuilder {
    /**
     * Builds a tokenizer model.
     */
    fun buildTokenizerModel(
        context: TokenizerContext,
        configuration: NlpApplicationConfiguration,
        expressions: List<SampleExpression>,
    ): TokenizerModelHolder = TokenizerModelHolder(context.language, configuration)

    /**
     * Builds an intent model.
     */
    fun buildIntentModel(
        context: IntentContext,
        configuration: NlpApplicationConfiguration,
        expressions: List<SampleExpression>,
    ): IntentModelHolder

    /**
     * Builds an entity model.
     */
    fun buildEntityModel(
        context: EntityBuildContext,
        configuration: NlpApplicationConfiguration,
        expressions: List<SampleExpression>,
    ): EntityModelHolder?

    /**
     * Default tokenizer properties for the nlp engine.
     */
    val defaultTokenizerConfiguration: NlpModelConfiguration get() = NlpModelConfiguration()

    /**
     * Default intent classifier properties for the nlp engine.
     */
    val defaultIntentClassifierConfiguration: NlpModelConfiguration get() = NlpModelConfiguration()

    /**
     * Default entity classifier properties for the nlp engine.
     */
    val defaultEntityClassifierConfiguration: NlpModelConfiguration get() = NlpModelConfiguration()

    /**
     * Helper method.
     */
    fun defaultNlpApplicationConfiguration(): NlpApplicationConfiguration =
        NlpApplicationConfiguration(
            defaultTokenizerConfiguration,
            defaultIntentClassifierConfiguration,
            defaultEntityClassifierConfiguration,
        )
}
