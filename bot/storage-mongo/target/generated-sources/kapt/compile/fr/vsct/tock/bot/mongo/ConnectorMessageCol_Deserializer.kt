package fr.vsct.tock.bot.mongo

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import fr.vsct.tock.shared.jackson.AnyValueWrapper
import java.time.Instant
import kotlin.collections.List
import org.litote.jackson.JacksonModuleServiceLoader

internal class ConnectorMessageCol_Deserializer : StdDeserializer<ConnectorMessageCol>(ConnectorMessageCol::class.java),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addDeserializer(ConnectorMessageCol::class.java, this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): ConnectorMessageCol {
        with(p) {
        var _id: ConnectorMessageColId? = null
        var messages: List<AnyValueWrapper>? = null
        var date: Instant? = null
        while (currentToken != JsonToken.END_OBJECT && currentToken != JsonToken.END_ARRAY) { 
        nextToken() 
        if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.END_ARRAY) { break } 
        val fieldName = currentName
        nextToken()
        if(currentToken != JsonToken.VALUE_NULL) when (fieldName) {
        "_id" -> _id = p.readValueAs(ConnectorMessageColId::class.java)
        "messages" -> messages = p.readValueAs(messages_reference)
        "date" -> date = p.readValueAs(Instant::class.java)
        else -> if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.END_ARRAY) { p.skipChildren() } else { nextToken() }
         }  }
        return ConnectorMessageCol(_id!!, messages!!, date!!) }
    }
    companion object {
        val messages_reference: TypeReference<List<AnyValueWrapper>> =
                object : TypeReference<List<AnyValueWrapper>>() {}
    }
}
