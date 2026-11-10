package ai.tock.bot.mongo

import java.time.Instant
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
import org.litote.kmongo.Id
import tools.jackson.core.JsonParser
import tools.jackson.core.JsonToken
import tools.jackson.core.type.TypeReference
import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.JacksonModule
import tools.jackson.databind.ValueDeserializer
import tools.jackson.databind.module.SimpleModule

internal class ArchivedEntityValuesCol_Deserializer : ValueDeserializer<ArchivedEntityValuesCol>(),
    JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addDeserializer(ArchivedEntityValuesCol::class.java, this)

  override fun deserialize(p: JsonParser, ctxt: DeserializationContext): ArchivedEntityValuesCol {
    with(p) {
      var __id_: Id<ArchivedEntityValuesCol>? = null
      var __id_set: Boolean = false
      var _values_: List<ArchivedEntityValuesCol.ArchivedEntityValueWrapper>? = null
      var _values_set: Boolean = false
      var _lastUpdateDate_: Instant? = null
      var _lastUpdateDate_set: Boolean = false
      var token_: JsonToken? = currentToken()
      if (token_ == JsonToken.START_OBJECT) token_ = nextToken()
      while (token_ == JsonToken.PROPERTY_NAME) {
        val fieldName_ = currentName()
        token_ = nextToken()
        when (fieldName_) {
          "_id" -> {
            __id_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(__id__reference)
            __id_set = true
          }
          "values" -> {
            _values_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(_values__reference)
            _values_set = true
          }
          "lastUpdateDate" -> {
            _lastUpdateDate_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(Instant::class.javaObjectType)
            _lastUpdateDate_set = true
          }
          else -> {
            if (token_?.isStructStart == true) p.skipChildren()
          }
        }
        token_ = nextToken()
      }
      val result_ = if (__id_set && _values_set && _lastUpdateDate_set) {
        ArchivedEntityValuesCol(_id = __id_!!, values = _values_!!, lastUpdateDate = _lastUpdateDate_!!)
      } else {
        val map_ = mutableMapOf<KParameter, Any?>()
        if (__id_set) map_[parameters.getValue("_id")] = __id_
        if (_values_set) map_[parameters.getValue("values")] = _values_
        if (_lastUpdateDate_set) map_[parameters.getValue("lastUpdateDate")] = _lastUpdateDate_
        primaryConstructor.callBy(map_)
      }
      return result_
    }
  }

  private companion object {
    private val primaryConstructor: KFunction<ArchivedEntityValuesCol> by
        lazy(LazyThreadSafetyMode.PUBLICATION) { ArchivedEntityValuesCol::class.primaryConstructor!! }

    private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
          mapOf(
            "_id" to primaryConstructor.findParameterByName("_id")!!,
            "values" to primaryConstructor.findParameterByName("values")!!,
            "lastUpdateDate" to primaryConstructor.findParameterByName("lastUpdateDate")!!,
          )
        }

    private val __id__reference: TypeReference<Id<ArchivedEntityValuesCol>> =
        object : TypeReference<Id<ArchivedEntityValuesCol>>() {}

    private val _values__reference:
        TypeReference<List<ArchivedEntityValuesCol.ArchivedEntityValueWrapper>> =
        object : TypeReference<List<ArchivedEntityValuesCol.ArchivedEntityValueWrapper>>() {}
  }
}
