package ai.tock.bot.connector.web.send

import ai.tock.bot.engine.message.GenericMessage
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
interface WebCustom {
    fun toGenericMessage(): GenericMessage = GenericMessage(texts = mapOf("custom" to this.toString()))
}