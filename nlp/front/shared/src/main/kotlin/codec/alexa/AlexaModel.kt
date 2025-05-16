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

package ai.tock.nlp.front.shared.codec.alexa

import ai.tock.nlp.front.shared.config.EntityDefinition
import ai.tock.nlp.front.shared.config.IntentDefinition

/**
 * Specify the intents you want to keep when exporting to Alexa model.
 */
data class AlexaFilter(val intents: List<AlexaIntentFilter> = emptyList()) {

    fun findSlot(intent: IntentDefinition, entity: EntityDefinition): AlexaSlotFilter? =
        intents.firstOrNull { it.intent == intent.name }?.slots?.firstOrNull { it.name == entity.role }
}

/**
 * Manage final transformation of [AlexaModel].
 */
interface AlexaModelTransformer {

    /**
     * Last change to transform Alexa model.
     */
    fun transform(schema: AlexaIntentsSchema): AlexaIntentsSchema

    /**
     * Filter samples.
     */
    fun filterCustomSlotSamples(samples: List<String>) = samples.distinct()
}

data class AlexaIntentFilter(
    val intent: String,
    val slots: List<AlexaSlotFilter> = emptyList()
)

data class AlexaSlotFilter(
    val name: String,
    val type: String,
    val targetName: String = name.replace("-", "_"),
    val targetType: String = type.replace("-", "_")
)

data class AlexaIntentsSchema(
    val languageModel: AlexaLanguageModel
)

data class AlexaLanguageModel(
    val invocationName: String = "",
    val types: List<AlexaType> = emptyList(),
    val intents: List<AlexaIntent> = emptyList()
)

data class AlexaType(
    val name: String,
    val values: List<AlexaTypeDefinition> = emptyList()
)

data class AlexaTypeDefinition(
    val id: String?,
    val name: AlexaTypeDefinitionName
)

data class AlexaTypeDefinitionName(
    val value: String,
    val synonyms: List<String>
)

data class AlexaIntent(
    val name: String,
    val samples: List<String> = emptyList(),
    val slots: List<AlexaSlot> = emptyList()
)

data class AlexaSlot(
    val name: String,
    val type: String
)
