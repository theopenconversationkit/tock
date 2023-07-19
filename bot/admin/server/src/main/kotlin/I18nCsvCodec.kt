/*
 * Copyright (C) 2017/2021 e-voyageurs technologies
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

package ai.tock.bot.admin

import ai.tock.bot.admin.model.I18LabelQuery
import ai.tock.bot.admin.model.BotStoryDefinitionConfiguration
import ai.tock.bot.admin.model.BotSimpleAnswerConfiguration
import ai.tock.bot.admin.model.BotStoryDefinitionConfigurationStep
import ai.tock.bot.admin.model.BotConfiguredSteps
import ai.tock.bot.admin.model.BotConfiguredAnswer
import ai.tock.bot.admin.model.BotAnswerConfiguration
import ai.tock.nlp.admin.CsvCodec
import ai.tock.nlp.admin.CsvCodec.csvFormat
import ai.tock.shared.error
import ai.tock.shared.injector
import ai.tock.shared.vertx.WebVerticle
import ai.tock.translator.I18nDAO
import ai.tock.translator.I18nLabel
import ai.tock.translator.I18nLocalizedLabel
import ai.tock.translator.UserInterfaceType
import com.github.salomonbrys.kodein.instance
import mu.KotlinLogging
import org.litote.kmongo.toId
import java.io.StringReader
import java.util.Locale
import kotlin.collections.component1
import kotlin.collections.component2

/**
 *
 */
object I18nCsvCodec {

    private val logger = KotlinLogging.logger {}

    private val i18nDAO: I18nDAO by injector.instance()

    internal enum class CsvColumn {
        Label,
        Namespace,
        Category,
        Language,
        Interface,
        Id,
        Validated,
        Connector,
        Alternatives
    }

    fun exportCsv(namespace: String, query: I18LabelQuery? = null): String {
        val sb = StringBuilder()
        val printer = CsvCodec.newPrinter(sb)
        printer.printRecord(CsvColumn.values().map { it.name })
        i18nDAO.getLabels(namespace, query?.toI18nLabelFilter())
            .forEach { label ->
                label.i18n.forEach { localizedLabel ->
                    printer.printRecord(
                        *(
                            listOf(
                                localizedLabel.label,
                                label.namespace,
                                label.category,
                                localizedLabel.locale.language,
                                localizedLabel.interfaceType,
                                label._id,
                                localizedLabel.validated,
                                localizedLabel.connectorId ?: ""
                            ) + localizedLabel.alternatives
                            ).toTypedArray()
                    )
                }
            }
        return sb.toString()
    }

    fun importCsv(namespace: String, content: String): Int {
        return try {
            val parsedCsv = csvFormat().withFirstRecordAsHeader()
                .parse(StringReader(content))
            val headers = parsedCsv.headerNames
            val isNamespaceInCsv = headers.contains(CsvColumn.Namespace.name)
            parsedCsv
                .records
                .mapNotNull {
                    I18nLabel(
                        if (isNamespaceInCsv)
                            it.get(CsvColumn.Id.name).replaceFirst(it.get(CsvColumn.Namespace.name), namespace).toId()
                        else
                            it.get(CsvColumn.Id.name).toId(),
                        namespace,
                        it.get(CsvColumn.Category.name),
                        LinkedHashSet(
                            listOf(
                                I18nLocalizedLabel(
                                    Locale(it.get(CsvColumn.Language.name)),
                                    UserInterfaceType.valueOf(it.get(CsvColumn.Interface.name)),
                                    it[0], // First header has a strange char before it, simpler with index
                                    it.get(CsvColumn.Validated.name)?.toBoolean() ?: false,
                                    it.get(CsvColumn.Connector.name).run { if (isBlank()) null else this },
                                    if (it.size() < headers.indexOf(CsvColumn.Alternatives.name))
                                        emptyList()
                                    else
                                        (headers.indexOf(CsvColumn.Alternatives.name) until it.size()).mapNotNull { index -> if (it[index].isNullOrBlank()) null else it[index] }
                                )
                            )
                        )
                    )
                }
                .filter { it.i18n.any { it.validated } }
                .groupBy { it._id }
                .map { (key, value) ->
                    value
                        .first()
                        .run {
                            val localized = value.flatMap { it.i18n }
                            copy(
                                i18n = LinkedHashSet(
                                    localized +
                                        (
                                            i18nDAO.getLabelById(key)
                                                ?.i18n
                                                ?.filter { old ->
                                                    localized.none {
                                                        old.locale == it.locale && old.interfaceType == it.interfaceType && old.connectorId == it.connectorId
                                                    }
                                                }
                                                ?: emptyList()
                                            )
                                )
                            )
                        }
                }.also {
                    it.forEach {
                        logger.info { "Save $it" }
                        i18nDAO.save(it)
                    }
                }.size
        } catch (t: IllegalArgumentException) {
            logger.error(t)
            WebVerticle.badRequest("Error importing CSV: ${t.message}")
        } catch (t: Throwable) {
            logger.error(t)
            0
        }
    }

    fun extractLabelsFromStory(fullStory : BotStoryDefinitionConfiguration?) : List<I18nLabel> {
        val allLabels = mutableListOf<I18nLabel>()
        allLabels.addAll(extractLabelsFromAnswer(fullStory?.answers.orEmpty()))
        allLabels.addAll(extractLabelsFromSteps(fullStory?.steps.orEmpty()))
        allLabels.addAll(extractLabelsFromConfiguredAnswers(fullStory?.configuredAnswers.orEmpty()))
        allLabels.addAll(extractLabelsFromConfiguredSteps(fullStory?.configuredSteps.orEmpty()))
        return allLabels
    }
    private fun extractLabelsFromAnswer(answers: List<BotAnswerConfiguration>): List<I18nLabel> {
        val allLabels = mutableListOf<I18nLabel>()
        for (answer in answers) {
            allLabels.addAll((answer as BotSimpleAnswerConfiguration).answers.mapNotNull { it.label })
        }
        return allLabels
    }

    private fun extractLabelsFromSteps(steps: List<BotStoryDefinitionConfigurationStep>): List<I18nLabel> {
        val allLabels = mutableListOf<I18nLabel>()
        for (step in steps) {
            allLabels.add(step.userSentence)
            allLabels.addAll(extractLabelsFromAnswer(step.answers))
        }
        return allLabels
    }

    private fun extractLabelsFromConfiguredAnswers(configuredAnswers: List<BotConfiguredAnswer>): List<I18nLabel> {
        val allLabels = mutableListOf<I18nLabel>()
        for (configuredAnswer in configuredAnswers) {
            allLabels.addAll(extractLabelsFromAnswer(configuredAnswer.answers))
        }
        return allLabels
    }

    private fun extractLabelsFromConfiguredSteps(configuredSteps: List<BotConfiguredSteps>): List<I18nLabel> {
        val allLabels = mutableListOf<I18nLabel>()
        for (configuredStep in configuredSteps) {
            allLabels.addAll(extractLabelsFromSteps(configuredStep.steps))
        }
        return allLabels
    }
}
