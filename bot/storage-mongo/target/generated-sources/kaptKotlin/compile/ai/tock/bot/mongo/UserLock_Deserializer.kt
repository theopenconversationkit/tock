package ai.tock.bot.mongo

import java.time.Instant
import kotlin.Any
import kotlin.Boolean
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

internal class UserLock_Deserializer : ValueDeserializer<MongoUserLock.UserLock>(),
    JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addDeserializer(MongoUserLock.UserLock::class.java, this)

  override fun deserialize(p: JsonParser, ctxt: DeserializationContext): MongoUserLock.UserLock {
    with(p) {
      var __id_: Id<MongoUserLock.UserLock>? = null
      var __id_set: Boolean = false
      var _locked_: Boolean? = null
      var _locked_set: Boolean = false
      var _date_: Instant? = null
      var _date_set: Boolean = false
      var _lockId_: String? = null
      var _lockId_set: Boolean = false
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
          "locked" -> {
            _locked_ = if (token_ == JsonToken.VALUE_NULL) null else p.booleanValue
            _locked_set = true
          }
          "date" -> {
            _date_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(Instant::class.javaObjectType)
            _date_set = true
          }
          "lockId" -> {
            _lockId_ = if (token_ == JsonToken.VALUE_NULL) null else p.string
            _lockId_set = true
          }
          else -> {
            if (token_?.isStructStart == true) p.skipChildren()
          }
        }
        token_ = nextToken()
      }
      val result_ = if (__id_set && _locked_set && _date_set && _lockId_set) {
        MongoUserLock.UserLock(_id = __id_!!, locked = _locked_!!, date = _date_!!, lockId = _lockId_)
      } else {
        val map_ = mutableMapOf<KParameter, Any?>()
        if (__id_set) map_[parameters.getValue("_id")] = __id_
        if (_locked_set) map_[parameters.getValue("locked")] = _locked_
        if (_date_set) map_[parameters.getValue("date")] = _date_
        if (_lockId_set) map_[parameters.getValue("lockId")] = _lockId_
        primaryConstructor.callBy(map_)
      }
      return result_
    }
  }

  private companion object {
    private val primaryConstructor: KFunction<MongoUserLock.UserLock> by
        lazy(LazyThreadSafetyMode.PUBLICATION) { MongoUserLock.UserLock::class.primaryConstructor!! }

    private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
          mapOf(
            "_id" to primaryConstructor.findParameterByName("_id")!!,
            "locked" to primaryConstructor.findParameterByName("locked")!!,
            "date" to primaryConstructor.findParameterByName("date")!!,
            "lockId" to primaryConstructor.findParameterByName("lockId")!!,
          )
        }

    private val __id__reference: TypeReference<Id<MongoUserLock.UserLock>> =
        object : TypeReference<Id<MongoUserLock.UserLock>>() {}
  }
}
