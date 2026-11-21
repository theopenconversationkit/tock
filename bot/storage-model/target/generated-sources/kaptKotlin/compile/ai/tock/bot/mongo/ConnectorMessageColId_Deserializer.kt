package ai.tock.bot.mongo

import ai.tock.bot.engine.action.Action
import ai.tock.bot.engine.dialog.Dialog
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

internal class ConnectorMessageColId_Deserializer : ValueDeserializer<ConnectorMessageColId>(),
    JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addDeserializer(ConnectorMessageColId::class.java, this)

  override fun deserialize(p: JsonParser, ctxt: DeserializationContext): ConnectorMessageColId {
    with(p) {
      var _actionId_: Id<Action>? = null
      var _actionId_set: Boolean = false
      var _dialogId_: Id<Dialog>? = null
      var _dialogId_set: Boolean = false
      var token_: JsonToken? = currentToken()
      if (token_ == JsonToken.START_OBJECT) token_ = nextToken()
      while (token_ == JsonToken.PROPERTY_NAME) {
        val fieldName_ = currentName()
        token_ = nextToken()
        when (fieldName_) {
          "actionId" -> {
            _actionId_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(_actionId__reference)
            _actionId_set = true
          }
          "dialogId" -> {
            _dialogId_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(_dialogId__reference)
            _dialogId_set = true
          }
          else -> {
            if (token_?.isStructStart == true) p.skipChildren()
          }
        }
        token_ = nextToken()
      }
      val result_ = if (_actionId_set && _dialogId_set) {
        ConnectorMessageColId(actionId = _actionId_!!, dialogId = _dialogId_!!)
      } else {
        val map_ = mutableMapOf<KParameter, Any?>()
        if (_actionId_set) map_[parameters.getValue("actionId")] = _actionId_
        if (_dialogId_set) map_[parameters.getValue("dialogId")] = _dialogId_
        primaryConstructor.callBy(map_)
      }
      return result_
    }
  }

  private companion object {
    private val primaryConstructor: KFunction<ConnectorMessageColId> by
        lazy(LazyThreadSafetyMode.PUBLICATION) { ConnectorMessageColId::class.primaryConstructor!! }

    private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
          mapOf(
            "actionId" to primaryConstructor.findParameterByName("actionId")!!,
            "dialogId" to primaryConstructor.findParameterByName("dialogId")!!,
          )
        }

    private val _actionId__reference: TypeReference<Id<Action>> =
        object : TypeReference<Id<Action>>() {}

    private val _dialogId__reference: TypeReference<Id<Dialog>> =
        object : TypeReference<Id<Dialog>>() {}
  }
}
