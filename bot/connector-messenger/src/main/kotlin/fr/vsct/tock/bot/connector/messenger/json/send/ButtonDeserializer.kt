package fr.vsct.tock.bot.connector.messenger.json.send

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import fr.vsct.tock.bot.connector.messenger.model.send.Button
import fr.vsct.tock.bot.connector.messenger.model.send.ButtonType
import fr.vsct.tock.bot.connector.messenger.model.send.PostbackButton
import fr.vsct.tock.bot.connector.messenger.model.send.UrlButton
import fr.vsct.tock.shared.jackson.readValueAs
import mu.KotlinLogging

/**
 *
 */
class ButtonDeserializer : JsonDeserializer<Button>() {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override fun deserialize(jp: JsonParser, ctxt: DeserializationContext): Button? {
        var type: ButtonType? = null
        var url: String? = null
        var title: String? = null
        var payload: String? = null

        while (jp.nextValue() != JsonToken.END_OBJECT) {
            when (jp.currentName) {
                Button::type.name -> type = jp.readValueAs(ButtonType::class)
                UrlButton::url.name -> url = jp.valueAsString
                UrlButton::title.name -> title = jp.valueAsString
                PostbackButton::payload.name -> payload = jp.valueAsString
                else -> logger.warn { "Unsupported field : ${jp.currentName}" }
            }
        }

        return if (type != null) {
            when (type) {
                ButtonType.postback -> PostbackButton(payload ?: "", title ?: "")
                ButtonType.web_url -> UrlButton(url ?: "", title ?: "")
            }
        } else {
            logger.warn { "invalid button" }
            null
        }
    }
}