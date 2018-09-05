package fr.vsct.tock.bot.mongo

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import kotlin.String
import kotlin.collections.Set
import org.litote.jackson.JacksonModuleServiceLoader
import org.litote.kmongo.Id

internal class ClientIdCol_Deserializer : StdDeserializer<ClientIdCol>(ClientIdCol::class.java),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addDeserializer(ClientIdCol::class.java, this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): ClientIdCol {
        with(p) {
        var userIds: Set<String>? = null
        var _id: Id<ClientIdCol>? = null
        while (currentToken != JsonToken.END_OBJECT && currentToken != JsonToken.END_ARRAY) { 
        nextToken() 
        if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.END_ARRAY) { break } 
        val fieldName = currentName
        nextToken()
        if(currentToken != JsonToken.VALUE_NULL) when (fieldName) {
        "userIds" -> userIds = p.readValueAs(userIds_reference)
        "_id" -> _id = p.readValueAs(_id_reference)
        else -> if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.END_ARRAY) { p.skipChildren() } else { nextToken() }
         }  }
        return ClientIdCol(userIds!!, _id!!) }
    }
    companion object {
        val userIds_reference: TypeReference<Set<String>> = object : TypeReference<Set<String>>() {}

        val _id_reference: TypeReference<Id<ClientIdCol>> =
                object : TypeReference<Id<ClientIdCol>>() {}
    }
}
