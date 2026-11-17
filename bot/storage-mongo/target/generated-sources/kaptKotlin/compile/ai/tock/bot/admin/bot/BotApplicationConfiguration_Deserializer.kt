package ai.tock.bot.admin.bot

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

internal class BotApplicationConfiguration_Deserializer : ValueDeserializer<BotApplicationConfiguration>(),
    JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addDeserializer(BotApplicationConfiguration::class.java, this)

  override fun deserialize(p: JsonParser, ctxt: DeserializationContext): BotApplicationConfiguration {
    with(p) {
      var _applicationId_: String? = null
      var _applicationId_set: Boolean = false
      var _botId_: String? = null
      var _botId_set: Boolean = false
      var _namespace_: String? = null
      var _namespace_set: Boolean = false
      var _nlpModel_: String? = null
      var _nlpModel_set: Boolean = false
      var _connectorType_: ConnectorType? = null
      var _connectorType_set: Boolean = false
      var _ownerConnectorType_: ConnectorType? = null
      var _ownerConnectorType_set: Boolean = false
      var _name_: String? = null
      var _name_set: Boolean = false
      var _baseUrl_: String? = null
      var _baseUrl_set: Boolean = false
      var _parameters_: Map<String, String>? = null
      var _parameters_set: Boolean = false
      var _path_: String? = null
      var _path_set: Boolean = false
      var __id_: Id<BotApplicationConfiguration>? = null
      var __id_set: Boolean = false
      var _targetConfigurationId_: Id<BotApplicationConfiguration>? = null
      var _targetConfigurationId_set: Boolean = false
      var token_: JsonToken? = currentToken()
      if (token_ == JsonToken.START_OBJECT) token_ = nextToken()
      while (token_ == JsonToken.PROPERTY_NAME) {
        val fieldName_ = currentName()
        token_ = nextToken()
        when (fieldName_) {
          "applicationId" -> {
            _applicationId_ = if (token_ == JsonToken.VALUE_NULL) null else p.string
            _applicationId_set = true
          }
          "botId" -> {
            _botId_ = if (token_ == JsonToken.VALUE_NULL) null else p.string
            _botId_set = true
          }
          "namespace" -> {
            _namespace_ = if (token_ == JsonToken.VALUE_NULL) null else p.string
            _namespace_set = true
          }
          "nlpModel" -> {
            _nlpModel_ = if (token_ == JsonToken.VALUE_NULL) null else p.string
            _nlpModel_set = true
          }
          "connectorType" -> {
            _connectorType_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(ConnectorType::class.javaObjectType)
            _connectorType_set = true
          }
          "ownerConnectorType" -> {
            _ownerConnectorType_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(ConnectorType::class.javaObjectType)
            _ownerConnectorType_set = true
          }
          "name" -> {
            _name_ = if (token_ == JsonToken.VALUE_NULL) null else p.string
            _name_set = true
          }
          "baseUrl" -> {
            _baseUrl_ = if (token_ == JsonToken.VALUE_NULL) null else p.string
            _baseUrl_set = true
          }
          "parameters" -> {
            _parameters_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(_parameters__reference)
            _parameters_set = true
          }
          "path" -> {
            _path_ = if (token_ == JsonToken.VALUE_NULL) null else p.string
            _path_set = true
          }
          "_id" -> {
            __id_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(__id__reference)
            __id_set = true
          }
          "targetConfigurationId" -> {
            _targetConfigurationId_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(_targetConfigurationId__reference)
            _targetConfigurationId_set = true
          }
          else -> {
            if (token_?.isStructStart == true) p.skipChildren()
          }
        }
        token_ = nextToken()
      }
      val result_ = if (_applicationId_set && _botId_set && _namespace_set && _nlpModel_set && _connectorType_set && _ownerConnectorType_set && _name_set && _baseUrl_set && _parameters_set && _path_set && __id_set && _targetConfigurationId_set) {
        BotApplicationConfiguration(applicationId = _applicationId_!!, botId = _botId_!!, namespace = _namespace_!!, nlpModel = _nlpModel_!!, connectorType = _connectorType_!!, ownerConnectorType = _ownerConnectorType_, name = _name_!!, baseUrl = _baseUrl_, parameters = _parameters_!!, path = _path_, _id = __id_!!, targetConfigurationId = _targetConfigurationId_)
      } else {
        val map_ = mutableMapOf<KParameter, Any?>()
        if (_applicationId_set) map_[parameters.getValue("applicationId")] = _applicationId_
        if (_botId_set) map_[parameters.getValue("botId")] = _botId_
        if (_namespace_set) map_[parameters.getValue("namespace")] = _namespace_
        if (_nlpModel_set) map_[parameters.getValue("nlpModel")] = _nlpModel_
        if (_connectorType_set) map_[parameters.getValue("connectorType")] = _connectorType_
        if (_ownerConnectorType_set) map_[parameters.getValue("ownerConnectorType")] = _ownerConnectorType_
        if (_name_set) map_[parameters.getValue("name")] = _name_
        if (_baseUrl_set) map_[parameters.getValue("baseUrl")] = _baseUrl_
        if (_parameters_set) map_[parameters.getValue("parameters")] = _parameters_
        if (_path_set) map_[parameters.getValue("path")] = _path_
        if (__id_set) map_[parameters.getValue("_id")] = __id_
        if (_targetConfigurationId_set) map_[parameters.getValue("targetConfigurationId")] = _targetConfigurationId_
        primaryConstructor.callBy(map_)
      }
      return result_
    }
  }

  private companion object {
    private val primaryConstructor: KFunction<BotApplicationConfiguration> by
        lazy(LazyThreadSafetyMode.PUBLICATION) { BotApplicationConfiguration::class.primaryConstructor!! }

    private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
          mapOf(
            "applicationId" to primaryConstructor.findParameterByName("applicationId")!!,
            "botId" to primaryConstructor.findParameterByName("botId")!!,
            "namespace" to primaryConstructor.findParameterByName("namespace")!!,
            "nlpModel" to primaryConstructor.findParameterByName("nlpModel")!!,
            "connectorType" to primaryConstructor.findParameterByName("connectorType")!!,
            "ownerConnectorType" to primaryConstructor.findParameterByName("ownerConnectorType")!!,
            "name" to primaryConstructor.findParameterByName("name")!!,
            "baseUrl" to primaryConstructor.findParameterByName("baseUrl")!!,
            "parameters" to primaryConstructor.findParameterByName("parameters")!!,
            "path" to primaryConstructor.findParameterByName("path")!!,
            "_id" to primaryConstructor.findParameterByName("_id")!!,
            "targetConfigurationId" to primaryConstructor.findParameterByName("targetConfigurationId")!!,
          )
        }

    private val _parameters__reference: TypeReference<Map<String, String>> =
        object : TypeReference<Map<String, String>>() {}

    private val __id__reference: TypeReference<Id<BotApplicationConfiguration>> =
        object : TypeReference<Id<BotApplicationConfiguration>>() {}

    private val _targetConfigurationId__reference: TypeReference<Id<BotApplicationConfiguration>> =
        object : TypeReference<Id<BotApplicationConfiguration>>() {}
  }
}
