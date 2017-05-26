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

package fr.vsct.tock.duckling.client

import fr.vsct.tock.nlp.core.EntityType
import fr.vsct.tock.shared.name

/**
 *
 */
internal object DucklingDimensions {

    val timeDucklingDimension = "time"
    private val datetimeEntityTypeName = "datetime"
    val datetimeEntityType = datetimeEntityTypeName.withDucklingPrefix()

    val dimensions = listOf(
            datetimeEntityTypeName,
            "temperature",
            "number",
            "ordinal",
            "distance",
            "volume",
            "amount-of-money",
            "duration",
            "email",
            "url",
            "phone-number")

    private fun String.withDucklingPrefix() = "duckling:$this"

    val entityTypes = dimensions.map { it.withDucklingPrefix() }.toSet()

    val mergeSupport = setOf(datetimeEntityTypeName).map { it.withDucklingPrefix() }.toSet()

    fun tockTypeToDucklingType(type: String): String {
        return when (type) {
            datetimeEntityTypeName -> timeDucklingDimension
            else -> type
        }
    }

    fun tockTypeToDucklingType(entityType: EntityType): String {
        return tockTypeToDucklingType(entityType.name.name())
    }
}