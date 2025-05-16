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

package ai.tock.translator.google

import ai.tock.translator.TranslatorEngine
import com.google.cloud.translate.Translate.TranslateOption
import com.google.cloud.translate.TranslateOptions

import java.util.Locale
import org.apache.commons.text.StringEscapeUtils

internal object GoogleTranslatorEngine : TranslatorEngine {

    private val translate = TranslateOptions.getDefaultInstance().service

    override fun translate(text: String, source: Locale, target: Locale): String {
        val translation = translate.translate(
            text,
            TranslateOption.sourceLanguage(source.language),
            TranslateOption.targetLanguage(target.language)
        )
        return StringEscapeUtils.unescapeHtml4(translation.translatedText)
    }

    override val supportAdminTranslation: Boolean = true
}
