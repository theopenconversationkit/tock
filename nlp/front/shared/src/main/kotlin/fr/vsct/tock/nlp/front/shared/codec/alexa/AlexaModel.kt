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

package fr.vsct.tock.nlp.front.shared.codec.alexa

data class AlexaFilter(val intents: List<AlexaIntentFilter> = emptyList())

data class AlexaIntentFilter(
        val intent: String,
        val slots: List<AlexaSlot> = emptyList())

data class AlexaIntentsSchema(
        val languageModel: AlexaLanguageModel)

data class AlexaLanguageModel(
        val types: List<AlexaType> = emptyList(),
        val intents: List<AlexaIntent> = emptyList())

data class AlexaType(
        val name: String,
        val values: List<AlexaTypeDefinition> = emptyList())

data class AlexaTypeDefinition(
        val id: String?,
        val name: AlexaTypeDefinitionName)

data class AlexaTypeDefinitionName(
        val value: String,
        val synonyms: List<String>)

data class AlexaIntent(
        val name: String,
        val samples: List<String> = emptyList(),
        val slots: List<AlexaSlot> = emptyList())

data class AlexaSlot(
        val name: String,
        val type: String)