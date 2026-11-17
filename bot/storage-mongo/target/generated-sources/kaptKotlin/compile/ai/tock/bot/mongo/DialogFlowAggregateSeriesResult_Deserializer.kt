package ai.tock.bot.mongo

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

internal class DialogFlowAggregateSeriesResult_Deserializer : ValueDeserializer<DialogFlowAggregateSeriesResult>(),
    JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addDeserializer(DialogFlowAggregateSeriesResult::class.java, this)

  override fun deserialize(p: JsonParser, ctxt: DeserializationContext): DialogFlowAggregateSeriesResult {
    with(p) {
      var _values_: List<DialogFlowAggregateResult>? = null
      var _values_set: Boolean = false
      var _seriesKey_: String? = null
      var _seriesKey_set: Boolean = false
      var token_: JsonToken? = currentToken()
      if (token_ == JsonToken.START_OBJECT) token_ = nextToken()
      while (token_ == JsonToken.PROPERTY_NAME) {
        val fieldName_ = currentName()
        token_ = nextToken()
        when (fieldName_) {
          "values" -> {
            _values_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(_values__reference)
            _values_set = true
          }
          "seriesKey" -> {
            _seriesKey_ = if (token_ == JsonToken.VALUE_NULL) null else p.string
            _seriesKey_set = true
          }
          else -> {
            if (token_?.isStructStart == true) p.skipChildren()
          }
        }
        token_ = nextToken()
      }
      val result_ = if (_values_set && _seriesKey_set) {
        DialogFlowAggregateSeriesResult(values = _values_!!, seriesKey = _seriesKey_!!)
      } else {
        val map_ = mutableMapOf<KParameter, Any?>()
        if (_values_set) map_[parameters.getValue("values")] = _values_
        if (_seriesKey_set) map_[parameters.getValue("seriesKey")] = _seriesKey_
        primaryConstructor.callBy(map_)
      }
      return result_
    }
  }

  private companion object {
    private val primaryConstructor: KFunction<DialogFlowAggregateSeriesResult> by
        lazy(LazyThreadSafetyMode.PUBLICATION) { DialogFlowAggregateSeriesResult::class.primaryConstructor!! }

    private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
          mapOf(
            "values" to primaryConstructor.findParameterByName("values")!!,
            "seriesKey" to primaryConstructor.findParameterByName("seriesKey")!!,
          )
        }

    private val _values__reference: TypeReference<List<DialogFlowAggregateResult>> =
        object : TypeReference<List<DialogFlowAggregateResult>>() {}
  }
}
