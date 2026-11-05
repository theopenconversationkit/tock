package ai.tock.nlp.front.storage.mongo

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

internal class DayAndYear_Deserializer : ValueDeserializer<ParseRequestLogMongoDAO.DayAndYear>(),
    JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addDeserializer(ParseRequestLogMongoDAO.DayAndYear::class.java, this)

  override fun deserialize(p: JsonParser, ctxt: DeserializationContext): ParseRequestLogMongoDAO.DayAndYear {
    with(p) {
      var _dayOfYear_: Int? = null
      var _dayOfYear_set: Boolean = false
      var _year_: Int? = null
      var _year_set: Boolean = false
      var token_: JsonToken? = currentToken()
      if (token_ == JsonToken.START_OBJECT) token_ = nextToken()
      while (token_ == JsonToken.PROPERTY_NAME) {
        val fieldName_ = currentName()
        token_ = nextToken()
        when (fieldName_) {
          "dayOfYear" -> {
            _dayOfYear_ = if (token_ == JsonToken.VALUE_NULL) null else p.intValue
            _dayOfYear_set = true
          }
          "year" -> {
            _year_ = if (token_ == JsonToken.VALUE_NULL) null else p.intValue
            _year_set = true
          }
          else -> {
            if (token_?.isStructStart == true) p.skipChildren()
          }
        }
        token_ = nextToken()
      }
      val result_ = if (_dayOfYear_set && _year_set) {
        ParseRequestLogMongoDAO.DayAndYear(dayOfYear = _dayOfYear_!!, year = _year_!!)
      } else {
        val map_ = mutableMapOf<KParameter, Any?>()
        if (_dayOfYear_set) map_[parameters.getValue("dayOfYear")] = _dayOfYear_
        if (_year_set) map_[parameters.getValue("year")] = _year_
        primaryConstructor.callBy(map_)
      }
      return result_
    }
  }

  private companion object {
    private val primaryConstructor: KFunction<ParseRequestLogMongoDAO.DayAndYear> by
        lazy(LazyThreadSafetyMode.PUBLICATION) { ParseRequestLogMongoDAO.DayAndYear::class.primaryConstructor!! }

    private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
          mapOf(
            "dayOfYear" to primaryConstructor.findParameterByName("dayOfYear")!!,
            "year" to primaryConstructor.findParameterByName("year")!!,
          )
        }
  }
}
