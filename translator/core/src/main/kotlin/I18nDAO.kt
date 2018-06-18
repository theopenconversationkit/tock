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

import org.litote.kmongo.Id

/**
 * I18n storage.
 */
interface I18nDAO {

    /**
     * Gets all labels for the specified namespace.
     */
    fun getLabels(namespace: String): List<I18nLabel>

    /**
     * Gets label by id.
     */
    fun getLabelById(id: Id<I18nLabel>): I18nLabel?

    /**
     * Saves label.
     */
    fun save(i18n: I18nLabel)

    /**
     * Saves all specified labels.
     */
    fun save(i18n: List<I18nLabel>)

    /**
     * Saves all labels that does not exist yet.
     */
    fun saveIfNotExist(i18n: List<I18nLabel>)

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

}