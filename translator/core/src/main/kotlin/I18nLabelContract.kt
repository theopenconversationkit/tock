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

import java.util.Locale
import org.litote.kmongo.Id

interface I18nLabelContract {
    val _id: Id<I18nLabel>
    val namespace: String?
    val category: String
    val i18n: LinkedHashSet<I18nLocalizedLabel>
    val defaultLabel: String?
    val defaultLocale: Locale?
    val defaultI18n: Set<I18nLocalizedLabel>?
    val version: Int?

    fun withDefaultLabel(defaultLabel: String?): I18nLabelContract
    fun withUpdatedI18n(i18n: LinkedHashSet<I18nLocalizedLabel>, version: Int?): I18nLabelContract
}
