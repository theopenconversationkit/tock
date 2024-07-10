/*
 * Copyright (C) 2017/2021 e-voyageurs technologies
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

package ai.tock.translator.deepl

import ai.tock.shared.propertyOrNull
import ai.tock.translator.TranslatorEngine
import java.util.Locale
import org.apache.commons.text.StringEscapeUtils

internal class DeeplTranslatorEngine(client: DeeplClient) : TranslatorEngine {
    private val supportedLanguagesProperty = propertyOrNull("tock_translator_deepl_target_languages")
    private val supportedLanguages: Set<String>? = supportedLanguagesProperty?.split(",")?.map { it.trim() }?.toSet()

    private val deeplClient = client
    private val glossaryId = propertyOrNull("tock_translator_deepl_glossary_id")
    override val supportAdminTranslation: Boolean = true

    override fun translate(text: String, source: Locale, target: Locale): String {
        var translatedTextHTML4 = ""
        // Allows to filter translation on a specific language
        if (supportedLanguages?.contains(target.language) == true || supportedLanguages == null) {
            val translatedText = deeplClient.translate(text, source.language, target.language, true, glossaryId)
            translatedTextHTML4 = StringEscapeUtils.unescapeHtml4(translatedText)
        }
        return translatedTextHTML4
    }
}
