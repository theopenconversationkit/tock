package fr.vsct.tock.nlp.front.shared.parser

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import java.util.Locale
import kotlin.Double
import kotlin.String
import kotlin.collections.List
import kotlin.collections.Map
import org.litote.jackson.JacksonModuleServiceLoader

class ParseResult_Deserializer : StdDeserializer<ParseResult>(ParseResult::class.java),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addDeserializer(ParseResult::class.java, this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): ParseResult {
        with(p) {
        var intent: String? = null
        var intentNamespace: String? = null
        var language: Locale? = null
        var entities: List<ParsedEntityValue>? = null
        var notRetainedEntities: List<ParsedEntityValue>? = null
        var intentProbability: Double? = null
        var entitiesProbability: Double? = null
        var retainedQuery: String? = null
        var otherIntentsProbabilities: Map<String, Double>? = null
        while (currentToken != JsonToken.END_OBJECT && currentToken != JsonToken.END_ARRAY) { 
        nextToken() 
        if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.END_ARRAY) { break } 
        val fieldName = currentName
        nextToken()
        if(currentToken != JsonToken.VALUE_NULL) when (fieldName) {
        "intent" -> intent = p.text
        "intentNamespace" -> intentNamespace = p.text
        "language" -> language = p.readValueAs(Locale::class.java)
        "entities" -> entities = p.readValueAs(entities_reference)
        "notRetainedEntities" -> notRetainedEntities = p.readValueAs(notRetainedEntities_reference)
        "intentProbability" -> intentProbability = p.readValueAs(Double::class.java)
        "entitiesProbability" -> entitiesProbability = p.readValueAs(Double::class.java)
        "retainedQuery" -> retainedQuery = p.text
        "otherIntentsProbabilities" -> otherIntentsProbabilities = p.readValueAs(otherIntentsProbabilities_reference)
        else -> if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.END_ARRAY) { p.skipChildren() } else { nextToken() }
         }  }
        return ParseResult(intent!!, intentNamespace!!, language!!, entities!!, notRetainedEntities!!, intentProbability!!, entitiesProbability!!, retainedQuery!!, otherIntentsProbabilities!!)
                }
    }
    companion object {
        val entities_reference: TypeReference<List<ParsedEntityValue>> =
                object : TypeReference<List<ParsedEntityValue>>() {}

        val notRetainedEntities_reference: TypeReference<List<ParsedEntityValue>> =
                object : TypeReference<List<ParsedEntityValue>>() {}

        val otherIntentsProbabilities_reference: TypeReference<Map<String, Double>> =
                object : TypeReference<Map<String, Double>>() {}
    }
}
