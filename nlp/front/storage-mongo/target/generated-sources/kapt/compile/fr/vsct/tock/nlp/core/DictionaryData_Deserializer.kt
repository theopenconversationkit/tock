package fr.vsct.tock.nlp.core

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import kotlin.Boolean
import kotlin.Double
import kotlin.String
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.findParameterByName
import kotlin.reflect.full.primaryConstructor
import org.litote.jackson.JacksonModuleServiceLoader

internal class DictionaryData_Deserializer : JsonDeserializer<DictionaryData>(),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addDeserializer(DictionaryData::class.java, this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): DictionaryData {
        with(p) {
            var _namespace_: String? = null
            var _namespace_set : Boolean = false
            var _entityName_: String? = null
            var _entityName_set : Boolean = false
            var _values_: MutableList<PredefinedValue>? = null
            var _values_set : Boolean = false
            var _onlyValues_: Boolean? = null
            var _onlyValues_set : Boolean = false
            var _minDistance_: Double? = null
            var _minDistance_set : Boolean = false
            var _textSearch_: Boolean? = null
            var _textSearch_set : Boolean = false
            var _token_ : JsonToken? = currentToken
            while (_token_?.isStructEnd != true) { 
                if(_token_ != JsonToken.FIELD_NAME) {
                        _token_ = nextToken()
                        if (_token_?.isStructEnd == true) break
                        }

                val _fieldName_ = currentName
                _token_ = nextToken()
                when (_fieldName_) { 
                    "namespace" -> {
                            _namespace_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.text;
                            _namespace_set = true
                            }
                    "entityName" -> {
                            _entityName_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.text;
                            _entityName_set = true
                            }
                    "values" -> {
                            _values_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(_values__reference);
                            _values_set = true
                            }
                    "onlyValues" -> {
                            _onlyValues_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.booleanValue;
                            _onlyValues_set = true
                            }
                    "minDistance" -> {
                            _minDistance_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.doubleValue;
                            _minDistance_set = true
                            }
                    "textSearch" -> {
                            _textSearch_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.booleanValue;
                            _textSearch_set = true
                            }
                    else -> {
                            if (_token_?.isStructStart == true)
                            p.skipChildren()
                            nextToken()
                            }
                    } 
                _token_ = currentToken
                        } 
            return if(_namespace_set && _entityName_set && _values_set && _onlyValues_set &&
                    _minDistance_set && _textSearch_set)
                    DictionaryData(namespace = _namespace_!!, entityName = _entityName_!!, values =
                            _values_!!, onlyValues = _onlyValues_!!, minDistance = _minDistance_!!,
                            textSearch = _textSearch_!!)
                    else {
                    val map = mutableMapOf<KParameter, Any?>()
                    if(_namespace_set)
                    map[parameters.getValue("namespace")] = _namespace_
                    if(_entityName_set)
                    map[parameters.getValue("entityName")] = _entityName_
                    if(_values_set)
                    map[parameters.getValue("values")] = _values_
                    if(_onlyValues_set)
                    map[parameters.getValue("onlyValues")] = _onlyValues_
                    if(_minDistance_set)
                    map[parameters.getValue("minDistance")] = _minDistance_
                    if(_textSearch_set)
                    map[parameters.getValue("textSearch")] = _textSearch_ 
                    primaryConstructor.callBy(map) 
                    }
        } 
    }

    companion object {
        private val primaryConstructor: KFunction<DictionaryData> by
                lazy(LazyThreadSafetyMode.PUBLICATION) { DictionaryData::class.primaryConstructor!!
                }

        private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
                kotlin.collections.mapOf("namespace" to
                primaryConstructor.findParameterByName("namespace")!!, "entityName" to
                primaryConstructor.findParameterByName("entityName")!!, "values" to
                primaryConstructor.findParameterByName("values")!!, "onlyValues" to
                primaryConstructor.findParameterByName("onlyValues")!!, "minDistance" to
                primaryConstructor.findParameterByName("minDistance")!!, "textSearch" to
                primaryConstructor.findParameterByName("textSearch")!!) }

        private val _values__reference: TypeReference<List<PredefinedValue>> = object :
                TypeReference<List<PredefinedValue>>() {}
    }
}
