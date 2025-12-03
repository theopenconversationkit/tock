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

package ai.tock.duckling.client

import ai.tock.nlp.core.EntityType
import ai.tock.shared.name

/**
 *
 */
internal object DucklingDimensions {
    const val DUCKLING = "duckling"
    const val TIME_DIMENSION = "time"
    const val DATETIME_DIMENSION = "datetime"
    val datetimeEntityType = DATETIME_DIMENSION.withDucklingPrefix()

    val dimensions =
        listOf(
            DATETIME_DIMENSION,
            "temperature",
            "number",
            "ordinal",
            "distance",
            "volume",
            "amount-of-money",
            "duration",
            "email",
            "url",
            "phone-number",
        )

    private fun String.withDucklingPrefix() = "$DUCKLING:$this"

    val entityTypes = dimensions.map { it.withDucklingPrefix() }.toSet()

    fun tockTypeToDucklingType(type: String): String {
        return when (type) {
            DATETIME_DIMENSION -> TIME_DIMENSION
            else -> type
        }
    }

    fun tockTypeToDucklingType(entityType: EntityType): String {
        return tockTypeToDucklingType(entityType.name.name())
    }
}
