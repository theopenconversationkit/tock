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
import fr.vsct.tock.nlp.model.EntityBuildContext
import fr.vsct.tock.nlp.model.EntityCallContext
import fr.vsct.tock.nlp.model.EntityContextKey
import fr.vsct.tock.nlp.model.IntentContext
import fr.vsct.tock.nlp.model.IntentContext.IntentContextKey
import fr.vsct.tock.nlp.model.ModelNotInitializedException
import fr.vsct.tock.nlp.model.TokenizerContext
import fr.vsct.tock.nlp.model.service.storage.NlpEngineModelIO
import fr.vsct.tock.shared.Executor
import fr.vsct.tock.shared.booleanProperty
import fr.vsct.tock.shared.error
import fr.vsct.tock.shared.injector
import fr.vsct.tock.shared.longProperty
import mu.KotlinLogging
import java.io.InputStream
import java.io.OutputStream
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.time.Duration
import java.time.Instant
import java.time.Instant.now
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit.MINUTES


/**
 *
 */
internal object NlpModelRepository {

    private val logger = KotlinLogging.logger {}

    private data class TimeStampedModel(val nativeModel: Any?, val lastUpdate: Instant)

    private val modelIO: NlpEngineModelIO by injector.instance()

    private val executor: Executor by injector.instance()

    private val intentModelsCache: Cache<IntentContextKey, TimeStampedModel>
            = CacheBuilder.newBuilder().softValues().build()

    private val entityModelsCache: Cache<EntityContextKey, TimeStampedModel>
            = CacheBuilder.newBuilder().softValues().build()

    init {
        if (booleanProperty("tock_nlp_model_refresh", true)) {
            logger.info { "start refresh model thread" }
            //10 minutes by default
            executor.setPeriodic(Duration.ofSeconds(longProperty("tock_nlp_model_refresh_rate", 10 * 60))) {
                try {
                    logger.debug { "start refresh model process" }
                    intentModelsCache.asMap().forEach { key, value ->
                        logger.trace { "check intent model $key" }
                        if (modelIO.getIntentModelLastUpdate(key) != value.lastUpdate) {
                            logger.info { "refresh intent model for $key" }
                            intentModelsCache.put(key,
                                    loadIntentModel(
                                            key,
                                            NlpEngineRepository.getProvider(key.nlpEngineType))
                            )
                        }
                    }
                    entityModelsCache.asMap().forEach { key, value ->
                        logger.trace { "check entity model $key" }
                        if (modelIO.getEntityModelLastUpdate(key) != value.lastUpdate) {
                            logger.info { "refresh entity model for $key" }
                            entityModelsCache.put(key,
                                    loadEntityModel(
                                            key,
                                            NlpEngineRepository.getProvider(key.nlpEngineType))
                            )
                        }
                    }
                } catch (t: Throwable) {
                    logger.error(t)
                }
            }
        } else {
            logger.info { "refresh model is disabled" }
        }
    }

    fun getTokenizerModelHolder(context: TokenizerContext): TokenizerModelHolder {
        return TokenizerModelHolder(context.language)
    }

    fun getIntentModelHolder(context: IntentContext, provider: NlpEngineProvider): IntentModelHolder {
        return context
                .key()
                .let { key ->
                    intentModelsCache.get(key) {
                        loadIntentModel(key, provider)
                    }
                }
                .let { IntentModelHolder(context.application, it.nativeModel!!, it.lastUpdate) }
    }

    private fun loadIntentModel(
            contextKey: IntentContextKey,
            provider: NlpEngineProvider): TimeStampedModel {
        val inputStream = modelIO.getIntentModelInputStream(contextKey)
        if (inputStream != null) {
            logger.debug { "load intent model for $contextKey" }
            val model = provider.getModelIo().loadIntentModel(inputStream)
            return TimeStampedModel(model, inputStream.updatedDate)
        }

        throw ModelNotInitializedException("no intent model found for $contextKey")
    }

    fun getEntityModelHolder(context: EntityCallContext, provider: NlpEngineProvider): EntityModelHolder? {
        return context
                .key()
                .let {
                    entityModelsCache.get(it) { loadEntityModel(it, provider) }
                }
                .let { (nativeModel, lastUpdate) ->
                    if (nativeModel == null) null else EntityModelHolder(nativeModel, lastUpdate)
                }
    }

    private fun loadEntityModel(contextKey: EntityContextKey, provider: NlpEngineProvider): TimeStampedModel? {
        return modelIO.getEntityModelInputStream(contextKey)
                ?.let { inputStream ->
                    logger.debug { "load entity model for $contextKey" }
                    val model = provider.getModelIo().loadEntityModel(inputStream)
                    return TimeStampedModel(model, inputStream.updatedDate)
                } ?: TimeStampedModel(null, now())
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

    fun saveIntentModel(intentContextKey: IntentContextKey,
                        model: IntentModelHolder,
                        modelIo: NlpEngineModelIo) {
        saveModel(
                { modelIo.copyIntentModel(model.nativeModel, it) },
                { modelIO.saveIntentModel(intentContextKey, it) }
        )
    }

    fun saveEntityModel(entityContextKey: EntityContextKey,
                        model: EntityModelHolder,
                        modelIo: NlpEngineModelIo) {
        saveModel(
                { modelIo.copyEntityModel(model.nativeModel, it) },
                { modelIO.saveEntityModel(entityContextKey, it) }
        )
    }

    fun isIntentModelExist(context: IntentContext): Boolean {
        return modelIO.getIntentModelLastUpdate(context.key()) != null
    }

    fun isEntityModelExist(context: EntityBuildContext): Boolean {
        return modelIO.getEntityModelLastUpdate(context.key()) != null
    }

    fun removeEntityModelsNotIn(keys: List<EntityContextKey>) {
        modelIO.removeEntityModelsNotIn(keys)
    }

    fun removeIntentModelsNotIn(keys: List<IntentContextKey>) {
        modelIO.removeIntentModelsNotIn(keys)
    }

}