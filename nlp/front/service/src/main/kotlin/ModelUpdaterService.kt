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

package ai.tock.nlp.front.service

import ai.tock.nlp.core.BuildContext
import ai.tock.nlp.core.Intent
import ai.tock.nlp.core.ModelCore
import ai.tock.nlp.core.NlpEngineType
import ai.tock.nlp.front.service.ConfigurationRepository.entityTypeByName
import ai.tock.nlp.front.service.ConfigurationRepository.toApplication
import ai.tock.nlp.front.service.storage.ModelBuildTriggerDAO
import ai.tock.nlp.front.shared.ModelUpdater
import ai.tock.nlp.front.shared.build.ModelBuild
import ai.tock.nlp.front.shared.build.ModelBuildTrigger
import ai.tock.nlp.front.shared.build.ModelBuildType
import ai.tock.nlp.front.shared.config.ApplicationDefinition
import ai.tock.nlp.front.shared.config.ClassifiedSentence
import ai.tock.nlp.front.shared.config.ClassifiedSentenceStatus
import ai.tock.nlp.front.shared.config.EntityTypeDefinition
import ai.tock.nlp.front.shared.config.IntentDefinition
import ai.tock.nlp.front.shared.config.SentencesQuery
import ai.tock.shared.error
import ai.tock.shared.injector
import ai.tock.shared.provide
import com.github.salomonbrys.kodein.instance
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
        builder: () -> Int,
    ) {
        var build =
            ModelBuild(
                application._id,
                language,
                type,
                intentId,
                entityTypeName,
                0,
                Duration.ZERO,
                false,
                null,
                Instant.now(),
            )
        try {
            build = build.copy(nbSentences = builder.invoke())
        } catch (e: Throwable) {
            logger.error(e)
            build = build.copy(error = true, errorMessage = e.message)
        } finally {
            try {
                if (build.error || build.nbSentences != 0) {
                    val buildToSave = build.copy(duration = Duration.between(build.date, Instant.now()))
                    logger.info { "build saved: $buildToSave" }
                    triggerDAO.save(buildToSave)
                } else {
                    logger.info { "do not save build - no sentence included: $build" }
                }
            } catch (e: Exception) {
                logger.error(e)
            }
        }
    }

    override fun updateIntentsModelForApplication(
        validatedSentences: List<ClassifiedSentence>,
        application: ApplicationDefinition,
        language: Locale,
        engineType: NlpEngineType,
        onlyIfNotExists: Boolean,
    ) {
        logBuild(application, language, ModelBuildType.intent, null, null) {
            val intentCache = mutableMapOf<Id<IntentDefinition>, Intent>()
            val modelSentences = config.getSentencesForModel(application, language)
            val samples =
                (modelSentences + validatedSentences).map { s ->
                    s.toSampleExpression({ config.toIntent(it, intentCache) }, { entityTypeByName(it) })
                }
            model.updateIntentModel(
                BuildContext(toApplication(application), language, engineType, onlyIfNotExists),
                samples,
            )
            samples.size
        }
    }

    override fun updateEntityModelForIntent(
        validatedSentences: List<ClassifiedSentence>,
        application: ApplicationDefinition,
        intentId: Id<IntentDefinition>,
        language: Locale,
        engineType: NlpEngineType,
        onlyIfNotExists: Boolean,
    ) {
        logBuild(application, language, ModelBuildType.intentEntities, intentId, null) {
            val i = config.toIntent(intentId)
            val modelSentences = config.getSentences(setOf(intentId), language, ClassifiedSentenceStatus.model)
            val sharedSentences = getSharedIntentSentences(application, language, intentId)
            val samples =
                (modelSentences + validatedSentences + sharedSentences)
                    .map { sentence ->
                        sentence.toSampleExpression({ i }, { entityTypeByName(it) })
                    }
            model.updateEntityModelForIntent(
                BuildContext(
                    toApplication(application),
                    language,
                    engineType,
                    onlyIfNotExists,
                ),
                i,
                samples,
            )
            samples.size
        }
    }

    private fun getSharedIntentSentences(
        application: ApplicationDefinition,
        language: Locale,
        intentId: Id<IntentDefinition>,
    ): List<ClassifiedSentence> {
        val intentDefinition = config.getIntentById(intentId)
        return intentDefinition?.sharedIntents?.flatMap { sharedIntentId ->
            config
                .search(
                    SentencesQuery(
                        application._id,
                        language,
                        size = Integer.MAX_VALUE,
                        intentId = sharedIntentId,
                        status = setOf(ClassifiedSentenceStatus.model),
                    ),
                )
                .sentences
                .filter { s -> s.classification.entities.all { intentDefinition.hasEntity(it) } }
        } ?: emptyList()
    }

    override fun updateEntityModelForEntityType(
        validatedSentences: List<ClassifiedSentence>,
        application: ApplicationDefinition,
        entityTypeDefinition: EntityTypeDefinition,
        language: Locale,
        engineType: NlpEngineType,
        onlyIfNotExists: Boolean,
    ) {
        val entityType = entityTypeByName(entityTypeDefinition.name)
        if (entityType != null) {
            logBuild(application, language, ModelBuildType.entityTypeEntities, null, entityTypeDefinition.name) {
                val modelSentences =
                    config.search(
                        SentencesQuery(
                            application._id,
                            language,
                            size = Integer.MAX_VALUE,
                            status = setOf(ClassifiedSentenceStatus.model),
                            entityType = entityTypeDefinition.name,
                            searchSubEntities = true,
                            wholeNamespace = true,
                        ),
                    )
                val samples =
                    (modelSentences.sentences + validatedSentences).map { s ->
                        s.toSampleExpression({ config.toIntent(it) }, { entityTypeByName(it) })
                    }
                model.updateEntityModelForEntityType(
                    BuildContext(toApplication(application), language, engineType, onlyIfNotExists),
                    entityType,
                    samples,
                )

                samples.size
            }
        }
    }

    override fun deleteOrphans() {
        model.deleteOrphans(
            config.getApplications().associate {
                toApplication(it) to config.getIntentsByApplicationId(it._id).map { i -> config.toIntent(i) }.toSet()
            },
            config.getEntityTypes().mapNotNull { ConfigurationRepository.toEntityType(it) },
        )
    }
}
