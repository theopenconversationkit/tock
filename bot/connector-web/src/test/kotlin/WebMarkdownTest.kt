import ai.tock.bot.connector.web.WebMarkdown
import ai.tock.bot.connector.web.WebMarkdown.regex
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

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

class WebMarkdownTest {

    @Test
    fun `test simple markdown`() {
        Assertions.assertThat(WebMarkdown.markdown("* item 1")).isEqualTo(
            "<ul>\n" +
                    "<li>item 1</li>\n" +
                    "</ul>"
        )
        Assertions.assertThat(WebMarkdown.markdown("1. item 1")).isEqualTo(
            "<ol>\n" +
                    "<li>item 1</li>\n" +
                    "</ol>"
        )
    }

    @Test
    fun `test custom markdown`() {
        Assertions.assertThat(WebMarkdown.markdown("*This is italic text*"))
            .isEqualTo("<em style=\"font-style: italic\">This is italic text</em>")
        Assertions.assertThat(WebMarkdown.markdown("**This is bold text**"))
            .isEqualTo("<strong style=\"font-weight: bold\">This is bold text</strong>")
        Assertions.assertThat(WebMarkdown.markdown("# h1 Heading "))
            .isEqualTo("<h1 style=\"display: block; font-size: 2em; margin-top: 0.67em; margin-bottom: 0.67em; margin-left: 0; margin-right: 0; font-weight: bold;\">h1 Heading</h1>")
        Assertions.assertThat(WebMarkdown.markdown("## h2 Heading "))
            .isEqualTo("<h2 style=\"display: block; font-size: 1.5em; margin-top: 0.83em; margin-bottom: 0.83em; margin-left: 0; margin-right: 0; font-weight: bold;\">h2 Heading</h2>")
        Assertions.assertThat(WebMarkdown.markdown("### h3 Heading "))
            .isEqualTo("<h3 style=\"display: block; font-size: 1.17em; margin-top: 1em; margin-bottom: 1em; margin-left: 0; margin-right: 0; font-weight: bold;\">h3 Heading</h3>")
        Assertions.assertThat(WebMarkdown.markdown("> Blockquote"))
            .isEqualTo(
                "<blockquote style=\"font-style: normal; font-size: 15px; margin-left: 0px; font-family: Arial; border-left: 4px solid rgb(0 0 0 / 28%); padding-left: 8px; background-color: #f5f5f5;\">\n" +
                        "Blockquote\n" +
                        "</blockquote>"
            )
        Assertions.assertThat(
            WebMarkdown.markdown(
                "title 1 :\n" +
                        "# h1 Heading\n" +
                        "title 2 :\n" +
                        "## h2 Heading\n" +
                        "title 3 :\n" +
                        "### h3 Heading"
            )
        ).isEqualTo(
            "title 1 :\n" +
                    "<h1 style=\"display: block; font-size: 2em; margin-top: 0.67em; margin-bottom: 0.67em; margin-left: 0; margin-right: 0; font-weight: bold;\">h1 Heading</h1>\n" +
                    "title 2 :\n" +
                    "<h2 style=\"display: block; font-size: 1.5em; margin-top: 0.83em; margin-bottom: 0.83em; margin-left: 0; margin-right: 0; font-weight: bold;\">h2 Heading</h2>\n" +
                    "title 3 :\n" +
                    "<h3 style=\"display: block; font-size: 1.17em; margin-top: 1em; margin-bottom: 1em; margin-left: 0; margin-right: 0; font-weight: bold;\">h3 Heading</h3>"
        )
        Assertions.assertThat(WebMarkdown.markdown("`code`"))
            .isEqualTo("<code style=\"padding: 2px 4px; font-size: 90%; background-color: #f5f5f5; border-radius: 4px;\">code</code>")
        Assertions.assertThat(WebMarkdown.markdown("[TOCK STUDIO](https://doc.tock.ai/)"))
            .isEqualTo("<a target=\"_blank\" rel=\"noopener noreferrer\" href=\"https://doc.tock.ai/\">TOCK STUDIO</a>")
    }

    @Test
    fun `regex for strikethrough `() {
        Assertions.assertThat(WebMarkdown.extractAllDataWithRegex(regex, "~~some multiline strikethrough~~"))
            .isEqualTo("<div style=\"text-decoration: line-through;\">some multiline strikethrough</div>")
        Assertions.assertThat(
            WebMarkdown.extractAllDataWithRegex(
                regex,
                "~~first strikethrblough~~ and blabla ~~ other information~~"
            )
        )
            .isEqualTo("<div style=\"text-decoration: line-through;\">first strikethrblough</div> and blabla <div style=\"text-decoration: line-through;\"> other information</div>")
    }

