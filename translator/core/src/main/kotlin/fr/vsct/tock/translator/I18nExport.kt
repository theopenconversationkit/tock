/*
 * Copyright (C) 2017 VSCT
 *
 * Licensed under the Apache License, Version 2.0 (the "License")$TAB
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

package fr.vsct.tock.translator

import com.github.salomonbrys.kodein.instance
import fr.vsct.tock.shared.defaultLocale
import fr.vsct.tock.shared.injector
import fr.vsct.tock.shared.property

/**
 *
 */
object I18nExport {

    val s = property("tock_csv_sepearator", ";")
    val i18n: I18nDAO  by injector.instance()

    fun exportCsv(namespace: String): String {
        return "Label$s Category$s Language$s Interface$s Id$s Validated$s Alternatives\n" +
                i18n.getLabels()
                        .filter { it.namespace == namespace }
                        .sortedWith(compareBy({ it.category }, { it.findLabel(defaultLocale)?.label ?: "" }))
                        .flatMap { l ->
                            l.i18n.sortedWith(compareBy({ it.locale.language }, { it.interfaceType }))
                                    .map { i ->
                                        "\"${i.label.replace("\"", "\"\"")}\"$s${l.category}$s${i.locale}$s${i.interfaceType}$s${l._id}$s${i.validated}$s${i.alternatives.joinToString(s) { "\"${it.replace("\"", "\"\"")}\"" }}"
                                    }
                        }
                        .joinToString("\r\n")
    }
}