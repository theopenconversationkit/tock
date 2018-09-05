package fr.vsct.tock.nlp.front.shared.config

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import fr.vsct.tock.nlp.core.PredefinedValue
import kotlin.String
import kotlin.collections.List
import org.litote.jackson.JacksonModuleServiceLoader
import org.litote.kmongo.Id

class EntityTypeDefinition_Deserializer : StdDeserializer<EntityTypeDefinition>(EntityTypeDefinition::class.java),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addDeserializer(EntityTypeDefinition::class.java, this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): EntityTypeDefinition {
        with(p) {
        var name: String? = null
        var description: String? = null
        var subEntities: List<EntityDefinition>? = null
        var predefinedValues: List<PredefinedValue>? = null
        var _id: Id<EntityTypeDefinition>? = null
        while (currentToken != JsonToken.END_OBJECT && currentToken != JsonToken.END_ARRAY) { 
        nextToken() 
        if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.END_ARRAY) { break } 
        val fieldName = currentName
        nextToken()
        if(currentToken != JsonToken.VALUE_NULL) when (fieldName) {
        "name" -> name = p.text
        "description" -> description = p.text
        "subEntities" -> subEntities = p.readValueAs(subEntities_reference)
        "predefinedValues" -> predefinedValues = p.readValueAs(predefinedValues_reference)
        "_id" -> _id = p.readValueAs(_id_reference)
        else -> if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.END_ARRAY) { p.skipChildren() } else { nextToken() }
         }  }
        return EntityTypeDefinition(name!!, description!!, subEntities!!, predefinedValues!!, _id!!)
                }
    }
    companion object {
        val subEntities_reference: TypeReference<List<EntityDefinition>> =
                object : TypeReference<List<EntityDefinition>>() {}

        val predefinedValues_reference: TypeReference<List<PredefinedValue>> =
                object : TypeReference<List<PredefinedValue>>() {}

        val _id_reference: TypeReference<Id<EntityTypeDefinition>> =
                object : TypeReference<Id<EntityTypeDefinition>>() {}
    }
}
