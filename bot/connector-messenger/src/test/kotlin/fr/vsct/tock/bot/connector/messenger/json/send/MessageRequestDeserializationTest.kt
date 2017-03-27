package fr.vsct.tock.bot.connector.messenger.json.send

import fr.vsct.tock.bot.connector.messenger.model.Recipient
import fr.vsct.tock.bot.connector.messenger.model.send.Attachment
import fr.vsct.tock.bot.connector.messenger.model.send.AttachmentMessage
import fr.vsct.tock.bot.connector.messenger.model.send.AttachmentType
import fr.vsct.tock.bot.connector.messenger.model.send.Element
import fr.vsct.tock.bot.connector.messenger.model.send.GenericPayload
import fr.vsct.tock.bot.connector.messenger.model.send.MessageRequest
import fr.vsct.tock.bot.connector.messenger.model.send.PostbackButton
import fr.vsct.tock.bot.connector.messenger.model.send.UrlPayload
import fr.vsct.tock.shared.jackson.mapper
import org.junit.Test
import kotlin.test.assertEquals

/**
 *
 */
class MessageRequestDeserializationTest {

    @Test
    fun testMessageRequestWithButtonDeserialization() {
        val m = MessageRequest(Recipient("2"), AttachmentMessage(Attachment(AttachmentType.template, GenericPayload(listOf(Element("title", buttons = listOf(PostbackButton("payload", "titleButton"))))))))
        val s = mapper.writeValueAsString(m)
        println(s)
        assertEquals(m, mapper.readValue(s, MessageRequest::class.java))
    }

    @Test
    fun testMessageRequestWithUrlPayload() {
        val m = MessageRequest(Recipient("2"), AttachmentMessage(Attachment(AttachmentType.image, UrlPayload("http://test/test.png"))))
        val s = mapper.writeValueAsString(m)
        println(s)
        assertEquals(m, mapper.readValue(s, MessageRequest::class.java))
    }
}