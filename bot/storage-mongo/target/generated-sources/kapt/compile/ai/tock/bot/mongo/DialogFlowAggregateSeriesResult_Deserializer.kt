package ai.tock.bot.mongo

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import kotlin.String
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.findParameterByName
import kotlin.reflect.full.primaryConstructor
import org.litote.jackson.JacksonModuleServiceLoader

internal class DialogFlowAggregateSeriesResult_Deserializer :
        JsonDeserializer<DialogFlowAggregateSeriesResult>(), JacksonModuleServiceLoader {
    override fun module() =
            SimpleModule().addDeserializer(DialogFlowAggregateSeriesResult::class.java, this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext):
            DialogFlowAggregateSeriesResult {
        with(p) {
            var _values_: MutableList<DialogFlowAggregateResult>? = null
            var _values_set : Boolean = false
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
                    "values" -> {
                            _values_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(_values__reference);
                            _values_set = true
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
            return if(_values_set && _seriesKey_set)
                    DialogFlowAggregateSeriesResult(values = _values_!!, seriesKey = _seriesKey_!!)
                    else {
                    val map = mutableMapOf<KParameter, Any?>()
                    if(_values_set)
                    map[parameters.getValue("values")] = _values_
                    if(_seriesKey_set)
                    map[parameters.getValue("seriesKey")] = _seriesKey_ 
                    primaryConstructor.callBy(map) 
                    }
        } 
    }

    companion object {
        private val primaryConstructor: KFunction<DialogFlowAggregateSeriesResult> by
                lazy(LazyThreadSafetyMode.PUBLICATION) {
                DialogFlowAggregateSeriesResult::class.primaryConstructor!! }

        private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
                kotlin.collections.mapOf("values" to
                primaryConstructor.findParameterByName("values")!!, "seriesKey" to
                primaryConstructor.findParameterByName("seriesKey")!!) }

        private val _values__reference: TypeReference<List<DialogFlowAggregateResult>> = object :
                TypeReference<List<DialogFlowAggregateResult>>() {}
    }
}