    @Test
    fun `test full text`() {
        val fullText =
            "**Tock**  (_The Open Conversation Kit_) is a complete and open platform to build conversational agents - also known as  _bots_.\n" +
                    "\n" +
                    "Tock does not depend on 3rd-party APIs, although it is possible to integrate with them. Users choose which components to embed and decide to keep (or share) ownership of conversational data and models.\n" +
                    "\n" +
                    "> Tock has been used in production since 2016 by  [OUI.sncf](https://www.oui.sncf/services/assistant)  (Web/mobile, messaging platforms, smart speakers) and  [more and more organisations](https://doc.tock.ai/tock/en/about/showcase)  (energy, banking, healthcare…).\n" +
                    "\n" +
                    "The platform source code is available on  [GitHub](https://github.com/theopenconversationkit/tock)  under the  [Apache License, version 2.0](https://github.com/theopenconversationkit/tock/blob/master/LICENSE).\n" +
                    "\n" +
                    "## Overview\n" +
                    "\n" +
                    "The  [Tock.ai](https://doc.tock.ai/)  site is a good starting point to learn about the solution and its growing community.  [Tutorials](https://doc.tock.ai/tock/en/guide/studio),  [presentations](https://doc.tock.ai/tock/en/about/resources)  and a  [live demo](https://www.youtube.com/watch?v=UsKkpYL7Hto)  (20 minutes, in English,  [ℹ️ abstract](https://www.opensource-experience.com/en/event/20-minutes-from-zero-to-live-chatbot-with-tock/)) are also available."
        Assertions.assertThat(WebMarkdown.markdown(fullText))
            .isEqualTo(
                "<strong style=\"font-weight: bold\">Tock</strong>  (<em style=\"font-style: italic\">The Open Conversation Kit</em>) is a complete and open platform to build conversational agents - also known as  <em style=\"font-style: italic\">bots</em>.\n" +
                        "Tock does not depend on 3rd-party APIs, although it is possible to integrate with them. Users choose which components to embed and decide to keep (or share) ownership of conversational data and models.\n" +
                        "<blockquote style=\"font-style: normal; font-size: 15px; margin-left: 0px; font-family: Arial; border-left: 4px solid rgb(0 0 0 / 28%); padding-left: 8px; background-color: #f5f5f5;\">\n" +
                        "Tock has been used in production since 2016 by  <a target=\"_blank\" rel=\"noopener noreferrer\" href=\"https://www.oui.sncf/services/assistant\">OUI.sncf</a>  (Web/mobile, messaging platforms, smart speakers) and  <a target=\"_blank\" rel=\"noopener noreferrer\" href=\"https://doc.tock.ai/tock/en/about/showcase\">more and more organisations</a>  (energy, banking, healthcare…).\n" +
                        "</blockquote>\n" +
                        "The platform source code is available on  <a target=\"_blank\" rel=\"noopener noreferrer\" href=\"https://github.com/theopenconversationkit/tock\">GitHub</a>  under the  <a target=\"_blank\" rel=\"noopener noreferrer\" href=\"https://github.com/theopenconversationkit/tock/blob/master/LICENSE\">Apache License, version 2.0</a>.\n" +
                        "<h2 style=\"display: block; font-size: 1.5em; margin-top: 0.83em; margin-bottom: 0.83em; margin-left: 0; margin-right: 0; font-weight: bold;\">Overview</h2>\n" +
                        "The  <a target=\"_blank\" rel=\"noopener noreferrer\" href=\"https://doc.tock.ai/\">Tock.ai</a>  site is a good starting point to learn about the solution and its growing community.  <a target=\"_blank\" rel=\"noopener noreferrer\" href=\"https://doc.tock.ai/tock/en/guide/studio\">Tutorials</a>,  <a target=\"_blank\" rel=\"noopener noreferrer\" href=\"https://doc.tock.ai/tock/en/about/resources\">presentations</a>  and a  <a target=\"_blank\" rel=\"noopener noreferrer\" href=\"https://www.youtube.com/watch?v=UsKkpYL7Hto\">live demo</a>  (20 minutes, in English,  <a target=\"_blank\" rel=\"noopener noreferrer\" href=\"https://www.opensource-experience.com/en/event/20-minutes-from-zero-to-live-chatbot-with-tock/\">ℹ️ abstract</a>) are also available."
            )
    }
}