/*
 * Copyright (C) 2017/2021 e-voyageurs technologies
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

package ai.tock.nlp.model.service

import ai.tock.nlp.core.*
import ai.tock.nlp.core.configuration.NlpApplicationConfiguration
import ai.tock.nlp.core.sample.SampleExpression
import ai.tock.nlp.model.*
import ai.tock.nlp.model.IntentContext.IntentContextKey
import ai.tock.nlp.model.service.engine.*
import ai.tock.nlp.model.service.engine.NlpEngineRepository.getModelBuilder
import ai.tock.nlp.model.service.engine.NlpEngineRepository.getModelIo
import ai.tock.nlp.model.service.engine.NlpEngineRepository.getProvider
import ai.tock.nlp.model.service.engine.NlpEngineRepository.registeredNlpEngineTypes
import ai.tock.nlp.model.service.engine.NlpModelRepository.saveEntityModel
import ai.tock.nlp.model.service.engine.NlpModelRepository.saveIntentModel
import ai.tock.nlp.model.service.storage.NlpApplicationConfigurationDAO
import ai.tock.shared.injector
import com.github.salomonbrys.kodein.instance
import mu.KotlinLogging

/**
 *
 */
object NlpClassifierService : NlpClassifier {

    private val logger = KotlinLogging.logger {}
    private val nlpApplicationConfigurationDAO: NlpApplicationConfigurationDAO by injector.instance()

    override fun supportedNlpEngineTypes(): Set<NlpEngineType> {
        return NlpEngineRepository.registeredNlpEngineTypes()
    }

    private fun tokenizeForIntentClassifier(context: IntentContext, text: String): Array<String> =
        NlpEngineRepository.getTokenizer(context).tokenize(TokenizerContext(context), text)

    private fun tokenizeForEntityClassifier(context: EntityCallContext, text: String): Array<String> =
        NlpEngineRepository.getTokenizer(context).tokenize(TokenizerContext(context), text)

    override fun classifyIntent(context: IntentContext, text: String): IntentClassification {
        return NlpEngineRepository.getIntentClassifier(context).classifyIntent(
            context,
            text,
            tokenizeForIntentClassifier(context, text)
        )
    }

    override fun classifyIntent(
        context: IntentContext,
        modelHolder: ModelHolder,
        text: String
    ): IntentClassification {
        return NlpEngineRepository.getIntentClassifier(context, modelHolder as IntentModelHolder)
            .classifyIntent(context, text, tokenizeForIntentClassifier(context, text))
    }

    override fun classifyEntities(
        context: EntityCallContext,
        text: String
    ): List<EntityRecognition> = NlpEngineRepository.getEntityClassifier(context)
        ?.let {
            classifyEntities(
                it,
                context,
                text,
                tokenizeForEntityClassifier(context, text)
            )
        } ?: emptyList()

    override fun classifyEntities(
        context: EntityCallContext,
        modelHolder: ModelHolder,
        text: String
    ): List<EntityRecognition> {
        return NlpEngineRepository.getEntityClassifier(context, modelHolder as EntityModelHolder)
            ?.let { classifyEntities(it, context, text, tokenizeForEntityClassifier(context, text)) }
            ?: emptyList()
    }

    private fun classifyEntities(
        entityClassifier: EntityClassifier,
        context: EntityCallContext,
        text: String,
        tokens: Array<String>
    ): List<EntityRecognition> {

        val result = entityClassifier.classifyEntities(context, text, tokens)
        return result.map { e ->
            if (e.hasSubEntities()) {
                val subEntities = classifyEntities(
                    EntityCallContextForSubEntities(e.entityType, context),
                    e.textValue(text)
                )
                e.copy(value = e.value.copy(subEntities = subEntities))
            } else {
                e
            }
        }
    }

    override fun buildAndSaveTokenizerModel(context: TokenizerContext, expressions: List<SampleExpression>) {
        // do nothing at this time
    }

