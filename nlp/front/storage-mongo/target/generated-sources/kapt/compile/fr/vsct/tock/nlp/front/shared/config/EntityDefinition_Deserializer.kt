package fr.vsct.tock.nlp.front.shared.config

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import kotlin.Boolean
import kotlin.String
import org.litote.jackson.JacksonModuleServiceLoader

class EntityDefinition_Deserializer : StdDeserializer<EntityDefinition>(EntityDefinition::class.java),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addDeserializer(EntityDefinition::class.java, this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): EntityDefinition {
        with(p) {
        var entityTypeName: String? = null
        var role: String? = null
        var atStartOfDay: Boolean? = null
        while (currentToken != JsonToken.END_OBJECT && currentToken != JsonToken.END_ARRAY) { 
        nextToken() 
        if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.END_ARRAY) { break } 
        val fieldName = currentName
        nextToken()
        if(currentToken != JsonToken.VALUE_NULL) when (fieldName) {
        "entityTypeName" -> entityTypeName = p.text
        "role" -> role = p.text
        "atStartOfDay" -> atStartOfDay = p.booleanValue
        else -> if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.END_ARRAY) { p.skipChildren() } else { nextToken() }
         }  }
        return EntityDefinition(entityTypeName!!, role!!, atStartOfDay) }
    }
    companion object
}
