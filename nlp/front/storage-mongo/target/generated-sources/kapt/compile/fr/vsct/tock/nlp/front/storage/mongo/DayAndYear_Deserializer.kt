package fr.vsct.tock.nlp.front.storage.mongo

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import kotlin.Int
import org.litote.jackson.JacksonModuleServiceLoader

internal class DayAndYear_Deserializer : StdDeserializer<ParseRequestLogMongoDAO.DayAndYear>(ParseRequestLogMongoDAO.DayAndYear::class.java),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addDeserializer(ParseRequestLogMongoDAO.DayAndYear::class.java, this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): ParseRequestLogMongoDAO.DayAndYear {
        with(p) {
        var dayOfYear: Int? = null
        var year: Int? = null
        while (currentToken != JsonToken.END_OBJECT && currentToken != JsonToken.END_ARRAY) { 
        nextToken() 
        if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.END_ARRAY) { break } 
        val fieldName = currentName
        nextToken()
        if(currentToken != JsonToken.VALUE_NULL) when (fieldName) {
        "dayOfYear" -> dayOfYear = p.readValueAs(Int::class.java)
        "year" -> year = p.readValueAs(Int::class.java)
        else -> if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.END_ARRAY) { p.skipChildren() } else { nextToken() }
         }  }
        return ParseRequestLogMongoDAO.DayAndYear(dayOfYear!!, year!!) }
    }
    companion object
}
