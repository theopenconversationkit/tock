package fr.vsct.tock.nlp.front.storage.mongo

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import kotlin.Double
import kotlin.Int
import org.litote.jackson.JacksonModuleServiceLoader

internal class ParseRequestLogStatResult_Deserializer : StdDeserializer<ParseRequestLogMongoDAO.ParseRequestLogStatResult>(ParseRequestLogMongoDAO.ParseRequestLogStatResult::class.java),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addDeserializer(ParseRequestLogMongoDAO.ParseRequestLogStatResult::class.java, this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): ParseRequestLogMongoDAO.ParseRequestLogStatResult {
        with(p) {
        var _id: ParseRequestLogMongoDAO.DayAndYear? = null
        var error: Int? = null
        var count: Int? = null
        var duration: Double? = null
        var intentProbability: Double? = null
        var entitiesProbability: Double? = null
        while (currentToken != JsonToken.END_OBJECT && currentToken != JsonToken.END_ARRAY) { 
        nextToken() 
        if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.END_ARRAY) { break } 
        val fieldName = currentName
        nextToken()
        if(currentToken != JsonToken.VALUE_NULL) when (fieldName) {
        "_id" -> _id = p.readValueAs(ParseRequestLogMongoDAO.DayAndYear::class.java)
        "error" -> error = p.readValueAs(Int::class.java)
        "count" -> count = p.readValueAs(Int::class.java)
        "duration" -> duration = p.readValueAs(Double::class.java)
        "intentProbability" -> intentProbability = p.readValueAs(Double::class.java)
        "entitiesProbability" -> entitiesProbability = p.readValueAs(Double::class.java)
        else -> if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.END_ARRAY) { p.skipChildren() } else { nextToken() }
         }  }
        return ParseRequestLogMongoDAO.ParseRequestLogStatResult(_id!!, error!!, count!!, duration!!, intentProbability!!, entitiesProbability!!)
                }
    }
    companion object
}
