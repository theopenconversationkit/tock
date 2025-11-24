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

package ai.tock.nlp.admin.model

import ai.tock.nlp.core.NlpEngineType
import ai.tock.nlp.front.shared.config.ApplicationDefinition
import ai.tock.nlp.front.shared.config.IntentDefinition
import org.litote.kmongo.Id
import org.litote.kmongo.newId
import java.util.Locale

/**
 * Application definition with intents.
 */
data class ApplicationWithIntents(
    /**
     * The name of the application.
     */
    val name: String,
    /**
     * The label of the app.
     */
    val label: String = name,
    /**
     * The namespace of the application.
     */
    val namespace: String,
    /**
     * The intent definitions of the application.
     */
    val intents: List<IntentDefinition>,
    /**
     * The locales supported by the application.
     */
    val supportedLocales: Set<Locale>,
    /**
     * The current nlp engine used to build the model.
     */
    val nlpEngineType: NlpEngineType = NlpEngineType(),
    /**
     * Is intent entity model and "standalone" entity models are used to find the better values ?
     */
    val mergeEngineTypes: Boolean = true,
    /**
     * Is "standalone" entity models used? Useful for entity disambiguation.
     */
    val useEntityModels: Boolean = true,
    /**
     * Does this app support sub entities ?
     */
    val supportSubEntities: Boolean = false,
    /**
     * Unknown intent threshold level.
     */
    val unknownIntentThreshold: Double = 0.7,
    /**
     * Known intent threshold level.
     */
    val knownIntentThreshold: Double = 0.1,
    /**
     * Normalized text model - sentences are persisted with normalizedText.
     */
    val normalizeText: Boolean = false,
    /**
     * The shared intent definitions for other namespaces.
     */
    val namespaceIntents: List<IntentDefinition> = emptyList(),
    /**
     * The id of the app.
     */
    val _id: Id<ApplicationDefinition>?,
) {
    constructor(application: ApplicationDefinition, intents: List<IntentDefinition>, namespacesIntents: List<IntentDefinition>) :
        this(
            application.name,
            application.label,
            application.namespace,
            intents.sortedWith(compareBy({ it.label }, { it.name })),
            application.supportedLocales,
            application.nlpEngineType,
            application.mergeEngineTypes,
            application.useEntityModels,
            application.supportSubEntities,
            application.unknownIntentThreshold,
            application.knownIntentThreshold,
            application.normalizeText,
            namespacesIntents.sortedWith(compareBy({ it.label }, { it.name })),
            application._id,
        )

    fun toApplication(): ApplicationDefinition {
        return ApplicationDefinition(
            name,
            label,
            namespace,
            intents.map { it._id }.toSet(),
            supportedLocales,
            emptyMap(),
            nlpEngineType,
            mergeEngineTypes,
            useEntityModels,
            supportSubEntities,
            unknownIntentThreshold,
            knownIntentThreshold,
            normalizeText,
            _id ?: newId(),
        )
    }
}
