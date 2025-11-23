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

package ai.tock.nlp.core

import java.util.Locale

/**
 * A dictionary model used for predefined values entities.
 */
data class DictionaryData(
    /**
     * The namespace of the entity.
     */
    val namespace: String,
    /**
     * The name of the entity.
     */
    val entityName: String,
    /**
     * The values of the dictionary.
     */
    val values: List<PredefinedValue> = emptyList(),
    /**
     * Are only defined labels allowed or can we try to use Damerau-Levenshtein distance ?
     */
    val onlyValues: Boolean = false,
    /**
     * The minimum distance to be allowed.
     */
    val minDistance: Double = 0.5,
    /**
     * If the text is contained by at least one label, the value is eligible.
     */
    val textSearch: Boolean = false,
) {
    /**
     * Returns a [PredefinedValue] -> list of labels map for the specified [locale].
     */
    fun getLabelsMap(locale: Locale): Map<PredefinedValue, List<String>?> = values.associateWith { v -> v.labels[locale] }

    @Transient
    val qualifiedName: String = "$namespace:$entityName"
}
