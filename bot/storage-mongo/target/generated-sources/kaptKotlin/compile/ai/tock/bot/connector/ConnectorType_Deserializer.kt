package ai.tock.bot.connector

import ai.tock.translator.UserInterfaceType
import kotlin.Any
import kotlin.Boolean
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

internal class ConnectorType_Deserializer : ValueDeserializer<ConnectorType>(),
    JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addDeserializer(ConnectorType::class.java, this)

  override fun deserialize(p: JsonParser, ctxt: DeserializationContext): ConnectorType {
    with(p) {
      var _id_: String? = null
      var _id_set: Boolean = false
      var _userInterfaceType_: UserInterfaceType? = null
      var _userInterfaceType_set: Boolean = false
      var token_: JsonToken? = currentToken()
      if (token_ == JsonToken.START_OBJECT) token_ = nextToken()
      while (token_ == JsonToken.PROPERTY_NAME) {
        val fieldName_ = currentName()
        token_ = nextToken()
        when (fieldName_) {
          "id" -> {
            _id_ = if (token_ == JsonToken.VALUE_NULL) null else p.string
            _id_set = true
          }
          "userInterfaceType" -> {
            _userInterfaceType_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(UserInterfaceType::class.javaObjectType)
            _userInterfaceType_set = true
          }
          else -> {
            if (token_?.isStructStart == true) p.skipChildren()
          }
        }
        token_ = nextToken()
      }
      val result_ = if (_id_set && _userInterfaceType_set) {
        ConnectorType(id = _id_!!, userInterfaceType = _userInterfaceType_!!)
      } else {
        val map_ = mutableMapOf<KParameter, Any?>()
        if (_id_set) map_[parameters.getValue("id")] = _id_
        if (_userInterfaceType_set) map_[parameters.getValue("userInterfaceType")] = _userInterfaceType_
        primaryConstructor.callBy(map_)
      }
      return result_
    }
  }

  private companion object {
    private val primaryConstructor: KFunction<ConnectorType> by
        lazy(LazyThreadSafetyMode.PUBLICATION) { ConnectorType::class.primaryConstructor!! }

    private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
          mapOf(
            "id" to primaryConstructor.findParameterByName("id")!!,
            "userInterfaceType" to primaryConstructor.findParameterByName("userInterfaceType")!!,
          )
        }
  }
}
