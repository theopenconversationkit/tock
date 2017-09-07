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

package fr.vsct.tock.bot.admin

import com.github.salomonbrys.kodein.instance
import fr.vsct.tock.shared.defaultLocale
import fr.vsct.tock.shared.error
import fr.vsct.tock.shared.injector
import fr.vsct.tock.shared.property
import fr.vsct.tock.translator.I18nDAO
import fr.vsct.tock.translator.I18nLabel
import fr.vsct.tock.translator.I18nLocalizedLabel
import fr.vsct.tock.translator.UserInterfaceType
import mu.KotlinLogging
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import java.io.StringReader
import java.util.Locale

/**
 *
 */
object I18nCsvCodec {

    private val logger = KotlinLogging.logger {}

    val s = property("tock_csv_delimiter", ";")
    val i18n: I18nDAO  by injector.instance()

    private fun csvFormat(): CSVFormat = CSVFormat.DEFAULT.withDelimiter(s[0]).withTrim(true)

    fun exportCsv(namespace: String): String {
        val sb = StringBuilder()
        val printer = CSVPrinter(sb, csvFormat())
        printer.printRecord("Label", "Category", "Language", "Interface", "Id", "Validated", "Alternatives")
        i18n.getLabels()
                .filter { it.namespace == namespace }
                .sortedWith(compareBy({ it.category }, { it.findLabel(defaultLocale)?.label ?: "" }))
                .forEach { l ->
                    l.i18n.sortedWith(compareBy({ it.locale.language }, { it.interfaceType }))
                            .forEach { i ->
                                printer.printRecord(*(listOf(i.label, l.category, i.locale.language, i.interfaceType, l._id, i.validated) + i.alternatives).toTypedArray())
                            }
                }
        return sb.toString()
    }

    fun importCsv(namespace: String, content: String): Boolean {
        return try {
            csvFormat()
                    .parse(StringReader(content))
                    .records
                    .mapIndexedNotNull { i, it ->
                        if (i == 0) {
                            null
                        } else {
                            I18nLabel(
                                    it[4],
                                    namespace,
                                    it[1],
                                    listOf(
                                            I18nLocalizedLabel(
                                                    Locale(it[2]),
                                                    UserInterfaceType.valueOf(it[3]),
                                                    it[0],
                                                    it[5].toBoolean(),
                                                    if (it.size() < 6) emptyList() else (6 until it.size()).mapNotNull { index -> if (it[index].isNullOrBlank()) null else it[index] }
                                            )
                                    )
                            )
                        }
                    }
                    .filter { it.i18n.first().validated }
                    .groupBy { it._id }
                    .map { it.value.first().copy(i18n = it.value.flatMap { it.i18n }) }
                    .forEach {
                        logger.info { "Save $it" }
                        i18n.save(it)
                    }
            true
        } catch (t: Throwable) {
            logger.error(t)
            false
        }
    }
}