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

import java.util.Locale

/**
 * The translator API translate one sentence from a source [Locale] to a target [Locale].
 */
interface TranslatorEngine {
    /**
     * Translates a text from [source] to [destination] locale.
     */
    fun translate(
        text: String,
        source: Locale,
        target: Locale,
    ): String

    /**
     * Does the engine support "on the fly" translations?
     */
    val supportAdminTranslation: Boolean get() = false
}
