package fr.vsct.tock.nlp.core.configuration

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import java.util.Properties
import org.litote.jackson.JacksonModuleServiceLoader

class NlpModelConfiguration_Deserializer : StdDeserializer<NlpModelConfiguration>(NlpModelConfiguration::class.java),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addDeserializer(NlpModelConfiguration::class.java, this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): NlpModelConfiguration {
        with(p) {
        var properties: Properties? = null
        while (currentToken != JsonToken.END_OBJECT && currentToken != JsonToken.END_ARRAY) { 
        nextToken() 
        if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.END_ARRAY) { break } 
        val fieldName = currentName
        nextToken()
        if(currentToken != JsonToken.VALUE_NULL) when (fieldName) {
        "properties" -> properties = p.readValueAs(Properties::class.java)
        else -> if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.END_ARRAY) { p.skipChildren() } else { nextToken() }
         }  }
        return NlpModelConfiguration(properties!!) }
    }
    companion object
}
