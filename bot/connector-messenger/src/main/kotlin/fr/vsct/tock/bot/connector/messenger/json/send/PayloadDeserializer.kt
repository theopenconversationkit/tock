package fr.vsct.tock.bot.connector.messenger.json.send

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import fr.vsct.tock.bot.connector.messenger.model.send.Button
import fr.vsct.tock.bot.connector.messenger.model.send.ButtonPayload
import fr.vsct.tock.bot.connector.messenger.model.send.Element
import fr.vsct.tock.bot.connector.messenger.model.send.GenericPayload
import fr.vsct.tock.bot.connector.messenger.model.send.ModelType
import fr.vsct.tock.bot.connector.messenger.model.send.Payload
import fr.vsct.tock.bot.connector.messenger.model.send.UrlPayload
import fr.vsct.tock.shared.jackson.readListValuesAs
import fr.vsct.tock.shared.jackson.readValueAs
import mu.KotlinLogging

/**
 *
 */
class PayloadDeserializer : JsonDeserializer<Payload>() {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override fun deserialize(jp: JsonParser, ctxt: DeserializationContext): Payload? {
        var templateType: ModelType? = null
        var url: String? = null
        var text: String? = null
        var buttons: List<Button>? = null
        var elements: List<Element>? = null

        while (jp.nextValue() != JsonToken.END_OBJECT) {
            when (jp.currentName) {
                "template_type" -> templateType = jp.readValueAs(ModelType::class)
                UrlPayload::url.name -> url = jp.valueAsString
                GenericPayload::elements.name -> elements = jp.readListValuesAs()
                ButtonPayload::buttons.name -> buttons = jp.readListValuesAs()
                ButtonPayload::text.name -> text = jp.valueAsString
                else -> logger.warn { "Unsupported field : ${jp.currentName}" }
            }
        }

        return if (templateType != null) {
            when (templateType) {
                ModelType.generic -> GenericPayload(elements ?: emptyList())
                ModelType.button -> ButtonPayload(text ?: "", buttons ?: emptyList())
            }
        } else if (url != null) {
            UrlPayload(url)
        } else {
            logger.warn { "invalid message" }
            null
        }
    }
}