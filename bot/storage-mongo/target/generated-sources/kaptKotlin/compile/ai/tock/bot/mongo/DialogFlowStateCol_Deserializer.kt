package ai.tock.bot.mongo

import ai.tock.bot.admin.answer.AnswerConfigurationType
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

internal class DialogFlowStateCol_Deserializer : ValueDeserializer<DialogFlowStateCol>(),
    JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addDeserializer(DialogFlowStateCol::class.java, this)

  override fun deserialize(p: JsonParser, ctxt: DeserializationContext): DialogFlowStateCol {
    with(p) {
      var _namespace_: String? = null
      var _namespace_set: Boolean = false
      var _botId_: String? = null
      var _botId_set: Boolean = false
      var _storyDefinitionId_: String? = null
      var _storyDefinitionId_set: Boolean = false
      var _intent_: String? = null
      var _intent_set: Boolean = false
      var _step_: String? = null
      var _step_set: Boolean = false
      var _entities_: Set<String>? = null
      var _entities_set: Boolean = false
      var __id_: Id<DialogFlowStateCol>? = null
      var __id_set: Boolean = false
      var _storyType_: AnswerConfigurationType? = null
      var _storyType_set: Boolean = false
      var _storyName_: String? = null
      var _storyName_set: Boolean = false
      var token_: JsonToken? = currentToken()
      if (token_ == JsonToken.START_OBJECT) token_ = nextToken()
      while (token_ == JsonToken.PROPERTY_NAME) {
        val fieldName_ = currentName()
        token_ = nextToken()
        when (fieldName_) {
          "namespace" -> {
            _namespace_ = if (token_ == JsonToken.VALUE_NULL) null else p.string
            _namespace_set = true
          }
          "botId" -> {
            _botId_ = if (token_ == JsonToken.VALUE_NULL) null else p.string
            _botId_set = true
          }
          "storyDefinitionId" -> {
            _storyDefinitionId_ = if (token_ == JsonToken.VALUE_NULL) null else p.string
            _storyDefinitionId_set = true
          }
          "intent" -> {
            _intent_ = if (token_ == JsonToken.VALUE_NULL) null else p.string
            _intent_set = true
          }
          "step" -> {
            _step_ = if (token_ == JsonToken.VALUE_NULL) null else p.string
            _step_set = true
          }
          "entities" -> {
            _entities_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(_entities__reference)
            _entities_set = true
          }
          "_id" -> {
            __id_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(__id__reference)
            __id_set = true
          }
          "storyType" -> {
            _storyType_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(AnswerConfigurationType::class.javaObjectType)
            _storyType_set = true
          }
          "storyName" -> {
            _storyName_ = if (token_ == JsonToken.VALUE_NULL) null else p.string
            _storyName_set = true
          }
          else -> {
            if (token_?.isStructStart == true) p.skipChildren()
          }
        }
        token_ = nextToken()
      }
      val result_ = if (_namespace_set && _botId_set && _storyDefinitionId_set && _intent_set && _step_set && _entities_set && __id_set && _storyType_set && _storyName_set) {
        DialogFlowStateCol(namespace = _namespace_!!, botId = _botId_!!, storyDefinitionId = _storyDefinitionId_!!, intent = _intent_!!, step = _step_, entities = _entities_!!, _id = __id_!!, storyType = _storyType_, storyName = _storyName_!!)
      } else {
        val map_ = mutableMapOf<KParameter, Any?>()
        if (_namespace_set) map_[parameters.getValue("namespace")] = _namespace_
        if (_botId_set) map_[parameters.getValue("botId")] = _botId_
        if (_storyDefinitionId_set) map_[parameters.getValue("storyDefinitionId")] = _storyDefinitionId_
        if (_intent_set) map_[parameters.getValue("intent")] = _intent_
        if (_step_set) map_[parameters.getValue("step")] = _step_
        if (_entities_set) map_[parameters.getValue("entities")] = _entities_
        if (__id_set) map_[parameters.getValue("_id")] = __id_
        if (_storyType_set) map_[parameters.getValue("storyType")] = _storyType_
        if (_storyName_set) map_[parameters.getValue("storyName")] = _storyName_
        primaryConstructor.callBy(map_)
      }
      return result_
    }
  }

  private companion object {
    private val primaryConstructor: KFunction<DialogFlowStateCol> by
        lazy(LazyThreadSafetyMode.PUBLICATION) { DialogFlowStateCol::class.primaryConstructor!! }

    private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
          mapOf(
            "namespace" to primaryConstructor.findParameterByName("namespace")!!,
            "botId" to primaryConstructor.findParameterByName("botId")!!,
            "storyDefinitionId" to primaryConstructor.findParameterByName("storyDefinitionId")!!,
            "intent" to primaryConstructor.findParameterByName("intent")!!,
            "step" to primaryConstructor.findParameterByName("step")!!,
            "entities" to primaryConstructor.findParameterByName("entities")!!,
            "_id" to primaryConstructor.findParameterByName("_id")!!,
            "storyType" to primaryConstructor.findParameterByName("storyType")!!,
            "storyName" to primaryConstructor.findParameterByName("storyName")!!,
          )
        }

    private val _entities__reference: TypeReference<Set<String>> =
        object : TypeReference<Set<String>>() {}

    private val __id__reference: TypeReference<Id<DialogFlowStateCol>> =
        object : TypeReference<Id<DialogFlowStateCol>>() {}
  }
}
