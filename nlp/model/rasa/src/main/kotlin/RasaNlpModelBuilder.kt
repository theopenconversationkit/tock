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
package ai.tock.nlp.rasa

import ai.tock.nlp.core.NlpEngineType
import ai.tock.nlp.core.configuration.NlpApplicationConfiguration
import ai.tock.nlp.core.configuration.NlpModelConfiguration
import ai.tock.nlp.core.sample.SampleExpression
import ai.tock.nlp.model.EntityBuildContext
import ai.tock.nlp.model.IntentContext
import ai.tock.nlp.model.service.engine.EntityModelHolder
import ai.tock.nlp.model.service.engine.IntentModelHolder
import ai.tock.nlp.model.service.engine.NlpEngineModelBuilder
import ai.tock.nlp.rasa.RasaClient.PutModelRequest
import ai.tock.nlp.rasa.RasaClient.TrainModelRequest

internal object RasaNlpModelBuilder : NlpEngineModelBuilder {

    override fun buildIntentModel(
        context: IntentContext,
        configuration: NlpApplicationConfiguration,
        expressions: List<SampleExpression>
    ): IntentModelHolder {
        val conf = configuration.toRasaConfiguration()
        return RasaClientProvider.getClient(conf).run {
            val modelFileName = train(
                TrainModelRequest(
                    domain = RasaMarkdown.toModelDomainMarkdown(context),
                    config = conf.getMarkdownConfiguration(context.language),
                    nlu = RasaMarkdown.toModelNluMarkdown(expressions)
                )
            )
            setModel(PutModelRequest(conf.getModelFilePath(modelFileName)))
            IntentModelHolder(
                application = context.application,
                nativeModel = RasaModelConfiguration(modelFileName),
                configuration = configuration
            )
        }
    }

    // for rasa, intent & entity models are the same so... do nothing
    override fun buildEntityModel(
        context: EntityBuildContext,
        configuration: NlpApplicationConfiguration,
        expressions: List<SampleExpression>
    ): EntityModelHolder =
        EntityModelHolder(
            nativeModel = NlpEngineType.rasa.name,
            configuration = configuration
        )

    override fun defaultNlpApplicationConfiguration(): NlpApplicationConfiguration =
        NlpApplicationConfiguration(
            applicationConfiguration = NlpModelConfiguration(
                hasProperties = true,
                hasMarkdown = true
            ),
            hasTokenizerConfiguration = false,
            hasIntentConfiguration = false,
            hasEntityConfiguration = false,
            hasApplicationConfiguration = true
        )
}
