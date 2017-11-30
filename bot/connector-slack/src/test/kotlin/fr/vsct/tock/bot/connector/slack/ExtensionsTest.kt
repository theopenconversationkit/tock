package fr.vsct.tock.bot.connector.slack


import fr.vsct.tock.bot.connector.slack.model.AttachmentField
import fr.vsct.tock.bot.engine.BotBus
import org.junit.Test
import org.mockito.Mockito.mock
import kotlin.test.assertEquals


class ExtensionsTest {

    val bus: BotBus = mock(BotBus::class.java)

    @Test
    fun testAttachmentMessage() {
        val field = arrayOf(AttachmentField("title", "value", false))
        val fallback = "fallback"

        val attachmentMessage = bus.attachmentMessage(*field, fallback = fallback)
        assertEquals("good", attachmentMessage.color)
        assertEquals(fallback, attachmentMessage.fallback)
        assertEquals(field.toList(), attachmentMessage.fields)
    }

    @Test
    fun testMultiLineMessage() {
        val messages = listOf("line 1", "line 2", "line 3")
        val expectedMessage = "line 1\nline 2\nline 3"
        val multiLineMessage = bus.multiLineMessage(messages)
        assertEquals(expectedMessage, multiLineMessage.text)
    }

}