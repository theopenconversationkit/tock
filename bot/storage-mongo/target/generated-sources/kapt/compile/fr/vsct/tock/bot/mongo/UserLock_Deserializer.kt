package fr.vsct.tock.bot.mongo

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import java.time.Instant
import kotlin.Boolean
import org.litote.jackson.JacksonModuleServiceLoader
import org.litote.kmongo.Id

internal class UserLock_Deserializer : StdDeserializer<MongoUserLock.UserLock>(MongoUserLock.UserLock::class.java),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addDeserializer(MongoUserLock.UserLock::class.java, this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): MongoUserLock.UserLock {
        with(p) {
        var _id: Id<MongoUserLock.UserLock>? = null
        var locked: Boolean? = null
        var date: Instant? = null
        while (currentToken != JsonToken.END_OBJECT && currentToken != JsonToken.END_ARRAY) { 
        nextToken() 
        if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.END_ARRAY) { break } 
        val fieldName = currentName
        nextToken()
        if(currentToken != JsonToken.VALUE_NULL) when (fieldName) {
        "_id" -> _id = p.readValueAs(_id_reference)
        "locked" -> locked = p.readValueAs(Boolean::class.java)
        "date" -> date = p.readValueAs(Instant::class.java)
        else -> if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.END_ARRAY) { p.skipChildren() } else { nextToken() }
         }  }
        return MongoUserLock.UserLock(_id!!, locked!!, date!!) }
    }
    companion object {
        val _id_reference: TypeReference<Id<MongoUserLock.UserLock>> =
                object : TypeReference<Id<MongoUserLock.UserLock>>() {}
    }
}
