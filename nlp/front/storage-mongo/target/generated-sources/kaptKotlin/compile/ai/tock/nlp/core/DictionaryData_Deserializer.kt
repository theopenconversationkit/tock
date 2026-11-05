package ai.tock.nlp.core

import kotlin.Any
import kotlin.Boolean
import kotlin.Double
import kotlin.String
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.findParameterByName
import kotlin.reflect.full.primaryConstructor
import org.litote.jackson.JacksonModuleServiceLoader
import tools.jackson.core.JsonParser
import tools.jackson.core.JsonToken
import tools.jackson.core.type.TypeReference
import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.JacksonModule
import tools.jackson.databind.ValueDeserializer
import tools.jackson.databind.module.SimpleModule

internal class DictionaryData_Deserializer : ValueDeserializer<DictionaryData>(),
    JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addDeserializer(DictionaryData::class.java, this)

  override fun deserialize(p: JsonParser, ctxt: DeserializationContext): DictionaryData {
    with(p) {
      var _namespace_: String? = null
      var _namespace_set: Boolean = false
      var _entityName_: String? = null
      var _entityName_set: Boolean = false
      var _values_: List<PredefinedValue>? = null
      var _values_set: Boolean = false
      var _onlyValues_: Boolean? = null
      var _onlyValues_set: Boolean = false
      var _minDistance_: Double? = null
      var _minDistance_set: Boolean = false
      var _textSearch_: Boolean? = null
      var _textSearch_set: Boolean = false
      var token_: JsonToken? = currentToken()
      if (token_ == JsonToken.START_OBJECT) token_ = nextToken()
      while (token_ == JsonToken.PROPERTY_NAME) {
        val fieldName_ = currentName()
        token_ = nextToken()
        when (fieldName_) {
          "namespace" -> {
            _namespace_ = if (token_ == JsonToken.VALUE_NULL) null else p.string
            _namespace_set = true
          }
          "entityName" -> {
            _entityName_ = if (token_ == JsonToken.VALUE_NULL) null else p.string
            _entityName_set = true
          }
          "values" -> {
            _values_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(_values__reference)
            _values_set = true
          }
          "onlyValues" -> {
            _onlyValues_ = if (token_ == JsonToken.VALUE_NULL) null else p.booleanValue
            _onlyValues_set = true
          }
          "minDistance" -> {
            _minDistance_ = if (token_ == JsonToken.VALUE_NULL) null else p.doubleValue
            _minDistance_set = true
          }
          "textSearch" -> {
            _textSearch_ = if (token_ == JsonToken.VALUE_NULL) null else p.booleanValue
            _textSearch_set = true
          }
          else -> {
            if (token_?.isStructStart == true) p.skipChildren()
          }
        }
        token_ = nextToken()
      }
      val result_ = if (_namespace_set && _entityName_set && _values_set && _onlyValues_set && _minDistance_set && _textSearch_set) {
        DictionaryData(namespace = _namespace_!!, entityName = _entityName_!!, values = _values_!!, onlyValues = _onlyValues_!!, minDistance = _minDistance_!!, textSearch = _textSearch_!!)
      } else {
        val map_ = mutableMapOf<KParameter, Any?>()
        if (_namespace_set) map_[parameters.getValue("namespace")] = _namespace_
        if (_entityName_set) map_[parameters.getValue("entityName")] = _entityName_
        if (_values_set) map_[parameters.getValue("values")] = _values_
        if (_onlyValues_set) map_[parameters.getValue("onlyValues")] = _onlyValues_
        if (_minDistance_set) map_[parameters.getValue("minDistance")] = _minDistance_
        if (_textSearch_set) map_[parameters.getValue("textSearch")] = _textSearch_
        primaryConstructor.callBy(map_)
      }
      return result_
    }
  }

  private companion object {
    private val primaryConstructor: KFunction<DictionaryData> by
        lazy(LazyThreadSafetyMode.PUBLICATION) { DictionaryData::class.primaryConstructor!! }

    private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
          mapOf(
            "namespace" to primaryConstructor.findParameterByName("namespace")!!,
            "entityName" to primaryConstructor.findParameterByName("entityName")!!,
            "values" to primaryConstructor.findParameterByName("values")!!,
            "onlyValues" to primaryConstructor.findParameterByName("onlyValues")!!,
            "minDistance" to primaryConstructor.findParameterByName("minDistance")!!,
            "textSearch" to primaryConstructor.findParameterByName("textSearch")!!,
          )
        }

    private val _values__reference: TypeReference<List<PredefinedValue>> =
        object : TypeReference<List<PredefinedValue>>() {}
  }
}
