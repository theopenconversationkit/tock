package fr.vsct.tock.bot.mongo

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import kotlin.Boolean
import kotlin.String
import org.litote.jackson.JacksonModuleServiceLoader

internal class Feature_Deserializer : StdDeserializer<Feature>(Feature::class.java),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addDeserializer(Feature::class.java, this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Feature {
        with(p) {
        var _id: String? = null
        var key: String? = null
        var enabled: Boolean? = null
        var botId: String? = null
        var namespace: String? = null
        while (currentToken != JsonToken.END_OBJECT && currentToken != JsonToken.END_ARRAY) { 
        nextToken() 
        if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.END_ARRAY) { break } 
        val fieldName = currentName
        nextToken()
        if(currentToken != JsonToken.VALUE_NULL) when (fieldName) {
        "_id" -> _id = p.text
        "key" -> key = p.text
        "enabled" -> enabled = p.readValueAs(Boolean::class.java)
        "botId" -> botId = p.text
        "namespace" -> namespace = p.text
        else -> if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.END_ARRAY) { p.skipChildren() } else { nextToken() }
         }  }
        return Feature(_id!!, key!!, enabled!!, botId!!, namespace!!) }
    }
    companion object
}
