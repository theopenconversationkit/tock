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
import ai.tock.bot.admin.model.genai.*
import ai.tock.bot.admin.service.*
import ai.tock.nlp.front.client.FrontClient
import ai.tock.nlp.front.shared.config.ApplicationDefinition
import ai.tock.shared.exception.rest.NotFoundException
import ai.tock.shared.security.TockUser
import ai.tock.shared.security.TockUserRole.*
import ai.tock.shared.vertx.WebVerticle
import io.vertx.ext.web.RoutingContext

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
                val namespace = getNamespace(context)
                front.getApplicationByNamespaceAndName(
                    namespace, botId
                ) ?: throw NotFoundException(404, "Could not find $botId in $namespace")
            }

            // --------------------------------------- Config - RAG ----------------------------------------
            blockingJsonPost(
                PATH_CONFIG_RAG, admin
            ) { context: RoutingContext, request: BotRAGConfigurationDTO ->
                return@blockingJsonPost checkNamespaceAndExecute(context, currentContextApp) {
                    logger.info { "Saving 'RAG' configuration..." }
                    BotRAGConfigurationDTO(
                        RAGService.saveRag(request)
                    )
                }
            }

            blockingJsonGet(
                PATH_CONFIG_RAG, admin
            ) { context: RoutingContext ->
                checkNamespaceAndExecute(context, currentContextApp) { app ->
                    logger.info { "Retrieving 'RAG' configuration..." }
                    RAGService.getRAGConfiguration(app.namespace, app.name)?.let { BotRAGConfigurationDTO(it) }
                }
            }

            blockingDelete(
                PATH_CONFIG_RAG, admin
            ) { context: RoutingContext ->
                checkNamespaceAndExecute(context, currentContextApp) { app ->
                    logger.info { "Deleting 'RAG' configuration..." }
                    RAGService.deleteConfig(app.namespace, app.name)
                }
            }

            // ---------------------------------- Config - Sentence Generation -----------------------------
            blockingJsonPost(
                PATH_CONFIG_SENTENCE_GENERATION, admin
            ) { context: RoutingContext, request: BotSentenceGenerationConfigurationDTO ->
                return@blockingJsonPost checkNamespaceAndExecute(context, currentContextApp) {
                    logger.info { "Saving 'Sentence Generation' configuration..." }
                    BotSentenceGenerationConfigurationDTO(
                        SentenceGenerationService.saveSentenceGeneration(request)
                    )
                }
            }

            blockingJsonGet(
                PATH_CONFIG_SENTENCE_GENERATION, admin
            ) { context: RoutingContext ->
                checkNamespaceAndExecute(context, currentContextApp) { app ->
                    logger.info { "Retrieving 'Sentence Generation' configuration..." }
                    SentenceGenerationService.getSentenceGenerationConfiguration(app.namespace, app.name)
                        ?.let { BotSentenceGenerationConfigurationDTO(it) }
                }
            }

            blockingJsonGet(
                PATH_CONFIG_SENTENCE_GENERATION_INFO, nlpUser
            ) { context: RoutingContext ->
                checkNamespaceAndExecute(context, currentContextApp) { app ->
                    logger.info { "Retrieving 'Sentence Generation' configuration info..." }
                    SentenceGenerationService.getSentenceGenerationConfiguration(app.namespace, app.name)
                        ?.let { BotSentenceGenerationInfoDTO(it) } ?: BotSentenceGenerationInfoDTO()
                }
            }

            blockingDelete(
                PATH_CONFIG_SENTENCE_GENERATION, admin
            ) { context: RoutingContext ->
                checkNamespaceAndExecute(context, currentContextApp) { app ->
                    logger.info { "Deleting 'Sentence Generation' configuration..." }
                    SentenceGenerationService.deleteConfig(app.namespace, app.name)
                }
            }

            // ----------------------------------- Config - Vector Store -----------------------------------
            blockingJsonPost(
                PATH_CONFIG_VECTOR_STORE, admin
            ) { context: RoutingContext, request: BotVectorStoreConfigurationDTO ->
                return@blockingJsonPost checkNamespaceAndExecute(context, currentContextApp) {
                    logger.info { "Saving 'Vector Store' configuration..." }
                    BotVectorStoreConfigurationDTO(
                        VectorStoreService.saveVectorStore(request)
                    )
                }
            }

            blockingJsonGet(
                PATH_CONFIG_VECTOR_STORE, admin
            ) { context: RoutingContext ->
                checkNamespaceAndExecute(context, currentContextApp) { app ->
                    logger.info { "Retrieving 'Vector Store' configuration..." }
                    VectorStoreService.getVectorStoreConfiguration(app.namespace, app.name)
                        ?.let { BotVectorStoreConfigurationDTO(it) }
                }
            }

            blockingDelete(
                PATH_CONFIG_VECTOR_STORE, admin
            ) { context: RoutingContext ->
                checkNamespaceAndExecute(context, currentContextApp) { app ->
                    logger.info { "Deleting 'Vector Store' configuration..." }
                    VectorStoreService.deleteConfig(app.namespace, app.name)
                }
            }

            // ----------------------------------- Config - Observability ------------------------------------
            blockingJsonPost(
                PATH_CONFIG_VECTOR_OBSERVABILITY, admin
            ) { context: RoutingContext, request: BotObservabilityConfigurationDTO ->
                return@blockingJsonPost checkNamespaceAndExecute(context, currentContextApp) {
                    logger.info { "Saving 'Observability' configuration..." }
                    BotObservabilityConfigurationDTO(
                        ObservabilityService.saveObservability(request)
                    )
                }
            }

            blockingJsonGet(
                PATH_CONFIG_VECTOR_OBSERVABILITY, admin
            ) { context: RoutingContext ->
                checkNamespaceAndExecute(context, currentContextApp) { app ->
                    logger.info { "Retrieving 'Observability' configuration..." }
                    ObservabilityService.getObservabilityConfiguration(app.namespace, app.name)
                        ?.let { BotObservabilityConfigurationDTO(it) }
                }
            }

            blockingDelete(
                PATH_CONFIG_VECTOR_OBSERVABILITY, admin
            ) { context: RoutingContext ->
                checkNamespaceAndExecute(context, currentContextApp) { app ->
                    logger.info { "Deleting 'Observability' configuration..." }
                    ObservabilityService.deleteConfig(app.namespace, app.name)
                }
            }

            // ---------------------------------- Config - Document Compressor -------------------------------
            blockingJsonPost(
                PATH_CONFIG_DOCUMENT_COMPRESSOR, admin
            ) { context: RoutingContext, request: BotDocumentCompressorConfigurationDTO ->
                return@blockingJsonPost checkNamespaceAndExecute(context, currentContextApp) {
                    logger.info { "Saving 'Document Compressor' configuration..." }
                    BotDocumentCompressorConfigurationDTO(
                        DocumentCompressorService.saveDocumentCompressor(request)
                    )
                }
            }

            blockingJsonGet(
                PATH_CONFIG_DOCUMENT_COMPRESSOR, admin
            ) { context: RoutingContext ->
                checkNamespaceAndExecute(context, currentContextApp) { app ->
                    logger.info { "Retrieving 'Document Compressor' configuration..." }
                    DocumentCompressorService.getDocumentCompressorConfiguration(app.namespace, app.name)
                        ?.let { BotDocumentCompressorConfigurationDTO(it) }
                }
            }

            blockingDelete(
                PATH_CONFIG_DOCUMENT_COMPRESSOR, admin
            ) { context: RoutingContext ->
                checkNamespaceAndExecute(context, currentContextApp) { app ->
                    logger.info { "Deleting 'Document Compressor' configuration..." }
                    DocumentCompressorService.deleteConfig(app.namespace, app.name)
                }
            }

            // ------------------------------- Generation - Sentence Generation --------------------------
            blockingJsonPost(
                PATH_COMPLETION_SENTENCE_GENERATION, botUser
            ) { context: RoutingContext, request: SentenceGenerationRequest ->
                return@blockingJsonPost checkNamespaceAndExecute(context, currentContextApp) { app ->
                    logger.info { "GEN AI - Generating sentences..." }
                    CompletionService.generateSentences(request, app.namespace, app.name)
                }
            }

            // ---------------------------------- Generation - Playground --------------------------
            blockingJsonPost(
                PATH_COMPLETION_PLAYGROUND, admin
            ) { context: RoutingContext, request: PlaygroundRequest ->
                return@blockingJsonPost checkNamespaceAndExecute(context, currentContextApp) { app ->
                    logger.info { "GEN AI - Playground..." }
                    CompletionService.generate(request, app.namespace, app.name)
                }
            }
        }
    }

    /**
     * Get the namespace from the context
     * @param context : the vertx routing context
     */
    private fun getNamespace(context: RoutingContext) = (context.user() as TockUser).namespace

    /**
     * Merge botId on requested [MetricFilter]
     * @param botId the bot id
     * @param filter a given [MetricFilter]
     */
    private fun createFilterMetric(botId: String, filter: MetricFilter?) = filter?.copy(botId) ?: MetricFilter(botId)
}

