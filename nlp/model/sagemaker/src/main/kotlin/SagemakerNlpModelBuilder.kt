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
package ai.tock.nlp.sagemaker

import ai.tock.nlp.core.NlpEngineType
import ai.tock.nlp.core.configuration.NlpApplicationConfiguration
import ai.tock.nlp.core.configuration.NlpModelConfiguration
import ai.tock.nlp.core.sample.SampleExpression
import ai.tock.nlp.model.EntityBuildContext
import ai.tock.nlp.model.IntentContext
import ai.tock.nlp.model.service.engine.EntityModelHolder
import ai.tock.nlp.model.service.engine.IntentModelHolder
import ai.tock.nlp.model.service.engine.NlpEngineModelBuilder

internal object SagemakerNlpModelBuilder : NlpEngineModelBuilder {
    override fun buildIntentModel(
        context: IntentContext,
        configuration: NlpApplicationConfiguration,
        expressions: List<SampleExpression>,
    ): IntentModelHolder {
        return IntentModelHolder(
            application = context.application,
            nativeModel = SagemakerModelConfiguration(),
            configuration = configuration,
        )
    }

    override fun buildEntityModel(
        context: EntityBuildContext,
        configuration: NlpApplicationConfiguration,
        expressions: List<SampleExpression>,
    ): EntityModelHolder =
        EntityModelHolder(
            nativeModel = NlpEngineType.bgem3.name,
            configuration = configuration,
        )

    override fun defaultNlpApplicationConfiguration(): NlpApplicationConfiguration =
        NlpApplicationConfiguration(
            applicationConfiguration =
                NlpModelConfiguration(
                    hasProperties = true,
                    hasMarkdown = true,
                ),
            hasTokenizerConfiguration = false,
            hasIntentConfiguration = false,
            hasEntityConfiguration = false,
            hasApplicationConfiguration = true,
        )
}
