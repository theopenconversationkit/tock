package fr.vsct.tock.bot.definition

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import kotlin.String
import org.litote.jackson.JacksonModuleServiceLoader

class Intent_Deserializer : StdDeserializer<Intent>(Intent::class.java),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addDeserializer(Intent::class.java, this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Intent {
        with(p) {
        var name: String? = null
        while (currentToken != JsonToken.END_OBJECT && currentToken != JsonToken.END_ARRAY) { 
        nextToken() 
        if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.END_ARRAY) { break } 
        val fieldName = currentName
        nextToken()
        if(currentToken != JsonToken.VALUE_NULL) when (fieldName) {
        "name" -> name = p.text
        else -> if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.END_ARRAY) { p.skipChildren() } else { nextToken() }
         }  }
        return Intent(name!!) }
    }
    companion object
}
