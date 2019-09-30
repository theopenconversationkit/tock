package ai.tock.bot.mongo

import ai.tock.bot.definition.DialogFlowStateTransitionType
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import kotlin.String
import kotlin.collections.Map
import kotlin.collections.MutableSet
import kotlin.collections.Set
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.findParameterByName
import kotlin.reflect.full.primaryConstructor
import org.litote.jackson.JacksonModuleServiceLoader
import org.litote.kmongo.Id

internal class DialogFlowStateTransitionCol_Deserializer :
        JsonDeserializer<DialogFlowStateTransitionCol>(), JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addDeserializer(DialogFlowStateTransitionCol::class.java,
            this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext):
            DialogFlowStateTransitionCol {
        with(p) {
            var _namespace_: String? = null
            var _namespace_set : Boolean = false
            var _botId_: String? = null
            var _botId_set : Boolean = false
            var _previousStateId_: Id<DialogFlowStateCol>? = null
            var _previousStateId_set : Boolean = false
            var _nextStateId_: Id<DialogFlowStateCol>? = null
            var _nextStateId_set : Boolean = false
            var _intent_: String? = null
            var _intent_set : Boolean = false
            var _step_: String? = null
            var _step_set : Boolean = false
            var _newEntities_: MutableSet<String>? = null
            var _newEntities_set : Boolean = false
            var _type_: DialogFlowStateTransitionType? = null
            var _type_set : Boolean = false
            var __id_: Id<DialogFlowStateTransitionCol>? = null
            var __id_set : Boolean = false
            var _token_ : JsonToken? = currentToken
            while (_token_?.isStructEnd != true) { 
                if(_token_ != JsonToken.FIELD_NAME) {
                        _token_ = nextToken()
                        if (_token_?.isStructEnd == true) break
                        }

                val _fieldName_ = currentName
                _token_ = nextToken()
                when (_fieldName_) { 
                    "namespace" -> {
                            _namespace_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.text;
                            _namespace_set = true
                            }
                    "botId" -> {
                            _botId_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.text;
                            _botId_set = true
                            }
                    "previousStateId" -> {
                            _previousStateId_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(_previousStateId__reference);
                            _previousStateId_set = true
                            }
                    "nextStateId" -> {
                            _nextStateId_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(_nextStateId__reference);
                            _nextStateId_set = true
                            }
                    "intent" -> {
                            _intent_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.text;
                            _intent_set = true
                            }
                    "step" -> {
                            _step_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.text;
                            _step_set = true
                            }
                    "newEntities" -> {
                            _newEntities_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(_newEntities__reference);
                            _newEntities_set = true
                            }
                    "type" -> {
                            _type_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(DialogFlowStateTransitionType::class.java);
                            _type_set = true
                            }
                    "_id" -> {
                            __id_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(__id__reference);
                            __id_set = true
                            }
                    else -> {
                            if (_token_?.isStructStart == true)
                            p.skipChildren()
                            nextToken()
                            }
                    } 
                _token_ = currentToken
                        } 
            return if(_namespace_set && _botId_set && _previousStateId_set && _nextStateId_set &&
                    _intent_set && _step_set && _newEntities_set && _type_set && __id_set)
                    DialogFlowStateTransitionCol(namespace = _namespace_!!, botId = _botId_!!,
                            previousStateId = _previousStateId_, nextStateId = _nextStateId_!!,
                            intent = _intent_, step = _step_, newEntities = _newEntities_!!, type =
                            _type_!!, _id = __id_!!)
                    else {
                    val map = mutableMapOf<KParameter, Any?>()
                    if(_namespace_set)
                    map[parameters.getValue("namespace")] = _namespace_
                    if(_botId_set)
                    map[parameters.getValue("botId")] = _botId_
                    if(_previousStateId_set)
                    map[parameters.getValue("previousStateId")] = _previousStateId_
                    if(_nextStateId_set)
                    map[parameters.getValue("nextStateId")] = _nextStateId_
                    if(_intent_set)
                    map[parameters.getValue("intent")] = _intent_
                    if(_step_set)
                    map[parameters.getValue("step")] = _step_
                    if(_newEntities_set)
                    map[parameters.getValue("newEntities")] = _newEntities_
                    if(_type_set)
                    map[parameters.getValue("type")] = _type_
                    if(__id_set)
                    map[parameters.getValue("_id")] = __id_ 
                    primaryConstructor.callBy(map) 
                    }
        } 
    }

    companion object {
        private val primaryConstructor: KFunction<DialogFlowStateTransitionCol> by
                lazy(LazyThreadSafetyMode.PUBLICATION) {
                DialogFlowStateTransitionCol::class.primaryConstructor!! }

        private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
                kotlin.collections.mapOf("namespace" to
                primaryConstructor.findParameterByName("namespace")!!, "botId" to
                primaryConstructor.findParameterByName("botId")!!, "previousStateId" to
                primaryConstructor.findParameterByName("previousStateId")!!, "nextStateId" to
                primaryConstructor.findParameterByName("nextStateId")!!, "intent" to
                primaryConstructor.findParameterByName("intent")!!, "step" to
                primaryConstructor.findParameterByName("step")!!, "newEntities" to
                primaryConstructor.findParameterByName("newEntities")!!, "type" to
                primaryConstructor.findParameterByName("type")!!, "_id" to
                primaryConstructor.findParameterByName("_id")!!) }

        private val _previousStateId__reference: TypeReference<Id<DialogFlowStateCol>> = object :
                TypeReference<Id<DialogFlowStateCol>>() {}

        private val _nextStateId__reference: TypeReference<Id<DialogFlowStateCol>> = object :
                TypeReference<Id<DialogFlowStateCol>>() {}

        private val _newEntities__reference: TypeReference<Set<String>> = object :
                TypeReference<Set<String>>() {}

        private val __id__reference: TypeReference<Id<DialogFlowStateTransitionCol>> = object :
                TypeReference<Id<DialogFlowStateTransitionCol>>() {}
    }
}
