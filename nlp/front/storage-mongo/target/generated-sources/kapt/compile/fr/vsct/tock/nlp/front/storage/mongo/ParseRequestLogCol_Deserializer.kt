package fr.vsct.tock.nlp.front.storage.mongo

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import fr.vsct.tock.nlp.front.shared.config.ApplicationDefinition
import fr.vsct.tock.nlp.front.shared.parser.ParseQuery
import fr.vsct.tock.nlp.front.shared.parser.ParseResult
import java.time.Instant
import kotlin.Boolean
import kotlin.Long
import kotlin.String
import kotlin.collections.Map
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.findParameterByName
import kotlin.reflect.full.primaryConstructor
import org.litote.jackson.JacksonModuleServiceLoader
import org.litote.kmongo.Id

internal class ParseRequestLogCol_Deserializer :
        JsonDeserializer<ParseRequestLogMongoDAO.ParseRequestLogCol>(), JacksonModuleServiceLoader {
    override fun module() =
            SimpleModule().addDeserializer(ParseRequestLogMongoDAO.ParseRequestLogCol::class.java,
            this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext):
            ParseRequestLogMongoDAO.ParseRequestLogCol {
        with(p) {
            var _text_: String? = null
            var _text_set : Boolean = false
            var _applicationId_: Id<ApplicationDefinition>? = null
            var _applicationId_set : Boolean = false
            var _query_: ParseQuery? = null
            var _query_set : Boolean = false
            var _result_: ParseResult? = null
            var _result_set : Boolean = false
            var _durationInMS_: Long? = null
            var _durationInMS_set : Boolean = false
            var _error_: Boolean? = null
            var _error_set : Boolean = false
            var _date_: Instant? = null
            var _date_set : Boolean = false
            var _token_ : JsonToken? = currentToken
            while (_token_?.isStructEnd != true) { 
                if(_token_ != JsonToken.FIELD_NAME) {
                        _token_ = nextToken()
                        if (_token_?.isStructEnd == true) break
                        }

                val _fieldName_ = currentName
                _token_ = nextToken()
                when (_fieldName_) { 
                    "text" -> {
                            _text_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.text;
                            _text_set = true
                            }
                    "applicationId" -> {
                            _applicationId_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(_applicationId__reference);
                            _applicationId_set = true
                            }
                    "query" -> {
                            _query_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(ParseQuery::class.java);
                            _query_set = true
                            }
                    "result" -> {
                            _result_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(ParseResult::class.java);
                            _result_set = true
                            }
                    "durationInMS" -> {
                            _durationInMS_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.longValue;
                            _durationInMS_set = true
                            }
                    "error" -> {
                            _error_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.booleanValue;
                            _error_set = true
                            }
                    "date" -> {
                            _date_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(Instant::class.java);
                            _date_set = true
                            }
                    else -> {
                            if (_token_?.isStructStart == true)
                            p.skipChildren()
                            nextToken()
                            }
                    } 
                _token_ = currentToken
                        } 
            return if(_text_set && _applicationId_set && _query_set && _result_set &&
                    _durationInMS_set && _error_set && _date_set)
                    ParseRequestLogMongoDAO.ParseRequestLogCol(text = _text_!!, applicationId =
                            _applicationId_!!, query = _query_!!, result = _result_, durationInMS =
                            _durationInMS_!!, error = _error_!!, date = _date_!!)
                    else {
                    val map = mutableMapOf<KParameter, Any?>()
                    if(_text_set)
                    map[parameters.getValue("text")] = _text_
                    if(_applicationId_set)
                    map[parameters.getValue("applicationId")] = _applicationId_
                    if(_query_set)
                    map[parameters.getValue("query")] = _query_
                    if(_result_set)
                    map[parameters.getValue("result")] = _result_
                    if(_durationInMS_set)
                    map[parameters.getValue("durationInMS")] = _durationInMS_
                    if(_error_set)
                    map[parameters.getValue("error")] = _error_
                    if(_date_set)
                    map[parameters.getValue("date")] = _date_ 
                    primaryConstructor.callBy(map) 
                    }
        } 
    }

    companion object {
        private val primaryConstructor: KFunction<ParseRequestLogMongoDAO.ParseRequestLogCol> by
                lazy(LazyThreadSafetyMode.PUBLICATION) {
                ParseRequestLogMongoDAO.ParseRequestLogCol::class.primaryConstructor!! }

        private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
                kotlin.collections.mapOf("text" to primaryConstructor.findParameterByName("text")!!,
                "applicationId" to primaryConstructor.findParameterByName("applicationId")!!,
                "query" to primaryConstructor.findParameterByName("query")!!, "result" to
                primaryConstructor.findParameterByName("result")!!, "durationInMS" to
                primaryConstructor.findParameterByName("durationInMS")!!, "error" to
                primaryConstructor.findParameterByName("error")!!, "date" to
                primaryConstructor.findParameterByName("date")!!) }

        private val _applicationId__reference: TypeReference<Id<ApplicationDefinition>> = object :
                TypeReference<Id<ApplicationDefinition>>() {}
    }
}
