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
import kotlin.collections.Map
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.findParameterByName
import kotlin.reflect.full.primaryConstructor
import org.litote.jackson.JacksonModuleServiceLoader
import org.litote.kmongo.Id

internal class ParseRequestLogStatCol_Deserializer :
        StdDeserializer<ParseRequestLogMongoDAO.ParseRequestLogStatCol>(ParseRequestLogMongoDAO.ParseRequestLogStatCol::class.java),
        JacksonModuleServiceLoader {
    override fun module() =
            SimpleModule().addDeserializer(ParseRequestLogMongoDAO.ParseRequestLogStatCol::class.java,
            this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext):
            ParseRequestLogMongoDAO.ParseRequestLogStatCol {
        with(p) {
            var _text_: String? = null
            var _text_set = false
            var _applicationId_: Id<ApplicationDefinition>? = null
            var _applicationId_set = false
            var _language_: Locale? = null
            var _language_set = false
            var _intentProbability_: Double? = null
            var _intentProbability_set = false
            var _entitiesProbability_: Double? = null
            var _entitiesProbability_set = false
            var _lastUsage_: Instant? = null
            var _lastUsage_set = false
            var _count_: Long? = null
            var _count_set = false
            while (currentToken != JsonToken.END_OBJECT && currentToken != JsonToken.END_ARRAY) { 
                if(currentToken != JsonToken.FIELD_NAME) { nextToken() }
                if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.END_ARRAY) {
                        break } 
                val fieldName = currentName
                nextToken()
                when (fieldName) { 
                    "text" -> {
                            _text_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.text;
                            _text_set = true
                            }
                    "applicationId" -> {
                            _applicationId_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.readValueAs(_applicationId__reference);
                            _applicationId_set = true
                            }
                    "language" -> {
                            _language_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.readValueAs(Locale::class.java);
                            _language_set = true
                            }
                    "intentProbability" -> {
                            _intentProbability_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.doubleValue;
                            _intentProbability_set = true
                            }
                    "entitiesProbability" -> {
                            _entitiesProbability_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.doubleValue;
                            _entitiesProbability_set = true
                            }
                    "lastUsage" -> {
                            _lastUsage_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.readValueAs(Instant::class.java);
                            _lastUsage_set = true
                            }
                    "count" -> {
                            _count_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.readValueAs(Long::class.java);
                            _count_set = true
                            }
                    else -> {
                            if (currentToken == JsonToken.START_OBJECT || currentToken ==
                                    JsonToken.START_ARRAY)
                            p.skipChildren()
                            nextToken()
                            }
                    } 
                } 
            return if(_text_set && _applicationId_set && _language_set && _intentProbability_set &&
                    _entitiesProbability_set && _lastUsage_set && _count_set)
                    ParseRequestLogMongoDAO.ParseRequestLogStatCol(text = _text_!!, applicationId =
                            _applicationId_!!, language = _language_!!, intentProbability =
                            _intentProbability_, entitiesProbability = _entitiesProbability_,
                            lastUsage = _lastUsage_!!, count = _count_!!)
                    else {
                    val map = mutableMapOf<KParameter, Any?>()
                    if(_text_set)
                    map[parameters.getValue("text")] = _text_
                    if(_applicationId_set)
                    map[parameters.getValue("applicationId")] = _applicationId_
                    if(_language_set)
                    map[parameters.getValue("language")] = _language_
                    if(_intentProbability_set)
                    map[parameters.getValue("intentProbability")] = _intentProbability_
                    if(_entitiesProbability_set)
                    map[parameters.getValue("entitiesProbability")] = _entitiesProbability_
                    if(_lastUsage_set)
                    map[parameters.getValue("lastUsage")] = _lastUsage_
                    if(_count_set)
                    map[parameters.getValue("count")] = _count_ 
                    primaryConstructor.callBy(map) 
                    }
        } 
    }

    companion object {
        private val primaryConstructor: KFunction<ParseRequestLogMongoDAO.ParseRequestLogStatCol> by
                lazy(LazyThreadSafetyMode.PUBLICATION) {
                ParseRequestLogMongoDAO.ParseRequestLogStatCol::class.primaryConstructor!! }

        private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
                kotlin.collections.mapOf("text" to primaryConstructor.findParameterByName("text")!!,
                "applicationId" to primaryConstructor.findParameterByName("applicationId")!!,
                "language" to primaryConstructor.findParameterByName("language")!!,
                "intentProbability" to
                primaryConstructor.findParameterByName("intentProbability")!!, "entitiesProbability"
                to primaryConstructor.findParameterByName("entitiesProbability")!!, "lastUsage" to
                primaryConstructor.findParameterByName("lastUsage")!!, "count" to
                primaryConstructor.findParameterByName("count")!!) }

        private val _applicationId__reference: TypeReference<Id<ApplicationDefinition>> = object :
                TypeReference<Id<ApplicationDefinition>>() {}
    }
}
