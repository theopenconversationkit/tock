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

import java.time.Instant
import org.litote.kmongo.Id

/**
 * I18n storage.
 */
interface I18nDAO {

    /**
     * Listen all i18n changes.
     */
    fun listenI18n(listener: (Id<I18nLabel>) -> Unit)

    /**
     * Gets all labels for the specified namespace with filters
     */
    fun getLabels(namespace: String, filter: I18nLabelFilter? = null): List<I18nLabel>

    /**
     * Gets label by id.
     */
    fun getLabelById(id: Id<I18nLabel>): I18nLabel?

    @Deprecated(message = "Replaced with more generic method", level = DeprecationLevel.HIDDEN)
    fun save(label: I18nLabel) = save(label as I18nLabelContract)

    /**
     * Saves label.
     */
    fun save(label: I18nLabelContract)

    /**
     * Saves all specified labels.
     */
    fun save(i18n: List<I18nLabelContract>)

    /**
     * Saves all labels that does not exist yet.
     */
    fun saveIfNotExist(i18n: List<I18nLabelContract>)

    /**
     * Delete the label of specified id if and only if it has the specified namespace.
     */
    fun deleteByNamespaceAndId(namespace: String, id: Id<I18nLabel>)

    /**
     * Marks an alternative index as used, for the given localized label and context identifier.
     */
    fun addAlternativeIndex(label: I18nLabel, localized: I18nLocalizedLabel, alternativeIndex: Int, contextId: String)

    /**
     * Removes all alternative indexes for the given localized label and context identifier.
     */
    fun deleteAlternativeIndexes(label: I18nLabel, localized: I18nLocalizedLabel, contextId: String)

    /**
     * Gets all current alternative indexes for the given localized label and context identifier.
     */
    fun getAlternativeIndexes(label: I18nLabel, localized: I18nLocalizedLabel, contextId: String): Set<Int>

    /**
     * Increments [I18nLabelStat] usage.
     */
    fun incrementLabelStat(stat: I18nLabelStat)

    /**
     * Gets all label stats for the specified namespace.
     */
    fun getLabelStats(namespace: String): List<I18nLabelStat>

    /**
     * Gets all label stats for the specified namespace.
     */
    fun getLabelIdsFromStats(namespace: String, timeMarker: Instant): Set<Id<I18nLabel>>
}
