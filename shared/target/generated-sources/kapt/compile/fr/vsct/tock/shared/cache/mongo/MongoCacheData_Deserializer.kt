package fr.vsct.tock.shared.cache.mongo

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import fr.vsct.tock.shared.jackson.AnyValueWrapper
import java.time.Instant
import kotlin.ByteArray
import kotlin.String
import org.litote.jackson.JacksonModuleServiceLoader
import org.litote.kmongo.Id

internal class MongoCacheData_Deserializer : StdDeserializer<MongoCacheData>(MongoCacheData::class.java),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addDeserializer(MongoCacheData::class.java, this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): MongoCacheData {
        with(p) {
        var id: Id<*>? = null
        var type: String? = null
        var s: String? = null
        var b: ByteArray? = null
        var a: AnyValueWrapper? = null
        var date: Instant? = null
        while (currentToken != JsonToken.END_OBJECT && currentToken != JsonToken.END_ARRAY) { 
        nextToken() 
        if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.END_ARRAY) { break } 
        val fieldName = currentName
        nextToken()
        if(currentToken != JsonToken.VALUE_NULL) when (fieldName) {
        "id" -> id = p.readValueAs(id_reference)
        "type" -> type = p.text
        "s" -> s = p.text
        "b" -> b = p.readValueAs(b_reference)
        "a" -> a = p.readValueAs(AnyValueWrapper::class.java)
        "date" -> date = p.readValueAs(Instant::class.java)
        else -> if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.END_ARRAY) { p.skipChildren() } else { nextToken() }
         }  }
        return MongoCacheData(id!!, type!!, s, b, a, date!!) }
    }
    companion object {
        val id_reference: TypeReference<Id<*>> = object : TypeReference<Id<*>>() {}

        val b_reference: TypeReference<ByteArray> = object : TypeReference<ByteArray>() {}
    }
}
