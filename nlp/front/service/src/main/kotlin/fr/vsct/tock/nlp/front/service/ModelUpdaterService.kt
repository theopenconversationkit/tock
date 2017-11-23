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

package fr.vsct.tock.nlp.front.service

import com.github.salomonbrys.kodein.instance
import fr.vsct.tock.nlp.core.BuildContext
import fr.vsct.tock.nlp.core.Intent
import fr.vsct.tock.nlp.core.ModelCore
import fr.vsct.tock.nlp.core.NlpEngineType
import fr.vsct.tock.nlp.front.service.FrontRepository.entityTypeByName
import fr.vsct.tock.nlp.front.service.FrontRepository.toApplication
import fr.vsct.tock.nlp.front.service.storage.ModelBuildTriggerDAO
import fr.vsct.tock.nlp.front.shared.ModelUpdater
import fr.vsct.tock.nlp.front.shared.build.ModelBuild
import fr.vsct.tock.nlp.front.shared.build.ModelBuildTrigger
import fr.vsct.tock.nlp.front.shared.build.ModelBuildType
import fr.vsct.tock.nlp.front.shared.config.ApplicationDefinition
import fr.vsct.tock.nlp.front.shared.config.ClassifiedSentence
import fr.vsct.tock.nlp.front.shared.config.ClassifiedSentenceStatus
import fr.vsct.tock.nlp.front.shared.config.EntityTypeDefinition
import fr.vsct.tock.nlp.front.shared.config.IntentDefinition
import fr.vsct.tock.nlp.front.shared.config.SentencesQuery
import fr.vsct.tock.shared.error
import fr.vsct.tock.shared.injector
import fr.vsct.tock.shared.provide
import mu.KotlinLogging
import org.litote.kmongo.Id
import java.time.Duration
import java.time.Instant
import java.util.Locale

val triggerDAO: ModelBuildTriggerDAO by injector.instance()

/**
 *
 */
object ModelUpdaterService : ModelUpdater, ModelBuildTriggerDAO by triggerDAO {

    private val logger = KotlinLogging.logger {}

    private val config = ApplicationConfigurationService
    val model: ModelCore get() = injector.provide()

    override fun triggerBuild(trigger: ModelBuildTrigger) {
        save(trigger)
    }

    private fun logBuild(
            application: ApplicationDefinition,
            language: Locale,
            type: ModelBuildType,
            intentId: Id<IntentDefinition>?,
            entityTypeName: String?,
            builder: () -> Int) {
        var build = ModelBuild(application._id, language, type, intentId, entityTypeName, 0, Duration.ZERO, false, null, Instant.now())
        try {
            build = build.copy(nbSentences = builder.invoke())
        } catch (e: Throwable) {
            logger.error(e)
            build = build.copy(error = true, errorMessage = e.message)
        } finally {
            triggerDAO.save(build.copy(duration = Duration.between(build.date, Instant.now())))
        }
    }

    override fun updateIntentsModelForApplication(
            validatedSentences: List<ClassifiedSentence>,
            application: ApplicationDefinition,
            language: Locale,
            engineType: NlpEngineType,
            onlyIfNotExists: Boolean) {
        logBuild(application, language, ModelBuildType.intent, null, null) {
            val intentCache = mutableMapOf<Id<IntentDefinition>, Intent>()
            val modelSentences = config.getSentences(application.intents, language, ClassifiedSentenceStatus.model)
            val samples = (modelSentences + validatedSentences).map { it.toSampleExpression({ config.toIntent(it, intentCache) }, { entityTypeByName(it) }) }
            model.updateIntentModel(BuildContext(toApplication(application), language, engineType, onlyIfNotExists), samples)
            samples.size
        }
    }

    override fun updateEntityModelForIntent(
            validatedSentences: List<ClassifiedSentence>,
            application: ApplicationDefinition,
            intentId: Id<IntentDefinition>,
            language: Locale,
            engineType: NlpEngineType,
            onlyIfNotExists: Boolean) {
        logBuild(application, language, ModelBuildType.intentEntities, intentId, null) {
            val i = config.toIntent(intentId)
            val modelSentences = config.getSentences(setOf(intentId), language, ClassifiedSentenceStatus.model)
            val samples = (modelSentences + validatedSentences).map {
                it.toSampleExpression({ i }, { entityTypeByName(it) })
            }
            model.updateEntityModelForIntent(BuildContext(toApplication(application), language, engineType, onlyIfNotExists), i, samples)
            samples.size
        }
    }

    override fun updateEntityModelForEntityType(
            validatedSentences: List<ClassifiedSentence>,
            application: ApplicationDefinition,
            entityTypeDefinition: EntityTypeDefinition,
            language: Locale,
            engineType: NlpEngineType,
            onlyIfNotExists: Boolean) {
        val entityType = entityTypeByName(entityTypeDefinition.name)
        if (entityType != null) {
            logBuild(application, language, ModelBuildType.intentEntities, null, entityTypeDefinition.name) {
                val modelSentences = config.search(
                        SentencesQuery(
                                application._id,
                                language,
                                size = Integer.MAX_VALUE,
                                status = setOf(ClassifiedSentenceStatus.model),
                                entityType = entityTypeDefinition.name)
                )
                val samples = (modelSentences.sentences + validatedSentences).map {
                    it.toSampleExpression({ config.toIntent(it) }, { entityType })
                }
                model.updateEntityModelForEntityType(
                        BuildContext(toApplication(application), language, engineType, onlyIfNotExists),
                        entityType,
                        samples)

                samples.size
            }
        }
    }

    override fun deleteOrphans() {
        model.deleteOrphans(
                config.getApplications()
                        .map {
                            toApplication(it) to config.getIntentsByApplicationId(it._id).map { config.toIntent(it) }.toSet()
                        }
                        .toMap(),
                config.getEntityTypes().map { FrontRepository.toEntityType(it) }
        )
    }


}