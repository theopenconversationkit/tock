package fr.vsct.tock.nlp.core.configuration

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import org.litote.jackson.JacksonModuleServiceLoader

class NlpApplicationConfiguration_Deserializer : StdDeserializer<NlpApplicationConfiguration>(NlpApplicationConfiguration::class.java),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addDeserializer(NlpApplicationConfiguration::class.java, this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): NlpApplicationConfiguration {
        with(p) {
        var tokenizerConfiguration: NlpModelConfiguration? = null
        var intentConfiguration: NlpModelConfiguration? = null
        var entityConfiguration: NlpModelConfiguration? = null
        while (currentToken != JsonToken.END_OBJECT && currentToken != JsonToken.END_ARRAY) { 
        nextToken() 
        if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.END_ARRAY) { break } 
        val fieldName = currentName
        nextToken()
        if(currentToken != JsonToken.VALUE_NULL) when (fieldName) {
        "tokenizerConfiguration" -> tokenizerConfiguration = p.readValueAs(NlpModelConfiguration::class.java)
        "intentConfiguration" -> intentConfiguration = p.readValueAs(NlpModelConfiguration::class.java)
        "entityConfiguration" -> entityConfiguration = p.readValueAs(NlpModelConfiguration::class.java)
        else -> if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.END_ARRAY) { p.skipChildren() } else { nextToken() }
         }  }
        return NlpApplicationConfiguration(tokenizerConfiguration!!, intentConfiguration!!, entityConfiguration!!)
                }
    }
    companion object
}
