package fr.vsct.tock.nlp.front.shared.config

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import kotlin.Int
import kotlin.String
import kotlin.collections.List
import org.litote.jackson.JacksonModuleServiceLoader

class ClassifiedEntity_Deserializer : StdDeserializer<ClassifiedEntity>(ClassifiedEntity::class.java),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addDeserializer(ClassifiedEntity::class.java, this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): ClassifiedEntity {
        with(p) {
        var type: String? = null
        var role: String? = null
        var start: Int? = null
        var end: Int? = null
        var subEntities: List<ClassifiedEntity>? = null
        while (currentToken != JsonToken.END_OBJECT && currentToken != JsonToken.END_ARRAY) { 
        nextToken() 
        if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.END_ARRAY) { break } 
        val fieldName = currentName
        nextToken()
        if(currentToken != JsonToken.VALUE_NULL) when (fieldName) {
        "type" -> type = p.text
        "role" -> role = p.text
        "start" -> start = p.readValueAs(Int::class.java)
        "end" -> end = p.readValueAs(Int::class.java)
        "subEntities" -> subEntities = p.readValueAs(subEntities_reference)
        else -> if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.END_ARRAY) { p.skipChildren() } else { nextToken() }
         }  }
        return ClassifiedEntity(type!!, role!!, start!!, end!!, subEntities!!) }
    }
    companion object {
        val subEntities_reference: TypeReference<List<ClassifiedEntity>> =
                object : TypeReference<List<ClassifiedEntity>>() {}
    }
}
