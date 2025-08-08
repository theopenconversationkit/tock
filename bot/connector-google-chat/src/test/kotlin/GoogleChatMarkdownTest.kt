import ai.tock.bot.connector.googlechat.GoogleChatMarkdown
import kotlin.test.Test
import kotlin.test.assertEquals

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

class GoogleChatMarkdownTest {

    @Test
    fun `bold text is converted to Google Chat format`() {
        val input = "**hello**"
        val expected = "*hello*"
        assertEquals(expected, GoogleChatMarkdown.toGoogleChat(input))
    }

    @Test
    fun `italic text is converted to Google Chat format`() {
        val input = "_hello_"
        val expected = "_hello_"
        assertEquals(expected, GoogleChatMarkdown.toGoogleChat(input))
    }

    @Test
    fun `strikethrough text is preserved with tildes`() {
        val input = "~~hello~~"
        val expected = "~~hello~~" // Not transformed
        assertEquals(expected, GoogleChatMarkdown.toGoogleChat(input))
    }

    @Test
    fun `inline code is wrapped with backticks`() {
        val input = "`hello`"
        val expected = "`hello`"
        assertEquals(expected, GoogleChatMarkdown.toGoogleChat(input))
    }

    @Test
    fun `code block is wrapped with triple backticks`() {
        val input = "```\nHello World\n```"
        val expected = "```Hello World```"
        assertEquals(expected, GoogleChatMarkdown.toGoogleChat(input))
    }

    @Test
    fun `unordered list is rendered with stars`() {
        val input = """
            * Item 1
            * Item 2
        """.trimIndent()

        val expected = """
            * Item 1
            * Item 2
        """.trimIndent()

        assertEquals(expected, GoogleChatMarkdown.toGoogleChat(input))
    }

    @Test
    fun `link is converted to Google Chat hyperlink format`() {
        val input = "[Example](https://example.com)"
        val expected = "<https://example.com|Example>"
        assertEquals(expected, GoogleChatMarkdown.toGoogleChat(input))
    }

    @Test
    fun `link with empty label uses URL as fallback`() {
        val input = "[](https://example.com)"
        val expected = "<https://example.com|https://example.com>"
        assertEquals(expected, GoogleChatMarkdown.toGoogleChat(input))
    }

    @Test
    fun `headings are converted to bold with spacing`() {
        val input = """
            # Title
            ## Subtitle
        """.trimIndent()

        val expected = """
            *Title*

            *Subtitle*
        """.trimIndent()

        assertEquals(expected, GoogleChatMarkdown.toGoogleChat(input))
    }

    @Test
    fun `paragraphs are separated by double line breaks`() {
        val input = "First paragraph.\n\nSecond paragraph."
        val expected = "First paragraph.\n\nSecond paragraph."
        assertEquals(expected, GoogleChatMarkdown.toGoogleChat(input))
    }
}