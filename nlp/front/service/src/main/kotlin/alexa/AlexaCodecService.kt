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

package ai.tock.nlp.front.service.alexa

import ai.tock.nlp.front.shared.ApplicationConfiguration
import ai.tock.nlp.front.shared.codec.alexa.AlexaCodec
import ai.tock.nlp.front.shared.codec.alexa.AlexaFilter
import ai.tock.nlp.front.shared.codec.alexa.AlexaIntent
import ai.tock.nlp.front.shared.codec.alexa.AlexaIntentsSchema
import ai.tock.nlp.front.shared.codec.alexa.AlexaLanguageModel
import ai.tock.nlp.front.shared.codec.alexa.AlexaModelTransformer
import ai.tock.nlp.front.shared.codec.alexa.AlexaSlot
import ai.tock.nlp.front.shared.codec.alexa.AlexaType
import ai.tock.nlp.front.shared.codec.alexa.AlexaTypeDefinition
import ai.tock.nlp.front.shared.codec.alexa.AlexaTypeDefinitionName
import ai.tock.nlp.front.shared.config.ApplicationDefinition
import ai.tock.nlp.front.shared.config.ClassifiedSentence
import ai.tock.nlp.front.shared.config.ClassifiedSentenceStatus
import ai.tock.nlp.front.shared.config.EntityDefinition
import ai.tock.nlp.front.shared.config.IntentDefinition
import ai.tock.shared.injector
import ai.tock.shared.name
import ai.tock.shared.provide
import com.vdurmont.emoji.EmojiManager
import com.vdurmont.emoji.EmojiParser
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
                            filter == null ||
                                    filter.intents.first { intent.name == it.intent }.slots.any { it.name == entity.role }
                        }
                        .map {
                            AlexaSlot(
                                (
                                        filter?.findSlot(intent, it)?.targetName
                                            ?: it.role
                                        ) + "_slot",
                                filter?.findSlot(intent, it)?.targetType ?: it.entityTypeName.name()
                            )
                        }
                )
            }
    }

    private fun exportAlexaTypes(
        intents: List<IntentDefinition>,
        sentences: List<ClassifiedSentence>,
        filter: AlexaFilter?,
        transformer: AlexaModelTransformer
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
                    exportAlexaTypeDefinition(intent, entity, sentences, transformer)
                        .distinctBy { type -> type.name.value.lowercase().trim() }
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
                    exportAlexaTypes(intents, sentences, filter, transformer),
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
        val spaceRegex = "\\s{2,}".toRegex()
        return sentences
            .filter { it.classification.intentId == intent._id }
            .filter { filter == null || it.classification.entities.all { filteredRoles!!.contains(it.role) } }
            .map { sentence ->
                var t = sentence.text.lowercase()
                sentence
                    .classification
                    .entities
                    .sortedByDescending { it.start }
                    .forEach {
                        t = t.substring(0, it.start) + "{${it.role}_slot}" + t.substring(it.end, t.length)
                    }
                t
            }
            .map { it.lowercase() }
            .filter { !it.contains("*") }
            .map { sentence -> sentence.replace("'{", " {") }
            .map { sentence -> EmojiParser.removeAllEmojis(sentence) }
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
        sentences: List<ClassifiedSentence>,
        transformer: AlexaModelTransformer
    ): List<AlexaTypeDefinition> {
        val nonChar = "[^0-9a-záàâäãåçéèêëíìîïñóòôöõúùûüýÿ']".toRegex()
        val spaceRegex = "\\s{2,}".toRegex()

        return transformer.filterCustomSlotSamples(
            sentences
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
                        .map { it.lowercase() }
                        .map { it.replace(nonChar, " ") }
                        .map { it.trim() }
                        .map { it.replace(spaceRegex, " ") }
                }
        )
            .filter {
                !it.contains("*")
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
