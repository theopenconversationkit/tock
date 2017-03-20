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

package fr.vsct.tock.nlp.opennlp

import fr.vsct.tock.nlp.core.sample.SampleEntity
import fr.vsct.tock.nlp.core.sample.SampleExpression
import fr.vsct.tock.nlp.model.EntityBuildContext
import fr.vsct.tock.nlp.model.IntentContext
import fr.vsct.tock.nlp.model.TokenizerContext
import fr.vsct.tock.nlp.model.service.engine.EntityModelHolder
import fr.vsct.tock.nlp.model.service.engine.IntentModelHolder
import fr.vsct.tock.nlp.model.service.engine.NlpEngineModelBuilder
import fr.vsct.tock.nlp.model.service.engine.TokenizerModelHolder
import mu.KotlinLogging
import opennlp.tools.ml.maxent.GIS
import opennlp.tools.ml.maxent.GISModel
import opennlp.tools.ml.model.Context
import opennlp.tools.ml.model.Event
import opennlp.tools.ml.model.TwoPassDataIndexer
import opennlp.tools.namefind.BilouCodec
import opennlp.tools.namefind.NameFinderME
import opennlp.tools.namefind.NameSample
import opennlp.tools.namefind.TokenNameFinderFactory
import opennlp.tools.util.ObjectStreamUtils
import opennlp.tools.util.Span
import opennlp.tools.util.TrainingParameters
import java.time.Instant

/**
 *
 */
internal object OpenNlpModelBuilder : NlpEngineModelBuilder {

    private val logger = KotlinLogging.logger {}

    override fun buildTokenizerModel(context: TokenizerContext, expressions: List<SampleExpression>): TokenizerModelHolder {
        return TokenizerModelHolder(context.language)
    }

    override fun buildIntentModel(context: IntentContext, expressions: List<SampleExpression>): IntentModelHolder {
        val tokenizer = OpenNlpTokenizer(TokenizerModelHolder(context.language))
        val tokenizerContext = TokenizerContext(context)

        val model = if (expressions.isEmpty()) {
            GISModel(arrayOf<Context>(), arrayOf<String>(), arrayOf<String>())
        } else {
            val events = ObjectStreamUtils.createObjectStream(expressions
                    .map {
                        Event(it.intent.name, tokenizer.tokenize(tokenizerContext, it.text))
                    })
            val twoPassDataIndexer = TwoPassDataIndexer()
            twoPassDataIndexer.init(TrainingParameters(), null)
            twoPassDataIndexer.index(events)
            GIS.trainModel(1000, twoPassDataIndexer, false, false, null)
        }

        return IntentModelHolder(context.application, model, Instant.now())
    }

    override fun buildEntityModel(context: EntityBuildContext, expressions: List<SampleExpression>): EntityModelHolder {
        val tokenizer = OpenNlpTokenizer(TokenizerModelHolder(context.language))
        val tokenizerContext = TokenizerContext(context)

        val spanEntityMap = mutableMapOf<Span, SampleEntity>()

        val trainingEvents = expressions.mapNotNull {
            expression ->
            val text = expression.text
            val tokens = tokenizer.tokenize(tokenizerContext, text)
            val spans = expression.entities.mapNotNull {
                e ->
                val start = if (e.start == 0) 0 else tokenizer.tokenize(tokenizerContext, text.substring(0, e.start)).size
                val end = start + tokenizer.tokenize(tokenizerContext, text.substring(e.start, e.end)).size
                if (start >= tokens.size || end > tokens.size) {
                    null
                } else {
                    val roleSpan = Span(start, end, e.definition.role)
                    spanEntityMap.put(roleSpan, e)
                    roleSpan
                }
            }.toTypedArray().sortedArray()

            if (spans.size == expression.entities.size) {
                NameSample(tokens,
                        spans,
                        false
                )
            } else {
                logger.error { "error with $text when reunify entities" }
                null
            }
        }


        val params = TrainingParameters()

        params.put(TrainingParameters.ITERATIONS_PARAM, Integer.toString(200))
        params.put(TrainingParameters.CUTOFF_PARAM, Integer.toString(0))
        //params.put(BeamSearch.BEAM_SIZE_PARAMETER, Integer.toString(5));

        val model = NameFinderME.train(
                context.language.language,
                null,
                ObjectStreamUtils.createObjectStream(trainingEvents),
                params,
                TokenNameFinderFactory(null, null, BilouCodec()))

        return EntityModelHolder(OpenNlpNameFinderME(model), Instant.now())
    }
}