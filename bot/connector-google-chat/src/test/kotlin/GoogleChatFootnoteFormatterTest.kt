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

import ai.tock.bot.connector.googlechat.GoogleChatFootnoteFormatter
import ai.tock.bot.engine.action.Footnote
import kotlin.test.Test
import kotlin.test.assertEquals

class GoogleChatFootnoteFormatterTest {

    @Test
    fun `format with no footnotes returns original text`() {
        val text = "Hello world"
        val result = GoogleChatFootnoteFormatter.format(text, emptyList(), condensed = false)
        assertEquals("Hello world", result)
    }

    @Test
    fun `formatDetailed keeps footnotes with same URL but different titles`() {
        val text = "Check this out"
        val footnotes = listOf(
            Footnote("id1", "Title A", "https://example.com", null, null),
            Footnote("id2", "Title B", "https://example.com", null, null)
        )
        val result = GoogleChatFootnoteFormatter.format(text, footnotes, condensed = false)

        assertEquals(
            """
        Check this out

        *Sources :*
        <https://example.com|Title A>
        <https://example.com|Title B>
        """.trimIndent(),
            result
        )
    }

    @Test
    fun `formatDetailed formats mix of footnotes with and without URL`() {
        val text = "Here's some info"
        val footnotes = listOf(
            Footnote("id1", "Google", "https://google.com", null, null),
            Footnote("id2", "Just text", null, null, null)
        )

        val result = GoogleChatFootnoteFormatter.format(text, footnotes, condensed = false)

        assertEquals(
            """
            Here's some info

            *Sources :*
            <https://google.com|Google>
            Just text
            """.trimIndent(),
            result
        )
    }

    @Test
    fun `formatCondensed generates numbered links with and without URLs`() {
        val text = "Sources below"
        val footnotes = listOf(
            Footnote("id1", "Tock", "https://tock.ai", null, null),
            Footnote("id2", "Offline doc", null, null, null)
        )

        val result = GoogleChatFootnoteFormatter.format(text, footnotes, condensed = true)

        assertEquals(
            """
            Sources below

            *Sources:* [[1]](https://tock.ai) [2]
            """.trimIndent(),
            result
        )
    }

    @Test
    fun `formatDetailed does not deduplicate footnotes with same title and different URLs`() {
        val text = "Interesting links"
        val footnotes = listOf(
            Footnote("id1", "Duplicate", "https://a.com", null, null),
            Footnote("id2", "Duplicate", "https://b.com", null, null)
        )

        val result = GoogleChatFootnoteFormatter.format(text, footnotes, condensed = false)

        assertEquals(
            """
            Interesting links

            *Sources :*
            <https://a.com|Duplicate>
            <https://b.com|Duplicate>
            """.trimIndent(),
            result
        )
    }

    @Test
    fun `formatDetailed deduplicates footnotes based only on url and title ignoring other fields`() {
        val text = "References"
        val footnotes = listOf(
            Footnote("id1", "Doc", "https://doc.com", "Content A", 0.9f),
            Footnote("id2", "Doc", "https://doc.com", "Content B", 0.2f)
        )

        val result = GoogleChatFootnoteFormatter.format(text, footnotes, condensed = false)

        assertEquals(
            """
            References

            *Source :*
            <https://doc.com|Doc>
            """.trimIndent(),
            result
        )
    }
}