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

import com.github.salomonbrys.kodein.instance
import fr.vsct.tock.nlp.core.CallContext
import fr.vsct.tock.nlp.core.EntityRecognition
import fr.vsct.tock.nlp.core.EntityValue
import fr.vsct.tock.nlp.core.Intent
import fr.vsct.tock.nlp.core.Intent.Companion.UNKNOWN_INTENT
import fr.vsct.tock.nlp.core.IntentClassification
import fr.vsct.tock.nlp.core.IntentSelector
import fr.vsct.tock.nlp.front.service.FrontRepository.config
import fr.vsct.tock.nlp.front.service.FrontRepository.core
import fr.vsct.tock.nlp.front.service.FrontRepository.toApplication
import fr.vsct.tock.nlp.front.service.storage.ParseRequestLogDAO
import fr.vsct.tock.nlp.front.shared.Parser
import fr.vsct.tock.nlp.front.shared.config.ApplicationDefinition
import fr.vsct.tock.nlp.front.shared.config.ClassifiedSentence
import fr.vsct.tock.nlp.front.shared.config.ClassifiedSentenceStatus.model
import fr.vsct.tock.nlp.front.shared.config.ClassifiedSentenceStatus.validated
import fr.vsct.tock.nlp.front.shared.config.IntentDefinition
import fr.vsct.tock.nlp.front.shared.config.SentencesQuery
import fr.vsct.tock.nlp.front.shared.merge.ValuesMergeQuery
import fr.vsct.tock.nlp.front.shared.merge.ValuesMergeResult
import fr.vsct.tock.nlp.front.shared.monitoring.ParseRequestLog
import fr.vsct.tock.nlp.front.shared.parser.ParseIntentEntitiesQuery
import fr.vsct.tock.nlp.front.shared.parser.ParseQuery
import fr.vsct.tock.nlp.front.shared.parser.ParseResult
import fr.vsct.tock.nlp.front.shared.parser.ParsedEntityValue
import fr.vsct.tock.nlp.front.shared.value.ValueTransformer
import fr.vsct.tock.shared.Executor
import fr.vsct.tock.shared.defaultLocale
import fr.vsct.tock.shared.injector
import fr.vsct.tock.shared.name
import fr.vsct.tock.shared.namespace
import fr.vsct.tock.shared.withNamespace
import fr.vsct.tock.shared.withoutNamespace
import mu.KotlinLogging
import java.time.ZonedDateTime
import java.util.Locale

/**
 *
 */
object ParserService : Parser {

    private val logger = KotlinLogging.logger {}
    private val tabCarriageRegexp = "[\\n\\r\\t]+".toRegex()

    private val executor: Executor by injector.instance()
    private val logDAO: ParseRequestLogDAO by injector.instance()

    private data class ParseMetadata(
            val application: ApplicationDefinition,
            val language: Locale,
            val referenceDate: ZonedDateTime,
            val expectedIntent: IntentDefinition? = null)

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

    override fun parseIntentEntities(query: ParseIntentEntitiesQuery): ParseResult {
        return setMetadataAndParse(
                query.query,
                {
                    config.getIntentByNamespaceAndName(
                            query.intent.namespace(),
                            query.intent.name())
                }
        )
    }

    override fun parse(query: ParseQuery): ParseResult {
        return setMetadataAndParse(query, { null })
    }

    private fun setMetadataAndParse(
            query: ParseQuery,
            expectedIntentLoader: () -> IntentDefinition?): ParseResult {
        val time = System.currentTimeMillis()
        with(query) {
            val application = config.getApplicationByNamespaceAndName(namespace, applicationName)
                    ?: error("unknown application $namespace:$applicationName")

            val language = findLanguage(application, context.language)

            val referenceDate = context.referenceDate.withZoneSameInstant(context.referenceTimezone)

            val metadata = ParseMetadata(application, language, referenceDate, expectedIntentLoader.invoke())

            var result: ParseResult? = null
            try {
                result = parse(query, metadata)
                return result
            } finally {
                executor.executeBlocking {
                    logDAO.save(
                            ParseRequestLog(
                                    application._id!!,
                                    query,
                                    result,
                                    System.currentTimeMillis() - time
                            )
                    )
                }
            }
        }
    }

