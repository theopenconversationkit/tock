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
import fr.vsct.tock.nlp.core.Intent.Companion.UNKNOWN_INTENT
import fr.vsct.tock.nlp.front.service.FrontRepository.config
import fr.vsct.tock.nlp.front.service.FrontRepository.core
import fr.vsct.tock.nlp.front.service.FrontRepository.toApplication
import fr.vsct.tock.nlp.front.shared.Parser
import fr.vsct.tock.nlp.front.shared.config.ApplicationDefinition
import fr.vsct.tock.nlp.front.shared.config.ClassifiedSentence
import fr.vsct.tock.nlp.front.shared.config.ClassifiedSentenceStatus.model
import fr.vsct.tock.nlp.front.shared.config.ClassifiedSentenceStatus.validated
import fr.vsct.tock.nlp.front.shared.config.SentencesQuery
import fr.vsct.tock.nlp.front.shared.merge.ValuesMergeQuery
import fr.vsct.tock.nlp.front.shared.merge.ValuesMergeResult
import fr.vsct.tock.nlp.front.shared.parser.ParseQuery
import fr.vsct.tock.nlp.front.shared.parser.ParseResult
import fr.vsct.tock.nlp.front.shared.parser.ParsedEntityValue
import fr.vsct.tock.nlp.front.shared.value.ValueTransformer
import fr.vsct.tock.shared.defaultLocale
import fr.vsct.tock.shared.withoutNamespace
import mu.KotlinLogging
import java.util.Locale

/**
 *
 */
object ParserService : Parser {

    private val logger = KotlinLogging.logger {}
    private val tabCarriageRegexp = "[\\n\\r\\t]+".toRegex()

    internal fun formatQuery(query: String): String {
        return query.replace(tabCarriageRegexp, "").trim()
    }

    internal fun findLanguage(application: ApplicationDefinition, locale: Locale): Locale {
        return application.supportedLocales.let { locales ->
            if (locales.contains(locale)) {
                locale
            } else {
                val language = Locale(locale.language)
                if (locales.contains(language)) {
                    language
                } else if (locales.contains(defaultLocale)) {
                    logger.warn { "locale not found - $locale - use default $defaultLocale" }
                    defaultLocale
                } else {
                    val first = locales.first()
                    logger.warn { "locale not found - $locale - use first found $first" }
                    first
                }
            }
        }
    }

    override fun parse(query: ParseQuery): ParseResult {
        with(query) {
            val application = config.getApplicationByNamespaceAndName(namespace, applicationName) ?: error("unknown application $namespace:$applicationName")

            val language = findLanguage(application, context.language)

            val referenceDate = context.referenceDate.withZoneSameInstant(context.referenceTimezone)

            val q = formatQuery(queries.first())
            if (q.isEmpty()) {
                logger.warn { "empty query after format - $query" }
                return ParseResult(UNKNOWN_INTENT, emptyList(), 0.0, 0.0, q, emptyMap())
            }

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

            val callContext = CallContext(toApplication(application), language, application.nlpEngineType, referenceDate)

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
                return ParseResult(
                        config.getIntentById(validatedSentence.classification.intentId)!!.shortQualifiedName(query.namespace),
                        entityValues.map { ParsedEntityValue(it.value, 1.0, core.supportValuesMerge(it.entityType)) },
                        1.0,
                        1.0,
                        q,
                        emptyMap()
                )
            }

            //TODO multi query handling
            //TODO state handling
            val otherIntents: MutableMap<String, Double> = mutableMapOf()
            val parseResult = core.parse(callContext, q) {
                //select first
                if (it.hasNext()) {
                    it.next() to it.probability()
                } else {
                    null
                }
                        .apply {
                            //and take all other intents where probability is greater than 0.1
                            while (it.hasNext()) {
                                (it.next() to it.probability())
                                        .takeIf { (_, prob) -> prob > 0.1 }
                                        ?.let { (intent, prob) -> otherIntents.put(intent.name, prob) }
                                        ?: break
                            }
                        }
            }

            val result = ParseResult(
                    parseResult.intent.withoutNamespace(query.namespace),
                    parseResult.entities.map { ParsedEntityValue(it.value, it.probability, core.supportValuesMerge(it.entityType)) },
                    parseResult.intentProbability,
                    parseResult.entitiesProbability,
                    q,
                    otherIntents
            )

            if (context.registerQuery) {
                val intentId = config.getIntentIdByQualifiedName(parseResult.intent)!!
                val sentence = ClassifiedSentence(
                        result,
                        language,
                        application._id!!,
                        intentId,
                        parseResult.intentProbability,
                        parseResult.entitiesProbability
                )
                if (!sentence.hasSameContent(validatedSentence)) {
                    config.save(sentence)
                }
            }

            return result
        }
    }

    override fun mergeValues(query: ValuesMergeQuery): ValuesMergeResult {
        with(query) {
            val application = config.getApplicationByNamespaceAndName(namespace, applicationName) ?: error("unknown application $namespace:$applicationName")

            val language = findLanguage(application, context.language)

            val referenceDate = context.referenceDate.withZoneSameInstant(context.referenceTimezone)

            val callContext = CallContext(toApplication(application), language, application.nlpEngineType, referenceDate)

            val result = core.mergeValues(callContext, entity.entityType, values.map { it.toValueDescriptor() })

            return ValuesMergeResult(ValueTransformer.wrapNullableValue(result?.value), result?.content)
        }
    }
}