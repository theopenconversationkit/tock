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

import ai.tock.shared.defaultLocale
import ai.tock.shared.defaultNamespace
import org.litote.kmongo.Id
import java.util.Locale

/**
 * The label persisted in database.
 */
data class I18nLabel(
    override val _id: Id<I18nLabel>,
    override val namespace: String = defaultNamespace,
    override val category: String,
    override val i18n: LinkedHashSet<I18nLocalizedLabel>,
    override val defaultLabel: String? = null,
    override val defaultLocale: Locale = findDefaultLabelLocale(defaultLabel, i18n),
    override val defaultI18n: Set<I18nLocalizedLabel> = emptySet(),
    override val version: Int = 0,
) : I18nLabelContract {
    override fun withDefaultLabel(defaultLabel: String?) = copy(defaultLabel = defaultLabel)

    override fun withUpdatedI18n(
        i18n: LinkedHashSet<I18nLocalizedLabel>,
        version: Int?,
    ) = copy(i18n = i18n, version = version ?: 0)

    companion object {
        fun findDefaultLabelLocale(
            defaultLabel: String?,
            i18n: MutableSet<I18nLocalizedLabel>,
        ): Locale =
            if (defaultLabel == null) {
                defaultLocale
            } else {
                i18n.firstOrNull { defaultLabel == it.label }?.locale ?: defaultLocale
            }

        fun findLabel(
            i18n: Set<I18nLocalizedLabel>,
            locale: Locale,
            userInterfaceType: UserInterfaceType,
            connectorId: String?,
        ): I18nLocalizedLabel? =
            i18n.firstOrNull { it.locale == locale && it.interfaceType == userInterfaceType && it.connectorId == connectorId }
                ?: i18n.firstOrNull { it.locale == locale && it.interfaceType == userInterfaceType && it.connectorId == null }
                ?: i18n.firstOrNull { it.locale.language == locale.language && it.interfaceType == userInterfaceType && it.connectorId == connectorId }
                ?: i18n.firstOrNull { it.locale.language == locale.language && it.interfaceType == userInterfaceType && it.connectorId == null }
    }

    fun findLabel(
        locale: Locale,
        userInterfaceType: UserInterfaceType,
        connectorId: String?,
    ): I18nLocalizedLabel? = findLabel(i18n, locale, userInterfaceType, connectorId)

    fun findLabel(
        locale: Locale,
        connectorId: String? = null,
    ): I18nLocalizedLabel? =
        i18n.firstOrNull { it.locale == locale && it.label.isNotBlank() && it.connectorId == connectorId }
            ?: i18n.firstOrNull { it.locale == locale && it.label.isNotBlank() && it.connectorId == null }
            ?: i18n.firstOrNull { it.locale.language == locale.language && it.label.isNotBlank() && it.connectorId == connectorId }
            ?: i18n.firstOrNull { it.locale.language == locale.language && it.label.isNotBlank() && it.connectorId == null }

    fun findExistingLabelForOtherLocale(
        forbiddenLocale: Locale,
        userInterfaceType: UserInterfaceType,
        connectorId: String?,
    ): I18nLocalizedLabel? =
        i18n.firstOrNull { it.label.isNotBlank() && it.locale == defaultLocale && it.interfaceType == userInterfaceType && it.connectorId == connectorId }
            ?.takeIf { forbiddenLocale != defaultLocale }
            ?: i18n.firstOrNull { it.label.isNotBlank() && it.locale != forbiddenLocale && it.interfaceType == userInterfaceType && it.connectorId == connectorId }
}
