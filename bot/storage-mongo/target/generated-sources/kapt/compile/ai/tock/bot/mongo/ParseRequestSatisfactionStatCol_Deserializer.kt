package ai.tock.bot.mongo

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

internal class ParseRequestSatisfactionStatCol_Deserializer :
        JsonDeserializer<ParseRequestSatisfactionStatCol>(), JacksonModuleServiceLoader {
    override fun module() =
            SimpleModule().addDeserializer(ParseRequestSatisfactionStatCol::class.java, this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext):
            ParseRequestSatisfactionStatCol {
        with(p) {
            var _rating_: Int? = null
            var _rating_set : Boolean = false
            var _count_: Int? = null
            var _count_set : Boolean = false
            var _token_ : JsonToken? = currentToken
            while (_token_?.isStructEnd != true) { 
                if(_token_ != JsonToken.FIELD_NAME) {
                        _token_ = nextToken()
                        if (_token_?.isStructEnd == true) break
                        }

                val _fieldName_ = currentName
                _token_ = nextToken()
                when (_fieldName_) { 
                    "rating" -> {
                            _rating_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.intValue;
                            _rating_set = true
                            }
                    "count" -> {
                            _count_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.intValue;
                            _count_set = true
                            }
                    else -> {
                            if (_token_?.isStructStart == true)
                            p.skipChildren()
                            nextToken()
                            }
                    } 
                _token_ = currentToken
                        } 
            return if(_rating_set && _count_set)
                    ParseRequestSatisfactionStatCol(rating = _rating_!!, count = _count_!!)
                    else {
                    val map = mutableMapOf<KParameter, Any?>()
                    if(_rating_set)
                    map[parameters.getValue("rating")] = _rating_
                    if(_count_set)
                    map[parameters.getValue("count")] = _count_ 
                    primaryConstructor.callBy(map) 
                    }
        } 
    }

    companion object {
        private val primaryConstructor: KFunction<ParseRequestSatisfactionStatCol> by
                lazy(LazyThreadSafetyMode.PUBLICATION) {
                ParseRequestSatisfactionStatCol::class.primaryConstructor!! }

        private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
                kotlin.collections.mapOf("rating" to
                primaryConstructor.findParameterByName("rating")!!, "count" to
                primaryConstructor.findParameterByName("count")!!) }
    }
}
