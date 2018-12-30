package fr.vsct.tock.bot.mongo

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import fr.vsct.tock.bot.engine.dialog.Dialog
import fr.vsct.tock.bot.engine.user.PlayerId
import java.time.Instant
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

internal class DialogCol_Deserializer : StdDeserializer<DialogCol>(DialogCol::class.java),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addDeserializer(DialogCol::class.java, this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): DialogCol {
        with(p) {
            var _playerIds_: MutableSet<PlayerId>? = null
            var _playerIds_set = false
            var __id_: Id<Dialog>? = null
            var __id_set = false
            var _state_: DialogCol.DialogStateMongoWrapper? = null
            var _state_set = false
            var _stories_: MutableList<DialogCol.StoryMongoWrapper>? = null
            var _stories_set = false
            var _applicationIds_: MutableSet<String>? = null
            var _applicationIds_set = false
            var _lastUpdateDate_: Instant? = null
            var _lastUpdateDate_set = false
            var _groupId_: String? = null
            var _groupId_set = false
            while (currentToken != JsonToken.END_OBJECT && currentToken != JsonToken.END_ARRAY) { 
                if(currentToken != JsonToken.FIELD_NAME) { nextToken() }
                if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.END_ARRAY) {
                        break } 
                val fieldName = currentName
                nextToken()
                when (fieldName) { 
                    "playerIds" -> {
                            _playerIds_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.readValueAs(_playerIds__reference);
                            _playerIds_set = true
                            }
                    "_id" -> {
                            __id_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.readValueAs(__id__reference);
                            __id_set = true
                            }
                    "state" -> {
                            _state_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.readValueAs(DialogCol.DialogStateMongoWrapper::class.java);
                            _state_set = true
                            }
                    "stories" -> {
                            _stories_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.readValueAs(_stories__reference);
                            _stories_set = true
                            }
                    "applicationIds" -> {
                            _applicationIds_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.readValueAs(_applicationIds__reference);
                            _applicationIds_set = true
                            }
                    "lastUpdateDate" -> {
                            _lastUpdateDate_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.readValueAs(Instant::class.java);
                            _lastUpdateDate_set = true
                            }
                    "groupId" -> {
                            _groupId_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.text;
                            _groupId_set = true
                            }
                    else -> {
                            if (currentToken == JsonToken.START_OBJECT || currentToken ==
                                    JsonToken.START_ARRAY)
                            p.skipChildren()
                            nextToken()
                            }
                    } 
                } 
            return if(_playerIds_set && __id_set && _state_set && _stories_set &&
                    _applicationIds_set && _lastUpdateDate_set && _groupId_set)
                    DialogCol(playerIds = _playerIds_!!, _id = __id_!!, state = _state_!!, stories =
                            _stories_!!, applicationIds = _applicationIds_!!, lastUpdateDate =
                            _lastUpdateDate_!!, groupId = _groupId_)
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
                primaryConstructor.findParameterByName("groupId")!!) }

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
