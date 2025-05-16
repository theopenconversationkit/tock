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

package ai.tock.nlp.api.client.model.dump

import ai.tock.nlp.api.client.model.NlpEngineType
import java.time.Instant
import java.util.Locale

/**
 * An application dump is a full NLP application data snapshot, with intents, entities and sentences.
 */
data class ApplicationDump(
    val application: ApplicationDefinition,
    val entityTypes: List<EntityTypeDefinition> = emptyList(),
    val intents: List<IntentDefinition> = emptyList(),
    val sentences: List<ClassifiedSentence> = emptyList(),
    val dumpType: DumpType = DumpType.full,
    val timestamp: Instant = Instant.now()
)

data class ApplicationDefinition(
    val name: String,
    val label: String = name,
    val namespace: String,
    val intents: Set<String> = emptySet(),
    val supportedLocales: Set<Locale> = emptySet(),
    val intentStatesMap: Map<String, Set<String>> = emptyMap(),
    val nlpEngineType: NlpEngineType = NlpEngineType.opennlp,
    val mergeEngineTypes: Boolean = true,
    val useEntityModels: Boolean = true,
    val supportSubEntities: Boolean = false,
    val _id: String
)

data class Classification(
    val intentId: String,
    val entities: List<ClassifiedEntity>
)

data class ClassifiedEntity(
    val type: String,
    val role: String,
    val start: Int,
    val end: Int
)

data class ClassifiedSentence(
    val text: String,
    val language: Locale,
    val applicationId: String,
    val creationDate: Instant,
    val updateDate: Instant,
    val status: ClassifiedSentenceStatus,
    val classification: Classification
)

enum class ClassifiedSentenceStatus {
    inbox, validated, model, deleted
}

enum class DumpType { full }

data class EntityDefinition(
    val entityTypeName: String,
    val role: String,
    /**
     * To evaluate time.
     */
    val atStartOfDay: Boolean? = null
)

data class EntityTypeDefinition(
    val name: String,
    val description: String,
    val subEntities: List<EntityDefinition> = emptyList(),
    val dictionary: Boolean = false,
    /**
     * Is the entity has to be systematically obfuscated?
     */
    val obfuscated: Boolean = false,
    val _id: String
)

data class IntentDefinition(
    val name: String,
    val namespace: String,
    val applications: Set<String>,
    val entities: Set<EntityDefinition>,
    val mandatoryStates: Set<String> = emptySet(),
    val sharedIntents: Set<String> = emptySet(),
    val label: String? = null,
    val description: String? = null,
    val category: String? = null,
    val _id: String
)