    override fun buildIntentModel(context: IntentContext, expressions: List<SampleExpression>): ModelHolder {
        return getModelBuilder(context).buildIntentModel(
            context,
            getCurrentModelConfiguration(context.applicationName, context.engineType),
            expressions
        )
    }

    override fun buildAndSaveIntentModel(context: IntentContext, expressions: List<SampleExpression>) {
        val model = buildIntentModel(context, expressions)
        saveIntentModel(context.key(), model as IntentModelHolder, getModelIo(context))
    }

    override fun buildEntityModel(context: EntityBuildContext, expressions: List<SampleExpression>): ModelHolder? {
        val exp = context.select(expressions)
        return if (exp.isNotEmpty())
            getModelBuilder(context).buildEntityModel(
                context,
                getCurrentModelConfiguration(context.applicationName, context.engineType),
                exp
            ) else null
    }

    override fun buildAndSaveEntityModel(context: EntityBuildContext, expressions: List<SampleExpression>) {
        val model = buildEntityModel(context, expressions)
        if (model != null) {
            saveEntityModel(context.key(), model as EntityModelHolder, getModelIo(context))
        }
    }

    override fun isIntentModelExist(context: IntentContext): Boolean {
        return NlpModelRepository.isIntentModelExist(context)
    }

    override fun isEntityModelExist(context: EntityBuildContext): Boolean {
        return NlpModelRepository.isEntityModelExist(context)
    }

    override fun deleteOrphans(applicationsAndIntents: Map<Application, Set<Intent>>, entityTypes: List<EntityType>) {
        // remove intents
        NlpModelRepository.removeIntentModelsNotIn(
            applicationsAndIntents.keys
                .flatMap { key ->
                    key.supportedLocales
                        .flatMap { locale ->
                            supportedNlpEngineTypes()
                                .map { engineType ->
                                    IntentContextKey(key.name, locale, engineType)
                                }
                        }
                }
        )

        // remove entities
        NlpModelRepository.removeEntityModelsNotIn(
            applicationsAndIntents.entries
                .flatMap { e ->
                    val appName = e.key.name
                    e.key.supportedLocales
                        .flatMap { locale ->
                            supportedNlpEngineTypes()
                                .flatMap { engineType ->
                                    e.value.map { intent ->
                                        EntityContextKey(appName, intent.name, locale, engineType)
                                    } + entityTypes.map { entityType ->
                                        EntityContextKey(
                                            appName,
                                            null,
                                            locale,
                                            engineType,
                                            entityType,
                                            true
                                        )
                                    }
                                }
                        }
                }
        )
    }

    override fun warmupIntentModel(context: IntentContext) {
        logger.debug { "warmup intent model $context" }
        NlpEngineRepository.getIntentClassifier(context)
    }

    override fun warmupEntityModel(context: EntityCallContext) {
        logger.debug { "warmup entity model $context" }
        NlpEngineRepository.getEntityClassifier(context)
    }

    override fun getCurrentModelConfiguration(
        applicationName: String,
        nlpEngineType: NlpEngineType
    ): NlpApplicationConfiguration {
        return nlpApplicationConfigurationDAO.loadLastConfiguration(applicationName, nlpEngineType)
            ?: NlpEngineRepository.getProvider(nlpEngineType).modelBuilder.defaultNlpApplicationConfiguration()
    }

    override fun updateModelConfiguration(
        applicationName: String,
        engineType: NlpEngineType,
        configuration: NlpApplicationConfiguration
    ) {
        nlpApplicationConfigurationDAO.saveNewConfiguration(applicationName, engineType, configuration)
    }

    fun healthcheck(): List<Pair<String, () -> Boolean>> {
        return registeredNlpEngineTypes().flatMap {
            val healthcheck = getProvider(it).healthcheck()
            listOf(
                "${it.name}-entity" to { healthcheck().entityClassifier },
                "${it.name}-intent" to { healthcheck().intentClassifier },
            )
        }
    }
}
