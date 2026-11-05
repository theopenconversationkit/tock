package ai.tock.bot.mongo

import ai.tock.bot.admin.bot.BotApplicationConfiguration
import ai.tock.bot.connector.ConnectorType
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

internal class GroupById_Deserializer : ValueDeserializer<GroupById>(), JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addDeserializer(GroupById::class.java, this)

  override fun deserialize(p: JsonParser, ctxt: DeserializationContext): GroupById {
    with(p) {
      var _date_: String? = null
      var _date_set: Boolean = false
      var _dialogId_: String? = null
      var _dialogId_set: Boolean = false
      var _connectorType_: ConnectorType? = null
      var _connectorType_set: Boolean = false
      var _configuration_: String? = null
      var _configuration_set: Boolean = false
      var _intent_: String? = null
      var _intent_set: Boolean = false
      var _storyDefinitionId_: String? = null
      var _storyDefinitionId_set: Boolean = false
      var _applicationId_: Id<BotApplicationConfiguration>? = null
      var _applicationId_set: Boolean = false
      var token_: JsonToken? = currentToken()
      if (token_ == JsonToken.START_OBJECT) token_ = nextToken()
      while (token_ == JsonToken.PROPERTY_NAME) {
        val fieldName_ = currentName()
        token_ = nextToken()
        when (fieldName_) {
          "date" -> {
            _date_ = if (token_ == JsonToken.VALUE_NULL) null else p.string
            _date_set = true
          }
          "dialogId" -> {
            _dialogId_ = if (token_ == JsonToken.VALUE_NULL) null else p.string
            _dialogId_set = true
          }
          "connectorType" -> {
            _connectorType_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(ConnectorType::class.javaObjectType)
            _connectorType_set = true
          }
          "configuration" -> {
            _configuration_ = if (token_ == JsonToken.VALUE_NULL) null else p.string
            _configuration_set = true
          }
          "intent" -> {
            _intent_ = if (token_ == JsonToken.VALUE_NULL) null else p.string
            _intent_set = true
          }
          "storyDefinitionId" -> {
            _storyDefinitionId_ = if (token_ == JsonToken.VALUE_NULL) null else p.string
            _storyDefinitionId_set = true
          }
          "applicationId" -> {
            _applicationId_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(_applicationId__reference)
            _applicationId_set = true
          }
          else -> {
            if (token_?.isStructStart == true) p.skipChildren()
          }
        }
        token_ = nextToken()
      }
      val result_ = if (_date_set && _dialogId_set && _connectorType_set && _configuration_set && _intent_set && _storyDefinitionId_set && _applicationId_set) {
        GroupById(date = _date_!!, dialogId = _dialogId_!!, connectorType = _connectorType_!!, configuration = _configuration_!!, intent = _intent_!!, storyDefinitionId = _storyDefinitionId_!!, applicationId = _applicationId_!!)
      } else {
        val map_ = mutableMapOf<KParameter, Any?>()
        if (_date_set) map_[parameters.getValue("date")] = _date_
        if (_dialogId_set) map_[parameters.getValue("dialogId")] = _dialogId_
        if (_connectorType_set) map_[parameters.getValue("connectorType")] = _connectorType_
        if (_configuration_set) map_[parameters.getValue("configuration")] = _configuration_
        if (_intent_set) map_[parameters.getValue("intent")] = _intent_
        if (_storyDefinitionId_set) map_[parameters.getValue("storyDefinitionId")] = _storyDefinitionId_
        if (_applicationId_set) map_[parameters.getValue("applicationId")] = _applicationId_
        primaryConstructor.callBy(map_)
      }
      return result_
    }
  }

  private companion object {
    private val primaryConstructor: KFunction<GroupById> by
        lazy(LazyThreadSafetyMode.PUBLICATION) { GroupById::class.primaryConstructor!! }

    private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
          mapOf(
            "date" to primaryConstructor.findParameterByName("date")!!,
            "dialogId" to primaryConstructor.findParameterByName("dialogId")!!,
            "connectorType" to primaryConstructor.findParameterByName("connectorType")!!,
            "configuration" to primaryConstructor.findParameterByName("configuration")!!,
            "intent" to primaryConstructor.findParameterByName("intent")!!,
            "storyDefinitionId" to primaryConstructor.findParameterByName("storyDefinitionId")!!,
            "applicationId" to primaryConstructor.findParameterByName("applicationId")!!,
          )
        }

    private val _applicationId__reference: TypeReference<Id<BotApplicationConfiguration>> =
        object : TypeReference<Id<BotApplicationConfiguration>>() {}
  }
}
