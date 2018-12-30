package fr.vsct.tock.nlp.front.storage.mongo

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import kotlin.Double
import kotlin.Int
import kotlin.String
import kotlin.collections.Map
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.findParameterByName
import kotlin.reflect.full.primaryConstructor
import org.litote.jackson.JacksonModuleServiceLoader

internal class ParseRequestLogStatResult_Deserializer :
        StdDeserializer<ParseRequestLogMongoDAO.ParseRequestLogStatResult>(ParseRequestLogMongoDAO.ParseRequestLogStatResult::class.java),
        JacksonModuleServiceLoader {
    override fun module() =
            SimpleModule().addDeserializer(ParseRequestLogMongoDAO.ParseRequestLogStatResult::class.java,
            this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext):
            ParseRequestLogMongoDAO.ParseRequestLogStatResult {
        with(p) {
            var __id_: ParseRequestLogMongoDAO.DayAndYear? = null
            var __id_set = false
            var _error_: Int? = null
            var _error_set = false
            var _count_: Int? = null
            var _count_set = false
            var _duration_: Double? = null
            var _duration_set = false
            var _intentProbability_: Double? = null
            var _intentProbability_set = false
            var _entitiesProbability_: Double? = null
            var _entitiesProbability_set = false
            while (currentToken != JsonToken.END_OBJECT && currentToken != JsonToken.END_ARRAY) { 
                if(currentToken != JsonToken.FIELD_NAME) { nextToken() }
                if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.END_ARRAY) {
                        break } 
                val fieldName = currentName
                nextToken()
                when (fieldName) { 
                    "_id" -> {
                            __id_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.readValueAs(ParseRequestLogMongoDAO.DayAndYear::class.java);
                            __id_set = true
                            }
                    "error" -> {
                            _error_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.readValueAs(Int::class.java);
                            _error_set = true
                            }
                    "count" -> {
                            _count_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.readValueAs(Int::class.java);
                            _count_set = true
                            }
                    "duration" -> {
                            _duration_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.readValueAs(Double::class.java);
                            _duration_set = true
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
                    else -> {
                            if (currentToken == JsonToken.START_OBJECT || currentToken ==
                                    JsonToken.START_ARRAY)
                            p.skipChildren()
                            nextToken()
                            }
                    } 
                } 
            return if(__id_set && _error_set && _count_set && _duration_set &&
                    _intentProbability_set && _entitiesProbability_set)
                    ParseRequestLogMongoDAO.ParseRequestLogStatResult(_id = __id_!!, error =
                            _error_!!, count = _count_!!, duration = _duration_!!, intentProbability
                            = _intentProbability_!!, entitiesProbability = _entitiesProbability_!!)
                    else {
                    val map = mutableMapOf<KParameter, Any?>()
                    if(__id_set)
                    map[parameters.getValue("_id")] = __id_
                    if(_error_set)
                    map[parameters.getValue("error")] = _error_
                    if(_count_set)
                    map[parameters.getValue("count")] = _count_
                    if(_duration_set)
                    map[parameters.getValue("duration")] = _duration_
                    if(_intentProbability_set)
                    map[parameters.getValue("intentProbability")] = _intentProbability_
                    if(_entitiesProbability_set)
                    map[parameters.getValue("entitiesProbability")] = _entitiesProbability_ 
                    primaryConstructor.callBy(map) 
                    }
        } 
    }

    companion object {
        private val primaryConstructor: KFunction<ParseRequestLogMongoDAO.ParseRequestLogStatResult>
                by lazy(LazyThreadSafetyMode.PUBLICATION) {
                ParseRequestLogMongoDAO.ParseRequestLogStatResult::class.primaryConstructor!! }

        private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
                kotlin.collections.mapOf("_id" to primaryConstructor.findParameterByName("_id")!!,
                "error" to primaryConstructor.findParameterByName("error")!!, "count" to
                primaryConstructor.findParameterByName("count")!!, "duration" to
                primaryConstructor.findParameterByName("duration")!!, "intentProbability" to
                primaryConstructor.findParameterByName("intentProbability")!!, "entitiesProbability"
                to primaryConstructor.findParameterByName("entitiesProbability")!!) }
    }
}
