/*
 * Copyright (C) 2017/2019 e-voyageurs technologies
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

package ai.tock.nlp.build

import com.github.salomonbrys.kodein.instance
import ai.tock.nlp.front.client.FrontClient
import ai.tock.nlp.front.shared.build.ModelBuildTrigger
import ai.tock.nlp.front.shared.config.ApplicationDefinition
import ai.tock.nlp.front.shared.config.ClassifiedSentence
import ai.tock.nlp.front.shared.config.ClassifiedSentenceStatus.deleted
import ai.tock.nlp.front.shared.config.ClassifiedSentenceStatus.model
import ai.tock.nlp.front.shared.config.ClassifiedSentenceStatus.validated
import ai.tock.shared.Executor
import ai.tock.shared.booleanProperty
import ai.tock.shared.error
import ai.tock.shared.injector
import ai.tock.shared.listProperty
import ai.tock.shared.namespace
import io.vertx.core.AbstractVerticle
import mu.KotlinLogging
import org.litote.kmongo.Id
import java.time.Duration.ofHours
import java.time.Duration.ofSeconds
import java.time.LocalTime
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean

/**
 *
 */
class BuildModelWorkerVerticle : AbstractVerticle() {

    data class ModelRefreshKey(val applicationId: Id<ApplicationDefinition>, val language: Locale)

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
            logger.info { "Rebuild all models for application ${app.name} and nlp engine ${app.nlpEngineType.name}" }
            app.supportedLocales.forEach { locale ->
                front.updateIntentsModelForApplication(emptyList(), app, locale, app.nlpEngineType, onlyIfNotExists)
                app.intents.forEach { intentId ->
                    logger.info { "start model update for ${app.name}, intent ${intentId} and $locale" }
                    front.updateEntityModelForIntent(
                        emptyList(),
                        app,
                        intentId,
                        locale,
                        app.nlpEngineType,
                        onlyIfNotExists
                    )
                }
                updateEntityModels(app, locale, emptyList(), onlyIfNotExists)
            }
        }

        private fun updateModel(
            key: ModelRefreshKey,
            sentences: List<ClassifiedSentence>,
            onlyIfNotExists: Boolean = false
        ) {
            try {
                val app = front.getApplicationById(key.applicationId)
                if (app == null) {
                    logger.warn { "Unknown application : $key" }
                    return
                }
                logger.info { "Start model update for ${app.name} and ${key.language}" }
                logger.trace { "New sentences : ${sentences.map { it.text }}" }

                front.updateIntentsModelForApplication(sentences, app, key.language, app.nlpEngineType, onlyIfNotExists)
                sentences.groupBy { it.classification.intentId }.forEach { intentId, intentSentences ->
                    logger.info { "start model update for ${app.name}, intent ${intentId} and ${key.language}" }
                    front.updateEntityModelForIntent(
                        intentSentences,
                        app,
                        intentId,
                        key.language,
                        app.nlpEngineType,
                        onlyIfNotExists
                    )
                }

                updateEntityModels(app, key.language, sentences, onlyIfNotExists)

                logger.info { "Model updated for ${app.name} and ${key.language}" }
            } catch (e: Throwable) {
                logger.error(e)
            } finally {
                logger.info { "end model update for ${key.language}" }
                front.switchSentencesStatus(sentences, model)
            }
        }

        private fun updateEntityModels(
            app: ApplicationDefinition,
            locale: Locale,
            sentences: List<ClassifiedSentence>,
            onlyIfNotExists: Boolean = false
        ) {
            front.getEntityTypes()
                .filter { it.subEntities.isNotEmpty() }
                .filter { it.name.namespace() == app.namespace }
                .forEach { entityType ->
                    val sentencesWithEntity = sentences.filter { s -> s.classification.containsEntityOrSubEntity(entityType.name) }
                    if (sentencesWithEntity.isNotEmpty()) {
                        logger.info { "start model update for ${app.name}, entity type $entityType and $locale" }
                        front.updateEntityModelForEntityType(
                            sentencesWithEntity,
                            app,
                            entityType,
                            locale,
                            app.nlpEngineType,
                            onlyIfNotExists
                        )
                    }
                }

        }
    }

    private val executor: Executor by injector.instance()

    internal val canAnalyse = AtomicBoolean(true)

    override fun start() {
        executor.setPeriodic(ofSeconds(1)) {
            if (canAnalyse.get()) {
                try {
                    canAnalyse.set(false)
                    val validatedSentences = front.getSentences(status = validated)
                    val deletedSentences = front.getSentences(status = deleted)
                    if (validatedSentences.isNotEmpty()) {
                        logger.debug { "Sentences to update : ${validatedSentences.map { it.text }}" }

                        val refreshKeyMap =
                            validatedSentences.groupBy { ModelRefreshKey(it.applicationId, it.language) }
                        logger.info { "Model refresh keys : ${refreshKeyMap.keys}" }
                        refreshKeyMap.forEach {
                            updateModel(it.key, it.value)
                        }
                        logger.info { "end model update" }
                    } else if (deletedSentences.isNotEmpty()) {
                        logger.debug { "Sentences to remove from model : ${deletedSentences.map { it.text }}" }

                        val refreshKeyMap =
                            deletedSentences.map { ModelRefreshKey(it.applicationId, it.language) }.distinct()
                        logger.info { "Model refresh keys : $refreshKeyMap" }
                        refreshKeyMap.forEach {
                            updateModel(it, emptyList())
                        }
                        front.deleteSentencesByStatus(deleted)
                        logger.info { "end model update" }
                    } else {
                        val triggers = front.getTriggers()
                        if (triggers.isNotEmpty()) {
                            triggers[0].let { trigger ->
                                logger.info { "use trigger $trigger" }
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
                                            && minute % 1 == 0
                                    }
                            ) {
                                logger.info { "Start testing models" }
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
        }

        if (completeModelEnabled) {
            executor.setPeriodic(ofHours(1)) {
                logger.info { "trigger build to check not existing models" }
                front.getApplications().forEach {
                    front.triggerBuild(ModelBuildTrigger(it._id, true, true))
                }
            }
        }
    }


}