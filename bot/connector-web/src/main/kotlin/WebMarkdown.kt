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

package ai.tock.bot.connector.web

import org.commonmark.node.Node
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer
import java.util.regex.Matcher

object WebMarkdown {

    internal val regex = "`{3}([\\S\\s]*?)`{3}|`([^`]*)`|~~([\\S\\s]*?)~~".toRegex()

    fun markdown(text: String): String {
        val parser: Parser = Parser.builder().build()
        val document: Node = parser.parse(text)
        val renderer = HtmlRenderer.builder().build()
        var render = renderer.render(document).trim()
        if (render.contains("<strong>")) {
            render = render.replace("<strong>", "<strong style=\"font-weight: bold\">")
        }
        if (render.contains("<em>")) {
            render = render.replace("<em>", "<em style=\"font-style: italic\">")
        }
        if (render.contains("<h1>")) {
            render = render.replace(
                "<h1>",
                "<h1 style=\"display: block; font-size: 2em; margin-top: 0.67em; margin-bottom: 0.67em; margin-left: 0; margin-right: 0; font-weight: bold;\">"
            )
        }
        if (render.contains("<h2>")) {
            render = render.replace(
                "<h2>",
                "<h2 style=\"display: block; font-size: 1.5em; margin-top: 0.83em; margin-bottom: 0.83em; margin-left: 0; margin-right: 0; font-weight: bold;\">"
            )
        }
        if (render.contains("<h3>")) {
            render = render.replace(
                "<h3>",
                "<h3 style=\"display: block; font-size: 1.17em; margin-top: 1em; margin-bottom: 1em; margin-left: 0; margin-right: 0; font-weight: bold;\">"
            )
        }
        if (render.contains("<blockquote>")) {
            render = render.replace(
                "<blockquote>",
                "<blockquote style=\"font-style: normal; font-size: 15px; margin-left: 0px; font-family: Arial; border-left: 4px solid rgb(0 0 0 / 28%); padding-left: 8px; background-color: #f5f5f5;\">"
            )
        }
        if (render.contains("<code>")) {
            render = render.replace(
                "<code>",
                "<code style=\"padding: 2px 4px; font-size: 90%; background-color: #f5f5f5; border-radius: 4px;\">"
            )
        }
        if (render.contains("~~")) {
            render = extractAllDataWithRegex(regex, render)
        }
        if (render.contains("<a href=")) {
            render = render.replace(
                "<a href=",
                "<a target=\"_blank\" rel=\"noopener noreferrer\" href="
            )
        }
        return render
    }

    internal fun extractAllDataWithRegex(regex: Regex, value: String): String {
        val data = ArrayList<String>()
        val matcher: Matcher = regex.toPattern().matcher(value)
        var tmp = value

        while (matcher.find()) {
            for (i in 1..matcher.groupCount()) {
                data.add(matcher.group(i))
            }
        }
        data.forEach {
            val tmpIt = "~~$it~~"
            tmp = tmp.replace(tmpIt, "<s style=\"text-decoration: line-through;\">$it</s>")
        }
        return tmp
    }
}
