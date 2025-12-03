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

package ai.tock.translator.deepl

import ai.tock.shared.TockProxyAuthenticator
import ai.tock.shared.jackson.mapper
import ai.tock.shared.property
import ai.tock.shared.propertyOrNull
import com.fasterxml.jackson.module.kotlin.readValue
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.regex.Pattern

internal data class TranslationResponse(
    val translations: List<Translation>,
)

internal data class Translation(
    val text: String,
)

const val TAG_HANDLING = "xml"

interface DeeplClient {
    fun translate(
        text: String,
        sourceLang: String,
        targetLang: String,
        preserveFormatting: Boolean,
        glossaryId: String?,
    ): String?
}

class OkHttpDeeplClient(
    private val apiURL: String = property("tock_translator_deepl_api_url", "https://api.deepl.com/v2/translate"),
    private val apiKey: String? = propertyOrNull("tock_translator_deepl_api_key"),
    okHttpCustomizer: OkHttpClient.Builder.() -> Unit = {},
) : DeeplClient {
    private val client =
        OkHttpClient.Builder()
            .apply(TockProxyAuthenticator::install)
            .apply(okHttpCustomizer)
            .build()

    private fun replaceSpecificPlaceholders(text: String): Pair<String, List<String>> {
        // Store original placeholders for later restoration
        val placeholderPattern = Pattern.compile("\\{:([^}]*)}")
        val matcher = placeholderPattern.matcher(text)

        val placeholders = mutableListOf<String>()
        while (matcher.find()) {
            placeholders.add(matcher.group(1))
        }

        // Replace placeholders with '_PLACEHOLDER_'
        val replacedText = matcher.replaceAll("_PLACEHOLDER_")

        return Pair(replacedText, placeholders)
    }

    private fun revertSpecificPlaceholders(
        text: String,
        placeholders: List<String>,
    ): String {
        var resultText = text
        for (placeholder in placeholders) {
            resultText = resultText.replaceFirst("_PLACEHOLDER_", "{:$placeholder}")
        }
        return resultText
    }

    override fun translate(
        text: String,
        sourceLang: String,
        targetLang: String,
        preserveFormatting: Boolean,
        glossaryId: String?,
    ): String? {
        if (apiKey == null) return text

        val (textWithPlaceholders, originalPlaceholders) = replaceSpecificPlaceholders(text)

        val formBuilder = FormBody.Builder()

        val requestBody =
            formBuilder
                .add("text", textWithPlaceholders)
                .add("source_lang", sourceLang)
                .add("target_lang", targetLang)
                .add("preserve_formatting", preserveFormatting.toString())
                .add("tag_handling", TAG_HANDLING)
                .build()

        glossaryId?.let {
            formBuilder.add("glossary_id", it)
        }

        val request =
            Request.Builder()
                .url(apiURL)
                .addHeader("Authorization", "DeepL-Auth-Key $apiKey")
                .post(requestBody)
                .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Unexpected code $response")

            val responseBody = response.body?.string()
            val translationResponse = mapper.readValue<TranslationResponse>(responseBody!!)

            val translatedText = translationResponse.translations.firstOrNull()?.text
            return translatedText?.let { revertSpecificPlaceholders(it, originalPlaceholders) }
        }
    }
}
