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

package ai.tock.bot.connector.googlechat

import ai.tock.bot.engine.action.Footnote


/**
 * Utility object that formats footnotes for Google Chat messages.
 *
 * Supports two formats:
 * - Detailed: shows the full list of sources below the message.
 * - Condensed: shows compact numbered links at the end of the message.
 */
object GoogleChatFootnoteFormatter {

    fun format(text: CharSequence, footnotes: List<Footnote>, condensed: Boolean = false, truncateUrl: Boolean = false): String {
        if (footnotes.isEmpty()) return text.toString()
        return if (condensed) formatCondensed(text, footnotes, truncateUrl) else formatDetailed(text, footnotes, truncateUrl)
    }

    private fun formatDetailed(text: CharSequence, footnotes: List<Footnote>, truncateUrl: Boolean): String {
        // Even in detailed mode, we apply a deduplication step based on (url, title) pair.
        // This means that multiple footnotes pointing to the same document (e.g. same PDF) with the same title and URL
        // will appear only once in the formatted result.
        // This is acceptable in the context of Google Chat, where footnote content is not displayed,
        // and thus no relevant information is lost.
        val unique = footnotes.distinctBy { (it.url ?: "") to it.title.toString().trim() }
        val header = if (unique.size > 1) "Sources" else "Source"

        val formatted = unique.joinToString("\n") { fn ->
            val title = fn.title.toString().trim()
            val rawUrl = fn.url?.trim()
            val displayUrl = if (truncateUrl && !rawUrl.isNullOrBlank()) truncateMiddleOfPath(rawUrl) else rawUrl

            when {
                !displayUrl.isNullOrBlank() && title.isNotBlank() -> "<$rawUrl|$title>"
                !displayUrl.isNullOrBlank() -> "<$rawUrl>"
                else -> title
            }
        }

        return "$text\n\n*$header :*\n$formatted"
    }

    private fun formatCondensed(text: CharSequence, footnotes: List<Footnote>, truncateUrl: Boolean): String {
        val unique = footnotes.distinctBy { (it.url ?: "") to it.title.toString().trim() }
        val links = unique.mapIndexed { idx, fn ->
            val num = idx + 1
            val rawUrl = fn.url
            val displayUrl = if (truncateUrl && !rawUrl.isNullOrBlank()) truncateMiddleOfPath(rawUrl) else rawUrl

            displayUrl?.let { "[[$num]]($it)" } ?: "[$num]"
        }.joinToString(" ")

        val header = if (unique.size > 1) "Sources" else "Source"
        return "$text\n\n*$header:* $links"
    }

    private fun truncateMiddleOfPath(url: String, maxLength: Int = 50): String {
        if (url.length <= maxLength) return url

        val parts = url.split("/").toMutableList()
        if (parts.size <= 3) return url

        val first = parts.take(2).joinToString("/")
        val last = parts.takeLast(2).joinToString("/")
        val truncated = "$first/.../$last"

        return if (truncated.length <= maxLength) truncated else url
    }
}
