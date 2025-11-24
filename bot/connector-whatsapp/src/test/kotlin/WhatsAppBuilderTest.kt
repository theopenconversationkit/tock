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

package ai.tock.bot.connector.whatsapp

import ai.tock.bot.connector.whatsapp.model.send.WhatsAppBotInteractiveType
import ai.tock.bot.connector.whatsapp.model.send.WhatsAppBotMessageType
import ai.tock.bot.connector.whatsapp.model.send.WhatsAppBotRecipientType
import ai.tock.bot.definition.Intent
import ai.tock.bot.engine.BotBus
import ai.tock.bot.engine.user.UserPreferences
import ai.tock.shared.sharedTestModule
import ai.tock.shared.tockInternalInjector
import ai.tock.translator.raw
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.KodeinInjector
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class WhatsAppBuilderTest {
    val bus: BotBus = mockk(relaxed = true)
    val longText =
        "Lorem ipsum dolor sit amet, consectetur adipiscing elit. " +
            "Curabitur vitae augue dignissim, ultrices tortor sed, fringilla neque. Maecenas sit amet efficitur enim. " +
            "Pellentesque nec dictum tellus. Etiam rhoncus arcu nunc, eget sagittis lacus rutrum ac. Aenean eu ipsum " +
            "lorem. Vestibulum condimentum, ligula in euismod auctor, felis ante semper erat, ut laoreet est libero " +
            "ut tellus. Duis sollicitudin justo id est lobortis sollicitudin. Fusce gravida sagittis nibh id tempor. " +
            "Proin a imperdiet est. In arcu est, imperdiet quis pellentesque vitae, tincidunt sit amet massa. Nunc " +
            "laoreet orci eu fringilla auctor. Aliquam interdum odio vel metus."

    @BeforeEach
    fun before() {
        tockInternalInjector =
            KodeinInjector().apply {
                inject(
                    Kodein {
                        import(sharedTestModule)
                    },
                )
            }

        every { bus.targetConnectorType } returns whatsAppConnectorType
        every { bus.isCompatibleWith(whatsAppConnectorType) } returns true
        every { bus.applicationId } returns "appId"
        every { bus.userPreferences } returns UserPreferences()
        every { bus.translate(allAny()) } answers { firstArg() ?: "".raw }
        every { bus.translate(any<CharSequence>()) } answers { firstArg<CharSequence?>()?.raw ?: "".raw }
    }

    @AfterEach
    fun after() {
        tockInternalInjector = KodeinInjector()
    }

    @Test
    fun `create a reply button message`() {
        val result = bus.replyButtonMessage("text", bus.nlpQuickReply("title1"), bus.quickReply("title2", targetIntent = Intent("intent")))

        assertEquals(WhatsAppBotMessageType.interactive, result.type)
        assertEquals(WhatsAppBotRecipientType.individual, result.recipientType)
        assertEquals(WhatsAppBotInteractiveType.button, result.interactive.type)
        assertEquals("text", result.interactive.body?.text)
        assertEquals(2, result.interactive.action?.buttons?.size)
        assertEquals("title1", result.interactive.action?.buttons?.get(0)?.reply?.title)
        assertEquals("?_nlp=title1", result.interactive.action?.buttons?.get(0)?.reply?.id)
        assertEquals("title2", result.interactive.action?.buttons?.get(1)?.reply?.title)
        assertEquals("intent", result.interactive.action?.buttons?.get(1)?.reply?.id)
    }

    @Test
    fun `create a reply button message with long texts`() {
        assertThrows<IllegalStateException> {
            val quickReply = bus.nlpQuickReply(longText)

            bus.replyButtonMessage("text", quickReply)
        }
    }

    @Test
    fun `create a list message`() {
        val result = bus.listMessage("text", "button", bus.nlpQuickReply("title1"), bus.quickReply("title2", subTitle = "subtitle", targetIntent = Intent("intent")))

        assertEquals(WhatsAppBotMessageType.interactive, result.type)
        assertEquals(WhatsAppBotRecipientType.individual, result.recipientType)
        assertEquals(WhatsAppBotInteractiveType.list, result.interactive.type)
        assertEquals("text", result.interactive.body?.text)
        assertEquals(1, result.interactive.action?.sections?.size)
        assertEquals("title1", result.interactive.action?.sections?.get(0)?.rows?.get(0)?.title)
        assertEquals("?_nlp=title1", result.interactive.action?.sections?.get(0)?.rows?.get(0)?.id)
        assertEquals("title2", result.interactive.action?.sections?.get(0)?.rows?.get(1)?.title)
        assertEquals("subtitle", result.interactive.action?.sections?.get(0)?.rows?.get(1)?.description)
        assertEquals("intent", result.interactive.action?.sections?.get(0)?.rows?.get(1)?.id)
    }

    @Test
    fun `create a list message with too long texts`() {
        assertThrows<IllegalStateException> {
            val quickReply = bus.quickReply(longText, longText, Intent(longText))

            bus.listMessage("text", longText, quickReply)
        }
    }
}
