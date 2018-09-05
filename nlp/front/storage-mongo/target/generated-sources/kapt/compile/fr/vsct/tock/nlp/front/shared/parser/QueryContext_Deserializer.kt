package fr.vsct.tock.nlp.front.shared.parser

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Locale
import kotlin.Boolean
import kotlin.String
import org.litote.jackson.JacksonModuleServiceLoader

class QueryContext_Deserializer : StdDeserializer<QueryContext>(QueryContext::class.java),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addDeserializer(QueryContext::class.java, this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): QueryContext {
        with(p) {
        var language: Locale? = null
        var clientId: String? = null
        var clientDevice: String? = null
        var dialogId: String? = null
        var referenceDate: ZonedDateTime? = null
        var referenceTimezone: ZoneId? = null
        var test: Boolean? = null
        var registerQuery: Boolean? = null
        var checkExistingQuery: Boolean? = null
        var increaseQueryCounter: Boolean? = null
        while (currentToken != JsonToken.END_OBJECT && currentToken != JsonToken.END_ARRAY) { 
        nextToken() 
        if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.END_ARRAY) { break } 
        val fieldName = currentName
        nextToken()
        if(currentToken != JsonToken.VALUE_NULL) when (fieldName) {
        "language" -> language = p.readValueAs(Locale::class.java)
        "clientId" -> clientId = p.text
        "clientDevice" -> clientDevice = p.text
        "dialogId" -> dialogId = p.text
        "referenceDate" -> referenceDate = p.readValueAs(ZonedDateTime::class.java)
        "referenceTimezone" -> referenceTimezone = p.readValueAs(ZoneId::class.java)
        "test" -> test = p.readValueAs(Boolean::class.java)
        "registerQuery" -> registerQuery = p.readValueAs(Boolean::class.java)
        "checkExistingQuery" -> checkExistingQuery = p.readValueAs(Boolean::class.java)
        "increaseQueryCounter" -> increaseQueryCounter = p.readValueAs(Boolean::class.java)
        else -> if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.END_ARRAY) { p.skipChildren() } else { nextToken() }
         }  }
        return QueryContext(language!!, clientId!!, clientDevice, dialogId!!, referenceDate!!, referenceTimezone!!, test!!, registerQuery!!, checkExistingQuery!!, increaseQueryCounter!!)
                }
    }
    companion object
}
