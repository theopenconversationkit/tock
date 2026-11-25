package ai.tock.bot.mongo

import ai.tock.shared.jackson.AnyValueWrapper
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
import tools.jackson.core.JsonParser
import tools.jackson.core.JsonToken
import tools.jackson.core.type.TypeReference
import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.JacksonModule
import tools.jackson.databind.ValueDeserializer
import tools.jackson.databind.module.SimpleModule

internal class ConnectorMessageCol_Deserializer : ValueDeserializer<ConnectorMessageCol>(),
    JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addDeserializer(ConnectorMessageCol::class.java, this)

  override fun deserialize(p: JsonParser, ctxt: DeserializationContext): ConnectorMessageCol {
    with(p) {
      var __id_: ConnectorMessageColId? = null
      var __id_set: Boolean = false
      var _messages_: List<AnyValueWrapper?>? = null
      var _messages_set: Boolean = false
      var _date_: Instant? = null
      var _date_set: Boolean = false
      var token_: JsonToken? = currentToken()
      if (token_ == JsonToken.START_OBJECT) token_ = nextToken()
      while (token_ == JsonToken.PROPERTY_NAME) {
        val fieldName_ = currentName()
        token_ = nextToken()
        when (fieldName_) {
          "_id" -> {
            __id_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(ConnectorMessageColId::class.javaObjectType)
            __id_set = true
          }
          "messages" -> {
            _messages_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(_messages__reference)
            _messages_set = true
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
      val result_ = if (__id_set && _messages_set && _date_set) {
        ConnectorMessageCol(_id = __id_!!, messages = _messages_!!, date = _date_!!)
      } else {
        val map_ = mutableMapOf<KParameter, Any?>()
        if (__id_set) map_[parameters.getValue("_id")] = __id_
        if (_messages_set) map_[parameters.getValue("messages")] = _messages_
        if (_date_set) map_[parameters.getValue("date")] = _date_
        primaryConstructor.callBy(map_)
      }
      return result_
    }
  }

  private companion object {
    private val primaryConstructor: KFunction<ConnectorMessageCol> by
        lazy(LazyThreadSafetyMode.PUBLICATION) { ConnectorMessageCol::class.primaryConstructor!! }

    private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
          mapOf(
            "_id" to primaryConstructor.findParameterByName("_id")!!,
            "messages" to primaryConstructor.findParameterByName("messages")!!,
            "date" to primaryConstructor.findParameterByName("date")!!,
          )
        }

    private val _messages__reference: TypeReference<List<AnyValueWrapper?>> =
        object : TypeReference<List<AnyValueWrapper?>>() {}
  }
}
