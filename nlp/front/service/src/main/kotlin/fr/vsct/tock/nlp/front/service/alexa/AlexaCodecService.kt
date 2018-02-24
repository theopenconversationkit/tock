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

import emoji4j.EmojiUtils
import fr.vsct.tock.nlp.front.shared.ApplicationConfiguration
import fr.vsct.tock.nlp.front.shared.codec.alexa.AlexaCodec
import fr.vsct.tock.nlp.front.shared.codec.alexa.AlexaFilter
import fr.vsct.tock.nlp.front.shared.codec.alexa.AlexaIntent
import fr.vsct.tock.nlp.front.shared.codec.alexa.AlexaIntentsSchema
import fr.vsct.tock.nlp.front.shared.codec.alexa.AlexaLanguageModel
import fr.vsct.tock.nlp.front.shared.codec.alexa.AlexaModelTransformer
import fr.vsct.tock.nlp.front.shared.codec.alexa.AlexaSlot
import fr.vsct.tock.nlp.front.shared.codec.alexa.AlexaType
import fr.vsct.tock.nlp.front.shared.codec.alexa.AlexaTypeDefinition
import fr.vsct.tock.nlp.front.shared.codec.alexa.AlexaTypeDefinitionName
import fr.vsct.tock.nlp.front.shared.config.ApplicationDefinition
import fr.vsct.tock.nlp.front.shared.config.ClassifiedSentence
import fr.vsct.tock.nlp.front.shared.config.ClassifiedSentenceStatus
import fr.vsct.tock.nlp.front.shared.config.EntityDefinition
import fr.vsct.tock.nlp.front.shared.config.IntentDefinition
import fr.vsct.tock.shared.injector
import fr.vsct.tock.shared.name
import fr.vsct.tock.shared.provide
import org.litote.kmongo.Id
import java.util.Locale

/**
 * [AlexaCodec] implementation - mainly used for batch export.
 */
object AlexaCodecService : AlexaCodec {

    private val config: ApplicationConfiguration get() = injector.provide()

    private fun exportAlexaIntents(
        intents: List<IntentDefinition>,
        sentences: List<ClassifiedSentence>,
        filter: AlexaFilter?
    ): List<AlexaIntent> {
        return intents
            .map { intent ->
                AlexaIntent(
                    intent.name + "_intent",
                    exportSamples(
                        intent,
                        sentences,
                        filter
                    ),
                    intent.entities
                        .filter { entity ->
                            filter == null
                                    || filter.intents.first { intent.name == it.intent }.slots.any { it.name == entity.role }
                        }
                        .map {
                            AlexaSlot(
                                (filter?.findSlot(intent, it)?.targetName
                                        ?: it.role) + "_slot",
                                filter?.findSlot(intent, it)?.targetType ?: it.entityTypeName.name()
                            )
                        })
            }
    }

    private fun exportAlexaTypes(
        intents: List<IntentDefinition>,
        sentences: List<ClassifiedSentence>,
        filter: AlexaFilter?
    ): List<AlexaType> {

        return intents
            .flatMap { intent -> intent.entities.map { entity -> intent to entity } }
            .filter { (intent, entity) ->
                filter == null || filter.findSlot(intent, entity) != null
            }
            .map { (intent, entity) ->
                AlexaType(
                    filter?.findSlot(intent, entity)?.targetType
                            ?: entity.entityTypeName.name().replace("-", "_"),
                    exportAlexaTypeDefinition(intent, entity, sentences)
                        .distinctBy { type -> type.name.value.toLowerCase().trim() }
                )
            }
            .groupBy { it.name }
            .map { (_, types) ->
                types.first().run {
                    copy(values = (values + types.subList(1, types.size).flatMap { it.values }).distinct())
                }
            }
            .toList()
    }

