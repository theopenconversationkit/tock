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

package fr.vsct.tock.nlp.build

import fr.vsct.tock.nlp.front.client.FrontClient
import fr.vsct.tock.nlp.front.shared.config.ClassifiedSentence
import fr.vsct.tock.nlp.front.shared.config.ClassifiedSentenceStatus.deleted
import fr.vsct.tock.nlp.front.shared.config.ClassifiedSentenceStatus.model
import fr.vsct.tock.nlp.front.shared.config.ClassifiedSentenceStatus.validated
import fr.vsct.tock.nlp.front.shared.config.SentencesQuery
import fr.vsct.tock.shared.error
import io.vertx.core.AbstractVerticle
import mu.KotlinLogging
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean

/**
 *
 */
class BuildModelWorkerVerticle : AbstractVerticle() {


    data class ModelRefreshKey(val applicationId: String, val language: Locale)

    companion object {
        private val logger = KotlinLogging.logger {}

        fun updateAllModels() {
            val front = FrontClient
            front.getApplications().forEach { app ->
                app.supportedLocales.forEach { locale ->
                    updateModel(
                            ModelRefreshKey(app._id!!, locale),
                            front.search(SentencesQuery(app._id as String, locale, 0, Integer.MAX_VALUE, status = setOf(model))).sentences)
                }
            }
        }

        private fun updateModel(key: ModelRefreshKey, sentences: List<ClassifiedSentence>) {
            val front = FrontClient
            try {
                val app = front.getApplicationById(key.applicationId)!!
                logger.info { "start model update for ${app.name} and ${key.language}" }
                logger.trace { "Sentences : ${sentences.map { it.text }}" }

                front.registeredNlpEngineTypes().forEach { engineType ->
                    front.updateIntentsModelForApplication(sentences, app, key.language, engineType)
                    sentences.groupBy { it.classification.intentId }.forEach { intentId, sentences ->
                        front.updateEntityModelForIntent(sentences, app, intentId, key.language, engineType)
                    }
                }

                logger.info { "Model updated for ${app.name} and ${key.language}" }
            } catch(e: Throwable) {
                logger.error(e)
            } finally {
                front.switchSentencesStatus(sentences, model)
            }
        }
    }

    override fun start() {
        val front = FrontClient
        val canAnalyse = AtomicBoolean(true)

        vertx.setPeriodic(1000, {
            if (canAnalyse.get()) {
                try {
                    canAnalyse.set(false)
                    val validatedSentences = front.getSentences(validated)
                    val deletedSentences = front.getSentences(deleted)
                    if (validatedSentences.isNotEmpty()) {
                        logger.debug { "Sentences to update : ${validatedSentences.map { it.text }}" }

                        val refreshKeyMap = validatedSentences.groupBy { ModelRefreshKey(it.applicationId, it.language) }
                        refreshKeyMap.forEach {
                            updateModel(it.key, it.value)
                        }
                        logger.info { "end model update" }
                    } else if (deletedSentences.isNotEmpty()) {
                        logger.debug { "Sentences to remove from model : ${deletedSentences.map { it.text }}" }

                        val refreshKeyMap = deletedSentences.map { ModelRefreshKey(it.applicationId, it.language) }
                        refreshKeyMap.forEach {
                            updateModel(it, emptyList())
                        }
                        front.deleteSentencesByStatus(deleted)
                        logger.info { "end model update" }
                    }
                } catch(e: Throwable) {
                    logger.error(e)
                } finally {
                    canAnalyse.set(true)
                }
            }
        })
    }


}