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

import fr.vsct.tock.nlp.core.BuildContext
import fr.vsct.tock.nlp.core.CallContext
import fr.vsct.tock.nlp.core.Entity
import fr.vsct.tock.nlp.core.EntityEvaluationContext
import fr.vsct.tock.nlp.core.Intent
import fr.vsct.tock.nlp.core.Intent.Companion.UNKNOWN_INTENT_NAME
import fr.vsct.tock.nlp.core.ModelCore
import fr.vsct.tock.nlp.core.NlpCore
import fr.vsct.tock.nlp.front.service.FrontRepository.toApplication
import fr.vsct.tock.nlp.front.service.selector.IntentSelectorService
import fr.vsct.tock.nlp.front.service.selector.IntentSelectorService.isValidClassifiedSentence
import fr.vsct.tock.nlp.front.service.storage.ParseRequestLogDAO
import fr.vsct.tock.nlp.front.shared.ApplicationConfiguration
import fr.vsct.tock.nlp.front.shared.Parser
import fr.vsct.tock.nlp.front.shared.config.ApplicationDefinition
import fr.vsct.tock.nlp.front.shared.config.ClassifiedSentence
import fr.vsct.tock.nlp.front.shared.config.ClassifiedSentenceStatus.model
import fr.vsct.tock.nlp.front.shared.config.ClassifiedSentenceStatus.validated
import fr.vsct.tock.nlp.front.shared.config.IntentDefinition
import fr.vsct.tock.nlp.front.shared.config.SentencesQuery
import fr.vsct.tock.nlp.front.shared.evaluation.EntityEvaluationQuery
import fr.vsct.tock.nlp.front.shared.evaluation.EntityEvaluationResult
import fr.vsct.tock.nlp.front.shared.merge.ValuesMergeQuery
import fr.vsct.tock.nlp.front.shared.merge.ValuesMergeResult
import fr.vsct.tock.nlp.front.shared.monitoring.ParseRequestLog
import fr.vsct.tock.nlp.front.shared.parser.IntentQualifier
import fr.vsct.tock.nlp.front.shared.parser.ParseQuery
import fr.vsct.tock.nlp.front.shared.parser.ParseResult
import fr.vsct.tock.nlp.front.shared.parser.ParsedEntityValue
import fr.vsct.tock.nlp.front.shared.value.ValueTransformer
import fr.vsct.tock.shared.Executor
import fr.vsct.tock.shared.booleanProperty
import fr.vsct.tock.shared.defaultLocale
import fr.vsct.tock.shared.error
import fr.vsct.tock.shared.injector
import fr.vsct.tock.shared.name
import fr.vsct.tock.shared.namespace
import fr.vsct.tock.shared.provide
import fr.vsct.tock.shared.withNamespace
import fr.vsct.tock.shared.withoutNamespace
import mu.KotlinLogging
import org.litote.kmongo.toId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.util.Locale

/**
 *
 */
object ParserService : Parser {

    private val logger = KotlinLogging.logger {}
    private val tabCarriageRegexp = "[\\n\\r\\t]+".toRegex()

    private val validateSentenceTest = booleanProperty("tock_parser_validate_sentence_test", false)

    private val executor: Executor get() = injector.provide()
    private val logDAO: ParseRequestLogDAO get() = injector.provide()
    private val core: NlpCore get() = injector.provide()
    private val modelCore: ModelCore get() = injector.provide()

    private val config: ApplicationConfiguration get() = injector.provide()

    init {
        if (booleanProperty("tock_nlp_model_fill_cache", false)) {
            try {
                config.getApplications()
                    .forEach { app ->
                        app.supportedLocales.forEach { locale ->
                            modelCore.warmupModels(BuildContext(toApplication(app), locale, app.nlpEngineType))
                        }
                    }
            } catch (e: Exception) {
                logger.error(e)
            }
        }
    }

