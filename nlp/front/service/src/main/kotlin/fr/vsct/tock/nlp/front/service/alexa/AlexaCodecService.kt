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

package fr.vsct.tock.nlp.front.service.alexa

import fr.vsct.tock.nlp.front.shared.ApplicationConfiguration
import fr.vsct.tock.nlp.front.shared.codec.alexa.AlexaCodec
import fr.vsct.tock.nlp.front.shared.codec.alexa.AlexaFilter
import fr.vsct.tock.nlp.front.shared.codec.alexa.AlexaIntent
import fr.vsct.tock.nlp.front.shared.codec.alexa.AlexaIntentsSchema
import fr.vsct.tock.nlp.front.shared.codec.alexa.AlexaLanguageModel
import fr.vsct.tock.nlp.front.shared.codec.alexa.AlexaSlot
import fr.vsct.tock.nlp.front.shared.codec.alexa.AlexaType
import fr.vsct.tock.nlp.front.shared.codec.alexa.AlexaTypeDefinition
import fr.vsct.tock.nlp.front.shared.codec.alexa.AlexaTypeDefinitionName
import fr.vsct.tock.nlp.front.shared.config.ApplicationDefinition
import fr.vsct.tock.nlp.front.shared.config.ClassifiedSentence
import fr.vsct.tock.nlp.front.shared.config.ClassifiedSentenceStatus
import fr.vsct.tock.nlp.front.shared.config.IntentDefinition
import fr.vsct.tock.shared.injector
import fr.vsct.tock.shared.name
import fr.vsct.tock.shared.provide
import org.litote.kmongo.Id
import java.util.Locale

/**
 *
 */
object AlexaCodecService : AlexaCodec {

    private val config: ApplicationConfiguration get() = injector.provide()

    private fun exportAlexaIntents(
            intents: List<IntentDefinition>,
            sentences: List<ClassifiedSentence>,
            filter: AlexaFilter?
    ): List<AlexaIntent> {
        return intents.map {
            AlexaIntent(
                    it.name,
                    exportSamples(
                            it,
                            sentences,
                            filter
                    ),
                    it.entities.map {
                        AlexaSlot(it.role, it.entityTypeName.name())
                    })
        }
    }

    private fun exportAlexaTypes(
            intents: List<IntentDefinition>,
            sentences: List<ClassifiedSentence>,
            filter: AlexaFilter?): List<AlexaType> {
        return intents
                .flatMap { intent -> intent.entities.map { entity -> entity.entityTypeName.name() } }
                .distinct()
                .filter { typeName -> filter == null || filter.intents.any { it.slots.any { it.type == typeName } } }
                .map { typeName -> AlexaType(typeName, exportAlexaTypeDefinition(typeName, sentences)) }
                .toList()
    }

    override fun exportIntentsSchema(
            applicationId: Id<ApplicationDefinition>,
            localeToExport: Locale,
            filter: AlexaFilter?): AlexaIntentsSchema {
        val allIntents = config.getIntentsByApplicationId(applicationId)

        val intentSet = allIntents
                .filter { intent -> filter == null || filter.intents.any { it.intent == intent.name } }
                .map { it._id }
                .toSet()

        val intents = allIntents.filter { intentSet.contains(it._id) }

        val sentences = config.getSentences(intentSet, localeToExport, ClassifiedSentenceStatus.model)

        return AlexaIntentsSchema(
                AlexaLanguageModel(
                        exportAlexaTypes(intents, sentences, filter),
                        exportAlexaIntents(intents, sentences, filter)
                )
        )
    }

    private fun exportSamples(
            intent: IntentDefinition,
            sentences: List<ClassifiedSentence>,
            filter: AlexaFilter?): List<String> {

        val filteredTypeNames = filter?.intents?.flatMap { it.slots.map { it.type } }?.toSet()

        return sentences
                .filter { it.classification.intentId == intent._id }
                .filter { filter == null || it.classification.entities.all { filteredTypeNames!!.contains(it.type) } }
                .map { sentence ->
                    var t = sentence.text
                    sentence
                            .classification
                            .entities
                            .asReversed()
                            .forEach {
                                t = t.substring(0, it.start) + "{${it.role}}" + t.substring(it.end, t.length)
                            }
                    t
                }
                .distinct()
                .filter { !it.contains("*") && !it.contains("google", true) }
    }

    private fun exportAlexaTypeDefinition(
            typeName: String,
            sentences: List<ClassifiedSentence>): List<AlexaTypeDefinition> {
        return sentences
                .flatMap { sentence ->
                    sentence
                            .classification
                            .entities
                            .filter { it.type.name() == typeName }
                            .map {
                                sentence.text.substring(it.start, it.end).replace("\n", "")
                            }
                }
                .distinct()
                .filter { !it.contains("*") && !it.contains("google", true) }
                .map {
                    AlexaTypeDefinition(
                            null,
                            AlexaTypeDefinitionName(
                                    it,
                                    emptyList()
                            )
                    )
                }
    }

}