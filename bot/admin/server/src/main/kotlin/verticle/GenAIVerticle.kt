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

package ai.tock.bot.admin.verticle

import ai.tock.bot.admin.indicators.metric.MetricFilter
import ai.tock.bot.admin.model.genai.BotDocumentCompressorConfigurationDTO
import ai.tock.bot.admin.model.genai.BotObservabilityConfigurationDTO
import ai.tock.bot.admin.model.genai.BotRAGConfigurationDTO
import ai.tock.bot.admin.model.genai.BotSentenceGenerationConfigurationDTO
import ai.tock.bot.admin.model.genai.BotSentenceGenerationInfoDTO
import ai.tock.bot.admin.model.genai.BotVectorStoreConfigurationDTO
import ai.tock.bot.admin.model.genai.PlaygroundRequest
import ai.tock.bot.admin.model.genai.SentenceGenerationRequest
import ai.tock.bot.admin.model.genai.model.genai.SentenceParsingRequest
import ai.tock.bot.admin.service.CompletionService
import ai.tock.bot.admin.service.DocumentCompressorService
import ai.tock.bot.admin.service.ObservabilityService
import ai.tock.bot.admin.service.RAGService
import ai.tock.bot.admin.service.SentenceGenerationService
import ai.tock.bot.admin.service.VectorStoreService
import ai.tock.genai.orchestratorclient.responses.SentenceParsingResponse
import ai.tock.nlp.admin.AdminService
import ai.tock.nlp.admin.model.SentenceReport
import ai.tock.nlp.core.Intent.Companion.RAG_EXCLUDED_INTENT_NAME
import ai.tock.nlp.core.Intent.Companion.UNKNOWN_INTENT_NAME
import ai.tock.nlp.front.client.FrontClient
import ai.tock.nlp.front.shared.config.ApplicationDefinition
import ai.tock.nlp.front.shared.parser.ParseResult
import ai.tock.shared.exception.rest.NotFoundException
import ai.tock.shared.security.TockUser
import ai.tock.shared.security.TockUserRole.admin
import ai.tock.shared.security.TockUserRole.botUser
import ai.tock.shared.security.TockUserRole.nlpUser
import ai.tock.shared.vertx.WebVerticle
import ai.tock.shared.withNamespace
import io.vertx.ext.web.RoutingContext
import org.litote.kmongo.toId
import java.util.Locale

/**
 * [GenAIVerticle] contains all the routes and actions associated with the AI tasks
 */
class GenAIVerticle {
    companion object {
        // Configuration
        private const val PATH_CONFIG_RAG = "/gen-ai/bots/:botId/configuration/rag"
        private const val PATH_CONFIG_SENTENCE_GENERATION = "/gen-ai/bots/:botId/configuration/sentence-generation"
        private const val PATH_CONFIG_SENTENCE_GENERATION_INFO = "$PATH_CONFIG_SENTENCE_GENERATION/info"
        private const val PATH_CONFIG_VECTOR_STORE = "/gen-ai/bots/:botId/configuration/vector-store"
        private const val PATH_CONFIG_VECTOR_OBSERVABILITY = "/gen-ai/bots/:botId/configuration/observability"
        private const val PATH_CONFIG_DOCUMENT_COMPRESSOR = "/gen-ai/bots/:botId/configuration/document-compressor"

        // Completion
        private const val PATH_COMPLETION_SENTENCE_GENERATION = "/gen-ai/bots/:botId/completion/sentence-generation"
        private const val PATH_COMPLETION_SENTENCE_PARSING = "/gen-ai/bots/:botId/completion/sentence-parsing"

        private const val PATH_COMPLETION_PLAYGROUND = "/gen-ai/bots/:botId/completion/playground"
    }

    private val front = FrontClient

