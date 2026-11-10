package ai.tock.shared.cache.mongo

import ai.tock.shared.jackson.AnyValueWrapper
import java.time.Instant
import kotlin.Any
import kotlin.Boolean
import kotlin.ByteArray
import kotlin.String
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

internal class MongoCacheData_Deserializer : ValueDeserializer<MongoCacheData>(),
    JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addDeserializer(MongoCacheData::class.java, this)

  override fun deserialize(p: JsonParser, ctxt: DeserializationContext): MongoCacheData {
    with(p) {
      var _id_: Id<*>? = null
      var _id_set: Boolean = false
      var _type_: String? = null
      var _type_set: Boolean = false
      var _s_: String? = null
      var _s_set: Boolean = false
      var _b_: ByteArray? = null
      var _b_set: Boolean = false
      var _a_: AnyValueWrapper? = null
      var _a_set: Boolean = false
      var _date_: Instant? = null
      var _date_set: Boolean = false
      var token_: JsonToken? = currentToken()
      if (token_ == JsonToken.START_OBJECT) token_ = nextToken()
      while (token_ == JsonToken.PROPERTY_NAME) {
        val fieldName_ = currentName()
        token_ = nextToken()
        when (fieldName_) {
          "id" -> {
            _id_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(_id__reference)
            _id_set = true
          }
          "type" -> {
            _type_ = if (token_ == JsonToken.VALUE_NULL) null else p.string
            _type_set = true
          }
          "s" -> {
            _s_ = if (token_ == JsonToken.VALUE_NULL) null else p.string
            _s_set = true
          }
          "b" -> {
            _b_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(ByteArray::class.javaObjectType)
            _b_set = true
          }
          "a" -> {
            _a_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(AnyValueWrapper::class.javaObjectType)
            _a_set = true
          }
          "date" -> {
            _date_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(Instant::class.javaObjectType)
            _date_set = true
          }
          else -> {
            if (token_?.isStructStart == true) p.skipChildren()
          }
        }
        token_ = nextToken()
      }
      val result_ = if (_id_set && _type_set && _s_set && _b_set && _a_set && _date_set) {
        MongoCacheData(id = _id_!!, type = _type_!!, s = _s_, b = _b_, a = _a_, date = _date_!!)
      } else {
        val map_ = mutableMapOf<KParameter, Any?>()
        if (_id_set) map_[parameters.getValue("id")] = _id_
        if (_type_set) map_[parameters.getValue("type")] = _type_
        if (_s_set) map_[parameters.getValue("s")] = _s_
        if (_b_set) map_[parameters.getValue("b")] = _b_
        if (_a_set) map_[parameters.getValue("a")] = _a_
        if (_date_set) map_[parameters.getValue("date")] = _date_
        primaryConstructor.callBy(map_)
      }
      return result_
    }
  }

  private companion object {
    private val primaryConstructor: KFunction<MongoCacheData> by
        lazy(LazyThreadSafetyMode.PUBLICATION) { MongoCacheData::class.primaryConstructor!! }

    private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
          mapOf(
            "id" to primaryConstructor.findParameterByName("id")!!,
            "type" to primaryConstructor.findParameterByName("type")!!,
            "s" to primaryConstructor.findParameterByName("s")!!,
            "b" to primaryConstructor.findParameterByName("b")!!,
            "a" to primaryConstructor.findParameterByName("a")!!,
            "date" to primaryConstructor.findParameterByName("date")!!,
          )
        }

    private val _id__reference: TypeReference<Id<*>> = object : TypeReference<Id<*>>() {}
  }
}
