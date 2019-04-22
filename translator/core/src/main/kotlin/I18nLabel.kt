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
import org.litote.kmongo.Id
import java.util.Locale

/**
 * The label persisted in database.
 */
data class I18nLabel(
        val _id: Id<I18nLabel>,
        val namespace: String = defaultNamespace,
        val category: String,
        val i18n: LinkedHashSet<I18nLocalizedLabel>,
        val defaultLabel: String? = null,
        val version: Int = 0
) {

    fun findLabel(locale: Locale, userInterfaceType: UserInterfaceType, connectorId: String?): I18nLocalizedLabel? =
            i18n.firstOrNull { it.locale == locale && it.interfaceType == userInterfaceType && it.connectorId == connectorId }
                    ?: i18n.firstOrNull { it.locale == locale && it.interfaceType == userInterfaceType && it.connectorId == null }
                    ?: i18n.firstOrNull { it.locale.language == locale.language && it.interfaceType == userInterfaceType && it.connectorId == connectorId }
                    ?: i18n.firstOrNull { it.locale.language == locale.language && it.interfaceType == userInterfaceType && it.connectorId == null }

    fun findLabel(locale: Locale, connectorId: String?): I18nLocalizedLabel? =
            i18n.firstOrNull { it.locale == locale && it.label.isNotBlank() && it.connectorId == connectorId }
                    ?: i18n.firstOrNull { it.locale == locale && it.label.isNotBlank() && it.connectorId == null }
                    ?: i18n.firstOrNull { it.locale.language == locale.language && it.label.isNotBlank() && it.connectorId == connectorId }
                    ?: i18n.firstOrNull { it.locale.language == locale.language && it.label.isNotBlank() && it.connectorId == null }

}