package ai.tock.bot.mongo

import kotlin.Any
import kotlin.Boolean
import kotlin.Int
import kotlin.String
import kotlin.collections.Map
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.findParameterByName
import kotlin.reflect.full.primaryConstructor
import org.litote.jackson.JacksonModuleServiceLoader
import tools.jackson.core.JsonParser
import tools.jackson.core.JsonToken
import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.JacksonModule
import tools.jackson.databind.ValueDeserializer
import tools.jackson.databind.module.SimpleModule

internal class DialogFlowAggregateResult_Deserializer : ValueDeserializer<DialogFlowAggregateResult>(),
    JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addDeserializer(DialogFlowAggregateResult::class.java, this)

  override fun deserialize(p: JsonParser, ctxt: DeserializationContext): DialogFlowAggregateResult {
    with(p) {
      var _date_: String? = null
      var _date_set: Boolean = false
      var _count_: Int? = null
      var _count_set: Boolean = false
      var _seriesKey_: String? = null
      var _seriesKey_set: Boolean = false
      var token_: JsonToken? = currentToken()
      if (token_ == JsonToken.START_OBJECT) token_ = nextToken()
      while (token_ == JsonToken.PROPERTY_NAME) {
        val fieldName_ = currentName()
        token_ = nextToken()
        when (fieldName_) {
          "date" -> {
            _date_ = if (token_ == JsonToken.VALUE_NULL) null else p.string
            _date_set = true
          }
          "count" -> {
            _count_ = if (token_ == JsonToken.VALUE_NULL) null else p.intValue
            _count_set = true
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
      val result_ = if (_date_set && _count_set && _seriesKey_set) {
        DialogFlowAggregateResult(date = _date_!!, count = _count_!!, seriesKey = _seriesKey_!!)
      } else {
        val map_ = mutableMapOf<KParameter, Any?>()
        if (_date_set) map_[parameters.getValue("date")] = _date_
        if (_count_set) map_[parameters.getValue("count")] = _count_
        if (_seriesKey_set) map_[parameters.getValue("seriesKey")] = _seriesKey_
        primaryConstructor.callBy(map_)
      }
      return result_
    }
  }

  private companion object {
    private val primaryConstructor: KFunction<DialogFlowAggregateResult> by
        lazy(LazyThreadSafetyMode.PUBLICATION) { DialogFlowAggregateResult::class.primaryConstructor!! }

    private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
          mapOf(
            "date" to primaryConstructor.findParameterByName("date")!!,
            "count" to primaryConstructor.findParameterByName("count")!!,
            "seriesKey" to primaryConstructor.findParameterByName("seriesKey")!!,
          )
        }
  }
}
