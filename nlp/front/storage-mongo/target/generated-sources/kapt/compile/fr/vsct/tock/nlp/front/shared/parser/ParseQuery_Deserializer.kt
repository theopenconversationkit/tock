package fr.vsct.tock.nlp.front.shared.parser

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import kotlin.String
import kotlin.collections.List
import kotlin.collections.Set
import org.litote.jackson.JacksonModuleServiceLoader

class ParseQuery_Deserializer : StdDeserializer<ParseQuery>(ParseQuery::class.java),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addDeserializer(ParseQuery::class.java, this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): ParseQuery {
        with(p) {
        var queries: List<String>? = null
        var namespace: String? = null
        var applicationName: String? = null
        var context: QueryContext? = null
        var state: QueryState? = null
        var intentsSubset: Set<IntentQualifier>? = null
        while (currentToken != JsonToken.END_OBJECT && currentToken != JsonToken.END_ARRAY) { 
        nextToken() 
        if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.END_ARRAY) { break } 
        val fieldName = currentName
        nextToken()
        if(currentToken != JsonToken.VALUE_NULL) when (fieldName) {
        "queries" -> queries = p.readValueAs(queries_reference)
        "namespace" -> namespace = p.text
        "applicationName" -> applicationName = p.text
        "context" -> context = p.readValueAs(QueryContext::class.java)
        "state" -> state = p.readValueAs(QueryState::class.java)
        "intentsSubset" -> intentsSubset = p.readValueAs(intentsSubset_reference)
        else -> if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.END_ARRAY) { p.skipChildren() } else { nextToken() }
         }  }
        return ParseQuery(queries!!, namespace!!, applicationName!!, context!!, state!!, intentsSubset!!)
                }
    }
    companion object {
        val queries_reference: TypeReference<List<String>> =
                object : TypeReference<List<String>>() {}

        val intentsSubset_reference: TypeReference<Set<IntentQualifier>> =
                object : TypeReference<Set<IntentQualifier>>() {}
    }
}
