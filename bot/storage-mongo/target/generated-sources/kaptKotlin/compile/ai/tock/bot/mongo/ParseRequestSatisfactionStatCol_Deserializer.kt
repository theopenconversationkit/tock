package ai.tock.bot.mongo

import kotlin.Any
import kotlin.Boolean
import kotlin.Double
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

internal class ParseRequestSatisfactionStatCol_Deserializer : ValueDeserializer<ParseRequestSatisfactionStatCol>(),
    JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addDeserializer(ParseRequestSatisfactionStatCol::class.java, this)

  override fun deserialize(p: JsonParser, ctxt: DeserializationContext): ParseRequestSatisfactionStatCol {
    with(p) {
      var _rating_: Double? = null
      var _rating_set: Boolean = false
      var _count_: Int? = null
      var _count_set: Boolean = false
      var token_: JsonToken? = currentToken()
      if (token_ == JsonToken.START_OBJECT) token_ = nextToken()
      while (token_ == JsonToken.PROPERTY_NAME) {
        val fieldName_ = currentName()
        token_ = nextToken()
        when (fieldName_) {
          "rating" -> {
            _rating_ = if (token_ == JsonToken.VALUE_NULL) null else p.doubleValue
            _rating_set = true
          }
          "count" -> {
            _count_ = if (token_ == JsonToken.VALUE_NULL) null else p.intValue
            _count_set = true
          }
          else -> {
            if (token_?.isStructStart == true) p.skipChildren()
          }
        }
        token_ = nextToken()
      }
      val result_ = if (_rating_set && _count_set) {
        ParseRequestSatisfactionStatCol(rating = _rating_!!, count = _count_!!)
      } else {
        val map_ = mutableMapOf<KParameter, Any?>()
        if (_rating_set) map_[parameters.getValue("rating")] = _rating_
        if (_count_set) map_[parameters.getValue("count")] = _count_
        primaryConstructor.callBy(map_)
      }
      return result_
    }
  }

  private companion object {
    private val primaryConstructor: KFunction<ParseRequestSatisfactionStatCol> by
        lazy(LazyThreadSafetyMode.PUBLICATION) { ParseRequestSatisfactionStatCol::class.primaryConstructor!! }

    private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
          mapOf(
            "rating" to primaryConstructor.findParameterByName("rating")!!,
            "count" to primaryConstructor.findParameterByName("count")!!,
          )
        }
  }
}
