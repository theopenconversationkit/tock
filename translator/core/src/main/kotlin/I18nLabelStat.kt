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

package ai.tock.translator

import org.litote.kmongo.Id
import org.litote.kmongo.toId
import java.time.Instant
import java.util.Locale

/**
 * Stats about [I18nLabel] usage.
 */
data class I18nLabelStat(
    val labelId: Id<I18nLabel>,
    val namespace: String,
    val locale: Locale,
    val interfaceType: UserInterfaceType,
    val connectorId: String?,
    val count: Int = 1,
    val lastUpdate: Instant = Instant.now()
) {
    internal constructor(key: I18nLabelStatKey, count: Int) :
        this(
            key.labelId,
            key.namespace,
            key.locale,
            key.interfaceType,
            key.connectorId,
            count
        )

    /**
     * Does the locale of this stat has the same language than the specified label?
     */
    fun hasSameLanguage(label: I18nLocalizedLabel): Boolean = locale.language == label.locale.language
}

internal data class I18nLabelStatKey(
    val labelId: Id<I18nLabel>,
    val namespace: String,
    val locale: Locale,
    val interfaceType: UserInterfaceType,
    val connectorId: String?
) {
    constructor(value: I18nLabelValue, context: I18nContext) :
        this(
            value.key.toId(),
            value.namespace,
            context.userLocale,
            context.userInterfaceType,
            context.connectorId
        )
}
