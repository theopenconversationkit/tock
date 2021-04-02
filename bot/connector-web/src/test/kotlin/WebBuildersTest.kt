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

import ai.tock.bot.connector.web.HrefTargetType
import ai.tock.bot.connector.web.send.ButtonStyle
import ai.tock.bot.connector.web.send.ButtonType
import ai.tock.bot.connector.web.webIntentQuickReply
import ai.tock.bot.connector.web.webNlpQuickReply
import ai.tock.bot.connector.web.webPostbackButton
import ai.tock.bot.connector.web.webUrlButton
import ai.tock.bot.definition.Intent
import ai.tock.bot.engine.BotBus
import io.mockk.mockk
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class WebBuildersTest {

    val bus: BotBus = mockk(relaxed = true)

    @Nested
    inner class WebUrlButtonTests {

        @Test
        fun `webUrlButton with no target nor style`() {
            val webUrlButton = bus.webUrlButton(
                "title", "https://ab.c.de"
            )
            Assertions.assertThat(webUrlButton.type).isEqualTo(ButtonType.web_url)
            Assertions.assertThat(webUrlButton).hasFieldOrPropertyWithValue("target", "_blank")
            Assertions.assertThat(webUrlButton).hasFieldOrPropertyWithValue("style", "primary")
        }

        @Test
        fun `webUrlButton with no target but style`() {
            val webUrlButton = bus.webUrlButton(
                "title", "https://ab.c.de", style = ButtonStyle.secondary
            )
            Assertions.assertThat(webUrlButton.type).isEqualTo(ButtonType.web_url)
            Assertions.assertThat(webUrlButton).hasFieldOrPropertyWithValue("style", "secondary")
        }

        @Test
        fun `webUrlButton with target but no style`() {
            val webUrlButton = bus.webUrlButton(
                "title", "https://ab.c.de", target = HrefTargetType._self
            )
            Assertions.assertThat(webUrlButton.type).isEqualTo(ButtonType.web_url)
            Assertions.assertThat(webUrlButton).hasFieldOrPropertyWithValue("target", "_self")
        }

        @Test
        fun `webUrlButton with target and style`() {
            val webUrlButton = bus.webUrlButton(
                "title", "https://ab.c.de", target = HrefTargetType._self, style = ButtonStyle.secondary
            )
            Assertions.assertThat(webUrlButton.type).isEqualTo(ButtonType.web_url)
            Assertions.assertThat(webUrlButton).hasFieldOrPropertyWithValue("target", "_self")
            Assertions.assertThat(webUrlButton).hasFieldOrPropertyWithValue("style", "secondary")
        }

        @Test
        fun `webUrlButton with string target and style`() {
            val webUrlButton = bus.webUrlButton(
                "title", "https://ab.c.de", target = "target", style = "style"
            )
            Assertions.assertThat(webUrlButton.type).isEqualTo(ButtonType.web_url)
            Assertions.assertThat(webUrlButton).hasFieldOrPropertyWithValue("target", "target")
            Assertions.assertThat(webUrlButton).hasFieldOrPropertyWithValue("style", "style")
        }

        @Test
        fun `webUrlButton with null target and style`() {
            val webUrlButton = bus.webUrlButton(
                "title", "https://ab.c.de", target = null, style = null
            )
            Assertions.assertThat(webUrlButton.type).isEqualTo(ButtonType.web_url)
            Assertions.assertThat(webUrlButton).hasFieldOrPropertyWithValue("target", null)
            Assertions.assertThat(webUrlButton).hasFieldOrPropertyWithValue("style", null)
        }
    }

    @Nested
    inner class WebPostbackButtonTests {

        @Test
        fun `webPostbackButton with no style`() {
            val webPostbackButton = bus.webPostbackButton(
                "title", Intent("intent")
            )
            Assertions.assertThat(webPostbackButton.type).isEqualTo(ButtonType.postback)
            Assertions.assertThat(webPostbackButton).hasFieldOrPropertyWithValue("style", "primary")
        }

        @Test
        fun `webPostbackButton with style`() {
            val webPostbackButton = bus.webPostbackButton(
                "title", Intent("intent"),  style = ButtonStyle.secondary
            )
            Assertions.assertThat(webPostbackButton.type).isEqualTo(ButtonType.postback)
            Assertions.assertThat(webPostbackButton).hasFieldOrPropertyWithValue("style", "secondary")
        }

        @Test
        fun `webPostbackButton with string style`() {
            val webPostbackButton = bus.webPostbackButton(
                "title", Intent("intent"),  style = "style"
            )
            Assertions.assertThat(webPostbackButton.type).isEqualTo(ButtonType.postback)
            Assertions.assertThat(webPostbackButton).hasFieldOrPropertyWithValue("style", "style")
        }

        @Test
        fun `webPostbackButton with null style`() {
            val webPostbackButton = bus.webPostbackButton(
                "title", Intent("intent"),  style = null
            )
            Assertions.assertThat(webPostbackButton.type).isEqualTo(ButtonType.postback)
            Assertions.assertThat(webPostbackButton).hasFieldOrPropertyWithValue("style", null)
        }
    }

    @Nested
    inner class WebIntentQuickReplyTests {

        @Test
        fun `webIntentQuickReply with no style`() {
            val webIntentQuickReply = bus.webIntentQuickReply(
                "title", Intent("intent")
            )
            Assertions.assertThat(webIntentQuickReply.type).isEqualTo(ButtonType.quick_reply)
            Assertions.assertThat(webIntentQuickReply).hasFieldOrPropertyWithValue("style", "primary")
        }

        @Test
        fun `webIntentQuickReply with style`() {
            val webIntentQuickReply = bus.webIntentQuickReply(
                "title", Intent("intent"),  style = ButtonStyle.secondary
            )
            Assertions.assertThat(webIntentQuickReply.type).isEqualTo(ButtonType.quick_reply)
            Assertions.assertThat(webIntentQuickReply).hasFieldOrPropertyWithValue("style", "secondary")
        }

        @Test
        fun `webIntentQuickReply with string style`() {
            val webIntentQuickReply = bus.webIntentQuickReply(
                "title", Intent("intent"),  style = "style"
            )
            Assertions.assertThat(webIntentQuickReply.type).isEqualTo(ButtonType.quick_reply)
            Assertions.assertThat(webIntentQuickReply).hasFieldOrPropertyWithValue("style", "style")
        }

        @Test
        fun `webIntentQuickReply with null style`() {
            val webIntentQuickReply = bus.webIntentQuickReply(
                "title", Intent("intent"),  style = null
            )
            Assertions.assertThat(webIntentQuickReply.type).isEqualTo(ButtonType.quick_reply)
            Assertions.assertThat(webIntentQuickReply).hasFieldOrPropertyWithValue("style", null)
        }
    }

    @Nested
    inner class WebNlpQuickReplyTests {

        @Test
        fun `webNlpQuickReply with no style`() {
            val webNlpQuickReply = bus.webNlpQuickReply(
                "title"
            )
            Assertions.assertThat(webNlpQuickReply.type).isEqualTo(ButtonType.quick_reply)
            Assertions.assertThat(webNlpQuickReply).hasFieldOrPropertyWithValue("style", "primary")
        }

        @Test
        fun `webNlpQuickReply with style`() {
            val webNlpQuickReply = bus.webNlpQuickReply(
                "title",  style = ButtonStyle.secondary
            )
            Assertions.assertThat(webNlpQuickReply.type).isEqualTo(ButtonType.quick_reply)
            Assertions.assertThat(webNlpQuickReply).hasFieldOrPropertyWithValue("style", "secondary")
        }

        @Test
        fun `webNlpQuickReply with string style`() {
            val webNlpQuickReply = bus.webNlpQuickReply(
                "title",  style = "style"
            )
            Assertions.assertThat(webNlpQuickReply.type).isEqualTo(ButtonType.quick_reply)
            Assertions.assertThat(webNlpQuickReply).hasFieldOrPropertyWithValue("style", "style")
        }

        @Test
        fun `webNlpQuickReply with null style`() {
            val webNlpQuickReply = bus.webNlpQuickReply(
                "title",  style = null
            )
            Assertions.assertThat(webNlpQuickReply.type).isEqualTo(ButtonType.quick_reply)
            Assertions.assertThat(webNlpQuickReply).hasFieldOrPropertyWithValue("style", null)
        }
    }
}