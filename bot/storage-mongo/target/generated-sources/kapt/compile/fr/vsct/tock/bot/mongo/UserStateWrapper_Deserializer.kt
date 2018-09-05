package fr.vsct.tock.bot.mongo

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import java.time.Instant
import kotlin.String
import kotlin.collections.Map
import org.litote.jackson.JacksonModuleServiceLoader

internal class UserStateWrapper_Deserializer : StdDeserializer<UserTimelineCol.UserStateWrapper>(UserTimelineCol.UserStateWrapper::class.java),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addDeserializer(UserTimelineCol.UserStateWrapper::class.java, this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): UserTimelineCol.UserStateWrapper {
        with(p) {
        var creationDate: Instant? = null
        var lastUpdateDate: Instant? = null
        var flags: Map<String, UserTimelineCol.TimeBoxedFlagWrapper>? = null
        while (currentToken != JsonToken.END_OBJECT && currentToken != JsonToken.END_ARRAY) { 
        nextToken() 
        if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.END_ARRAY) { break } 
        val fieldName = currentName
        nextToken()
        if(currentToken != JsonToken.VALUE_NULL) when (fieldName) {
        "creationDate" -> creationDate = p.readValueAs(Instant::class.java)
        "lastUpdateDate" -> lastUpdateDate = p.readValueAs(Instant::class.java)
        "flags" -> flags = p.readValueAs(flags_reference)
        else -> if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.END_ARRAY) { p.skipChildren() } else { nextToken() }
         }  }
        return UserTimelineCol.UserStateWrapper(creationDate!!, lastUpdateDate!!, flags!!) }
    }
    companion object {
        val flags_reference: TypeReference<Map<String, UserTimelineCol.TimeBoxedFlagWrapper>> =
                object : TypeReference<Map<String, UserTimelineCol.TimeBoxedFlagWrapper>>() {}
    }
}
