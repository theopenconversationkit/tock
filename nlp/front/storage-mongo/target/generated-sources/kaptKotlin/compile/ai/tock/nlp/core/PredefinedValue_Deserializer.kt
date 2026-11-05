package ai.tock.nlp.core

import java.util.Locale
import kotlin.Any
import kotlin.Boolean
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

internal class PredefinedValue_Deserializer : ValueDeserializer<PredefinedValue>(),
    JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addDeserializer(PredefinedValue::class.java, this)

  override fun deserialize(p: JsonParser, ctxt: DeserializationContext): PredefinedValue {
    with(p) {
      var _value_: String? = null
      var _value_set: Boolean = false
      var _labels_: Map<Locale, List<String>>? = null
      var _labels_set: Boolean = false
      var token_: JsonToken? = currentToken()
      if (token_ == JsonToken.START_OBJECT) token_ = nextToken()
      while (token_ == JsonToken.PROPERTY_NAME) {
        val fieldName_ = currentName()
        token_ = nextToken()
        when (fieldName_) {
          "value" -> {
            _value_ = if (token_ == JsonToken.VALUE_NULL) null else p.string
            _value_set = true
          }
          "labels" -> {
            _labels_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(_labels__reference)
            _labels_set = true
          }
          else -> {
            if (token_?.isStructStart == true) p.skipChildren()
          }
        }
        token_ = nextToken()
      }
      val result_ = if (_value_set && _labels_set) {
        PredefinedValue(`value` = _value_!!, labels = _labels_!!)
      } else {
        val map_ = mutableMapOf<KParameter, Any?>()
        if (_value_set) map_[parameters.getValue("value")] = _value_
        if (_labels_set) map_[parameters.getValue("labels")] = _labels_
        primaryConstructor.callBy(map_)
      }
      return result_
    }
  }

  private companion object {
    private val primaryConstructor: KFunction<PredefinedValue> by
        lazy(LazyThreadSafetyMode.PUBLICATION) { PredefinedValue::class.primaryConstructor!! }

    private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
          mapOf(
            "value" to primaryConstructor.findParameterByName("value")!!,
            "labels" to primaryConstructor.findParameterByName("labels")!!,
          )
        }

    private val _labels__reference: TypeReference<Map<Locale, List<String>>> =
        object : TypeReference<Map<Locale, List<String>>>() {}
  }
}
