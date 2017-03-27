package fr.vsct.tock.bot.connector.messenger.json.send

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import fr.vsct.tock.bot.connector.messenger.model.send.Attachment
import fr.vsct.tock.bot.connector.messenger.model.send.AttachmentMessage
import fr.vsct.tock.bot.connector.messenger.model.send.Message
import fr.vsct.tock.bot.connector.messenger.model.send.TextMessage
import fr.vsct.tock.shared.jackson.readValueAs
import mu.KotlinLogging

/**
 *
 */
class MessageDeserializer : JsonDeserializer<Message>() {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override fun deserialize(jp: JsonParser, ctxt: DeserializationContext): Message? {
        var text: String? = null
        var attachment: Attachment? = null

        while (jp.nextValue() != JsonToken.END_OBJECT) {
            when (jp.currentName) {
                TextMessage::text.name -> text = jp.valueAsString
                AttachmentMessage::attachment.name -> attachment = jp.readValueAs(Attachment::class)
                else -> logger.warn { "Unsupported field : ${jp.currentName}" }
            }
        }

        return if (text != null) {
            TextMessage(text)
        } else if (attachment != null) {
            AttachmentMessage(attachment)
        } else {
            logger.warn { "invalid message" }
            null
        }
    }

}