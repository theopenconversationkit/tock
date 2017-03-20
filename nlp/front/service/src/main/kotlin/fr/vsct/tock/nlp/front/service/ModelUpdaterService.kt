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

import fr.vsct.tock.nlp.core.BuildContext
import fr.vsct.tock.nlp.core.Entity
import fr.vsct.tock.nlp.core.Intent
import fr.vsct.tock.nlp.core.NlpEngineType
import fr.vsct.tock.nlp.core.sample.SampleContext
import fr.vsct.tock.nlp.core.sample.SampleEntity
import fr.vsct.tock.nlp.core.sample.SampleExpression
import fr.vsct.tock.nlp.front.service.FrontRepository.core
import fr.vsct.tock.nlp.front.service.FrontRepository.entityTypes
import fr.vsct.tock.nlp.front.service.FrontRepository.toApplication
import fr.vsct.tock.nlp.front.shared.ModelUpdater
import fr.vsct.tock.nlp.front.shared.config.ApplicationDefinition
import fr.vsct.tock.nlp.front.shared.config.ClassifiedSentence
import fr.vsct.tock.nlp.front.shared.config.ClassifiedSentenceStatus
import fr.vsct.tock.nlp.front.shared.config.IntentDefinition
import java.util.Locale

/**
 *
 */
object ModelUpdaterService : ModelUpdater {

    private val config = ApplicationConfigurationService

    override fun updateIntentsModelForApplication(
            validatedSentences: List<ClassifiedSentence>,
            application: ApplicationDefinition,
            language: Locale,
            engineType: NlpEngineType) {
        val modelSentences = config.getSentences(application.intents, language, ClassifiedSentenceStatus.model)
        val samples = (modelSentences + validatedSentences).map { SampleExpression(it.text, toIntent(it.classification.intentId), it.classification.entities.map { SampleEntity(Entity(entityTypes.getValue(it.type), it.role), it.start, it.end) }, SampleContext(language)) }
        core.updateIntentModel(BuildContext(toApplication(application), language, engineType), samples)
    }

    override fun updateEntityModelForIntent(
            validatedSentences: List<ClassifiedSentence>,
            application: ApplicationDefinition,
            intentId: String,
            language: Locale,
            engineType: NlpEngineType) {
        val i = toIntent(intentId)
        val modelSentences = config.getSentences(setOf(intentId), language, ClassifiedSentenceStatus.model)
        val samples = (modelSentences + validatedSentences).map {
            SampleExpression(it.text, i, it.classification.entities.map { SampleEntity(Entity(entityTypes.getValue(it.type), it.role), it.start, it.end) }, SampleContext(language))
        }
        core.updateEntityModelForIntent(BuildContext(toApplication(application), language, engineType), i, samples)
    }

    override fun registeredNlpEngineTypes(): Set<NlpEngineType> {
        return core.registeredNlpEngineTypes()
    }

    private fun toIntent(intentId: String): Intent {
        return config.getIntentById(intentId)?.let {
            toIntent(it)
        } ?: Intent(Intent.Companion.unknownIntent, emptyList())
    }

    private fun toIntent(intent: IntentDefinition): Intent {
        return Intent(
                intent.name,
                intent.entities.map { Entity(entityTypes.getValue(it.entityTypeName), it.role) },
                intent.entitiesRegexp)
    }
}