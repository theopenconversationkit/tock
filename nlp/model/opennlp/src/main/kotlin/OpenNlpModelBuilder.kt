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

package ai.tock.nlp.opennlp

import ai.tock.nlp.core.configuration.NlpApplicationConfiguration
import ai.tock.nlp.core.configuration.NlpModelConfiguration
import ai.tock.nlp.core.sample.SampleEntity
import ai.tock.nlp.core.sample.SampleExpression
import ai.tock.nlp.model.EntityBuildContext
import ai.tock.nlp.model.IntentContext
import ai.tock.nlp.model.TokenizerContext
import ai.tock.nlp.model.service.engine.EntityModelHolder
import ai.tock.nlp.model.service.engine.IntentModelHolder
import ai.tock.nlp.model.service.engine.NlpEngineModelBuilder
import ai.tock.nlp.model.service.engine.TokenizerModelHolder
import ai.tock.shared.loadProperties
import mu.KotlinLogging
import opennlp.tools.ml.maxent.GISModel
import opennlp.tools.ml.maxent.GISTrainer
import opennlp.tools.ml.model.Event
import opennlp.tools.ml.model.OnePassRealValueDataIndexer
import opennlp.tools.ml.model.TwoPassDataIndexer
import opennlp.tools.namefind.BilouCodec
import opennlp.tools.namefind.NameFinderME
import opennlp.tools.namefind.NameSample
import opennlp.tools.namefind.TokenNameFinderFactory
import opennlp.tools.util.ObjectStreamUtils
import opennlp.tools.util.Span
import opennlp.tools.util.TrainingParameters
import opennlp.tools.util.TrainingParameters.CUTOFF_PARAM
import java.time.Instant

/**
 *
 */
internal object OpenNlpModelBuilder : NlpEngineModelBuilder {

    private val logger = KotlinLogging.logger {}
    private const val MIN_BUILD_SIZE = 2

    override fun buildIntentModel(
        context: IntentContext,
        configuration: NlpApplicationConfiguration,
        expressions: List<SampleExpression>
    ): IntentModelHolder {
        val tokenizer = OpenNlpTokenizer(TokenizerModelHolder(context.language, configuration))
        val tokenizerContext = TokenizerContext(context)

        val model = if (expressions.size < MIN_BUILD_SIZE) {
            GISModel(arrayOf(), arrayOf(), arrayOf())
        } else {
            val events = ObjectStreamUtils.createObjectStream(
                expressions
                    .map {
                        Event(it.intent.name, tokenizer.tokenize(tokenizerContext, it.text))
                    }
            )
            val dataIndexer = if (expressions.size < 100) OnePassRealValueDataIndexer() else TwoPassDataIndexer()
            val param = TrainingParameters()
            if (expressions.size < 1000) {
                param.put(CUTOFF_PARAM, 1)
            }
            configuration.intentConfiguration.properties.forEach {
                param.put(it.key.toString(), it.value?.toString())
            }

            dataIndexer.init(param, null)
            dataIndexer.index(events)
            GISTrainer().trainModel(1000, dataIndexer)
        }

        return IntentModelHolder(context.application, model, configuration, Instant.now())
    }

    override fun buildEntityModel(
        context: EntityBuildContext,
        configuration: NlpApplicationConfiguration,
        expressions: List<SampleExpression>
    ): EntityModelHolder? {
        val model = if (expressions.size < MIN_BUILD_SIZE) {
            null
        } else {
            val tokenizer = OpenNlpTokenizer(TokenizerModelHolder(context.language, configuration))
            val tokenizerContext = TokenizerContext(context)

            val spanEntityMap = mutableMapOf<Span, SampleEntity>()

            var entityCount = 0
            val trainingEvents = expressions.mapNotNull { expression ->
                val text = expression.text
                val tokens = tokenizer.tokenize(tokenizerContext, text)
                val spans = expression.entities.mapNotNull { e ->
                    val start =
                        if (e.start == 0) 0 else tokenizer.tokenize(tokenizerContext, text.substring(0, e.start)).size
                    val end = start + tokenizer.tokenize(tokenizerContext, text.substring(e.start, e.end)).size
                    if (start >= tokens.size || end > tokens.size) {
                        null
                    } else {
                        entityCount++
                        val roleSpan = Span(start, end, e.definition.role)
                        spanEntityMap.put(roleSpan, e)
                        roleSpan
                    }
                }.toTypedArray().sortedArray()

                if (spans.size == expression.entities.size) {
                    try {
                        NameSample(
                            tokens,
                            spans,
                            false
                        )
                    } catch (e: Exception) {
                        logger.warn("error with $text when reunify entities", e)
                        null
                    }
                } else {
                    logger.warn { "error with $text when reunify entities" }
                    null
                }
            }

            if (entityCount < MIN_BUILD_SIZE) {
                null
            } else {
                val params = TrainingParameters()
                configuration.entityConfiguration.properties.forEach {
                    params.put(it.key.toString(), it.value?.toString())
                }

                NameFinderME.train(
                    context.language.language,
                    null,
                    ObjectStreamUtils.createObjectStream(trainingEvents),
                    params,
                    TokenNameFinderFactory(null, null, BilouCodec())
                )
            }
        }

        return if (model == null) null else EntityModelHolder(OpenNlpNameFinderME(model), configuration, Instant.now())
    }

    /**
     * Default intent classifier properties for the nlp engine.
     */
    override val defaultIntentClassifierConfiguration: NlpModelConfiguration =
        NlpModelConfiguration(loadProperties("/opennlp/defaultIntentClassifier.properties"))

    /**
     * Default entity classifier properties for the nlp engine.
     */
    override val defaultEntityClassifierConfiguration: NlpModelConfiguration =
        NlpModelConfiguration(loadProperties("/opennlp/defaultEntityClassifier.properties"))

    override fun defaultNlpApplicationConfiguration(): NlpApplicationConfiguration =
        NlpApplicationConfiguration(
            intentConfiguration = defaultIntentClassifierConfiguration,
            entityConfiguration = defaultEntityClassifierConfiguration,
            hasTokenizerConfiguration = false
        )
}
