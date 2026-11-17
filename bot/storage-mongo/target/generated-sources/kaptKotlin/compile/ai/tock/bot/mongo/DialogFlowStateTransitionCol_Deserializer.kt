package ai.tock.bot.mongo

import ai.tock.bot.definition.DialogFlowStateTransitionType
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

internal class DialogFlowStateTransitionCol_Deserializer : ValueDeserializer<DialogFlowStateTransitionCol>(),
    JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addDeserializer(DialogFlowStateTransitionCol::class.java, this)

  override fun deserialize(p: JsonParser, ctxt: DeserializationContext): DialogFlowStateTransitionCol {
    with(p) {
      var _namespace_: String? = null
      var _namespace_set: Boolean = false
      var _botId_: String? = null
      var _botId_set: Boolean = false
      var _previousStateId_: Id<DialogFlowStateCol>? = null
      var _previousStateId_set: Boolean = false
      var _nextStateId_: Id<DialogFlowStateCol>? = null
      var _nextStateId_set: Boolean = false
      var _intent_: String? = null
      var _intent_set: Boolean = false
      var _step_: String? = null
      var _step_set: Boolean = false
      var _newEntities_: Set<String>? = null
      var _newEntities_set: Boolean = false
      var _type_: DialogFlowStateTransitionType? = null
      var _type_set: Boolean = false
      var __id_: Id<DialogFlowStateTransitionCol>? = null
      var __id_set: Boolean = false
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
          "previousStateId" -> {
            _previousStateId_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(_previousStateId__reference)
            _previousStateId_set = true
          }
          "nextStateId" -> {
            _nextStateId_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(_nextStateId__reference)
            _nextStateId_set = true
          }
          "intent" -> {
            _intent_ = if (token_ == JsonToken.VALUE_NULL) null else p.string
            _intent_set = true
          }
          "step" -> {
            _step_ = if (token_ == JsonToken.VALUE_NULL) null else p.string
            _step_set = true
          }
          "newEntities" -> {
            _newEntities_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(_newEntities__reference)
            _newEntities_set = true
          }
          "type" -> {
            _type_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(DialogFlowStateTransitionType::class.javaObjectType)
            _type_set = true
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
      val result_ = if (_namespace_set && _botId_set && _previousStateId_set && _nextStateId_set && _intent_set && _step_set && _newEntities_set && _type_set && __id_set) {
        DialogFlowStateTransitionCol(namespace = _namespace_!!, botId = _botId_!!, previousStateId = _previousStateId_, nextStateId = _nextStateId_!!, intent = _intent_, step = _step_, newEntities = _newEntities_!!, type = _type_!!, _id = __id_!!)
      } else {
        val map_ = mutableMapOf<KParameter, Any?>()
        if (_namespace_set) map_[parameters.getValue("namespace")] = _namespace_
        if (_botId_set) map_[parameters.getValue("botId")] = _botId_
        if (_previousStateId_set) map_[parameters.getValue("previousStateId")] = _previousStateId_
        if (_nextStateId_set) map_[parameters.getValue("nextStateId")] = _nextStateId_
        if (_intent_set) map_[parameters.getValue("intent")] = _intent_
        if (_step_set) map_[parameters.getValue("step")] = _step_
        if (_newEntities_set) map_[parameters.getValue("newEntities")] = _newEntities_
        if (_type_set) map_[parameters.getValue("type")] = _type_
        if (__id_set) map_[parameters.getValue("_id")] = __id_
        primaryConstructor.callBy(map_)
      }
      return result_
    }
  }

  private companion object {
    private val primaryConstructor: KFunction<DialogFlowStateTransitionCol> by
        lazy(LazyThreadSafetyMode.PUBLICATION) { DialogFlowStateTransitionCol::class.primaryConstructor!! }

    private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
          mapOf(
            "namespace" to primaryConstructor.findParameterByName("namespace")!!,
            "botId" to primaryConstructor.findParameterByName("botId")!!,
            "previousStateId" to primaryConstructor.findParameterByName("previousStateId")!!,
            "nextStateId" to primaryConstructor.findParameterByName("nextStateId")!!,
            "intent" to primaryConstructor.findParameterByName("intent")!!,
            "step" to primaryConstructor.findParameterByName("step")!!,
            "newEntities" to primaryConstructor.findParameterByName("newEntities")!!,
            "type" to primaryConstructor.findParameterByName("type")!!,
            "_id" to primaryConstructor.findParameterByName("_id")!!,
          )
        }

    private val _previousStateId__reference: TypeReference<Id<DialogFlowStateCol>> =
        object : TypeReference<Id<DialogFlowStateCol>>() {}

    private val _nextStateId__reference: TypeReference<Id<DialogFlowStateCol>> =
        object : TypeReference<Id<DialogFlowStateCol>>() {}

    private val _newEntities__reference: TypeReference<Set<String>> =
        object : TypeReference<Set<String>>() {}

    private val __id__reference: TypeReference<Id<DialogFlowStateTransitionCol>> =
        object : TypeReference<Id<DialogFlowStateTransitionCol>>() {}
  }
}
