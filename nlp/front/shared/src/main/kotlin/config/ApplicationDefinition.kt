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

package ai.tock.nlp.front.shared.config

import ai.tock.nlp.core.NlpEngineType
import ai.tock.shared.withNamespace
import org.litote.kmongo.Id
import org.litote.kmongo.newId
import java.util.Locale

/**
 * An NLP application definition.
 */
data class ApplicationDefinition(
    /**
     * The name of the app.
     */
    val name: String,
    /**
     * The label of the app.
     */
    val label: String = name,
    /**
     * The namespace of the app.
     */
    val namespace: String,
    /**
     * The intents id of the app.
     */
    val intents: Set<Id<IntentDefinition>> = emptySet(),
    /**
     * The locales supported by the app.
     */
    val supportedLocales: Set<Locale> = emptySet(),
    /**
     * The states defined for each intent.
     */
    val intentStatesMap: Map<Id<IntentDefinition>, Set<String>> = emptyMap(),
    /**
     * The current nlp engine used to build the model.
     */
    val nlpEngineType: NlpEngineType = NlpEngineType.opennlp,
    /**
     * Is intent entity model and "standalone" entity models are used to find the better values?
     */
    val mergeEngineTypes: Boolean = true,
    /**
     * Is "standalone" entity models used? Useful for entity disambiguation.
     */
    val useEntityModels: Boolean = true,
    /**
     * Does this app support sub entities?
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
     * The id of the app.
     */
    val _id: Id<ApplicationDefinition> = newId(),
) {
    /**
     * A qualified name (ie "namespace:name") of the app.
     */
    @Transient
    val qualifiedName: String = name.withNamespace(namespace)
}
