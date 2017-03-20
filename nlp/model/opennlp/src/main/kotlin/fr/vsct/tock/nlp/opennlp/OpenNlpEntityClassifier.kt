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

import fr.vsct.tock.nlp.core.EntityRecognition
import fr.vsct.tock.nlp.core.EntityValue
import fr.vsct.tock.nlp.model.EntityCallContext
import fr.vsct.tock.nlp.model.EntityCallContextForEntity
import fr.vsct.tock.nlp.model.EntityCallContextForIntent
import fr.vsct.tock.nlp.model.EntityCallContextForSubEntities
import fr.vsct.tock.nlp.model.service.engine.EntityModelHolder
import fr.vsct.tock.nlp.model.service.engine.NlpEntityClassifier
import opennlp.tools.namefind.NameFinderME

/**
 *
 */
internal class OpenNlpEntityClassifier(model: EntityModelHolder) : NlpEntityClassifier(model) {

    override fun classifyEntities(context: EntityCallContext, text: String, tokens: Array<String>): List<EntityRecognition> {
        return when (context) {
            is EntityCallContextForIntent -> classify(context, text, tokens)
            is EntityCallContextForEntity -> TODO()
            is EntityCallContextForSubEntities -> TODO()
        }
    }

    private fun classify(context: EntityCallContextForIntent, text: String, tokens: Array<String>): List<EntityRecognition> {
        with(model) {
            val finder = nativeModel as NameFinderME
            val spans = finder.find(tokens)

            var entityProbability = 0.0
            var nbEntitySpans = 0


            return spans.mapIndexedNotNull { index, span ->
                entityProbability += span.prob
                nbEntitySpans++
                val nextIndex = index + 1
                if (nextIndex < spans.size && spans[nextIndex].type == span.type) {
                    null
                } else {
                    //reunify text
                    var t = text
                    var start = 0
                    for (i in 0..span.start - 1) {
                        val nextTokenIndex = tokens[i].length + t.indexOf(tokens[i])
                        start += nextTokenIndex
                        t = t.substring(nextTokenIndex)
                    }

                    start += t.indexOf(tokens[span.start])
                    var end = start + tokens[span.start].length
                    t = t.substring(t.indexOf(tokens[span.start]) + tokens[span.start].length)

                    for (i in span.start + 1..span.end - 1) {
                        val nextTokenIndex = tokens[i].length + t.indexOf(tokens[i])
                        end += nextTokenIndex
                        t = t.substring(nextTokenIndex)
                    }
                    if (end > text.length) {
                        error("Parsing error")
                    }

                    //probability
                    val entityProba = entityProbability / nbEntitySpans
                    entityProbability = 0.0
                    nbEntitySpans = 0
                    EntityRecognition(EntityValue(start, end, context.intent.getEntity(span.type), null, false), entityProba)
                }
            }.toList()

        }
    }
}