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

import fr.vsct.tock.shared.Dice.newInt
import mu.KotlinLogging
import java.util.Locale

/**
 *
 */
data class I18nLocalizedLabel(val locale: Locale,
                              val interfaceType: UserInterfaceType,
                              val label: String,
                              val validated: Boolean,
                              val connectorId: String? = null,
                              val alternatives: List<String> = emptyList()) {

    constructor(locale: Locale, interfaceType: UserInterfaceType, label: String, alternatives: List<String>) : this(locale, interfaceType, label, false, null, alternatives)

    constructor(locale: Locale, interfaceType: UserInterfaceType, label: String) : this(locale, interfaceType, label, emptyList())

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    fun randomAlternativesIndex(): Int =
            if (alternatives.isEmpty()) 0 else newInt(alternatives.size + 1)

    fun randomText(index: Int? = null): String {
        return if (alternatives.isEmpty()) {
            label
        } else {
            (listOf(label) + alternatives).run {
                val i = index ?: randomAlternativesIndex()
                if (i >= size) {
                    logger.warn { "not valid index $i for $this" }
                }
                this[i]
            }
        }
    }
}