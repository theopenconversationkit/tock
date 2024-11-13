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

package ai.tock.shared

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.Nested
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 *
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class StringsTest {

    @Nested
    inner class PunctuationTests {
        @Test
        fun `string ending with dot should return true for endWithPunctuation`() {
            assertTrue("hkh.".endWithPunctuation())
        }
    }

    @Nested
    inner class RegexTests {
        @Test
        fun `should properly handle diacritics in regexp`() {
            val testCases = mapOf(
                "demo" to "d[eéèêë]m[oòóôõöø]",
                "c est une mise a jour" to "[cç]['-_ ][eéèêë]st['-_ ][uùúûü][nñ][eéèêë]['-_ ]m[iìíîï]s[eéèêë]['-_ ][aàáâãä]['-_ ]j[oòóôõöø][uùúûü]r"
            )

            testCases.forEach { (input, expected) ->
                assertEquals(expected, allowDiacriticsInRegexp(input))
            }
        }
    }

    @Nested
    inner class SafeHtmlTests {
        private val xssTestCases = mapOf(
            "empty string" to Pair("", ""),
            "basic XSS" to Pair(
                "<script>alert('xss')</script>",
                "&lt;&gt;('xss')&lt;/&gt;"
            ),
            "mouseover XSS" to Pair(
                "\\<a onmouseover=\"alert(document.cookie)\"\\>xxs link\\</a\\>",
                "\\&lt;a onmouseover=\"(document.cookie)\"\\&gt;xxs \\&lt;/a\\&gt;"
            ),
            "image XSS" to Pair(
                "<IMG SRC=\"javascript:alert('XSS');\">",
                "&lt;IMG SRC=\"java:('XSS');\"&gt;"
            ),
            "alertJs XSS" to Pair(
                "<object data=\"data:text/html;base64,PHNjcmlwdD5hbGVydCgxKTwvc2NyaXB0Pg==\"></object>",
                "&lt; data=\"data:text/html;base64,PHNjcmlwdD5hbGVydCgxKTwvc2NyaXB0Pg==\"&gt;&lt;/&gt;"
            ),
            "malformed IMG tags XSS" to Pair(
                "<IMG \"\"\"><SCRIPT>alert(\"XSS\")</SCRIPT>\"\\>",
                "&lt;IMG \"\"\"&gt;&lt;&gt;(\"XSS\")&lt;/&gt;\"\\&gt;"
            ),
            "fromCharCode XSS" to Pair(
                "<IMG SRC=javascript:alert(String.fromCharCode(88,83,83))>",
                "&lt;IMG SRC=java:(String.fromCharCode(88,83,83))&gt;"
            ),
            "fromCharCode XSS" to Pair(
                "<IMG SRC=javascript:alert(String.fromCharCode(88,83,83))>",
                "&lt;IMG SRC=java:(String.fromCharCode(88,83,83))&gt;"
            ),
            "Reflected XSS" to Pair(
                "\"\"><script>confirm(\"XSS Reflected!\")</script>",
                "\"\"&gt;&lt;&gt;confirm(\"XSS Reflected!\")&lt;/&gt;"
            ),
            "onerror XSS" to Pair(
                "\"><img src=x onerror=alert(document.cookie)>",
                "\"&gt;&lt;img src=x =(document.cookie)&gt;"
            ),
            "bypass WAF XSS" to Pair(
                "'\"><A HRef=\\\" AutoFocus OnFocus=top/**/?.['ale'%2B'rt'](document%2Bcookie)>",
                "'\"&gt;&lt;A =\\\" AutoFocus OnFocus=top/**/?.['ale'%2B'rt'](document%2Bcookie)&gt;"
            ),
            "bypass WAF 2 XSS" to Pair(
                "- 1'\"();<test><ScRiPt >window.alert(\"XSS_WAF_BYPASS\")",
                "- 1'\"();&lt;test&gt;&lt; &gt;window.(\"XSS_WAF_BYPASS\")"
            )
        )

        @Test
        fun `should properly sanitize HTML and prevent XSS attacks`() {
            xssTestCases.forEach { (testName, testCase) ->
                val (input, expected) = testCase
                assertEquals(expected, safeHTML(input), "Failed for test case: $testName")
            }
        }

        @Test
        fun `should preserve valid HTML while removing malicious content`() {
            val validHtmlTestCases = mapOf(
                "basic list" to Pair(
                    "<ul style=\"list-style: none;\"><li>⭐ Aux voyageurs Business Première</li></ul>",
                    "<ul style=\"list-style: none;\"><li>⭐ Aux voyageurs Business Première</li></ul>"
                ),
                "list with XSS" to Pair(
                    "<ul style=\"list-style: none;\"><li>⭐ Test</li><script>alert('xss')</script></ul>",
                    "<ul style=\"list-style: none;\"><li>⭐ Test</li>&lt;&gt;('xss')&lt;/&gt;</ul>"
                )
            )

            validHtmlTestCases.forEach { (testName, testCase) ->
                val (input, expected) = testCase
                assertEquals(
                    expected,
                    safeHTML(input),
                    "HTML sanitization failed for test case: $testName"
                )
            }
        }

        @Test
        fun `should properly handle non-ASCII characters`() {
            val nonAsciiTestCases = mapOf(
                "non-ASCII script tags" to Pair(
                    "<šcript>alert('xss')</šcript>",
                    "&lt;&scaron;cript&gt;('xss')&lt;/&scaron;cript&gt;"
                ),
                "Zalgo text" to Pair(
                    "n\u200Bot rè̑ͧ̌aͨl̘̝̙̃ͤ͂̾̆ ZA̡͊͠͝LGΌ",
                    "not rèal ZALG"
                )
            )

            nonAsciiTestCases.forEach { (testName, testCase) ->
                val (input, expected) = testCase
                assertEquals(
                    expected,
                    safeHTML(input),
                    "Non-ASCII handling failed for test case: $testName"
                )
            }
        }
    }
}
