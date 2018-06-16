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

package fr.vsct.tock.shared.security

import fr.vsct.tock.shared.security.StringObfuscatorMode.display
import fr.vsct.tock.shared.security.StringObfuscatorMode.normal

/**
 * Replaces a string by an other String.
 */
interface StringObfuscator {

    /**
     * The search regexp.
     */
    val regex: Regex

    /**
     * The replacement string - should have the same size than the replaced string.
     */
    val replacement: String

    /**
     * To match the replacement string in [StringObfuscatorMode.display] mode.
     */
    val replacementRegexp: Regex

    /**
     * Used in [StringObfuscatorMode.display] mode.
     */
    val displayed: String

    /**
     * Obfuscate the parameter.
     */
    fun obfuscate(text: String, mode: StringObfuscatorMode = normal): String {
        return when (mode) {
            normal -> regex
            display -> replacementRegexp
        }
                .toPattern()
                .matcher(text)
                .run {
                    replaceAll(
                            when (mode) {
                                normal -> replacement
                                display -> displayed
                            })
                }
    }
}