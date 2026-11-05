package ai.tock.bot.mongo

import ai.tock.bot.admin.bot.BotApplicationConfiguration
import ai.tock.bot.connector.ConnectorType
import ai.tock.bot.definition.DialogFlowStateTransitionType
import java.time.LocalDateTime
import java.util.Locale
import kotlin.Any
import kotlin.Boolean
import kotlin.Int
import kotlin.Long
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

internal class DialogFlowStateTransitionStatDateAggregationCol_Deserializer : ValueDeserializer<DialogFlowStateTransitionStatDateAggregationCol>(),
    JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addDeserializer(DialogFlowStateTransitionStatDateAggregationCol::class.java, this)

  override fun deserialize(p: JsonParser, ctxt: DeserializationContext): DialogFlowStateTransitionStatDateAggregationCol {
    with(p) {
      var _applicationId_: Id<BotApplicationConfiguration>? = null
      var _applicationId_set: Boolean = false
      var _date_: LocalDateTime? = null
      var _date_set: Boolean = false
      var _hourOfDay_: Int? = null
      var _hourOfDay_set: Boolean = false
      var _intent_: String? = null
      var _intent_set: Boolean = false
      var _storyDefinitionId_: String? = null
      var _storyDefinitionId_set: Boolean = false
      var _storyCategory_: String? = null
      var _storyCategory_set: Boolean = false
      var _storyType_: String? = null
      var _storyType_set: Boolean = false
      var _locale_: Locale? = null
      var _locale_set: Boolean = false
      var _configurationName_: String? = null
      var _configurationName_set: Boolean = false
      var _connectorType_: ConnectorType? = null
      var _connectorType_set: Boolean = false
      var _actionType_: DialogFlowStateTransitionType? = null
      var _actionType_set: Boolean = false
      var _count_: Long? = null
      var _count_set: Boolean = false
      var token_: JsonToken? = currentToken()
      if (token_ == JsonToken.START_OBJECT) token_ = nextToken()
      while (token_ == JsonToken.PROPERTY_NAME) {
        val fieldName_ = currentName()
        token_ = nextToken()
        when (fieldName_) {
          "applicationId" -> {
            _applicationId_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(_applicationId__reference)
            _applicationId_set = true
          }
          "date" -> {
            _date_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(LocalDateTime::class.javaObjectType)
            _date_set = true
          }
          "hourOfDay" -> {
            _hourOfDay_ = if (token_ == JsonToken.VALUE_NULL) null else p.intValue
            _hourOfDay_set = true
          }
          "intent" -> {
            _intent_ = if (token_ == JsonToken.VALUE_NULL) null else p.string
            _intent_set = true
          }
          "storyDefinitionId" -> {
            _storyDefinitionId_ = if (token_ == JsonToken.VALUE_NULL) null else p.string
            _storyDefinitionId_set = true
          }
          "storyCategory" -> {
            _storyCategory_ = if (token_ == JsonToken.VALUE_NULL) null else p.string
            _storyCategory_set = true
          }
          "storyType" -> {
            _storyType_ = if (token_ == JsonToken.VALUE_NULL) null else p.string
            _storyType_set = true
          }
          "locale" -> {
            _locale_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(Locale::class.javaObjectType)
            _locale_set = true
          }
          "configurationName" -> {
            _configurationName_ = if (token_ == JsonToken.VALUE_NULL) null else p.string
            _configurationName_set = true
          }
          "connectorType" -> {
            _connectorType_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(ConnectorType::class.javaObjectType)
            _connectorType_set = true
          }
          "actionType" -> {
            _actionType_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(DialogFlowStateTransitionType::class.javaObjectType)
            _actionType_set = true
          }
          "count" -> {
            _count_ = if (token_ == JsonToken.VALUE_NULL) null else p.longValue
            _count_set = true
          }
          else -> {
            if (token_?.isStructStart == true) p.skipChildren()
          }
        }
        token_ = nextToken()
      }
      val result_ = if (_applicationId_set && _date_set && _hourOfDay_set && _intent_set && _storyDefinitionId_set && _storyCategory_set && _storyType_set && _locale_set && _configurationName_set && _connectorType_set && _actionType_set && _count_set) {
        DialogFlowStateTransitionStatDateAggregationCol(applicationId = _applicationId_!!, date = _date_!!, hourOfDay = _hourOfDay_!!, intent = _intent_, storyDefinitionId = _storyDefinitionId_!!, storyCategory = _storyCategory_!!, storyType = _storyType_!!, locale = _locale_!!, configurationName = _configurationName_!!, connectorType = _connectorType_!!, actionType = _actionType_!!, count = _count_!!)
      } else {
        val map_ = mutableMapOf<KParameter, Any?>()
        if (_applicationId_set) map_[parameters.getValue("applicationId")] = _applicationId_
        if (_date_set) map_[parameters.getValue("date")] = _date_
        if (_hourOfDay_set) map_[parameters.getValue("hourOfDay")] = _hourOfDay_
        if (_intent_set) map_[parameters.getValue("intent")] = _intent_
        if (_storyDefinitionId_set) map_[parameters.getValue("storyDefinitionId")] = _storyDefinitionId_
        if (_storyCategory_set) map_[parameters.getValue("storyCategory")] = _storyCategory_
        if (_storyType_set) map_[parameters.getValue("storyType")] = _storyType_
        if (_locale_set) map_[parameters.getValue("locale")] = _locale_
        if (_configurationName_set) map_[parameters.getValue("configurationName")] = _configurationName_
        if (_connectorType_set) map_[parameters.getValue("connectorType")] = _connectorType_
        if (_actionType_set) map_[parameters.getValue("actionType")] = _actionType_
        if (_count_set) map_[parameters.getValue("count")] = _count_
        primaryConstructor.callBy(map_)
      }
      return result_
    }
  }

  private companion object {
    private val primaryConstructor: KFunction<DialogFlowStateTransitionStatDateAggregationCol> by
        lazy(LazyThreadSafetyMode.PUBLICATION) { DialogFlowStateTransitionStatDateAggregationCol::class.primaryConstructor!! }

    private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
          mapOf(
            "applicationId" to primaryConstructor.findParameterByName("applicationId")!!,
            "date" to primaryConstructor.findParameterByName("date")!!,
            "hourOfDay" to primaryConstructor.findParameterByName("hourOfDay")!!,
            "intent" to primaryConstructor.findParameterByName("intent")!!,
            "storyDefinitionId" to primaryConstructor.findParameterByName("storyDefinitionId")!!,
            "storyCategory" to primaryConstructor.findParameterByName("storyCategory")!!,
            "storyType" to primaryConstructor.findParameterByName("storyType")!!,
            "locale" to primaryConstructor.findParameterByName("locale")!!,
            "configurationName" to primaryConstructor.findParameterByName("configurationName")!!,
            "connectorType" to primaryConstructor.findParameterByName("connectorType")!!,
            "actionType" to primaryConstructor.findParameterByName("actionType")!!,
            "count" to primaryConstructor.findParameterByName("count")!!,
          )
        }

    private val _applicationId__reference: TypeReference<Id<BotApplicationConfiguration>> =
        object : TypeReference<Id<BotApplicationConfiguration>>() {}
  }
}
