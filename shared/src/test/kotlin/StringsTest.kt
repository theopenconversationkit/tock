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
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 *
 */
class StringsTest {

    @Test
    fun `WHEN string ends with dot THEN endWithPunctuation returns true`() {
        assertTrue("hkh.".endWithPunctuation())
    }

    @Test
    fun `Test Regex for replace accent`() {
        assertEquals(allowDiacriticsInRegexp("demo"), "d[eéèêë]m[oòóôõöø]")
        assertEquals(allowDiacriticsInRegexp("c est une mise a jour"), "[cç]['-_ ][eéèêë]st['-_ ][uùúûü][nñ][eéèêë]['-_ ]m[iìíîï]s[eéèêë]['-_ ][aàáâãä]['-_ ]j[oòóôõöø][uùúûü]r")
    }

    @Test
    fun `Safe HTML`() {
        val xssExample = "<script>alert('xss')</script>"
        assertEquals(safeHTML(xssExample), "&lt;script&gt;alert('xss')&lt;/script&gt;")

        val htmlExample = "\\<a onmouseover=\"alert(document.cookie)\"\\>xxs link\\</a\\>"
        assertEquals(safeHTML(htmlExample), "\\&lt;a onmouseover=&quot;alert(document.cookie)&quot;\\&gt;xxs link\\&lt;/a\\&gt;")

        val htmlExample2 = "<IMG SRC=\"jav&#x09;ascript:alert('XSS');\">"
        assertEquals(safeHTML(htmlExample2), "&lt;IMG SRC=&quot;jav&amp;#x09;ascript:alert('XSS');&quot;&gt;")

        val alertJs = "<object data=\"data:text/html;base64,PHNjcmlwdD5hbGVydCgxKTwvc2NyaXB0Pg==\"></object>"
        assertEquals(safeHTML(alertJs),"&lt;object data=&quot;data:text/html;base64,PHNjcmlwdD5hbGVydCgxKTwvc2NyaXB0Pg==&quot;&gt;&lt;/object&gt;")

        val malformedIMGTags = "<IMG \"\"\"><SCRIPT>alert(\"XSS\")</SCRIPT>\"\\>"
        assertEquals(safeHTML(malformedIMGTags),"&lt;IMG &quot;&quot;&quot;&gt;&lt;SCRIPT&gt;alert(&quot;XSS&quot;)&lt;/SCRIPT&gt;&quot;\\&gt;")

        val fromCharCode = "<IMG SRC=javascript:alert(String.fromCharCode(88,83,83))>"
        assertEquals(safeHTML(fromCharCode),"&lt;IMG SRC=javascript:alert(String.fromCharCode(88,83,83))&gt;")
    }
}
