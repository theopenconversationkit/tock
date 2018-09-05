package fr.vsct.tock.nlp.front.shared.config

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import kotlin.collections.List
import org.litote.jackson.JacksonModuleServiceLoader
import org.litote.kmongo.Id

class Classification_Deserializer : StdDeserializer<Classification>(Classification::class.java),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addDeserializer(Classification::class.java, this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Classification {
        with(p) {
        var intentId: Id<IntentDefinition>? = null
        var entities: List<ClassifiedEntity>? = null
        while (currentToken != JsonToken.END_OBJECT && currentToken != JsonToken.END_ARRAY) { 
        nextToken() 
        if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.END_ARRAY) { break } 
        val fieldName = currentName
        nextToken()
        if(currentToken != JsonToken.VALUE_NULL) when (fieldName) {
        "intentId" -> intentId = p.readValueAs(intentId_reference)
        "entities" -> entities = p.readValueAs(entities_reference)
        else -> if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.END_ARRAY) { p.skipChildren() } else { nextToken() }
         }  }
        return Classification(intentId!!, entities!!) }
    }
    companion object {
        val intentId_reference: TypeReference<Id<IntentDefinition>> =
                object : TypeReference<Id<IntentDefinition>>() {}

        val entities_reference: TypeReference<List<ClassifiedEntity>> =
                object : TypeReference<List<ClassifiedEntity>>() {}
    }
}
