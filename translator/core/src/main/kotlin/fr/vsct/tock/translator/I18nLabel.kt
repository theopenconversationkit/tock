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

package fr.vsct.tock.translator

import fr.vsct.tock.shared.defaultNamespace
import java.util.Locale

/**
 *
 */
data class I18nLabel(
        val _id: String,
        val namespace: String = defaultNamespace,
        val category: String,
        val i18n: List<I18nLocalizedLabel>) {

    fun findLabel(locale: Locale, userInterfaceType: UserInterfaceType): I18nLocalizedLabel?
            = i18n.firstOrNull { it.locale == locale && it.interfaceType == userInterfaceType }
            ?: i18n.firstOrNull { it.locale.language == locale.language && it.interfaceType == userInterfaceType }

    fun findLabel(locale: Locale): I18nLocalizedLabel?
            = i18n.firstOrNull { it.locale == locale }
            ?: i18n.firstOrNull { it.locale.language == locale.language }

}