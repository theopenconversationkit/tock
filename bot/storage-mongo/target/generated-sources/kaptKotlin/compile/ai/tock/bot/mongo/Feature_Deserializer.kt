package ai.tock.bot.mongo

import java.time.ZonedDateTime
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

internal class Feature_Deserializer : ValueDeserializer<Feature>(), JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addDeserializer(Feature::class.java, this)

  override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Feature {
    with(p) {
      var __id_: String? = null
      var __id_set: Boolean = false
      var _key_: String? = null
      var _key_set: Boolean = false
      var _enabled_: Boolean? = null
      var _enabled_set: Boolean = false
      var _botId_: String? = null
      var _botId_set: Boolean = false
      var _namespace_: String? = null
      var _namespace_set: Boolean = false
      var _startDate_: ZonedDateTime? = null
      var _startDate_set: Boolean = false
      var _endDate_: ZonedDateTime? = null
      var _endDate_set: Boolean = false
      var _graduation_: Int? = null
      var _graduation_set: Boolean = false
      var token_: JsonToken? = currentToken()
      if (token_ == JsonToken.START_OBJECT) token_ = nextToken()
      while (token_ == JsonToken.PROPERTY_NAME) {
        val fieldName_ = currentName()
        token_ = nextToken()
        when (fieldName_) {
          "_id" -> {
            __id_ = if (token_ == JsonToken.VALUE_NULL) null else p.string
            __id_set = true
          }
          "key" -> {
            _key_ = if (token_ == JsonToken.VALUE_NULL) null else p.string
            _key_set = true
          }
          "enabled" -> {
            _enabled_ = if (token_ == JsonToken.VALUE_NULL) null else p.booleanValue
            _enabled_set = true
          }
          "botId" -> {
            _botId_ = if (token_ == JsonToken.VALUE_NULL) null else p.string
            _botId_set = true
          }
          "namespace" -> {
            _namespace_ = if (token_ == JsonToken.VALUE_NULL) null else p.string
            _namespace_set = true
          }
          "startDate" -> {
            _startDate_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(ZonedDateTime::class.javaObjectType)
            _startDate_set = true
          }
          "endDate" -> {
            _endDate_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(ZonedDateTime::class.javaObjectType)
            _endDate_set = true
          }
          "graduation" -> {
            _graduation_ = if (token_ == JsonToken.VALUE_NULL) null else p.intValue
            _graduation_set = true
          }
          else -> {
            if (token_?.isStructStart == true) p.skipChildren()
          }
        }
        token_ = nextToken()
      }
      val result_ = if (__id_set && _key_set && _enabled_set && _botId_set && _namespace_set && _startDate_set && _endDate_set && _graduation_set) {
        Feature(_id = __id_!!, key = _key_!!, enabled = _enabled_!!, botId = _botId_!!, namespace = _namespace_!!, startDate = _startDate_, endDate = _endDate_, graduation = _graduation_)
      } else {
        val map_ = mutableMapOf<KParameter, Any?>()
        if (__id_set) map_[parameters.getValue("_id")] = __id_
        if (_key_set) map_[parameters.getValue("key")] = _key_
        if (_enabled_set) map_[parameters.getValue("enabled")] = _enabled_
        if (_botId_set) map_[parameters.getValue("botId")] = _botId_
        if (_namespace_set) map_[parameters.getValue("namespace")] = _namespace_
        if (_startDate_set) map_[parameters.getValue("startDate")] = _startDate_
        if (_endDate_set) map_[parameters.getValue("endDate")] = _endDate_
        if (_graduation_set) map_[parameters.getValue("graduation")] = _graduation_
        primaryConstructor.callBy(map_)
      }
      return result_
    }
  }

  private companion object {
    private val primaryConstructor: KFunction<Feature> by
        lazy(LazyThreadSafetyMode.PUBLICATION) { Feature::class.primaryConstructor!! }

    private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
          mapOf(
            "_id" to primaryConstructor.findParameterByName("_id")!!,
            "key" to primaryConstructor.findParameterByName("key")!!,
            "enabled" to primaryConstructor.findParameterByName("enabled")!!,
            "botId" to primaryConstructor.findParameterByName("botId")!!,
            "namespace" to primaryConstructor.findParameterByName("namespace")!!,
            "startDate" to primaryConstructor.findParameterByName("startDate")!!,
            "endDate" to primaryConstructor.findParameterByName("endDate")!!,
            "graduation" to primaryConstructor.findParameterByName("graduation")!!,
          )
        }
  }
}
