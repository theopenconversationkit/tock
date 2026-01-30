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

package ai.tock.bot.engine.feature

/**
 * Characters that are forbidden in feature names and categories.
 * These characters are used internally as separators in the storage layer:
 * - '+' is used to separate the applicationId from the feature identifier
 * - ',' is used to separate the category from the name
 */
private val FORBIDDEN_CHARACTERS = setOf('+', ',')

/**
 * Validates that a feature name does not contain forbidden characters.
 *
 * @param name the feature name to validate
 * @throws IllegalArgumentException if the name contains forbidden characters
 */
fun validateFeatureName(name: String) {
    val invalidChars = name.filter { it in FORBIDDEN_CHARACTERS }
    if (invalidChars.isNotEmpty()) {
        throw IllegalArgumentException(
            "Feature name must not contain the following characters: ${FORBIDDEN_CHARACTERS.joinToString(", ")}. " +
                "Found: ${invalidChars.toSet().joinToString(", ")}",
        )
    }
}

/**
 * Validates that a feature category does not contain forbidden characters.
 *
 * @param category the feature category to validate
 * @throws IllegalArgumentException if the category contains forbidden characters
 */
fun validateFeatureCategory(category: String) {
    val invalidChars = category.filter { it in FORBIDDEN_CHARACTERS }
    if (invalidChars.isNotEmpty()) {
        throw IllegalArgumentException(
            "Feature category must not contain the following characters: ${FORBIDDEN_CHARACTERS.joinToString(", ")}. " +
                "Found: ${invalidChars.toSet().joinToString(", ")}",
        )
    }
}