    override fun exportIntentsSchema(
        invocationName: String,
        applicationId: Id<ApplicationDefinition>,
        localeToExport: Locale,
        filter: AlexaFilter?,
        transformer: AlexaModelTransformer
    ): AlexaIntentsSchema {
        val allIntents = config.getIntentsByApplicationId(applicationId)

        val intentSet = allIntents
            .filter { intent -> filter == null || filter.intents.any { it.intent == intent.name } }
            .map { it._id }
            .toSet()

        val intents = allIntents.filter { intentSet.contains(it._id) }

        val sentences = config.getSentences(intentSet, localeToExport, ClassifiedSentenceStatus.model)

        return transformer.transform(
            AlexaIntentsSchema(
                AlexaLanguageModel(
                    invocationName,
                    exportAlexaTypes(intents, sentences, filter),
                    exportAlexaIntents(intents, sentences, filter)
                )
            )
        )
    }

    private fun exportSamples(
        intent: IntentDefinition,
        sentences: List<ClassifiedSentence>,
        filter: AlexaFilter?
    ): List<String> {

        val filteredRoles = filter?.intents?.first { it.intent == intent.name }?.slots?.map { it.name }?.toSet()

        val startByLetter = "^[a-z\\{].*".toRegex()
        val nonChar = "[^a-záàâäãåçéèêëíìîïñóòôöõúùûüýÿ\\{\\}'_]".toRegex()
        val spaceRegex = " +".toRegex()
        return sentences
            .filter { it.classification.intentId == intent._id }
            .filter { filter == null || it.classification.entities.all { filteredRoles!!.contains(it.role) } }
            .map { sentence ->
                var t = sentence.text
                sentence
                    .classification
                    .entities
                    .sortedByDescending { it.start }
                    .forEach {
                        t = t.substring(0, it.start) + "{${it.role}_slot}" + t.substring(it.end, t.length)
                    }
                t
            }
            .map { it.toLowerCase() }
            .filter { !it.contains("*") && !it.contains("google") }
            .map { sentence -> sentence.replace("'{", " {") }
            .map { sentence -> EmojiUtils.removeAllEmojis(sentence) }
            .map { sentence -> sentence.replace("☺", " ") }
            .map { sentence -> sentence.replace(nonChar, " ") }
            .map { sentence -> sentence.replace("( )*_+( )*".toRegex(), "_") }
            .map { sentence -> sentence.replace(" ' ", "") }
            .map { sentence -> sentence.replace("}_".toRegex(), "} ") }
            .map { sentence -> sentence.replace("_{", " {") }
            .map { sentence -> sentence.replace("}", "} ") }
            .map { sentence -> sentence.replace("{", " {") }
            .map { it.trim() }
            .map { sentence -> sentence.replace(spaceRegex, " ") }
            .filter { it.matches(startByLetter) }
            .groupBy { it }
            .entries
            .sortedByDescending { it.value.size }
            .map { it.key }
            .distinct()
    }

    private fun exportAlexaTypeDefinition(
        intent: IntentDefinition,
        entity: EntityDefinition,
        sentences: List<ClassifiedSentence>
    ): List<AlexaTypeDefinition> {
        val nonChar = "[^a-záàâäãåçéèêëíìîïñóòôöõúùûüýÿ']".toRegex()
        val spaceRegex = " +".toRegex()

        return sentences
            .filter { it.classification.intentId == intent._id }
            .flatMap { sentence ->
                sentence
                    .classification
                    .entities
                    .filter { it.type == entity.entityTypeName }
                    .distinct()
                    .map {
                        sentence.text.substring(it.start, it.end).replace("\n", "")
                    }
                    .map { it.toLowerCase() }
                    .map { it.replace(nonChar, " ") }
                    .map { it.trim() }
                    .map { it.replace(spaceRegex, " ") }
            }
            .distinct()
            .filter {
                !it.contains("*")
                        && !it.contains("google")
            }
            .map {
                AlexaTypeDefinition(
                    null,
                    AlexaTypeDefinitionName(
                        it.replace("-", "_").replace("\"", " "),
                        emptyList()
                    )
                )
            }
    }

}