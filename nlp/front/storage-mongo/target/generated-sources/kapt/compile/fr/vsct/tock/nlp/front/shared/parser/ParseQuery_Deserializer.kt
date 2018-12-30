package fr.vsct.tock.nlp.front.shared.parser

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import kotlin.String
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.collections.MutableSet
import kotlin.collections.Set
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.findParameterByName
import kotlin.reflect.full.primaryConstructor
import org.litote.jackson.JacksonModuleServiceLoader

internal class ParseQuery_Deserializer : StdDeserializer<ParseQuery>(ParseQuery::class.java),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addDeserializer(ParseQuery::class.java, this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): ParseQuery {
        with(p) {
            var _queries_: MutableList<String>? = null
            var _queries_set = false
            var _namespace_: String? = null
            var _namespace_set = false
            var _applicationName_: String? = null
            var _applicationName_set = false
            var _context_: QueryContext? = null
            var _context_set = false
            var _state_: QueryState? = null
            var _state_set = false
            var _intentsSubset_: MutableSet<IntentQualifier>? = null
            var _intentsSubset_set = false
            while (currentToken != JsonToken.END_OBJECT && currentToken != JsonToken.END_ARRAY) { 
                if(currentToken != JsonToken.FIELD_NAME) { nextToken() }
                if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.END_ARRAY) {
                        break } 
                val fieldName = currentName
                nextToken()
                when (fieldName) { 
                    "queries" -> {
                            _queries_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.readValueAs(_queries__reference);
                            _queries_set = true
                            }
                    "namespace" -> {
                            _namespace_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.text;
                            _namespace_set = true
                            }
                    "applicationName" -> {
                            _applicationName_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.text;
                            _applicationName_set = true
                            }
                    "context" -> {
                            _context_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.readValueAs(QueryContext::class.java);
                            _context_set = true
                            }
                    "state" -> {
                            _state_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.readValueAs(QueryState::class.java);
                            _state_set = true
                            }
                    "intentsSubset" -> {
                            _intentsSubset_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.readValueAs(_intentsSubset__reference);
                            _intentsSubset_set = true
                            }
                    else -> {
                            if (currentToken == JsonToken.START_OBJECT || currentToken ==
                                    JsonToken.START_ARRAY)
                            p.skipChildren()
                            nextToken()
                            }
                    } 
                } 
            return if(_queries_set && _namespace_set && _applicationName_set && _context_set &&
                    _state_set && _intentsSubset_set)
                    ParseQuery(queries = _queries_!!, namespace = _namespace_!!, applicationName =
                            _applicationName_!!, context = _context_!!, state = _state_!!,
                            intentsSubset = _intentsSubset_!!)
                    else {
                    val map = mutableMapOf<KParameter, Any?>()
                    if(_queries_set)
                    map[parameters.getValue("queries")] = _queries_
                    if(_namespace_set)
                    map[parameters.getValue("namespace")] = _namespace_
                    if(_applicationName_set)
                    map[parameters.getValue("applicationName")] = _applicationName_
                    if(_context_set)
                    map[parameters.getValue("context")] = _context_
                    if(_state_set)
                    map[parameters.getValue("state")] = _state_
                    if(_intentsSubset_set)
                    map[parameters.getValue("intentsSubset")] = _intentsSubset_ 
                    primaryConstructor.callBy(map) 
                    }
        } 
    }

    companion object {
        private val primaryConstructor: KFunction<ParseQuery> by
                lazy(LazyThreadSafetyMode.PUBLICATION) { ParseQuery::class.primaryConstructor!! }

        private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
                kotlin.collections.mapOf("queries" to
                primaryConstructor.findParameterByName("queries")!!, "namespace" to
                primaryConstructor.findParameterByName("namespace")!!, "applicationName" to
                primaryConstructor.findParameterByName("applicationName")!!, "context" to
                primaryConstructor.findParameterByName("context")!!, "state" to
                primaryConstructor.findParameterByName("state")!!, "intentsSubset" to
                primaryConstructor.findParameterByName("intentsSubset")!!) }

        private val _queries__reference: TypeReference<List<String>> = object :
                TypeReference<List<String>>() {}

        private val _intentsSubset__reference: TypeReference<Set<IntentQualifier>> = object :
                TypeReference<Set<IntentQualifier>>() {}
    }
}
