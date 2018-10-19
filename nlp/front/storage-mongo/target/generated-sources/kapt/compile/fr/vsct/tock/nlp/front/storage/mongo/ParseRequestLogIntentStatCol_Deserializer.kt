package fr.vsct.tock.nlp.front.storage.mongo

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import fr.vsct.tock.nlp.front.shared.config.ApplicationDefinition
import java.util.Locale
import kotlin.Double
import kotlin.Long
import kotlin.String
import org.litote.jackson.JacksonModuleServiceLoader
import org.litote.kmongo.Id

internal class ParseRequestLogIntentStatCol_Deserializer : StdDeserializer<ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol>(ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol::class.java),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addDeserializer(ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol::class.java, this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol {
        with(p) {
        var applicationId: Id<ApplicationDefinition>? = null
        var language: Locale? = null
        var intent1: String? = null
        var intent2: String? = null
        var averageDiff: Double? = null
        var count: Long? = null
        var _id: Id<ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol>? = null
        while (currentToken != JsonToken.END_OBJECT && currentToken != JsonToken.END_ARRAY) { 
        nextToken() 
        if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.END_ARRAY) { break } 
        val fieldName = currentName
        nextToken()
        if(currentToken != JsonToken.VALUE_NULL) when (fieldName) {
        "applicationId" -> applicationId = p.readValueAs(applicationId_reference)
        "language" -> language = p.readValueAs(Locale::class.java)
        "intent1" -> intent1 = p.text
        "intent2" -> intent2 = p.text
        "averageDiff" -> averageDiff = p.readValueAs(Double::class.java)
        "count" -> count = p.readValueAs(Long::class.java)
        "_id" -> _id = p.readValueAs(_id_reference)
        else -> if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.END_ARRAY) { p.skipChildren() } else { nextToken() }
         }  }
        return ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol(applicationId!!, language!!, intent1!!, intent2!!, averageDiff!!, count!!, _id!!)
                }
    }
    companion object {
        val applicationId_reference: TypeReference<Id<ApplicationDefinition>> =
                object : TypeReference<Id<ApplicationDefinition>>() {}

        val _id_reference: TypeReference<Id<ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol>> =
                object : TypeReference<Id<ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol>>() {}
    }
}
