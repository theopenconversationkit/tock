package ai.tock.nlp.front.storage.mongo

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

internal class ParseRequestLogStatResult_Deserializer : ValueDeserializer<ParseRequestLogMongoDAO.ParseRequestLogStatResult>(),
    JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addDeserializer(ParseRequestLogMongoDAO.ParseRequestLogStatResult::class.java, this)

  override fun deserialize(p: JsonParser, ctxt: DeserializationContext): ParseRequestLogMongoDAO.ParseRequestLogStatResult {
    with(p) {
      var __id_: ParseRequestLogMongoDAO.DayAndYear? = null
      var __id_set: Boolean = false
      var _error_: Int? = null
      var _error_set: Boolean = false
      var _count_: Int? = null
      var _count_set: Boolean = false
      var _duration_: Double? = null
      var _duration_set: Boolean = false
      var _intentProbability_: Double? = null
      var _intentProbability_set: Boolean = false
      var _entitiesProbability_: Double? = null
      var _entitiesProbability_set: Boolean = false
      var token_: JsonToken? = currentToken()
      if (token_ == JsonToken.START_OBJECT) token_ = nextToken()
      while (token_ == JsonToken.PROPERTY_NAME) {
        val fieldName_ = currentName()
        token_ = nextToken()
        when (fieldName_) {
          "_id" -> {
            __id_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(ParseRequestLogMongoDAO.DayAndYear::class.javaObjectType)
            __id_set = true
          }
          "error" -> {
            _error_ = if (token_ == JsonToken.VALUE_NULL) null else p.intValue
            _error_set = true
          }
          "count" -> {
            _count_ = if (token_ == JsonToken.VALUE_NULL) null else p.intValue
            _count_set = true
          }
          "duration" -> {
            _duration_ = if (token_ == JsonToken.VALUE_NULL) null else p.doubleValue
            _duration_set = true
          }
          "intentProbability" -> {
            _intentProbability_ = if (token_ == JsonToken.VALUE_NULL) null else p.doubleValue
            _intentProbability_set = true
          }
          "entitiesProbability" -> {
            _entitiesProbability_ = if (token_ == JsonToken.VALUE_NULL) null else p.doubleValue
            _entitiesProbability_set = true
          }
          else -> {
            if (token_?.isStructStart == true) p.skipChildren()
          }
        }
        token_ = nextToken()
      }
      val result_ = if (__id_set && _error_set && _count_set && _duration_set && _intentProbability_set && _entitiesProbability_set) {
        ParseRequestLogMongoDAO.ParseRequestLogStatResult(_id = __id_!!, error = _error_!!, count = _count_!!, duration = _duration_!!, intentProbability = _intentProbability_!!, entitiesProbability = _entitiesProbability_!!)
      } else {
        val map_ = mutableMapOf<KParameter, Any?>()
        if (__id_set) map_[parameters.getValue("_id")] = __id_
        if (_error_set) map_[parameters.getValue("error")] = _error_
        if (_count_set) map_[parameters.getValue("count")] = _count_
        if (_duration_set) map_[parameters.getValue("duration")] = _duration_
        if (_intentProbability_set) map_[parameters.getValue("intentProbability")] = _intentProbability_
        if (_entitiesProbability_set) map_[parameters.getValue("entitiesProbability")] = _entitiesProbability_
        primaryConstructor.callBy(map_)
      }
      return result_
    }
  }

  private companion object {
    private val primaryConstructor: KFunction<ParseRequestLogMongoDAO.ParseRequestLogStatResult> by
        lazy(LazyThreadSafetyMode.PUBLICATION) { ParseRequestLogMongoDAO.ParseRequestLogStatResult::class.primaryConstructor!! }

    private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
          mapOf(
            "_id" to primaryConstructor.findParameterByName("_id")!!,
            "error" to primaryConstructor.findParameterByName("error")!!,
            "count" to primaryConstructor.findParameterByName("count")!!,
            "duration" to primaryConstructor.findParameterByName("duration")!!,
            "intentProbability" to primaryConstructor.findParameterByName("intentProbability")!!,
            "entitiesProbability" to primaryConstructor.findParameterByName("entitiesProbability")!!,
          )
        }
  }
}
