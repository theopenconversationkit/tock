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
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.IOException
import java.util.regex.Pattern

data class TranslationResponse(
    val translations: List<Translation>
)

data class Translation(
    val text: String
)

const val TAG_HANDLING = "xml"

class DeeplClient(private val apiURL: String, private val apiKey: String) {
    private val client = OkHttpClient()
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val jsonAdapter = moshi.adapter(TranslationResponse::class.java)

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

    private fun revertSpecificPlaceholders(text: String, placeholders: List<String>): String {
        var resultText = text
        for (placeholder in placeholders) {
            resultText = resultText.replaceFirst("_PLACEHOLDER_", "{:$placeholder}")
        }
        return resultText
    }

    fun translate(text: String, sourceLang: String,targetLang: String,preserveFormatting: Boolean,glossaryId:String?): String? {
        val (textWithPlaceholders, originalPlaceholders) = replaceSpecificPlaceholders(text)

        val requestBody = buildString {
            append("text=$textWithPlaceholders")
            append("&source_lang=$sourceLang")
            append("&target_lang=$targetLang")
            append("&preserve_formatting=$preserveFormatting")
            append("&tag_handling=$TAG_HANDLING")

            if (glossaryId != "default") {
                append("&glossary=$glossaryId")
            }
        }

        val request = Request.Builder()
            .url(apiURL)
            .addHeader("Authorization", "DeepL-Auth-Key $apiKey")
            .post(requestBody.trimIndent().toRequestBody("application/x-www-form-urlencoded".toMediaTypeOrNull()))
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Unexpected code $response")

            val responseBody = response.body?.string()
            val translationResponse = jsonAdapter.fromJson(responseBody!!)

            val translatedText = translationResponse?.translations?.firstOrNull()?.text
            return translatedText?.let { revertSpecificPlaceholders(it,originalPlaceholders) }
        }
    }
}