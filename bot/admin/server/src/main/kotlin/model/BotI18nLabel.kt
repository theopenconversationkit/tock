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

package ai.tock.bot.admin.model

import ai.tock.shared.defaultNamespace
import ai.tock.translator.I18nLabel
import ai.tock.translator.I18nLabelStat
import ai.tock.translator.I18nLocalizedLabel
import ai.tock.translator.UserInterfaceType.textChat
import org.litote.kmongo.Id
import java.time.Instant
import java.util.Locale

/**
 * [I18nLabel] dto.
 */
data class BotI18nLabel(
    val _id: Id<I18nLabel>,
    val namespace: String = defaultNamespace,
    val category: String,
    val i18n: LinkedHashSet<BotI18nLocalizedLabel>,
    val defaultLabel: String? = null,
    val defaultLocale: Locale = ai.tock.shared.defaultLocale,
    val statCount: Int = 0,
    val lastUpdate: Instant? = null,
    val unhandledLocaleStats: List<I18nLabelStat> = emptyList(),
    val version: Int = 0
) {

    companion object {
        private fun selectStats(
            label: I18nLocalizedLabel,
            labels: LinkedHashSet<I18nLocalizedLabel>,
            stats: List<I18nLabelStat>
        ): List<I18nLabelStat> =
            stats.filter { s ->
                s.hasSameLanguage(label) &&
                    (
                        (label.interfaceType == s.interfaceType && s.connectorId == label.connectorId) ||
                            (label.connectorId == null && label.interfaceType == s.interfaceType && labels.none { it != label && it.label.isNotBlank() && s.hasSameLanguage(it) && it.interfaceType == s.interfaceType && it.connectorId == s.connectorId }) ||
                            (label.connectorId == null && label.interfaceType == textChat && labels.none { it != label && it.label.isNotBlank() && s.hasSameLanguage(it) && it.interfaceType == s.interfaceType })
                        )
            }
    }

    constructor(label: I18nLabel, stats: List<I18nLabelStat>) :
        this(
            label._id,
            label.namespace,
            label.category,
            label.i18n.mapTo(LinkedHashSet()) { BotI18nLocalizedLabel(it, selectStats(it, label.i18n, stats)) },
            label.defaultLabel,
            label.defaultLocale,
            stats.sumOf { it.count },
            stats.maxByOrNull { it.lastUpdate }?.lastUpdate,
            stats.filter { label.i18n.none { l -> it.hasSameLanguage(l) } },
            label.version
        )
}
