package ai.tock.nlp.front.storage.mongo

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import kotlin.Int
import kotlin.String
import kotlin.collections.Map
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.findParameterByName
import kotlin.reflect.full.primaryConstructor
import org.litote.jackson.JacksonModuleServiceLoader

internal class DayAndYear_Deserializer : JsonDeserializer<ParseRequestLogMongoDAO.DayAndYear>(),
        JacksonModuleServiceLoader {
    override fun module() =
            SimpleModule().addDeserializer(ParseRequestLogMongoDAO.DayAndYear::class.java, this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext):
            ParseRequestLogMongoDAO.DayAndYear {
        with(p) {
            var _dayOfYear_: Int? = null
            var _dayOfYear_set : Boolean = false
            var _year_: Int? = null
            var _year_set : Boolean = false
            var _token_ : JsonToken? = currentToken
            while (_token_?.isStructEnd != true) { 
                if(_token_ != JsonToken.FIELD_NAME) {
                        _token_ = nextToken()
                        if (_token_?.isStructEnd == true) break
                        }

                val _fieldName_ = currentName
                _token_ = nextToken()
                when (_fieldName_) { 
                    "dayOfYear" -> {
                            _dayOfYear_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.intValue;
                            _dayOfYear_set = true
                            }
                    "year" -> {
                            _year_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.intValue;
                            _year_set = true
                            }
                    else -> {
                            if (_token_?.isStructStart == true)
                            p.skipChildren()
                            nextToken()
                            }
                    } 
                _token_ = currentToken
                        } 
            return if(_dayOfYear_set && _year_set)
                    ParseRequestLogMongoDAO.DayAndYear(dayOfYear = _dayOfYear_!!, year = _year_!!)
                    else {
                    val map = mutableMapOf<KParameter, Any?>()
                    if(_dayOfYear_set)
                    map[parameters.getValue("dayOfYear")] = _dayOfYear_
                    if(_year_set)
                    map[parameters.getValue("year")] = _year_ 
                    primaryConstructor.callBy(map) 
                    }
        } 
    }

    companion object {
        private val primaryConstructor: KFunction<ParseRequestLogMongoDAO.DayAndYear> by
                lazy(LazyThreadSafetyMode.PUBLICATION) {
                ParseRequestLogMongoDAO.DayAndYear::class.primaryConstructor!! }

        private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
                kotlin.collections.mapOf("dayOfYear" to
                primaryConstructor.findParameterByName("dayOfYear")!!, "year" to
                primaryConstructor.findParameterByName("year")!!) }
    }
}
