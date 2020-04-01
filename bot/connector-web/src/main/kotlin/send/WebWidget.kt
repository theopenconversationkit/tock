package ai.tock.bot.connector.web.send

import ai.tock.bot.engine.message.GenericMessage
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
interface WebWidget {
    val data: Any
    fun toGenericMessage(): GenericMessage = GenericMessage(texts = mapOf("widget" to this.toString()))
}