    fun configure(webVerticle: WebVerticle) {
        with(webVerticle) {
            /**
             * lamdba calling database to retrieve application definition from request context
             * @return [ApplicationDefinition]
             */
            val currentContextApp: (RoutingContext) -> ApplicationDefinition? = { context ->
                val botId = context.pathParam("botId")
                getNamespace(context)?.let { namespace ->
                    front.getApplicationByNamespaceAndName(
                        namespace, botId,
                    )
                } ?: throw NotFoundException(404, "Could not find $botId in namespace")
            }

            // --------------------------------------- Config - RAG ----------------------------------------
            blockingJsonPost(
                PATH_CONFIG_RAG,
                admin,
            ) { context: RoutingContext, request: BotRAGConfigurationDTO ->
                return@blockingJsonPost checkNamespaceAndExecute(context, currentContextApp) {
                    logger.info { "Saving 'RAG' configuration..." }
                    BotRAGConfigurationDTO(
                        RAGService.saveRag(request),
                    )
                }
            }

            blockingJsonGet(
                PATH_CONFIG_RAG,
                admin,
            ) { context: RoutingContext ->
                checkNamespaceAndExecute(context, currentContextApp) { app ->
                    logger.info { "Retrieving 'RAG' configuration..." }
                    RAGService.getRAGConfiguration(app.namespace, app.name)?.let { BotRAGConfigurationDTO(it) }
                }
            }

            blockingDelete(
                PATH_CONFIG_RAG,
                admin,
            ) { context: RoutingContext ->
                checkNamespaceAndExecute(context, currentContextApp) { app ->
                    logger.info { "Deleting 'RAG' configuration..." }
                    RAGService.deleteConfig(app.namespace, app.name)
                }
            }

            // ---------------------------------- Config - Sentence Generation -----------------------------
            blockingJsonPost(
                PATH_CONFIG_SENTENCE_GENERATION,
                admin,
            ) { context: RoutingContext, request: BotSentenceGenerationConfigurationDTO ->
                return@blockingJsonPost checkNamespaceAndExecute(context, currentContextApp) {
                    logger.info { "Saving 'Sentence Generation' configuration..." }
                    BotSentenceGenerationConfigurationDTO(
                        SentenceGenerationService.saveSentenceGeneration(request),
                    )
                }
            }

            blockingJsonGet(
                PATH_CONFIG_SENTENCE_GENERATION,
                admin,
            ) { context: RoutingContext ->
                checkNamespaceAndExecute(context, currentContextApp) { app ->
                    logger.info { "Retrieving 'Sentence Generation' configuration..." }
                    SentenceGenerationService.getSentenceGenerationConfiguration(app.namespace, app.name)
                        ?.let { BotSentenceGenerationConfigurationDTO(it) }
                }
            }

            blockingJsonGet(
                PATH_CONFIG_SENTENCE_GENERATION_INFO,
                nlpUser,
            ) { context: RoutingContext ->
                checkNamespaceAndExecute(context, currentContextApp) { app ->
                    logger.info { "Retrieving 'Sentence Generation' configuration info..." }
                    SentenceGenerationService.getSentenceGenerationConfiguration(app.namespace, app.name)
                        ?.let { BotSentenceGenerationInfoDTO(it) } ?: BotSentenceGenerationInfoDTO()
                }
            }

            blockingDelete(
                PATH_CONFIG_SENTENCE_GENERATION,
                admin,
            ) { context: RoutingContext ->
                checkNamespaceAndExecute(context, currentContextApp) { app ->
                    logger.info { "Deleting 'Sentence Generation' configuration..." }
                    SentenceGenerationService.deleteConfig(app.namespace, app.name)
                }
            }

            // ----------------------------------- Config - Vector Store -----------------------------------
            blockingJsonPost(
                PATH_CONFIG_VECTOR_STORE,
                admin,
            ) { context: RoutingContext, request: BotVectorStoreConfigurationDTO ->
                return@blockingJsonPost checkNamespaceAndExecute(context, currentContextApp) {
                    logger.info { "Saving 'Vector Store' configuration..." }
                    BotVectorStoreConfigurationDTO(
                        VectorStoreService.saveVectorStore(request),
                    )
                }
            }

            blockingJsonGet(
                PATH_CONFIG_VECTOR_STORE,
                admin,
            ) { context: RoutingContext ->
                checkNamespaceAndExecute(context, currentContextApp) { app ->
                    logger.info { "Retrieving 'Vector Store' configuration..." }
                    VectorStoreService.getVectorStoreConfiguration(app.namespace, app.name)
                        ?.let { BotVectorStoreConfigurationDTO(it) }
                }
            }

            blockingDelete(
                PATH_CONFIG_VECTOR_STORE,
                admin,
            ) { context: RoutingContext ->
                checkNamespaceAndExecute(context, currentContextApp) { app ->
                    logger.info { "Deleting 'Vector Store' configuration..." }
                    VectorStoreService.deleteConfig(app.namespace, app.name)
                }
            }

            // ----------------------------------- Config - Observability ------------------------------------
            blockingJsonPost(
                PATH_CONFIG_VECTOR_OBSERVABILITY,
                admin,
            ) { context: RoutingContext, request: BotObservabilityConfigurationDTO ->
                return@blockingJsonPost checkNamespaceAndExecute(context, currentContextApp) {
                    logger.info { "Saving 'Observability' configuration..." }
                    BotObservabilityConfigurationDTO(
                        ObservabilityService.saveObservability(request),
                    )
                }
            }

            blockingJsonGet(
                PATH_CONFIG_VECTOR_OBSERVABILITY,
                admin,
            ) { context: RoutingContext ->
                checkNamespaceAndExecute(context, currentContextApp) { app ->
                    logger.info { "Retrieving 'Observability' configuration..." }
                    ObservabilityService.getObservabilityConfiguration(app.namespace, app.name)
                        ?.let { BotObservabilityConfigurationDTO(it) }
                }
            }

            blockingDelete(
                PATH_CONFIG_VECTOR_OBSERVABILITY,
                admin,
            ) { context: RoutingContext ->
                checkNamespaceAndExecute(context, currentContextApp) { app ->
                    logger.info { "Deleting 'Observability' configuration..." }
                    ObservabilityService.deleteConfig(app.namespace, app.name)
                }
            }

            // ---------------------------------- Config - Document Compressor -------------------------------
            blockingJsonPost(
                PATH_CONFIG_DOCUMENT_COMPRESSOR,
                admin,
            ) { context: RoutingContext, request: BotDocumentCompressorConfigurationDTO ->
                return@blockingJsonPost checkNamespaceAndExecute(context, currentContextApp) {
                    logger.info { "Saving 'Document Compressor' configuration..." }
                    BotDocumentCompressorConfigurationDTO(
                        DocumentCompressorService.saveDocumentCompressor(request),
                    )
                }
            }

            blockingJsonGet(
                PATH_CONFIG_DOCUMENT_COMPRESSOR,
                admin,
            ) { context: RoutingContext ->
                checkNamespaceAndExecute(context, currentContextApp) { app ->
                    logger.info { "Retrieving 'Document Compressor' configuration..." }
                    DocumentCompressorService.getDocumentCompressorConfiguration(app.namespace, app.name)
                        ?.let { BotDocumentCompressorConfigurationDTO(it) }
                }
            }

            blockingDelete(
                PATH_CONFIG_DOCUMENT_COMPRESSOR,
                admin,
            ) { context: RoutingContext ->
                checkNamespaceAndExecute(context, currentContextApp) { app ->
                    logger.info { "Deleting 'Document Compressor' configuration..." }
                    DocumentCompressorService.deleteConfig(app.namespace, app.name)
                }
            }

            // ------------------------------- Generation - Sentence Generation --------------------------
            blockingJsonPost(
                PATH_COMPLETION_SENTENCE_GENERATION,
                botUser,
            ) { context: RoutingContext, request: SentenceGenerationRequest ->
                return@blockingJsonPost checkNamespaceAndExecute(context, currentContextApp) { app ->
                    logger.info { "GEN AI - Generating sentences..." }
                    CompletionService.generateSentences(request, app.namespace, app.name)
                }
            }

            // ---------------------------------- Generation - Playground --------------------------
            blockingJsonPost(
                PATH_COMPLETION_PLAYGROUND,
                admin,
            ) { context: RoutingContext, request: PlaygroundRequest ->
                return@blockingJsonPost checkNamespaceAndExecute(context, currentContextApp) { app ->
                    logger.info { "GEN AI - Playground..." }
                    CompletionService.generate(request, app.namespace, app.name)
                }
            }

            // ---------------------------------- Generation - Parsing --------------------------
            blockingJsonPost(
                PATH_COMPLETION_SENTENCE_PARSING,
                nlpUser,
            ) { context: RoutingContext, request: SentenceParsingRequest ->
                return@blockingJsonPost checkNamespaceAndExecute(context, currentContextApp) { app ->
                    logger.info { "GEN AI - Sentence Parsing..." }
                    val parsedSentence: SentenceParsingResponse? =
                        CompletionService.parseSentence(
                            request,
                            app.namespace,
                            botId = app.name,
                            appId = app._id,
                        )

                    parsedSentence?.let {
                        val language = Locale.forLanguageTag(it.language)

                        val result =
                            ParseResult(
                                intent = it.intent,
                                intentNamespace = app.namespace,
                                language = Locale.forLanguageTag(it.language),
                                entities = emptyList(),
                                notRetainedEntities = emptyList(),
                                intentProbability = it.score,
                                entitiesProbability = 0.0,
                                retainedQuery = request.sentence,
                                otherIntentsProbabilities = it.suggestions.associate { o -> o.intent to o.score },
                                originalIntentsProbabilities = emptyMap(),
                            )

                        val intentId =
                            when (val intentWithNamespace = result.intent.withNamespace(result.intentNamespace)) {
                                UNKNOWN_INTENT_NAME -> UNKNOWN_INTENT_NAME.toId()
                                RAG_EXCLUDED_INTENT_NAME -> RAG_EXCLUDED_INTENT_NAME.toId()
                                else -> AdminService.front.getIntentIdByQualifiedName(intentWithNamespace)!!
                            }

                        SentenceReport(result, language, app._id, intentId)
                    }
                }
            }
        }
    }

//    fun parseSentence(query: ParseQuery): SentenceReport {
//        var result = AdminService.front.parse(query.toQuery())
//        result = ai.tock.genai.orchestratorclient.services.CompletionService.parseSentence(
//            request, app.namespace, botId = app.name, appId = app._id)
//
//        val intentWithNamespace = result.intent.withNamespace(result.intentNamespace)
//        val intentId =
//            when (intentWithNamespace) {
//                UNKNOWN_INTENT_NAME -> UNKNOWN_INTENT_NAME.toId()
//                RAG_EXCLUDED_INTENT_NAME -> RAG_EXCLUDED_INTENT_NAME.toId()
//                else -> AdminService.front.getIntentIdByQualifiedName(intentWithNamespace)!!
//            }
//        val application = AdminService.front.getApplicationByNamespaceAndName(query.namespace, query.applicationName)!!
//        return SentenceReport(result, query.currentLanguage, application._id, intentId)
//    }

    /**
     * Get the namespace from the context
     * @param context : the vertx routing context
     */
    private fun getNamespace(context: RoutingContext): String? = ((context.user() ?: context.session()?.get("tockUser")) as? TockUser)?.namespace

    /**
     * Merge namespace and botId on requested [MetricFilter]
     * @param namespace the namespace
     * @param botId the bot id
     * @param filter a given [MetricFilter]
     */
    private fun createFilterMetric(
        namespace: String,
        botId: String,
        filter: MetricFilter?,
    ) = filter?.copy(namespace = namespace, botId = botId) ?: MetricFilter(namespace, botId)
}
