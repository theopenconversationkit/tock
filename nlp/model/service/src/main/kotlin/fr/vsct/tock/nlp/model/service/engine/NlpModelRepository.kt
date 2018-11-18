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

package fr.vsct.tock.nlp.model.service.engine

import com.github.salomonbrys.kodein.instance
import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import fr.vsct.tock.nlp.core.configuration.NlpApplicationConfiguration
import fr.vsct.tock.nlp.model.EntityBuildContext
import fr.vsct.tock.nlp.model.EntityContext
import fr.vsct.tock.nlp.model.EntityContextKey
import fr.vsct.tock.nlp.model.IntentContext
import fr.vsct.tock.nlp.model.IntentContext.IntentContextKey
import fr.vsct.tock.nlp.model.ModelNotInitializedException
import fr.vsct.tock.nlp.model.TokenizerContext
import fr.vsct.tock.nlp.model.service.storage.NlpEngineModelDAO
import fr.vsct.tock.shared.Executor
import fr.vsct.tock.shared.booleanProperty
import fr.vsct.tock.shared.injector
import mu.KotlinLogging
import java.io.InputStream
import java.io.OutputStream
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.time.Instant
import java.time.Instant.now
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit.MINUTES


/**
 *
 */
internal object NlpModelRepository {

    private val logger = KotlinLogging.logger {}

    private data class ConfiguredModel(
        val nativeModel: Any?,
        val lastUpdate: Instant,
        val configuration: NlpApplicationConfiguration
    )

    private val modelDAO: NlpEngineModelDAO by injector.instance()

    private val executor: Executor by injector.instance()

    private val intentModelsCache: Cache<IntentContextKey, ConfiguredModel> =
        CacheBuilder.newBuilder().softValues().build()

    private val entityModelsCache: Cache<EntityContextKey, ConfiguredModel> =
        CacheBuilder.newBuilder().softValues().build()

    init {
        if (booleanProperty("tock_nlp_model_refresh", true)) {
            modelDAO.listenIntentModelChanges { key ->
                intentModelsCache
                    .asMap()
                    .keys
                    .find { it.id() == key }
                    ?.also {
                        logger.info { "refresh intent model for $key" }
                        intentModelsCache.put(
                            it,
                            loadIntentModel(
                                it,
                                NlpEngineRepository.getProvider(it.engineType)
                            )
                        )
                    }

            }
            modelDAO.listenEntityModelChanges { key ->
                entityModelsCache
                    .asMap()
                    .keys
                    .find { it.id() == key }
                    ?.also {
                        logger.info { "refresh entity model for $key" }
                        entityModelsCache.put(
                            it,
                            loadEntityModel(
                                it,
                                NlpEngineRepository.getProvider(it.engineType)
                            )
                        )
                    }
            }
        } else {
            logger.info { "refresh model is disabled" }
        }
    }

    fun getTokenizerModelHolder(context: TokenizerContext, conf: NlpApplicationConfiguration): TokenizerModelHolder {
        return TokenizerModelHolder(context.language, conf)
    }

    fun getConfiguration(context: IntentContext, provider: NlpEngineProvider): NlpApplicationConfiguration =
        getIntentModelHolder(context, provider).configuration

    fun getIntentModelHolder(context: IntentContext, provider: NlpEngineProvider): IntentModelHolder {
        return context
            .key()
            .let { key ->
                intentModelsCache.get(key) {
                    loadIntentModel(key, provider)
                }
            }
            .let { IntentModelHolder(context.application, it.nativeModel!!, it.configuration, it.lastUpdate) }
    }

    private fun NlpEngineProvider.configuration(configuration: NlpApplicationConfiguration? = null): NlpApplicationConfiguration =
        configuration ?: modelBuilder.defaultNlpApplicationConfiguration()

    private fun loadIntentModel(
        contextKey: IntentContextKey,
        provider: NlpEngineProvider
    ): ConfiguredModel {
        val inputStream = modelDAO.getIntentModelInputStream(contextKey)
        if (inputStream != null) {
            logger.debug { "load intent model for $contextKey" }
            val model = provider.modelIo.loadIntentModel(inputStream)
            return ConfiguredModel(
                model,
                inputStream.updatedDate,
                provider.configuration(inputStream.configuration)
            )
        }

        throw ModelNotInitializedException("no intent model found for $contextKey")
    }

    fun getConfiguration(context: EntityContext, provider: NlpEngineProvider): NlpApplicationConfiguration =
        getEntityModelHolder(context, provider)?.configuration
                ?: provider.modelBuilder.defaultNlpApplicationConfiguration()

    fun getEntityModelHolder(context: EntityContext, provider: NlpEngineProvider): EntityModelHolder? {
        return context
            .key()
            .let {
                entityModelsCache.get(it) { loadEntityModel(it, provider) }
            }
            .let { (nativeModel, lastUpdate, conf) ->
                if (nativeModel == null) null else EntityModelHolder(nativeModel, conf, lastUpdate)
            }
    }

    private fun loadEntityModel(contextKey: EntityContextKey, provider: NlpEngineProvider): ConfiguredModel {
        return modelDAO.getEntityModelInputStream(contextKey)
            ?.let { inputStream ->
                logger.debug { "load entity model for $contextKey" }
                val model = provider.modelIo.loadEntityModel(inputStream)
                return ConfiguredModel(
                    model,
                    inputStream.updatedDate,
                    provider.configuration(inputStream.configuration)
                )
            } ?: ConfiguredModel(null, now(), provider.configuration())
    }


    private fun saveModel(copy: (OutputStream) -> Unit, save: (InputStream) -> Unit) {
        val pipedOutputStream = PipedOutputStream()
        val pipedInputStream = PipedInputStream(pipedOutputStream)
        val latch = CountDownLatch(1)
        executor.executeBlocking {
            pipedOutputStream.use {
                try {
                    copy(it)
                } catch (t: Throwable) {
                    logger.error(t) { t.message }
                } finally {
                    logger.debug { "Copy model end" }
                }
            }
            latch.await(1, MINUTES)
            logger.debug { "latch release" }

        }
        pipedInputStream.use {
            try {
                logger.debug { "Start to save the model" }
                save(it)
            } catch (t: Throwable) {
                logger.error(t) { t.message }
            } finally {
                logger.debug { "Model saved" }
                latch.countDown()
            }
        }
    }

    fun saveIntentModel(
        intentContextKey: IntentContextKey,
        model: IntentModelHolder,
        modelIo: NlpEngineModelIo
    ) {
        saveModel(
            { modelIo.copyIntentModel(model.nativeModel, it) },
            { modelDAO.saveIntentModel(intentContextKey, it) }
        )
    }

    fun saveEntityModel(
        entityContextKey: EntityContextKey,
        model: EntityModelHolder,
        modelIo: NlpEngineModelIo
    ) {
        saveModel(
            { modelIo.copyEntityModel(model.nativeModel, it) },
            { modelDAO.saveEntityModel(entityContextKey, it) }
        )
    }

    fun isIntentModelExist(context: IntentContext): Boolean {
        return modelDAO.getIntentModelLastUpdate(context.key()) != null
    }

    fun isEntityModelExist(context: EntityBuildContext): Boolean {
        return modelDAO.getEntityModelLastUpdate(context.key()) != null
    }

    fun removeEntityModelsNotIn(keys: List<EntityContextKey>) {
        modelDAO.deleteEntityModelsNotIn(keys)
    }

    fun removeIntentModelsNotIn(keys: List<IntentContextKey>) {
        modelDAO.deleteIntentModelsNotIn(keys)
    }

}