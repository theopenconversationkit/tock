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
        val nothing = ""
        assertEquals(safeHTML(nothing), "")

        val xssExample = "<script>alert('xss')</script>"
        assertEquals(safeHTML(xssExample), "&lt;&gt;('xss')&lt;/&gt;")

        val htmlExample = "\\<a onmouseover=\"alert(document.cookie)\"\\>xxs link\\</a\\>"
        assertEquals(safeHTML(htmlExample), "\\&lt;a onmouseover=\"(document.cookie)\"\\&gt;xxs \\&lt;/a\\&gt;")

        val htmlExample2 = "<IMG SRC=\"jav&#x09;ascript:alert('XSS');\">"
        assertEquals(safeHTML(htmlExample2), "&lt;IMG SRC=\"jav&amp;#x09;a:('XSS');\"&gt;")

        val alertJs = "<object data=\"data:text/html;base64,PHNjcmlwdD5hbGVydCgxKTwvc2NyaXB0Pg==\"></object>"
        assertEquals(safeHTML(alertJs),"&lt; data=\"data:text/html;base64,PHNjcmlwdD5hbGVydCgxKTwvc2NyaXB0Pg==\"&gt;&lt;/&gt;")

        val malformedIMGTags = "<IMG \"\"\"><SCRIPT>alert(\"XSS\")</SCRIPT>\"\\>"
        assertEquals(safeHTML(malformedIMGTags),"&lt;IMG \"\"\"&gt;&lt;&gt;(\"XSS\")&lt;/&gt;\"\\&gt;")

        val fromCharCode = "<IMG SRC=javascript:alert(String.fromCharCode(88,83,83))>"
        assertEquals(safeHTML(fromCharCode),"&lt;IMG SRC=java:(String.fromCharCode(88,83,83))&gt;")

        val xss = "\"\"><script>confirm(\"XSS Reflected!\")</script>"
        assertEquals(safeHTML(xss),"\"\"&gt;&lt;&gt;confirm(\"XSS Reflected!\")&lt;/&gt;")

        val xss2 = "\"><img src=x onerror=alert(document.cookie)>"
        assertEquals(safeHTML(xss2),"\"&gt;&lt;img src=x =(document.cookie)&gt;")

        val bypassWaf =  "'\"><A HRef=\\\" AutoFocus OnFocus=top/**/?.['ale'%2B'rt'](document%2Bcookie)>"
        assertEquals(safeHTML(bypassWaf),"'\"&gt;&lt;A =\\\" AutoFocus OnFocus=top/**/?.['ale'%2B'rt'](document%2Bcookie)&gt;")

        val xssBypassWaf = "- 1'\"();<test><ScRiPt >window.alert(\"XSS_WAF_BYPASS\")"
        assertEquals(safeHTML(xssBypassWaf),"- 1'\"();&lt;test&gt;&lt; &gt;window.(\"XSS_WAF_BYPASS\")")

        val xssBypassWaf2 = "</Title/</Style/</Script/</textArea/</iFrame/</noScript>"
        assertEquals(safeHTML(xssBypassWaf2),"&lt;/Title/&lt;/Style/&lt;//&lt;/textArea/&lt;//&lt;/no&gt;")

        val tryTobYPass = "\"\\/><img%20s+src+c=x%20on+onerror+%20=\"alert(1)\"\\>"
        assertEquals(safeHTML(tryTobYPass),"\"\\/&gt;&lt;img%20s+src+c=x%20on++%20=\"(1)\"\\&gt;")
    }

    @Test
    fun `Test good value with xss`() {
       val goodHTML = "<ul style=\"list-style: none;\"><li>⭐ Aux voyageurs Business Première</li></ul>"
        assertEquals(safeHTML(goodHTML),"<ul style=\"list-style: none;\"><li>⭐ Aux voyageurs Business Première</li></ul>")

        val badHTML = "<ul style=\"list-style: none;\"><li>⭐ Aux voyageurs Business Première</li></ul><sCriPt>alert('xss')</ScriPt>"
        assertEquals(safeHTML(badHTML),"<ul style=\"list-style: none;\"><li>⭐ Aux voyageurs Business Première</li></ul>&lt;&gt;('xss')&lt;/&gt;")

        val toto = "<ul style=\"list-style: none;\"><li>⭐ Aux voyageurs Business Première</li><IMG SRC=\"jav&#x09;ascript:aLerT('XSS');\"></ul>"
        assertEquals(safeHTML(toto),"<ul style=\"list-style: none;\"><li>⭐ Aux voyageurs Business Première</li>&lt;IMG SRC=\"jav&amp;#x09;a:('XSS');\"&gt;</ul>")

        val tryWithMaliciousXSS = "<ul style=\"list-style: none;\"><svg onload=\"alert(1)\"></svg><li>⭐ Aux voyageurs Business Première</li></ul>"
        assertEquals(safeHTML(tryWithMaliciousXSS),"<ul style=\"list-style: none;\">&lt;svg =\"(1)\"&gt;&lt;/svg&gt;<li>⭐ Aux voyageurs Business Première</li></ul>")
    }

    @Test
    fun `Test with no ASCII character`() {
        val xssExample = "<šcript>alert('xss')</šcript>"
        assertEquals(safeHTML(xssExample), "&lt;&scaron;cript&gt;('xss')&lt;/&scaron;cript&gt;")

        val notGood ="n\u200Bot rè̑ͧ̌aͨl̘̝̙̃ͤ͂̾̆ ZA̡͊͠͝LGΌ ISͮ̂҉̯͈͕̹̘̱ TO͇̹̺ͅƝ̴ȳ̳ TH̘Ë͖́̉ ͠P̯͍̭O̚\u200BN̐Y̡ H̸̡̪̯ͨ͊̽̅̾̎Ȩ̬̩̾͛ͪ̈́̀́͘ ̶̧̨̱̹̭̯ͧ̾ͬC̷̙̲̝͖ͭ̏ͥͮ͟Oͮ͏̮̪̝͍M̲̖͊̒ͪͩͬ̚̚͜Ȇ̴̟̟͙̞ͩ͌͝S̨̥̫͎̭ͯͧͨ͛̉"
        assertEquals(safeHTML(notGood), "not rèal ZALG IS TO TH&Euml; PONY H COMS")

        val anotherOne = "Rege̿̔̉x-based HTM"
        assertEquals(safeHTML(anotherOne), "Regex-based HTM")
    }
}
