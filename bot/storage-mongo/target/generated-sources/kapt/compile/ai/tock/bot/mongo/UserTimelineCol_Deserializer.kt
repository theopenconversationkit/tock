package ai.tock.bot.mongo

import ai.tock.bot.engine.user.PlayerId
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import java.time.Instant
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

internal class UserTimelineCol_Deserializer : JsonDeserializer<UserTimelineCol>(),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addDeserializer(UserTimelineCol::class.java, this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): UserTimelineCol {
        with(p) {
            var __id_: Id<UserTimelineCol>? = null
            var __id_set : Boolean = false
            var _playerId_: PlayerId? = null
            var _playerId_set : Boolean = false
            var _userPreferences_: UserTimelineCol.UserPreferencesWrapper? = null
            var _userPreferences_set : Boolean = false
            var _userState_: UserTimelineCol.UserStateWrapper? = null
            var _userState_set : Boolean = false
            var _temporaryIds_: MutableSet<String>? = null
            var _temporaryIds_set : Boolean = false
            var _applicationIds_: MutableSet<String>? = null
            var _applicationIds_set : Boolean = false
            var _lastActionText_: String? = null
            var _lastActionText_set : Boolean = false
            var _lastUpdateDate_: Instant? = null
            var _lastUpdateDate_set : Boolean = false
            var _lastUserActionDate_: Instant? = null
            var _lastUserActionDate_set : Boolean = false
            var _namespace_: String? = null
            var _namespace_set : Boolean = false
            var _creationDate_: Instant? = null
            var _creationDate_set : Boolean = false
            var _token_ : JsonToken? = currentToken
            while (_token_?.isStructEnd != true) { 
                if(_token_ != JsonToken.FIELD_NAME) {
                        _token_ = nextToken()
                        if (_token_?.isStructEnd == true) break
                        }

                val _fieldName_ = currentName
                _token_ = nextToken()
                when (_fieldName_) { 
                    "_id" -> {
                            __id_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(__id__reference);
                            __id_set = true
                            }
                    "playerId" -> {
                            _playerId_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(PlayerId::class.java);
                            _playerId_set = true
                            }
                    "userPreferences" -> {
                            _userPreferences_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(UserTimelineCol.UserPreferencesWrapper::class.java);
                            _userPreferences_set = true
                            }
                    "userState" -> {
                            _userState_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(UserTimelineCol.UserStateWrapper::class.java);
                            _userState_set = true
                            }
                    "temporaryIds" -> {
                            _temporaryIds_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(_temporaryIds__reference);
                            _temporaryIds_set = true
                            }
                    "applicationIds" -> {
                            _applicationIds_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(_applicationIds__reference);
                            _applicationIds_set = true
                            }
                    "lastActionText" -> {
                            _lastActionText_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.text;
                            _lastActionText_set = true
                            }
                    "lastUpdateDate" -> {
                            _lastUpdateDate_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(Instant::class.java);
                            _lastUpdateDate_set = true
                            }
                    "lastUserActionDate" -> {
                            _lastUserActionDate_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(Instant::class.java);
                            _lastUserActionDate_set = true
                            }
                    "namespace" -> {
                            _namespace_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.text;
                            _namespace_set = true
                            }
                    "creationDate" -> {
                            _creationDate_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(Instant::class.java);
                            _creationDate_set = true
                            }
                    else -> {
                            if (_token_?.isStructStart == true)
                            p.skipChildren()
                            nextToken()
                            }
                    } 
                _token_ = currentToken
                        } 
            return if(__id_set && _playerId_set && _userPreferences_set && _userState_set &&
                    _temporaryIds_set && _applicationIds_set && _lastActionText_set &&
                    _lastUpdateDate_set && _lastUserActionDate_set && _namespace_set &&
                    _creationDate_set)
                    UserTimelineCol(_id = __id_!!, playerId = _playerId_!!, userPreferences =
                            _userPreferences_!!, userState = _userState_!!, temporaryIds =
                            _temporaryIds_!!, applicationIds = _applicationIds_!!, lastActionText =
                            _lastActionText_, lastUpdateDate = _lastUpdateDate_!!,
                            lastUserActionDate = _lastUserActionDate_!!, namespace = _namespace_,
                            creationDate = _creationDate_!!)
                    else {
                    val map = mutableMapOf<KParameter, Any?>()
                    if(__id_set)
                    map[parameters.getValue("_id")] = __id_
                    if(_playerId_set)
                    map[parameters.getValue("playerId")] = _playerId_
                    if(_userPreferences_set)
                    map[parameters.getValue("userPreferences")] = _userPreferences_
                    if(_userState_set)
                    map[parameters.getValue("userState")] = _userState_
                    if(_temporaryIds_set)
                    map[parameters.getValue("temporaryIds")] = _temporaryIds_
                    if(_applicationIds_set)
                    map[parameters.getValue("applicationIds")] = _applicationIds_
                    if(_lastActionText_set)
                    map[parameters.getValue("lastActionText")] = _lastActionText_
                    if(_lastUpdateDate_set)
                    map[parameters.getValue("lastUpdateDate")] = _lastUpdateDate_
                    if(_lastUserActionDate_set)
                    map[parameters.getValue("lastUserActionDate")] = _lastUserActionDate_
                    if(_namespace_set)
                    map[parameters.getValue("namespace")] = _namespace_
                    if(_creationDate_set)
                    map[parameters.getValue("creationDate")] = _creationDate_ 
                    primaryConstructor.callBy(map) 
                    }
        } 
    }

    companion object {
        private val primaryConstructor: KFunction<UserTimelineCol> by
                lazy(LazyThreadSafetyMode.PUBLICATION) { UserTimelineCol::class.primaryConstructor!!
                }

        private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
                kotlin.collections.mapOf("_id" to primaryConstructor.findParameterByName("_id")!!,
                "playerId" to primaryConstructor.findParameterByName("playerId")!!,
                "userPreferences" to primaryConstructor.findParameterByName("userPreferences")!!,
                "userState" to primaryConstructor.findParameterByName("userState")!!, "temporaryIds"
                to primaryConstructor.findParameterByName("temporaryIds")!!, "applicationIds" to
                primaryConstructor.findParameterByName("applicationIds")!!, "lastActionText" to
                primaryConstructor.findParameterByName("lastActionText")!!, "lastUpdateDate" to
                primaryConstructor.findParameterByName("lastUpdateDate")!!, "lastUserActionDate" to
                primaryConstructor.findParameterByName("lastUserActionDate")!!, "namespace" to
                primaryConstructor.findParameterByName("namespace")!!, "creationDate" to
                primaryConstructor.findParameterByName("creationDate")!!) }

        private val __id__reference: TypeReference<Id<UserTimelineCol>> = object :
                TypeReference<Id<UserTimelineCol>>() {}

        private val _temporaryIds__reference: TypeReference<Set<String>> = object :
                TypeReference<Set<String>>() {}

        private val _applicationIds__reference: TypeReference<Set<String>> = object :
                TypeReference<Set<String>>() {}
    }
}
