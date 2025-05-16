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

package ai.tock.bot.api.model.message.bot

import ai.tock.translator.TranslatedSequence
import mu.KotlinLogging

data class I18nText(
    val text: String,
    val args: List<String?> = emptyList(),
    val toBeTranslated: Boolean = true,
    val key: String? = null
) : CharSequence by text, TranslatedSequence {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override fun toString(): String = text

    override fun plus(other: Any?): I18nText {
        logger.warn { "adding a String to a TranslatedSequence is not recommended - please use message format pattern" }
        return copy(text = toString() + other.toString())
    }
}
