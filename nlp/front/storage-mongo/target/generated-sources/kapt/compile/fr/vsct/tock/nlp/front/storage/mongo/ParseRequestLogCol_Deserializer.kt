package fr.vsct.tock.nlp.front.storage.mongo

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import fr.vsct.tock.nlp.front.shared.config.ApplicationDefinition
import fr.vsct.tock.nlp.front.shared.parser.ParseQuery
import fr.vsct.tock.nlp.front.shared.parser.ParseResult
import java.time.Instant
import kotlin.Boolean
import kotlin.Long
import kotlin.String
import org.litote.jackson.JacksonModuleServiceLoader
import org.litote.kmongo.Id

internal class ParseRequestLogCol_Deserializer : StdDeserializer<ParseRequestLogMongoDAO.ParseRequestLogCol>(ParseRequestLogMongoDAO.ParseRequestLogCol::class.java),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addDeserializer(ParseRequestLogMongoDAO.ParseRequestLogCol::class.java, this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): ParseRequestLogMongoDAO.ParseRequestLogCol {
        with(p) {
        var text: String? = null
        var applicationId: Id<ApplicationDefinition>? = null
        var query: ParseQuery? = null
        var result: ParseResult? = null
        var durationInMS: Long? = null
        var error: Boolean? = null
        var date: Instant? = null
        while (currentToken != JsonToken.END_OBJECT && currentToken != JsonToken.END_ARRAY) { 
        nextToken() 
        if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.END_ARRAY) { break } 
        val fieldName = currentName
        nextToken()
        if(currentToken != JsonToken.VALUE_NULL) when (fieldName) {
        "text" -> text = p.text
        "applicationId" -> applicationId = p.readValueAs(applicationId_reference)
        "query" -> query = p.readValueAs(ParseQuery::class.java)
        "result" -> result = p.readValueAs(ParseResult::class.java)
        "durationInMS" -> durationInMS = p.readValueAs(Long::class.java)
        "error" -> error = p.readValueAs(Boolean::class.java)
        "date" -> date = p.readValueAs(Instant::class.java)
        else -> if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.END_ARRAY) { p.skipChildren() } else { nextToken() }
         }  }
        return ParseRequestLogMongoDAO.ParseRequestLogCol(text!!, applicationId!!, query!!, result, durationInMS!!, error!!, date!!)
                }
    }
    companion object {
        val applicationId_reference: TypeReference<Id<ApplicationDefinition>> =
                object : TypeReference<Id<ApplicationDefinition>>() {}
    }
}