    private data class CallMetadata(
        val application: ApplicationDefinition,
        val language: Locale,
        val referenceDate: ZonedDateTime,
        val intentsQualifiers: Set<IntentQualifier>
    )

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
        val time = System.currentTimeMillis()
        with(query) {
            val application = loadApplication(namespace, applicationName)

            val language = findLanguage(application, context.language)

            val referenceDate = context.referenceDate.withZoneSameInstant(context.referenceTimezone)

            val metadata = CallMetadata(application, language, referenceDate, query.intentsSubset)

            var result: ParseResult? = null
            try {
                result = parse(query, metadata)
                return result
            } finally {
                executor.executeBlocking {
                    logDAO.save(
                        ParseRequestLog(
                            application._id,
                            query,
                            result,
                            System.currentTimeMillis() - time
                        )
                    )
                }
            }
        }
    }

    private fun getReferenceDateByEntityMap(
        intents: List<IntentDefinition>,
        referenceDate: ZonedDateTime
    ): Map<Entity, ZonedDateTime>? = intents
        .flatMap { it.entities }
        .distinct()
        .filter { it.atStartOfDay == true }
        .mapNotNull { it.toEntity() }
        .map { it to referenceDate.truncatedTo(ChronoUnit.DAYS) }
        .toMap()
        .takeUnless { it.isEmpty() }

    private fun parse(query: ParseQuery, metadata: CallMetadata): ParseResult {
        with(query) {
            val (application, language, referenceDate, intentsQualifiers) = metadata

            //TODO multi query handling
            val q = formatQuery(queries.first())
            if (q.isEmpty()) {
                logger.warn { "empty query after format - $query" }
                return ParseResult(
                    UNKNOWN_INTENT_NAME,
                    application.namespace,
                    query.context.language,
                    emptyList(),
                    0.0,
                    0.0,
                    q,
                    emptyMap()
                )
            }

            val validatedSentence = config
                .search(
                    SentencesQuery(
                        application._id,
                        language,
                        search = q,
                        status = setOf(validated, model),
                        onlyExactMatch = true
                    )
                )
                .sentences
                .firstOrNull()

            val intents = config.getIntentsByApplicationId(application._id)

            val callContext = CallContext(
                toApplication(application),
                language, application.nlpEngineType,
                EntityEvaluationContext(
                    referenceDate,
                    application.mergeEngineTypes,
                    getReferenceDateByEntityMap(intents, referenceDate)
                )
            )

            val data = ParserRequestData(
                application,
                query,
                validatedSentence,
                intentsQualifiers,
                intents
            )

            if (isValidClassifiedSentence(data)) {
                val entityValues = core.evaluateEntities(
                    callContext,
                    q,
                    validatedSentence!!.classification
                        .entities
                        .mapNotNull { it.toEntityRecognition(FrontRepository::toEntity) }
                )
                val intent = config.getIntentById(validatedSentence.classification.intentId)
                return ParseResult(
                    intent?.name ?: Intent.UNKNOWN_INTENT_NAME.name(),
                    intent?.namespace ?: Intent.UNKNOWN_INTENT_NAME.namespace(),
                    language,
                    entityValues.map { ParsedEntityValue(it.value, 1.0, core.supportValuesMerge(it.entityType)) },
                    1.0,
                    1.0,
                    q,
                    emptyMap()
                )
            }

            val intentSelector = IntentSelectorService.selector(data)
            val result = core.parse(callContext, q, intentSelector)
                .run {
                    ParseResult(
                        intent.withoutNamespace(),
                        intent.namespace(),
                        language,
                        entities.map {
                            ParsedEntityValue(
                                it.value,
                                it.probability,
                                core.supportValuesMerge(it.entityType)
                            )
                        },
                        intentProbability,
                        entitiesProbability,
                        q,
                        intentSelector.otherIntents
                    )
                }

            fun toClassifiedSentence(): ClassifiedSentence {
                val intentId = config.getIntentIdByQualifiedName(result.intent.withNamespace(result.intentNamespace))!!
                return ClassifiedSentence(
                    result,
                    language,
                    application._id,
                    intentId,
                    result.intentProbability,
                    result.entitiesProbability
                )
            }

            if (context.registerQuery) {
                executor.executeBlocking {
                    saveSentence(toClassifiedSentence(), validatedSentence)
                }
            }

            //check cache for test
            if (validateSentenceTest && context.test && validatedSentence != null) {
                if (!validatedSentence.hasSameContent(toClassifiedSentence())) {
                    error("[TEST MODE] nlp model does not produce same output than validated sentence for query $q")
                }
            }

            return result
        }
    }

    internal fun saveSentence(newSentence: ClassifiedSentence, validatedSentence: ClassifiedSentence?) {
        with(newSentence) {
            if (validatedSentence?.status != validated &&
                validatedSentence?.status != model
                && !hasSameContent(validatedSentence)
            ) {
                //do not persist analysis if intent probability is < 0.1
                val sentence = if ((lastIntentProbability ?: 0.0) > 0.1) this
                else copy(classification = classification.copy(UNKNOWN_INTENT_NAME.toId(), emptyList()))

                config.save(sentence)
            }
        }
    }

    override fun evaluateEntities(query: EntityEvaluationQuery): EntityEvaluationResult {
        with(query) {
            val application = loadApplication(namespace, applicationName)

            val language = findLanguage(application, context.language)

            val referenceDate = context.referenceDate.withZoneSameInstant(context.referenceTimezone)

            val callContext = CallContext(
                toApplication(application),
                language,
                application.nlpEngineType,
                EntityEvaluationContext(
                    referenceDate,
                    referenceDateByEntityMap = getReferenceDateByEntityMap(
                        config.getIntentsByApplicationId(application._id),
                        referenceDate
                    )
                )
            )

            val result = core.evaluateEntities(
                callContext,
                text,
                query.entities.map { it.toEntityRecognition() })

            return EntityEvaluationResult(
                result.map { ParsedEntityValue(it.value, it.probability, core.supportValuesMerge(it.entityType)) }
            )
        }
    }

    override fun mergeValues(query: ValuesMergeQuery): ValuesMergeResult {
        with(query) {
            val application = loadApplication(namespace, applicationName)

            val language = findLanguage(application, context.language)

            val referenceDate = context.referenceDate.withZoneSameInstant(context.referenceTimezone)

            val callContext = CallContext(
                toApplication(application),
                language,
                application.nlpEngineType,
                EntityEvaluationContext(
                    referenceDate,
                    referenceDateByEntityMap = getReferenceDateByEntityMap(
                        config.getIntentsByApplicationId(application._id),
                        referenceDate
                    )
                )
            )

            val result = core.mergeValues(callContext, entity, values.map { it.toValueDescriptor() })

            return ValuesMergeResult(ValueTransformer.wrapNullableValue(result?.value), result?.content)
        }
    }

    private fun loadApplication(namespace: String, applicationName: String): ApplicationDefinition =
        config.getApplicationByNamespaceAndName(namespace, applicationName)
                ?: throw UnknownApplicationException(namespace, applicationName)

    override fun healthcheck(): Boolean {
        return core.healthcheck()
    }
}