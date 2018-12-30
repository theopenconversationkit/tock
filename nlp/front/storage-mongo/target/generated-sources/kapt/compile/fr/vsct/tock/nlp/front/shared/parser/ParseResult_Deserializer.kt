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
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.findParameterByName
import kotlin.reflect.full.primaryConstructor
import org.litote.jackson.JacksonModuleServiceLoader

internal class ParseResult_Deserializer : StdDeserializer<ParseResult>(ParseResult::class.java),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addDeserializer(ParseResult::class.java, this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): ParseResult {
        with(p) {
            var _intent_: String? = null
            var _intent_set = false
            var _intentNamespace_: String? = null
            var _intentNamespace_set = false
            var _language_: Locale? = null
            var _language_set = false
            var _entities_: MutableList<ParsedEntityValue>? = null
            var _entities_set = false
            var _notRetainedEntities_: MutableList<ParsedEntityValue>? = null
            var _notRetainedEntities_set = false
            var _intentProbability_: Double? = null
            var _intentProbability_set = false
            var _entitiesProbability_: Double? = null
            var _entitiesProbability_set = false
            var _retainedQuery_: String? = null
            var _retainedQuery_set = false
            var _otherIntentsProbabilities_: MutableMap<String, Double>? = null
            var _otherIntentsProbabilities_set = false
            while (currentToken != JsonToken.END_OBJECT && currentToken != JsonToken.END_ARRAY) { 
                if(currentToken != JsonToken.FIELD_NAME) { nextToken() }
                if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.END_ARRAY) {
                        break } 
                val fieldName = currentName
                nextToken()
                when (fieldName) { 
                    "intent" -> {
                            _intent_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.text;
                            _intent_set = true
                            }
                    "intentNamespace" -> {
                            _intentNamespace_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.text;
                            _intentNamespace_set = true
                            }
                    "language" -> {
                            _language_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.readValueAs(Locale::class.java);
                            _language_set = true
                            }
                    "entities" -> {
                            _entities_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.readValueAs(_entities__reference);
                            _entities_set = true
                            }
                    "notRetainedEntities" -> {
                            _notRetainedEntities_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.readValueAs(_notRetainedEntities__reference);
                            _notRetainedEntities_set = true
                            }
                    "intentProbability" -> {
                            _intentProbability_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.readValueAs(Double::class.java);
                            _intentProbability_set = true
                            }
                    "entitiesProbability" -> {
                            _entitiesProbability_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.readValueAs(Double::class.java);
                            _entitiesProbability_set = true
                            }
                    "retainedQuery" -> {
                            _retainedQuery_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.text;
                            _retainedQuery_set = true
                            }
                    "otherIntentsProbabilities" -> {
                            _otherIntentsProbabilities_ = if(currentToken == JsonToken.VALUE_NULL)
                                    null
                             else p.readValueAs(_otherIntentsProbabilities__reference);
                            _otherIntentsProbabilities_set = true
                            }
                    else -> {
                            if (currentToken == JsonToken.START_OBJECT || currentToken ==
                                    JsonToken.START_ARRAY)
                            p.skipChildren()
                            nextToken()
                            }
                    } 
                } 
            return if(_intent_set && _intentNamespace_set && _language_set && _entities_set &&
                    _notRetainedEntities_set && _intentProbability_set && _entitiesProbability_set
                    && _retainedQuery_set && _otherIntentsProbabilities_set)
                    ParseResult(intent = _intent_!!, intentNamespace = _intentNamespace_!!, language
                            = _language_!!, entities = _entities_!!, notRetainedEntities =
                            _notRetainedEntities_!!, intentProbability = _intentProbability_!!,
                            entitiesProbability = _entitiesProbability_!!, retainedQuery =
                            _retainedQuery_!!, otherIntentsProbabilities =
                            _otherIntentsProbabilities_!!)
                    else {
                    val map = mutableMapOf<KParameter, Any?>()
                    if(_intent_set)
                    map[parameters.getValue("intent")] = _intent_
                    if(_intentNamespace_set)
                    map[parameters.getValue("intentNamespace")] = _intentNamespace_
                    if(_language_set)
                    map[parameters.getValue("language")] = _language_
                    if(_entities_set)
                    map[parameters.getValue("entities")] = _entities_
                    if(_notRetainedEntities_set)
                    map[parameters.getValue("notRetainedEntities")] = _notRetainedEntities_
                    if(_intentProbability_set)
                    map[parameters.getValue("intentProbability")] = _intentProbability_
                    if(_entitiesProbability_set)
                    map[parameters.getValue("entitiesProbability")] = _entitiesProbability_
                    if(_retainedQuery_set)
                    map[parameters.getValue("retainedQuery")] = _retainedQuery_
                    if(_otherIntentsProbabilities_set)
                    map[parameters.getValue("otherIntentsProbabilities")] =
                            _otherIntentsProbabilities_ 
                    primaryConstructor.callBy(map) 
                    }
        } 
    }

    companion object {
        private val primaryConstructor: KFunction<ParseResult> by
                lazy(LazyThreadSafetyMode.PUBLICATION) { ParseResult::class.primaryConstructor!! }

        private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
                kotlin.collections.mapOf("intent" to
                primaryConstructor.findParameterByName("intent")!!, "intentNamespace" to
                primaryConstructor.findParameterByName("intentNamespace")!!, "language" to
                primaryConstructor.findParameterByName("language")!!, "entities" to
                primaryConstructor.findParameterByName("entities")!!, "notRetainedEntities" to
                primaryConstructor.findParameterByName("notRetainedEntities")!!, "intentProbability"
                to primaryConstructor.findParameterByName("intentProbability")!!,
                "entitiesProbability" to
                primaryConstructor.findParameterByName("entitiesProbability")!!, "retainedQuery" to
                primaryConstructor.findParameterByName("retainedQuery")!!,
                "otherIntentsProbabilities" to
                primaryConstructor.findParameterByName("otherIntentsProbabilities")!!) }

        private val _entities__reference: TypeReference<List<ParsedEntityValue>> = object :
                TypeReference<List<ParsedEntityValue>>() {}

        private val _notRetainedEntities__reference: TypeReference<List<ParsedEntityValue>> = object
                : TypeReference<List<ParsedEntityValue>>() {}

        private val _otherIntentsProbabilities__reference: TypeReference<Map<String, Double>> =
                object : TypeReference<Map<String, Double>>() {}
    }
}
