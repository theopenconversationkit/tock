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

import com.github.salomonbrys.kodein.instance
import fr.vsct.tock.nlp.front.client.FrontClient
import fr.vsct.tock.nlp.front.shared.build.ModelBuildTrigger
import fr.vsct.tock.nlp.front.shared.config.ApplicationDefinition
import fr.vsct.tock.nlp.front.shared.config.ClassifiedSentence
import fr.vsct.tock.nlp.front.shared.config.ClassifiedSentenceStatus.deleted
import fr.vsct.tock.nlp.front.shared.config.ClassifiedSentenceStatus.model
import fr.vsct.tock.nlp.front.shared.config.ClassifiedSentenceStatus.validated
import fr.vsct.tock.nlp.front.shared.config.SentencesQuery
import fr.vsct.tock.shared.Executor
import fr.vsct.tock.shared.booleanProperty
import fr.vsct.tock.shared.error
import fr.vsct.tock.shared.injector
import fr.vsct.tock.shared.listProperty
import io.vertx.core.AbstractVerticle
import mu.KotlinLogging
import java.time.Duration.ofHours
import java.time.Duration.ofSeconds
import java.time.LocalTime
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean

/**
 *
 */
class BuildModelWorkerVerticle : AbstractVerticle() {

    data class ModelRefreshKey(val applicationId: String, val language: Locale)

    companion object {
        private val logger = KotlinLogging.logger {}
        private val completeModelEnabled = booleanProperty("tock_complete_model_enabled", true)
        private val testModelEnabled = booleanProperty("tock_test_model_enabled", true)
        private val testModelTimeframe =
                listProperty("tock_test_model_timeframe", listOf("0", "5"))
                        .let { t ->
                            logger.info { "test timeframe: $t" }
                            listOf(t[0].toInt(), t[1].toInt())
                        }

        val front = FrontClient

        fun updateAllModels() {
            FrontClient.getApplications().forEach { updateApplicationModels(it) }
        }

        private fun updateApplicationModels(app: ApplicationDefinition, onlyIfNotExists: Boolean = false) {
            logger.debug { "Rebuild all models for application ${app.name} and nlp engine ${app.nlpEngineType.name}" }
            app.supportedLocales.forEach { locale ->
                updateModel(
                        ModelRefreshKey(app._id!!, locale),
                        FrontClient.search(SentencesQuery(app._id as String, locale, 0, Integer.MAX_VALUE, status = setOf(model))).sentences,
                        onlyIfNotExists)
            }
        }

        private fun updateModel(key: ModelRefreshKey, sentences: List<ClassifiedSentence>, onlyIfNotExists: Boolean = false) {
            try {
                val app = front.getApplicationById(key.applicationId)!!
                logger.info { "start model update for ${app.name} and ${key.language}" }
                logger.trace { "Sentences : ${sentences.map { it.text }}" }

                front.updateIntentsModelForApplication(sentences, app, key.language, app.nlpEngineType, onlyIfNotExists)
                sentences.groupBy { it.classification.intentId }.forEach { intentId, intentSentences ->
                    front.updateEntityModelForIntent(intentSentences, app, intentId, key.language, app.nlpEngineType, onlyIfNotExists)
                }


                front.getEntityTypes()
                        .filter { it.subEntities.isNotEmpty() }
                        .forEach { entityType ->
                            front.updateEntityModelForEntityType(
                                    sentences.filter { it.classification.entities.any { it.type == entityType.name } },
                                    app,
                                    entityType,
                                    key.language,
                                    app.nlpEngineType,
                                    onlyIfNotExists
                            )
                        }

                logger.info { "Model updated for ${app.name} and ${key.language}" }
            } catch (e: Throwable) {
                logger.error(e)
            } finally {
                front.switchSentencesStatus(sentences, model)
            }
        }
    }

    private val executor: Executor by injector.instance()

    override fun start() {
        val canAnalyse = AtomicBoolean(true)

        executor.setPeriodic(ofSeconds(1), {
            if (canAnalyse.get()) {
                try {
                    canAnalyse.set(false)
                    val validatedSentences = front.getSentences(status = validated)
                    val deletedSentences = front.getSentences(status = deleted)
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
                    } else {
                        val triggers = front.getTriggers()
                        if (triggers.isNotEmpty()) {
                            triggers.forEach { trigger ->
                                front.deleteTrigger(trigger)
                                front.getApplicationById(trigger.applicationId)?.let {
                                    updateApplicationModels(it, trigger.onlyIfModelNotExists)
                                } ?: logger.warn {
                                    "unknown application id trigger ${trigger} - skipped"
                                }
                            }

                        } else {
                            //test model each 10 minutes if it's in the time frame
                            if (testModelEnabled
                                    && LocalTime.now()
                                    .run {
                                        hour >= testModelTimeframe[0]
                                                && hour <= testModelTimeframe[1]
                                                && minute % 10 == 0
                                    }) {
                                front.testModels()
                            } else {
                                logger.trace { "nothing to do - skip" }
                            }
                        }
                    }
                } catch (e: Throwable) {
                    logger.error(e)
                } finally {
                    canAnalyse.set(true)
                }
            }
        })

        if (completeModelEnabled) {
            executor.setPeriodic(ofHours(1), {
                logger.debug { "trigger build to check not existing models" }
                front.getApplications().forEach {
                    front.triggerBuild(ModelBuildTrigger(it._id!!, true, true))
                }
            })
        }
    }


}