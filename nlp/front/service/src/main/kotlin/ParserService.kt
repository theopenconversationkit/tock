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

package ai.tock.nlp.front.service

import ai.tock.nlp.core.BuildContext
import ai.tock.nlp.core.CallContext
import ai.tock.nlp.core.Entity
import ai.tock.nlp.core.EntityEvaluationContext
import ai.tock.nlp.core.Intent
import ai.tock.nlp.core.Intent.Companion.RAG_EXCLUDED_INTENT
import ai.tock.nlp.core.Intent.Companion.RAG_EXCLUDED_INTENT_NAME
import ai.tock.nlp.core.Intent.Companion.UNKNOWN_INTENT
import ai.tock.nlp.core.Intent.Companion.UNKNOWN_INTENT_NAME
import ai.tock.nlp.core.ModelCore
import ai.tock.nlp.core.NlpCore
import ai.tock.nlp.front.service.ConfigurationRepository.toApplication
import ai.tock.nlp.front.service.selector.IntentSelectorService
import ai.tock.nlp.front.service.selector.IntentSelectorService.isValidClassifiedSentence
import ai.tock.nlp.front.service.storage.ParseRequestLogDAO
import ai.tock.nlp.front.shared.ApplicationConfiguration
import ai.tock.nlp.front.shared.Parser
import ai.tock.nlp.front.shared.config.ApplicationDefinition
import ai.tock.nlp.front.shared.config.ClassifiedSentence
import ai.tock.nlp.front.shared.config.ClassifiedSentenceStatus.model
import ai.tock.nlp.front.shared.config.ClassifiedSentenceStatus.validated
import ai.tock.nlp.front.shared.config.IntentDefinition
import ai.tock.nlp.front.shared.config.SentencesQuery
import ai.tock.nlp.front.shared.evaluation.EntityEvaluationQuery
import ai.tock.nlp.front.shared.evaluation.EntityEvaluationResult
import ai.tock.nlp.front.shared.merge.ValuesMergeQuery
import ai.tock.nlp.front.shared.merge.ValuesMergeResult
import ai.tock.nlp.front.shared.monitoring.MarkAsUnknownQuery
import ai.tock.nlp.front.shared.monitoring.ParseRequestLog
import ai.tock.nlp.front.shared.parser.IntentQualifier
import ai.tock.nlp.front.shared.parser.ParseQuery
import ai.tock.nlp.front.shared.parser.ParseResult
import ai.tock.nlp.front.shared.parser.ParsedEntityValue
import ai.tock.nlp.front.shared.value.ValueTransformer
import ai.tock.shared.Executor
import ai.tock.shared.TOCK_NAMESPACE
import ai.tock.shared.booleanProperty
import ai.tock.shared.defaultLocale
import ai.tock.shared.error
import ai.tock.shared.injector
import ai.tock.shared.name
import ai.tock.shared.namespace
import ai.tock.shared.normalize
import ai.tock.shared.provide
import ai.tock.shared.withNamespace
import ai.tock.shared.withoutNamespace
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
        .asSequence()
        .distinct()
        .filter { it.atStartOfDay == true }
        .mapNotNull { it.toEntity() }
        .map { it to referenceDate.truncatedTo(ChronoUnit.DAYS) }
        .toMap()
        .takeUnless { it.isEmpty() }

    private fun parse(query: ParseQuery, metadata: CallMetadata): ParseResult {
        with(query) {
            val (application, language, referenceDate, intentsQualifiers) = metadata

            // TODO multi query handling
            val q = formatQuery(queries.first())
            if (q.isEmpty()) {
                logger.warn { "empty query after format - $query" }
                return ParseResult(
                    UNKNOWN_INTENT_NAME,
                    TOCK_NAMESPACE,
                    query.context.language,
                    emptyList(),
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
                        onlyExactMatch = true,
                        normalizeText = application.normalizeText,
                    )
                )
                .sentences
                .firstOrNull()

            val intents = ConfigurationRepository.getSharedNamespaceIntentsByApplicationId(application._id)

            val callContext = CallContext(
                toApplication(application),
                language,
                application.nlpEngineType,
                EntityEvaluationContext(
                    referenceDate,
                    application.mergeEngineTypes,
                    application.useEntityModels,
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
                        .mapNotNull { it.toEntityRecognition(ConfigurationRepository::toEntity) }
                )
                val intent = ConfigurationRepository.getIntentById(validatedSentence.classification.intentId)

                return ParseResult(
                    intent?.name
                        ?: if (RAG_EXCLUDED_INTENT_NAME == validatedSentence.classification.intentId.toString()) {
                            RAG_EXCLUDED_INTENT_NAME.name()
                        } else {
                            UNKNOWN_INTENT_NAME.name()
                        },
                    intent?.namespace ?: UNKNOWN_INTENT_NAME.namespace(),
                    language,
                    entityValues.map { ParsedEntityValue(it.value, 1.0, core.supportValuesMerge(it.entityType)) },
                    emptyList(),
                    1.0,
                    1.0,
                    q,
                    emptyMap()
                )
            }

            val intentSelector = IntentSelectorService.selector(data)
            val result = core.parse(callContext, q, intentSelector)
                .run {
                    var realIntent = intent
                    var realIntentProbability = intentProbability
                    val realOtherIntents = intentSelector.otherIntents
                    if(intentsQualifiers.isEmpty() && intentProbability < application.unknownIntentThreshold) {
                        // Force the real intent to UNKNOWN
                        realIntent = UNKNOWN_INTENT_NAME
                        // Set the probability of the real intent at 100%
                        realIntentProbability = 1.0
                        // Add to otherIntents, the real intention and its probability, so as not to lose this statistic
                        intentSelector.otherIntents[intent] = intentProbability
                    }

                    ParseResult(
                        realIntent.withoutNamespace(),
                        realIntent.namespace(),
                        language,
                        entities.map {
                            ParsedEntityValue(
                                it.value,
                                it.probability,
                                core.supportValuesMerge(it.entityType)
                            )
                        },
                        notRetainedEntities.map {
                            ParsedEntityValue(
                                it.value,
                                it.probability,
                                core.supportValuesMerge(it.entityType)
                            )
                        },
                        realIntentProbability,
                        entitiesProbability,
                        q,
                        // Sort the other real intentions in descending order of probability.
                        realOtherIntents.toList().sortedByDescending { it.second }.toMap()
                    )
                }

            fun toClassifiedSentence(): ClassifiedSentence {
                val intentName = result.intent.withNamespace(result.intentNamespace)
                val intentId = config.getIntentIdByQualifiedName(intentName)
                    ?: error("unknown intent: $intentName")
                return ClassifiedSentence(
                    result,
                    language,
                    application._id,
                    intentId,
                    result.intentProbability,
                    result.entitiesProbability,
                    query.configuration
                )
            }

            if (context.registerQuery) {
                executor.executeBlocking {
                    saveSentence(application, toClassifiedSentence(), validatedSentence)
                }
            }

            // check cache for test
            if (validateSentenceTest && context.test && validatedSentence != null) {
                if (!validatedSentence.hasSameContent(application, toClassifiedSentence())) {
                    error("[TEST MODE] nlp model does not produce same output than validated sentence for query $q")
                }
            }

            return result
        }
    }

    internal fun saveSentence(
        application: ApplicationDefinition,
        newSentence: ClassifiedSentence,
        validatedSentence: ClassifiedSentence?
    ) {
        with(newSentence) {
            if (validatedSentence?.status != validated &&
                validatedSentence?.status != model &&
                !hasSameContent(application, validatedSentence)
            ) {
                // do not persist analysis if intent probability is < 0.1
                val sentence = if ((lastIntentProbability ?: 0.0) > 0.1) this
                else copy(classification = classification.copy(UNKNOWN_INTENT_NAME.toId(), emptyList()))

                config.save(sentence)
            }
        }
    }

    /**
     * Check if the sentence has the same content (status, creation & update dates excluded)
     */
    private fun ClassifiedSentence.hasSameContent(app: ApplicationDefinition, sentence: ClassifiedSentence?): Boolean {
        return copy(text = if (app.normalizeText) text.normalize(language) else text) ==
                sentence?.copy(
                    text = if (app.normalizeText) sentence.text.normalize(language) else sentence.text,
                    status = status,
                    creationDate = creationDate,
                    updateDate = updateDate,
                    lastIntentProbability = lastIntentProbability,
                    lastEntityProbability = lastEntityProbability,
                    otherIntentsProbabilities = otherIntentsProbabilities
                )
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
                        ConfigurationRepository.getSharedNamespaceIntentsByApplicationId(application._id),
                        referenceDate
                    )
                )
            )

            val result = core.evaluateEntities(
                callContext,
                text,
                query.entities.map { it.toEntityRecognition() }
            )

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
                        ConfigurationRepository.getSharedNamespaceIntentsByApplicationId(application._id),
                        referenceDate
                    )
                )
            )

            val result = core.mergeValues(callContext, entity, values.map { it.toValueDescriptor() })

            return ValuesMergeResult(ValueTransformer.wrapNullableValue(result?.value), result?.content)
        }
    }

    override fun incrementUnknown(query: MarkAsUnknownQuery) {
        with(query) {
            val application = loadApplication(namespace, applicationName)

            val language = findLanguage(application, language)

            sentenceDAO.incrementUnknownStat(application._id, language, text)
        }
    }

    private fun loadApplication(namespace: String, applicationName: String): ApplicationDefinition =
        ConfigurationRepository.getApplicationByNamespaceAndName(namespace, applicationName)
            ?: throw UnknownApplicationException(namespace, applicationName)

    override fun healthcheck(): Boolean {
        return core.healthcheck()
    }
}
