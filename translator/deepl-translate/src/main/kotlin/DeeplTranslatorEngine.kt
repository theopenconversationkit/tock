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

import ai.tock.shared.property
import ai.tock.translator.TranslatorEngine
import org.apache.commons.text.StringEscapeUtils
import java.util.Locale

internal object DeeplTranslatorEngine : TranslatorEngine {

    private val deeplClient = DeeplClient(property ("tock_translator_deepl_api_url", "default"),property ("tock_translator_deepl_api_key", "default"))
    private val glossaryId = property ("tock_translator_deepl_glossaryId", "default")
    override val supportAdminTranslation: Boolean = true

    override fun translate(text: String, source: Locale, target: Locale): String {
        var translatedTextHTML4 = ""
        // Allows to filter translation on a specific language
        if(target.language == property ("tock_translator_deepl_target_language", "en")) {
            val translatedText = deeplClient.translate(text, source.language, target.language, true, glossaryId)
            translatedTextHTML4 = StringEscapeUtils.unescapeHtml4(translatedText)
        }
        return translatedTextHTML4
    }
}
