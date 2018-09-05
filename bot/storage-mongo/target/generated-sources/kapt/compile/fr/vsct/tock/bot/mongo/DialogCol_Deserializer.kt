package fr.vsct.tock.bot.mongo

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import fr.vsct.tock.bot.engine.dialog.Dialog
import fr.vsct.tock.bot.engine.user.PlayerId
import java.time.Instant
import kotlin.String
import kotlin.collections.List
import kotlin.collections.Set
import org.litote.jackson.JacksonModuleServiceLoader
import org.litote.kmongo.Id

internal class DialogCol_Deserializer : StdDeserializer<DialogCol>(DialogCol::class.java),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addDeserializer(DialogCol::class.java, this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): DialogCol {
        with(p) {
        var playerIds: Set<PlayerId>? = null
        var _id: Id<Dialog>? = null
        var state: DialogCol.DialogStateMongoWrapper? = null
        var stories: List<DialogCol.StoryMongoWrapper>? = null
        var applicationIds: Set<String>? = null
        var lastUpdateDate: Instant? = null
        while (currentToken != JsonToken.END_OBJECT && currentToken != JsonToken.END_ARRAY) { 
        nextToken() 
        if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.END_ARRAY) { break } 
        val fieldName = currentName
        nextToken()
        if(currentToken != JsonToken.VALUE_NULL) when (fieldName) {
        "playerIds" -> playerIds = p.readValueAs(playerIds_reference)
        "_id" -> _id = p.readValueAs(_id_reference)
        "state" -> state = p.readValueAs(DialogCol.DialogStateMongoWrapper::class.java)
        "stories" -> stories = p.readValueAs(stories_reference)
        "applicationIds" -> applicationIds = p.readValueAs(applicationIds_reference)
        "lastUpdateDate" -> lastUpdateDate = p.readValueAs(Instant::class.java)
        else -> if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.END_ARRAY) { p.skipChildren() } else { nextToken() }
         }  }
        return DialogCol(playerIds!!, _id!!, state!!, stories!!, applicationIds!!, lastUpdateDate!!)
                }
    }
    companion object {
        val playerIds_reference: TypeReference<Set<PlayerId>> =
                object : TypeReference<Set<PlayerId>>() {}

        val _id_reference: TypeReference<Id<Dialog>> = object : TypeReference<Id<Dialog>>() {}

        val stories_reference: TypeReference<List<DialogCol.StoryMongoWrapper>> =
                object : TypeReference<List<DialogCol.StoryMongoWrapper>>() {}

        val applicationIds_reference: TypeReference<Set<String>> =
                object : TypeReference<Set<String>>() {}
    }
}