    private fun parse(query: ParseQuery, metadata: ParseMetadata): ParseResult {
        with(query) {
            val (application, language, referenceDate, expectedIntent) = metadata

            val q = formatQuery(queries.first())
            if (q.isEmpty()) {
                logger.warn { "empty query after format - $query" }
                return ParseResult(UNKNOWN_INTENT, application.namespace, emptyList(), 0.0, 0.0, q, emptyMap())
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

            if (validatedSentence != null
                    && query.context.checkExistingQuery
                    && (expectedIntent == null || expectedIntent._id == validatedSentence.classification.intentId)) {
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
                val intent = config.getIntentById(validatedSentence.classification.intentId)!!
                return ParseResult(
                        intent.name,
                        intent.namespace,
                        entityValues.map { ParsedEntityValue(it.value, 1.0, core.supportValuesMerge(it.entityType)) },
                        1.0,
                        1.0,
                        q,
                        emptyMap()
                )
            }

            val result = parseWithNLP(callContext, q, expectedIntent)

            fun toClassifiedSentence(): ClassifiedSentence {
                val intentId = config.getIntentIdByQualifiedName(result.intent.withNamespace(result.intentNamespace))!!
                return ClassifiedSentence(
                        result,
                        language,
                        application._id!!,
                        intentId,
                        result.intentProbability,
                        result.entitiesProbability
                )
            }

            if (context.registerQuery) {
                executor.executeBlocking {
                    toClassifiedSentence().apply {
                        if (!hasSameContent(validatedSentence)) {
                            config.save(this)
                        }
                    }
                }
            }

            //check cache for test
            if (context.test && validatedSentence != null) {
                if (!validatedSentence.hasSameContent(toClassifiedSentence())) {
                    error("[TEST MODE] nlp model do not produce same output than validated sentence for query $q")
                }
            }

            return result
        }
    }


    private fun parseWithNLP(callContext: CallContext, query: String, expectedIntent: IntentDefinition?): ParseResult {

        abstract class ParseSelector : IntentSelector {
            //the intents with p > 0.1
            val otherIntents: MutableMap<String, Double> = mutableMapOf()
        }

        class DefaultIntentSelector : ParseSelector() {
            override fun selectIntent(classification: IntentClassification): Pair<Intent, Double>? {
                return with(classification) {
                    //select first
                    if (hasNext()) {
                        next() to probability()
                    } else {
                        null
                    }
                            .apply {
                                //and take all other intents where probability is greater than 0.1
                                while (hasNext()) {
                                    (next() to probability())
                                            .takeIf { (_, prob) -> prob > 0.1 }
                                            ?.let { (intent, prob) -> otherIntents.put(intent.name, prob) }
                                            ?: break
                                }
                            }
                }
            }
        }

        class ExpectedIntentSelector(val forcedIntent: IntentDefinition) : ParseSelector() {

            override fun selectIntent(classification: IntentClassification): Pair<Intent, Double>? {
                with(classification) {
                    var result: Pair<Intent, Double>? = null
                    while (hasNext()) {
                        (next() to probability())
                                .also { (intent, prob) ->

                                    if (forcedIntent.qualifiedName == intent.name) {
                                        result = intent to prob
                                    }
                                    if (prob > 0.1) {
                                        otherIntents.put(intent.name, prob)
                                    } else if (result != null) {
                                        return result
                                    }
                                }
                    }
                }
                return null
            }
        }


        //TODO multi query handling
        //TODO state handling
        val selector = if (expectedIntent == null) DefaultIntentSelector() else ExpectedIntentSelector(expectedIntent)

        val result = core.parse(callContext, query, selector)

        return ParseResult(
                result.intent.withoutNamespace(),
                result.intent.namespace(),
                result.entities.map { ParsedEntityValue(it.value, it.probability, core.supportValuesMerge(it.entityType)) },
                result.intentProbability,
                result.entitiesProbability,
                query,
                selector.otherIntents)
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