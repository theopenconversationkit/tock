package fr.vsct.tock.bot.engine.user

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import kotlin.String
import org.litote.jackson.JacksonModuleServiceLoader

class PlayerId_Deserializer : StdDeserializer<PlayerId>(PlayerId::class.java),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addDeserializer(PlayerId::class.java, this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): PlayerId {
        with(p) {
        var id: String? = null
        var type: PlayerType? = null
        var clientId: String? = null
        while (currentToken != JsonToken.END_OBJECT && currentToken != JsonToken.END_ARRAY) { 
        nextToken() 
        if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.END_ARRAY) { break } 
        val fieldName = currentName
        nextToken()
        if(currentToken != JsonToken.VALUE_NULL) when (fieldName) {
        "id" -> id = p.text
        "type" -> type = p.readValueAs(PlayerType::class.java)
        "clientId" -> clientId = p.text
        else -> if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.END_ARRAY) { p.skipChildren() } else { nextToken() }
         }  }
        return PlayerId(id!!, type!!, clientId) }
    }
    companion object
}
