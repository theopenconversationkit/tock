package fr.vsct.tock.nlp.front.storage.mongo

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import fr.vsct.tock.nlp.front.shared.config.ApplicationDefinition
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

internal class ParseRequestLogIntentStatCol_Deserializer :
        JsonDeserializer<ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol>(),
        JacksonModuleServiceLoader {
    override fun module() =
            SimpleModule().addDeserializer(ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol::class.java,
            this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext):
            ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol {
        with(p) {
            var _applicationId_: Id<ApplicationDefinition>? = null
            var _applicationId_set : Boolean = false
            var _language_: Locale? = null
            var _language_set : Boolean = false
            var _intent1_: String? = null
            var _intent1_set : Boolean = false
            var _intent2_: String? = null
            var _intent2_set : Boolean = false
            var _averageDiff_: Double? = null
            var _averageDiff_set : Boolean = false
            var _count_: Long? = null
            var _count_set : Boolean = false
            var __id_: Id<ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol>? = null
            var __id_set : Boolean = false
            var _token_ : JsonToken? = currentToken
            while (_token_?.isStructEnd != true) { 
                if(_token_ != JsonToken.FIELD_NAME) {
                        _token_ = nextToken()
                        if (_token_?.isStructEnd == true) break
                        }

                val _fieldName_ = currentName
                _token_ = nextToken()
                when (_fieldName_) { 
                    "applicationId" -> {
                            _applicationId_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(_applicationId__reference);
                            _applicationId_set = true
                            }
                    "language" -> {
                            _language_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(Locale::class.java);
                            _language_set = true
                            }
                    "intent1" -> {
                            _intent1_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.text;
                            _intent1_set = true
                            }
                    "intent2" -> {
                            _intent2_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.text;
                            _intent2_set = true
                            }
                    "averageDiff" -> {
                            _averageDiff_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.doubleValue;
                            _averageDiff_set = true
                            }
                    "count" -> {
                            _count_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.longValue;
                            _count_set = true
                            }
                    "_id" -> {
                            __id_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(__id__reference);
                            __id_set = true
                            }
                    else -> {
                            if (_token_?.isStructStart == true)
                            p.skipChildren()
                            nextToken()
                            }
                    } 
                _token_ = currentToken
                        } 
            return if(_applicationId_set && _language_set && _intent1_set && _intent2_set &&
                    _averageDiff_set && _count_set && __id_set)
                    ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol(applicationId =
                            _applicationId_!!, language = _language_!!, intent1 = _intent1_!!,
                            intent2 = _intent2_!!, averageDiff = _averageDiff_!!, count = _count_!!,
                            _id = __id_!!)
                    else {
                    val map = mutableMapOf<KParameter, Any?>()
                    if(_applicationId_set)
                    map[parameters.getValue("applicationId")] = _applicationId_
                    if(_language_set)
                    map[parameters.getValue("language")] = _language_
                    if(_intent1_set)
                    map[parameters.getValue("intent1")] = _intent1_
                    if(_intent2_set)
                    map[parameters.getValue("intent2")] = _intent2_
                    if(_averageDiff_set)
                    map[parameters.getValue("averageDiff")] = _averageDiff_
                    if(_count_set)
                    map[parameters.getValue("count")] = _count_
                    if(__id_set)
                    map[parameters.getValue("_id")] = __id_ 
                    primaryConstructor.callBy(map) 
                    }
        } 
    }

    companion object {
        private val primaryConstructor:
                KFunction<ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol> by
                lazy(LazyThreadSafetyMode.PUBLICATION) {
                ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol::class.primaryConstructor!! }

        private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
                kotlin.collections.mapOf("applicationId" to
                primaryConstructor.findParameterByName("applicationId")!!, "language" to
                primaryConstructor.findParameterByName("language")!!, "intent1" to
                primaryConstructor.findParameterByName("intent1")!!, "intent2" to
                primaryConstructor.findParameterByName("intent2")!!, "averageDiff" to
                primaryConstructor.findParameterByName("averageDiff")!!, "count" to
                primaryConstructor.findParameterByName("count")!!, "_id" to
                primaryConstructor.findParameterByName("_id")!!) }

        private val _applicationId__reference: TypeReference<Id<ApplicationDefinition>> = object :
                TypeReference<Id<ApplicationDefinition>>() {}

        private val __id__reference:
                TypeReference<Id<ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol>> = object :
                TypeReference<Id<ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol>>() {}
    }
}
