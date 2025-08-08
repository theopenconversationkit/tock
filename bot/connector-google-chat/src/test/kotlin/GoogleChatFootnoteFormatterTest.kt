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
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream
import kotlin.test.assertEquals

class GoogleChatFootnoteFormatterTest {

    @Test
    fun `format with no footnotes returns original text`() {
        val text = "Hello world"
        val result = GoogleChatFootnoteFormatter.format(text, emptyList(), condensed = false)
        assertEquals("Hello world", result)
    }

    @ParameterizedTest(name = "formatDetailed: {0}")
    @MethodSource("detailedFormatTestCases")
    fun `formatDetailed test cases`(
        description: String,
        text: String,
        footnotes: List<Footnote>,
        expectedResult: String
    ) {
        val result = GoogleChatFootnoteFormatter.format(text, footnotes, condensed = false)
        assertEquals(expectedResult, result)
    }

    @ParameterizedTest(name = "formatCondensed: {0}")
    @MethodSource("condensedFormatTestCases")
    fun `formatCondensed test cases`(
        description: String,
        text: String,
        footnotes: List<Footnote>,
        expectedResult: String
    ) {
        val result = GoogleChatFootnoteFormatter.format(text, footnotes, condensed = true)
        assertEquals(expectedResult, result)
    }

    companion object {
        @JvmStatic
        fun detailedFormatTestCases(): Stream<Arguments> = Stream.of(
            Arguments.of(
                "keeps footnotes with same URL but different titles",
                "Check this out",
                listOf(
                    Footnote("id1", "Title A", "https://example.com", null, null),
                    Footnote("id2", "Title B", "https://example.com", null, null)
                ),
                """
                Check this out

                *Sources :*
                <https://example.com|Title A>
                <https://example.com|Title B>
                """.trimIndent()
            ),
            Arguments.of(
                "formats mix of footnotes with and without URL",
                "Here's some info",
                listOf(
                    Footnote("id1", "Google", "https://google.com", null, null),
                    Footnote("id2", "Just text", null, null, null)
                ),
                """
                Here's some info

                *Sources :*
                <https://google.com|Google>
                Just text
                """.trimIndent()
            ),
            Arguments.of(
                "does not deduplicate footnotes with same title and different URLs",
                "Interesting links",
                listOf(
                    Footnote("id1", "Duplicate", "https://a.com", null, null),
                    Footnote("id2", "Duplicate", "https://b.com", null, null)
                ),
                """
                Interesting links

                *Sources :*
                <https://a.com|Duplicate>
                <https://b.com|Duplicate>
                """.trimIndent()
            ),
            Arguments.of(
                "deduplicates footnotes based only on url and title ignoring other fields",
                "References",
                listOf(
                    Footnote("id1", "Doc", "https://doc.com", "Content A", 0.9f),
                    Footnote("id2", "Doc", "https://doc.com", "Content B", 0.2f)
                ),
                """
                References

                *Source :*
                <https://doc.com|Doc>
                """.trimIndent()
            )
        )

        @JvmStatic
        fun condensedFormatTestCases(): Stream<Arguments> = Stream.of(
            Arguments.of(
                "generates numbered links with and without URLs",
                "Sources below",
                listOf(
                    Footnote("id1", "Tock", "https://tock.ai", null, null),
                    Footnote("id2", "Offline doc", null, null, null)
                ),
                """
                Sources below

                *Sources:* [[1]](https://tock.ai) [2]
                """.trimIndent()
            )
        )
    }
}