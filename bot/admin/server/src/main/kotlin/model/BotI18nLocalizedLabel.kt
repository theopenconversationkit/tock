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

import ai.tock.translator.I18nLabelStat
import ai.tock.translator.I18nLocalizedLabel
import ai.tock.translator.UserInterfaceType
import java.util.Locale

/**
 * [I18nLocalizedLabel] dto.
 */
data class BotI18nLocalizedLabel(
    val locale: Locale,
    val interfaceType: UserInterfaceType,
    val label: String,
    val validated: Boolean,
    val connectorId: String? = null,
    val alternatives: List<String> = emptyList(),
    val stats: List<I18nLabelStat> = emptyList(),
) {
    constructor(label: I18nLocalizedLabel, stats: List<I18nLabelStat>) :
        this(
            label.locale,
            label.interfaceType,
            label.label,
            label.validated,
            label.connectorId,
            label.alternatives,
            stats,
        )
}
