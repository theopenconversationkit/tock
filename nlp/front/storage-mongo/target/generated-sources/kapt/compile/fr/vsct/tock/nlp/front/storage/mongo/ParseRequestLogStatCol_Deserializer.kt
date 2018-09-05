package fr.vsct.tock.nlp.front.storage.mongo

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import fr.vsct.tock.nlp.front.shared.config.ApplicationDefinition
import java.time.Instant
import java.util.Locale
import kotlin.Double
import kotlin.Long
import kotlin.String
import org.litote.jackson.JacksonModuleServiceLoader
import org.litote.kmongo.Id

internal class ParseRequestLogStatCol_Deserializer : StdDeserializer<ParseRequestLogMongoDAO.ParseRequestLogStatCol>(ParseRequestLogMongoDAO.ParseRequestLogStatCol::class.java),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addDeserializer(ParseRequestLogMongoDAO.ParseRequestLogStatCol::class.java, this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): ParseRequestLogMongoDAO.ParseRequestLogStatCol {
        with(p) {
        var text: String? = null
        var applicationId: Id<ApplicationDefinition>? = null
        var language: Locale? = null
        var intentProbability: Double? = null
        var entitiesProbability: Double? = null
        var lastUsage: Instant? = null
        var count: Long? = null
        while (currentToken != JsonToken.END_OBJECT && currentToken != JsonToken.END_ARRAY) { 
        nextToken() 
        if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.END_ARRAY) { break } 
        val fieldName = currentName
        nextToken()
        if(currentToken != JsonToken.VALUE_NULL) when (fieldName) {
        "text" -> text = p.text
        "applicationId" -> applicationId = p.readValueAs(applicationId_reference)
        "language" -> language = p.readValueAs(Locale::class.java)
        "intentProbability" -> intentProbability = p.doubleValue
        "entitiesProbability" -> entitiesProbability = p.doubleValue
        "lastUsage" -> lastUsage = p.readValueAs(Instant::class.java)
        "count" -> count = p.readValueAs(Long::class.java)
        else -> if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.END_ARRAY) { p.skipChildren() } else { nextToken() }
         }  }
        return ParseRequestLogMongoDAO.ParseRequestLogStatCol(text!!, applicationId!!, language!!, intentProbability, entitiesProbability, lastUsage!!, count!!)
                }
    }
    companion object {
        val applicationId_reference: TypeReference<Id<ApplicationDefinition>> =
                object : TypeReference<Id<ApplicationDefinition>>() {}
    }
}
