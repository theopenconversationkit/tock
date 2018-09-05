package fr.vsct.tock.bot.mongo

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import fr.vsct.tock.bot.engine.nlp.NlpCallStats
import java.time.Instant
import kotlin.String
import org.litote.jackson.JacksonModuleServiceLoader

internal class NlpStatsCol_Deserializer : StdDeserializer<NlpStatsCol>(NlpStatsCol::class.java),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addDeserializer(NlpStatsCol::class.java, this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): NlpStatsCol {
        with(p) {
        var _id: NlpStatsColId? = null
        var stats: NlpCallStats? = null
        var appNamespace: String? = null
        var date: Instant? = null
        while (currentToken != JsonToken.END_OBJECT && currentToken != JsonToken.END_ARRAY) { 
        nextToken() 
        if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.END_ARRAY) { break } 
        val fieldName = currentName
        nextToken()
        if(currentToken != JsonToken.VALUE_NULL) when (fieldName) {
        "_id" -> _id = p.readValueAs(NlpStatsColId::class.java)
        "stats" -> stats = p.readValueAs(NlpCallStats::class.java)
        "appNamespace" -> appNamespace = p.text
        "date" -> date = p.readValueAs(Instant::class.java)
        else -> if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.END_ARRAY) { p.skipChildren() } else { nextToken() }
         }  }
        return NlpStatsCol(_id!!, stats!!, appNamespace!!, date!!) }
    }
    companion object
}
