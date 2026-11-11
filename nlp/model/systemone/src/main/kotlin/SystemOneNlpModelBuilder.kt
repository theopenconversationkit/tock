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

import ai.tock.nlp.core.NlpEngineType
import ai.tock.nlp.core.configuration.NlpApplicationConfiguration
import ai.tock.nlp.core.configuration.NlpModelConfiguration
import ai.tock.nlp.core.sample.SampleExpression
import ai.tock.nlp.model.EntityBuildContext
import ai.tock.nlp.model.IntentContext
import ai.tock.nlp.model.service.engine.EntityModelHolder
import ai.tock.nlp.model.service.engine.IntentModelHolder
import ai.tock.nlp.model.service.engine.NlpEngineModelBuilder

/**
 * Clef has no local training step: the intent schema (criteria) is built from the live
 * application's intents and sent on every classification call - see [SystemOneIntentClassifier].
 * This builder only captures the current [SystemOneConfiguration] snapshot as the "model".
 */
internal object SystemOneNlpModelBuilder : NlpEngineModelBuilder {
    override fun buildIntentModel(
        context: IntentContext,
        configuration: NlpApplicationConfiguration,
        expressions: List<SampleExpression>,
    ): IntentModelHolder =
        IntentModelHolder(
            application = context.application,
            // Only the model variant is persisted - never the API token: Clef has no local
            // training step, so the engine always rebuilds a fresh SystemOneConfiguration() from
            // live properties at classification time (see SystemOneEngineProvider.getIntentClassifier).
            nativeModel = SystemOneConfiguration().model,
            configuration = configuration,
        )

    // Clef does not support entity extraction - use the dedicated entityNlpEngineType
    // application setting to route entity build/parse calls to another engine.
    override fun buildEntityModel(
        context: EntityBuildContext,
        configuration: NlpApplicationConfiguration,
        expressions: List<SampleExpression>,
    ): EntityModelHolder =
        EntityModelHolder(
            nativeModel = NlpEngineType.systemone.name,
            configuration = configuration,
        )

    override fun defaultNlpApplicationConfiguration(): NlpApplicationConfiguration =
        NlpApplicationConfiguration(
            applicationConfiguration =
                NlpModelConfiguration(
                    hasProperties = true,
                    hasMarkdown = false,
                ),
            hasTokenizerConfiguration = false,
            hasIntentConfiguration = true,
            hasEntityConfiguration = false,
            hasApplicationConfiguration = true,
        )
}
