package ai.tock.bot.mongo

import ai.tock.bot.admin.answer.AnswerConfigurationType
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

internal class DialogFlowStateCol_Deserializer : JsonDeserializer<DialogFlowStateCol>(),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addDeserializer(DialogFlowStateCol::class.java, this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): DialogFlowStateCol {
        with(p) {
            var _namespace_: String? = null
            var _namespace_set : Boolean = false
            var _botId_: String? = null
            var _botId_set : Boolean = false
            var _storyDefinitionId_: String? = null
            var _storyDefinitionId_set : Boolean = false
            var _intent_: String? = null
            var _intent_set : Boolean = false
            var _step_: String? = null
            var _step_set : Boolean = false
            var _entities_: MutableSet<String>? = null
            var _entities_set : Boolean = false
            var __id_: Id<DialogFlowStateCol>? = null
            var __id_set : Boolean = false
            var _storyType_: AnswerConfigurationType? = null
            var _storyType_set : Boolean = false
            var _storyName_: String? = null
            var _storyName_set : Boolean = false
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
                    "storyDefinitionId" -> {
                            _storyDefinitionId_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.text;
                            _storyDefinitionId_set = true
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
                    "entities" -> {
                            _entities_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(_entities__reference);
                            _entities_set = true
                            }
                    "_id" -> {
                            __id_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(__id__reference);
                            __id_set = true
                            }
                    "storyType" -> {
                            _storyType_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(AnswerConfigurationType::class.java);
                            _storyType_set = true
                            }
                    "storyName" -> {
                            _storyName_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.text;
                            _storyName_set = true
                            }
                    else -> {
                            if (_token_?.isStructStart == true)
                            p.skipChildren()
                            nextToken()
                            }
                    } 
                _token_ = currentToken
                        } 
            return if(_namespace_set && _botId_set && _storyDefinitionId_set && _intent_set &&
                    _step_set && _entities_set && __id_set && _storyType_set && _storyName_set)
                    DialogFlowStateCol(namespace = _namespace_!!, botId = _botId_!!,
                            storyDefinitionId = _storyDefinitionId_!!, intent = _intent_!!, step =
                            _step_, entities = _entities_!!, _id = __id_!!, storyType = _storyType_,
                            storyName = _storyName_!!)
                    else {
                    val map = mutableMapOf<KParameter, Any?>()
                    if(_namespace_set)
                    map[parameters.getValue("namespace")] = _namespace_
                    if(_botId_set)
                    map[parameters.getValue("botId")] = _botId_
                    if(_storyDefinitionId_set)
                    map[parameters.getValue("storyDefinitionId")] = _storyDefinitionId_
                    if(_intent_set)
                    map[parameters.getValue("intent")] = _intent_
                    if(_step_set)
                    map[parameters.getValue("step")] = _step_
                    if(_entities_set)
                    map[parameters.getValue("entities")] = _entities_
                    if(__id_set)
                    map[parameters.getValue("_id")] = __id_
                    if(_storyType_set)
                    map[parameters.getValue("storyType")] = _storyType_
                    if(_storyName_set)
                    map[parameters.getValue("storyName")] = _storyName_ 
                    primaryConstructor.callBy(map) 
                    }
        } 
    }

    companion object {
        private val primaryConstructor: KFunction<DialogFlowStateCol> by
                lazy(LazyThreadSafetyMode.PUBLICATION) {
                DialogFlowStateCol::class.primaryConstructor!! }

        private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
                kotlin.collections.mapOf("namespace" to
                primaryConstructor.findParameterByName("namespace")!!, "botId" to
                primaryConstructor.findParameterByName("botId")!!, "storyDefinitionId" to
                primaryConstructor.findParameterByName("storyDefinitionId")!!, "intent" to
                primaryConstructor.findParameterByName("intent")!!, "step" to
                primaryConstructor.findParameterByName("step")!!, "entities" to
                primaryConstructor.findParameterByName("entities")!!, "_id" to
                primaryConstructor.findParameterByName("_id")!!, "storyType" to
                primaryConstructor.findParameterByName("storyType")!!, "storyName" to
                primaryConstructor.findParameterByName("storyName")!!) }

        private val _entities__reference: TypeReference<Set<String>> = object :
                TypeReference<Set<String>>() {}

        private val __id__reference: TypeReference<Id<DialogFlowStateCol>> = object :
                TypeReference<Id<DialogFlowStateCol>>() {}
    }
}
