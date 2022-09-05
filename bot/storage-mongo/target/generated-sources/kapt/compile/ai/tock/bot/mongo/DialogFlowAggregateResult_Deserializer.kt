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

internal class DialogFlowAggregateResult_Deserializer :
        JsonDeserializer<DialogFlowAggregateResult>(), JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addDeserializer(DialogFlowAggregateResult::class.java,
            this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): DialogFlowAggregateResult
            {
        with(p) {
            var _date_: String? = null
            var _date_set : Boolean = false
            var _count_: Int? = null
            var _count_set : Boolean = false
            var _seriesKey_: String? = null
            var _seriesKey_set : Boolean = false
            var _token_ : JsonToken? = currentToken
            while (_token_?.isStructEnd != true) { 
                if(_token_ != JsonToken.FIELD_NAME) {
                        _token_ = nextToken()
                        if (_token_?.isStructEnd == true) break
                        }

                val _fieldName_ = currentName
                _token_ = nextToken()
                when (_fieldName_) { 
                    "date" -> {
                            _date_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.text;
                            _date_set = true
                            }
                    "count" -> {
                            _count_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.intValue;
                            _count_set = true
                            }
                    "seriesKey" -> {
                            _seriesKey_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.text;
                            _seriesKey_set = true
                            }
                    else -> {
                            if (_token_?.isStructStart == true)
                            p.skipChildren()
                            nextToken()
                            }
                    } 
                _token_ = currentToken
                        } 
            return if(_date_set && _count_set && _seriesKey_set)
                    DialogFlowAggregateResult(date = _date_!!, count = _count_!!, seriesKey =
                            _seriesKey_!!)
                    else {
                    val map = mutableMapOf<KParameter, Any?>()
                    if(_date_set)
                    map[parameters.getValue("date")] = _date_
                    if(_count_set)
                    map[parameters.getValue("count")] = _count_
                    if(_seriesKey_set)
                    map[parameters.getValue("seriesKey")] = _seriesKey_ 
                    primaryConstructor.callBy(map) 
                    }
        } 
    }

    companion object {
        private val primaryConstructor: KFunction<DialogFlowAggregateResult> by
                lazy(LazyThreadSafetyMode.PUBLICATION) {
                DialogFlowAggregateResult::class.primaryConstructor!! }

        private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
                kotlin.collections.mapOf("date" to primaryConstructor.findParameterByName("date")!!,
                "count" to primaryConstructor.findParameterByName("count")!!, "seriesKey" to
                primaryConstructor.findParameterByName("seriesKey")!!) }
    }
}
