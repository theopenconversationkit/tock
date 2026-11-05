package ai.tock.bot.mongo

import kotlin.Any
import kotlin.Boolean
import kotlin.String
import kotlin.collections.Map
import kotlin.collections.Set
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

internal class ClientIdCol_Deserializer : ValueDeserializer<ClientIdCol>(),
    JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addDeserializer(ClientIdCol::class.java, this)

  override fun deserialize(p: JsonParser, ctxt: DeserializationContext): ClientIdCol {
    with(p) {
      var _userIds_: Set<String>? = null
      var _userIds_set: Boolean = false
      var __id_: Id<ClientIdCol>? = null
      var __id_set: Boolean = false
      var token_: JsonToken? = currentToken()
      if (token_ == JsonToken.START_OBJECT) token_ = nextToken()
      while (token_ == JsonToken.PROPERTY_NAME) {
        val fieldName_ = currentName()
        token_ = nextToken()
        when (fieldName_) {
          "userIds" -> {
            _userIds_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(_userIds__reference)
            _userIds_set = true
          }
          "_id" -> {
            __id_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(__id__reference)
            __id_set = true
          }
          else -> {
            if (token_?.isStructStart == true) p.skipChildren()
          }
        }
        token_ = nextToken()
      }
      val result_ = if (_userIds_set && __id_set) {
        ClientIdCol(userIds = _userIds_!!, _id = __id_!!)
      } else {
        val map_ = mutableMapOf<KParameter, Any?>()
        if (_userIds_set) map_[parameters.getValue("userIds")] = _userIds_
        if (__id_set) map_[parameters.getValue("_id")] = __id_
        primaryConstructor.callBy(map_)
      }
      return result_
    }
  }

  private companion object {
    private val primaryConstructor: KFunction<ClientIdCol> by
        lazy(LazyThreadSafetyMode.PUBLICATION) { ClientIdCol::class.primaryConstructor!! }

    private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
          mapOf(
            "userIds" to primaryConstructor.findParameterByName("userIds")!!,
            "_id" to primaryConstructor.findParameterByName("_id")!!,
          )
        }

    private val _userIds__reference: TypeReference<Set<String>> =
        object : TypeReference<Set<String>>() {}

    private val __id__reference: TypeReference<Id<ClientIdCol>> =
        object : TypeReference<Id<ClientIdCol>>() {}
  }
}
