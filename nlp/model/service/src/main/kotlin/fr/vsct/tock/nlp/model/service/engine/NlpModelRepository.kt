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
import fr.vsct.tock.nlp.model.EntityBuildContext
import fr.vsct.tock.nlp.model.EntityCallContext
import fr.vsct.tock.nlp.model.EntityContextKey
import fr.vsct.tock.nlp.model.IntentContext
import fr.vsct.tock.nlp.model.IntentContext.IntentContextKey
import fr.vsct.tock.nlp.model.TokenizerContext
import fr.vsct.tock.nlp.model.service.storage.NlpEngineModelIO
import fr.vsct.tock.shared.Runner
import fr.vsct.tock.shared.injector
import mu.KotlinLogging
import java.io.InputStream
import java.io.OutputStream
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit


/**
 *
 */
object NlpModelRepository {

    private val logger = KotlinLogging.logger {}

    private val intentModelsCache = ConcurrentHashMap<IntentContextKey, IntentModelHolder>()
    private val entityModelsCache = ConcurrentHashMap<EntityContextKey, EntityModelHolder>()

    private val modelIO: NlpEngineModelIO by injector.instance()
    private val runner: Runner by injector.instance()

    fun getTokenizerModelHodler(context: TokenizerContext): TokenizerModelHolder {
        return TokenizerModelHolder(context.language)
    }

    fun getIntentModelHodler(context: IntentContext, provider: NlpEngineProvider): IntentModelHolder {
        return intentModelsCache[context.key()] ?: loadIntentModel(context, provider)
    }

    private fun loadIntentModel(context: IntentContext, provider: NlpEngineProvider): IntentModelHolder {
        val inputStream = modelIO.getIntentModelInputStream(context)
        if (inputStream != null) {
            logger.debug { "load intent model for $context" }
            val model = provider.getModelIo().loadIntentModel(inputStream)
            return IntentModelHolder(context.application, model, inputStream.updatedDate)
        }

        error("no intent model found for ${context}")
    }

    fun getEntityModelHodler(context: EntityCallContext, provider: NlpEngineProvider): EntityModelHolder? {
        return entityModelsCache[context.key()] ?: loadEntityModel(context, provider)
    }

    private fun loadEntityModel(context: EntityCallContext, provider: NlpEngineProvider): EntityModelHolder? {
        val inputStream = modelIO.getEntityModelInputStream(context)
        if (inputStream != null) {
            logger.debug { "load entity model for $context" }
            val model = provider.getModelIo().loadEntityModel(inputStream)
            return EntityModelHolder(model, inputStream.updatedDate)
        }

        return null
    }

    private fun saveModel(copy: (OutputStream) -> Unit, save: (InputStream) -> Unit) {
        val pipedOutputStream = PipedOutputStream()
        val pipedInputStream = PipedInputStream(pipedOutputStream)
        val latch = CountDownLatch(1)
        runner.executeBlocking {
            pipedOutputStream.use {
                try {
                    copy(it)
                } catch(t: Throwable) {
                    logger.error(t) { t.message }
                } finally {
                    logger.debug { "Copy model end" }
                }
            }
            latch.await(1, TimeUnit.MINUTES)
            logger.debug { "latch release" }

        }
        pipedInputStream.use {
            try {
                logger.debug { "Start to save the model" }
                save(it)
            } catch(t: Throwable) {
                logger.error(t) { t.message }
            } finally {
                logger.debug { "Model saved" }
                latch.countDown()
            }
        }
    }

    fun saveIntentModel(intentContext: IntentContext,
                        model: IntentModelHolder,
                        modelIo: NlpEngineModelIo) {
        saveModel(
                { modelIo.copyIntentModel(model.nativeModel, it) },
                { modelIO.saveIntentModel(intentContext, it) }
        )
    }

    fun saveEntityModel(entityContext: EntityBuildContext,
                        model: EntityModelHolder,
                        modelIo: NlpEngineModelIo) {
        saveModel(
                { modelIo.copyEntityModel(model.nativeModel, it) },
                { modelIO.saveEntityModel(entityContext, it) }
        )
    }
}