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

package fr.vsct.tock.nlp.front.service

import fr.vsct.tock.nlp.core.CallContext
import fr.vsct.tock.nlp.core.EntityRecognition
import fr.vsct.tock.nlp.core.EntityValue
import fr.vsct.tock.nlp.front.service.FrontRepository.config
import fr.vsct.tock.nlp.front.service.FrontRepository.core
import fr.vsct.tock.nlp.front.service.FrontRepository.toApplication
import fr.vsct.tock.nlp.front.shared.Parser
import fr.vsct.tock.nlp.front.shared.config.ClassifiedSentence
import fr.vsct.tock.nlp.front.shared.config.ClassifiedSentenceStatus.model
import fr.vsct.tock.nlp.front.shared.config.ClassifiedSentenceStatus.validated
import fr.vsct.tock.nlp.front.shared.config.SentencesQuery
import fr.vsct.tock.nlp.front.shared.parser.ParseResult
import fr.vsct.tock.nlp.front.shared.parser.QueryDescription
import fr.vsct.tock.shared.withoutNamespace
import java.util.Locale

/**
 *
 */
object ParserService : Parser {

    override fun parse(query: QueryDescription): ParseResult {
        //TODO validate text ("no \n\r\t")
        with(query) {
            val application = config.getApplicationByNamespaceAndName(namespace, applicationName) ?: error("unknown application $namespace:$applicationName")

            val language = application.supportedLocales.let {
                if (it.contains(context.language)) {
                    context.language
                } else {
                    val language = Locale(context.language.language)
                    if (it.contains(language)) {
                        language
                    } else {
                        error("Unsupported locale : ${context.language}")
                    }
                }
            }

            val q = query.queries.first()

            val validatedSentence = config
                    .search(
                            SentencesQuery(
                                    application._id!!,
                                    language,
                                    search = q,
                                    status = setOf(validated, model),
                                    onlyExactMatch = true
                            ))
                    .sentences
                    .firstOrNull()

            val callContext = CallContext(toApplication(application), language, context.engineType)

            if (validatedSentence != null && query.context.checkExistingQuery) {
                val entityValues = core.evaluateEntities(
                        callContext,
                        q,
                        validatedSentence.classification.entities.map {
                            EntityRecognition(
                                    EntityValue(
                                            it.start,
                                            it.end,
                                            FrontRepository.toEntity(it.type, it.role)
                                    ),
                                    1.0
                            )
                        })
                ParseResult(
                        config.getIntentById(validatedSentence.classification.intentId)!!.shortQualifiedName(query.namespace),
                        entityValues.map { it.value },
                        1.0,
                        1.0,
                        q
                )
            }

            //TODO multi query handling
            //TODO state handling
            val parseResult = core.parse(callContext, q)

            val result = ParseResult(
                    parseResult.intent.withoutNamespace(query.namespace),
                    parseResult.entities,
                    parseResult.intentProbability,
                    parseResult.entitiesProbability,
                    q)

            if (context.registerQuery) {
                val intentId = config.getIntentIdByQualifiedName(parseResult.intent)!!
                val sentence = ClassifiedSentence(result, language, application._id!!, intentId)
                if (!sentence.hasSameContent(validatedSentence)) {
                    config.save(sentence)
                }
            }

            return result
        }
    }

}