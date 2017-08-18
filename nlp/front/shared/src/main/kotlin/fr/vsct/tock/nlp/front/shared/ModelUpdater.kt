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

package fr.vsct.tock.nlp.front.shared

import fr.vsct.tock.nlp.core.NlpEngineType
import fr.vsct.tock.nlp.front.shared.config.ApplicationDefinition
import fr.vsct.tock.nlp.front.shared.config.ClassifiedSentence
import fr.vsct.tock.nlp.front.shared.updater.ModelBuildTrigger
import java.util.Locale

/**
 * Manage updates of NLP models (both intents and entity models).
 */
interface ModelUpdater {

    /**
     * Update intents model.
     */
    fun updateIntentsModelForApplication(
            validatedSentences: List<ClassifiedSentence>,
            application: ApplicationDefinition,
            language: Locale,
            engineType: NlpEngineType,
            onlyIfNotExists:Boolean = false)

    /**
     * Update all entities model of intent.
     */
    fun updateEntityModelForIntent(
            validatedSentences: List<ClassifiedSentence>,
            application: ApplicationDefinition,
            intentId: String,
            language: Locale,
            engineType: NlpEngineType,
            onlyIfNotExists:Boolean = false)

    /**
     * Delete orphans intent and entity models.
     */
    fun deleteOrphans()

    /**
     * Get all available triggers.
     */
    fun getTriggers(): List<ModelBuildTrigger>

    /**
     * Save the trigger.
     */
    fun triggerBuild(trigger: ModelBuildTrigger)

    /**
     * Delete the trigger.
     */
    fun deleteTrigger(trigger: ModelBuildTrigger)
}