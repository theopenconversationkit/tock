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
import ai.tock.translator.I18nLabelContract
import ai.tock.translator.I18nLocalizedLabel
import com.fasterxml.jackson.annotation.JsonIgnore
import org.litote.kmongo.Id
import java.util.Locale

/**
 * Incoming [I18nLabel] update from the client (missing defaultI18n).
 */
data class BotI18nLabelUpdate(
    override val _id: Id<I18nLabel>,
    override val namespace: String = defaultNamespace,
    override val category: String,
    override val i18n: LinkedHashSet<I18nLocalizedLabel>,
    override val defaultLabel: String? = null,
    override val defaultLocale: Locale = ai.tock.shared.defaultLocale,
    override val version: Int?,
) : I18nLabelContract {
    @get:JsonIgnore
    override val defaultI18n: Set<I18nLocalizedLabel>?
        get() = null

    override fun withDefaultLabel(defaultLabel: String?) = copy(defaultLabel = defaultLabel)

    override fun withUpdatedI18n(
        i18n: LinkedHashSet<I18nLocalizedLabel>,
        version: Int?,
    ) = copy(i18n = i18n, version = version)
}
