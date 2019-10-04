package ai.tock.bot.mongo

import ai.tock.bot.engine.dialog.Dialog
import ai.tock.bot.engine.user.PlayerId
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import java.time.Instant
import kotlin.Boolean
import kotlin.String
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.collections.MutableSet
import kotlin.collections.Set
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.findParameterByName
import kotlin.reflect.full.primaryConstructor
import org.litote.jackson.JacksonModuleServiceLoader
import org.litote.kmongo.Id

internal class DialogCol_Deserializer : JsonDeserializer<DialogCol>(), JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addDeserializer(DialogCol::class.java, this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): DialogCol {
        with(p) {
            var _playerIds_: MutableSet<PlayerId>? = null
            var _playerIds_set : Boolean = false
            var __id_: Id<Dialog>? = null
            var __id_set : Boolean = false
            var _state_: DialogCol.DialogStateMongoWrapper? = null
            var _state_set : Boolean = false
            var _stories_: MutableList<DialogCol.StoryMongoWrapper>? = null
            var _stories_set : Boolean = false
            var _applicationIds_: MutableSet<String>? = null
            var _applicationIds_set : Boolean = false
            var _lastUpdateDate_: Instant? = null
            var _lastUpdateDate_set : Boolean = false
            var _groupId_: String? = null
            var _groupId_set : Boolean = false
            var _test_: Boolean? = null
            var _test_set : Boolean = false
            var _namespace_: String? = null
            var _namespace_set : Boolean = false
            var _token_ : JsonToken? = currentToken
            while (_token_?.isStructEnd != true) { 
                if(_token_ != JsonToken.FIELD_NAME) {
                        _token_ = nextToken()
                        if (_token_?.isStructEnd == true) break
                        }

                val _fieldName_ = currentName
                _token_ = nextToken()
                when (_fieldName_) { 
                    "playerIds" -> {
                            _playerIds_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(_playerIds__reference);
                            _playerIds_set = true
                            }
                    "_id" -> {
                            __id_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(__id__reference);
                            __id_set = true
                            }
                    "state" -> {
                            _state_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(DialogCol.DialogStateMongoWrapper::class.java);
                            _state_set = true
                            }
                    "stories" -> {
                            _stories_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(_stories__reference);
                            _stories_set = true
                            }
                    "applicationIds" -> {
                            _applicationIds_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(_applicationIds__reference);
                            _applicationIds_set = true
                            }
                    "lastUpdateDate" -> {
                            _lastUpdateDate_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(Instant::class.java);
                            _lastUpdateDate_set = true
                            }
                    "groupId" -> {
                            _groupId_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.text;
                            _groupId_set = true
                            }
                    "test" -> {
                            _test_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.booleanValue;
                            _test_set = true
                            }
                    "namespace" -> {
                            _namespace_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.text;
                            _namespace_set = true
                            }
                    else -> {
                            if (_token_?.isStructStart == true)
                            p.skipChildren()
                            nextToken()
                            }
                    } 
                _token_ = currentToken
                        } 
            return if(_playerIds_set && __id_set && _state_set && _stories_set &&
                    _applicationIds_set && _lastUpdateDate_set && _groupId_set && _test_set &&
                    _namespace_set)
                    DialogCol(playerIds = _playerIds_!!, _id = __id_!!, state = _state_!!, stories =
                            _stories_!!, applicationIds = _applicationIds_!!, lastUpdateDate =
                            _lastUpdateDate_!!, groupId = _groupId_, test = _test_!!, namespace =
                            _namespace_)
                    else {
                    val map = mutableMapOf<KParameter, Any?>()
                    if(_playerIds_set)
                    map[parameters.getValue("playerIds")] = _playerIds_
                    if(__id_set)
                    map[parameters.getValue("_id")] = __id_
                    if(_state_set)
                    map[parameters.getValue("state")] = _state_
                    if(_stories_set)
                    map[parameters.getValue("stories")] = _stories_
                    if(_applicationIds_set)
                    map[parameters.getValue("applicationIds")] = _applicationIds_
                    if(_lastUpdateDate_set)
                    map[parameters.getValue("lastUpdateDate")] = _lastUpdateDate_
                    if(_groupId_set)
                    map[parameters.getValue("groupId")] = _groupId_
                    if(_test_set)
                    map[parameters.getValue("test")] = _test_
                    if(_namespace_set)
                    map[parameters.getValue("namespace")] = _namespace_ 
                    primaryConstructor.callBy(map) 
                    }
        } 
    }

    companion object {
        private val primaryConstructor: KFunction<DialogCol> by
                lazy(LazyThreadSafetyMode.PUBLICATION) { DialogCol::class.primaryConstructor!! }

        private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
                kotlin.collections.mapOf("playerIds" to
                primaryConstructor.findParameterByName("playerIds")!!, "_id" to
                primaryConstructor.findParameterByName("_id")!!, "state" to
                primaryConstructor.findParameterByName("state")!!, "stories" to
                primaryConstructor.findParameterByName("stories")!!, "applicationIds" to
                primaryConstructor.findParameterByName("applicationIds")!!, "lastUpdateDate" to
                primaryConstructor.findParameterByName("lastUpdateDate")!!, "groupId" to
                primaryConstructor.findParameterByName("groupId")!!, "test" to
                primaryConstructor.findParameterByName("test")!!, "namespace" to
                primaryConstructor.findParameterByName("namespace")!!) }

        private val _playerIds__reference: TypeReference<Set<PlayerId>> = object :
                TypeReference<Set<PlayerId>>() {}

        private val __id__reference: TypeReference<Id<Dialog>> = object :
                TypeReference<Id<Dialog>>() {}

        private val _stories__reference: TypeReference<List<DialogCol.StoryMongoWrapper>> = object :
                TypeReference<List<DialogCol.StoryMongoWrapper>>() {}

        private val _applicationIds__reference: TypeReference<Set<String>> = object :
                TypeReference<Set<String>>() {}
    }
}
