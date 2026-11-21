package ai.tock.bot.mongo

import ai.tock.bot.engine.user.PlayerId
import java.time.Instant
import kotlin.Any
import kotlin.Boolean
import kotlin.String
import kotlin.collections.Map
import kotlin.collections.MutableSet
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

internal class UserTimelineCol_Deserializer : ValueDeserializer<UserTimelineCol>(),
    JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addDeserializer(UserTimelineCol::class.java, this)

  override fun deserialize(p: JsonParser, ctxt: DeserializationContext): UserTimelineCol {
    with(p) {
      var __id_: Id<UserTimelineCol>? = null
      var __id_set: Boolean = false
      var _playerId_: PlayerId? = null
      var _playerId_set: Boolean = false
      var _userPreferences_: UserTimelineCol.UserPreferencesWrapper? = null
      var _userPreferences_set: Boolean = false
      var _userState_: UserTimelineCol.UserStateWrapper? = null
      var _userState_set: Boolean = false
      var _temporaryIds_: MutableSet<String>? = null
      var _temporaryIds_set: Boolean = false
      var _applicationIds_: MutableSet<String>? = null
      var _applicationIds_set: Boolean = false
      var _lastActionText_: String? = null
      var _lastActionText_set: Boolean = false
      var _lastUpdateDate_: Instant? = null
      var _lastUpdateDate_set: Boolean = false
      var _lastUserActionDate_: Instant? = null
      var _lastUserActionDate_set: Boolean = false
      var _namespace_: String? = null
      var _namespace_set: Boolean = false
      var _creationDate_: Instant? = null
      var _creationDate_set: Boolean = false
      var token_: JsonToken? = currentToken()
      if (token_ == JsonToken.START_OBJECT) token_ = nextToken()
      while (token_ == JsonToken.PROPERTY_NAME) {
        val fieldName_ = currentName()
        token_ = nextToken()
        when (fieldName_) {
          "_id" -> {
            __id_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(__id__reference)
            __id_set = true
          }
          "playerId" -> {
            _playerId_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(PlayerId::class.javaObjectType)
            _playerId_set = true
          }
          "userPreferences" -> {
            _userPreferences_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(UserTimelineCol.UserPreferencesWrapper::class.javaObjectType)
            _userPreferences_set = true
          }
          "userState" -> {
            _userState_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(UserTimelineCol.UserStateWrapper::class.javaObjectType)
            _userState_set = true
          }
          "temporaryIds" -> {
            _temporaryIds_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(_temporaryIds__reference)
            _temporaryIds_set = true
          }
          "applicationIds" -> {
            _applicationIds_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(_applicationIds__reference)
            _applicationIds_set = true
          }
          "lastActionText" -> {
            _lastActionText_ = if (token_ == JsonToken.VALUE_NULL) null else p.string
            _lastActionText_set = true
          }
          "lastUpdateDate" -> {
            _lastUpdateDate_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(Instant::class.javaObjectType)
            _lastUpdateDate_set = true
          }
          "lastUserActionDate" -> {
            _lastUserActionDate_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(Instant::class.javaObjectType)
            _lastUserActionDate_set = true
          }
          "namespace" -> {
            _namespace_ = if (token_ == JsonToken.VALUE_NULL) null else p.string
            _namespace_set = true
          }
          "creationDate" -> {
            _creationDate_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(Instant::class.javaObjectType)
            _creationDate_set = true
          }
          else -> {
            if (token_?.isStructStart == true) p.skipChildren()
          }
        }
        token_ = nextToken()
      }
      val result_ = if (__id_set && _playerId_set && _userPreferences_set && _userState_set && _temporaryIds_set && _applicationIds_set && _lastActionText_set && _lastUpdateDate_set && _lastUserActionDate_set && _namespace_set && _creationDate_set) {
        UserTimelineCol(_id = __id_!!, playerId = _playerId_!!, userPreferences = _userPreferences_!!, userState = _userState_!!, temporaryIds = _temporaryIds_!!, applicationIds = _applicationIds_!!, lastActionText = _lastActionText_, lastUpdateDate = _lastUpdateDate_!!, lastUserActionDate = _lastUserActionDate_!!, namespace = _namespace_, creationDate = _creationDate_!!)
      } else {
        val map_ = mutableMapOf<KParameter, Any?>()
        if (__id_set) map_[parameters.getValue("_id")] = __id_
        if (_playerId_set) map_[parameters.getValue("playerId")] = _playerId_
        if (_userPreferences_set) map_[parameters.getValue("userPreferences")] = _userPreferences_
        if (_userState_set) map_[parameters.getValue("userState")] = _userState_
        if (_temporaryIds_set) map_[parameters.getValue("temporaryIds")] = _temporaryIds_
        if (_applicationIds_set) map_[parameters.getValue("applicationIds")] = _applicationIds_
        if (_lastActionText_set) map_[parameters.getValue("lastActionText")] = _lastActionText_
        if (_lastUpdateDate_set) map_[parameters.getValue("lastUpdateDate")] = _lastUpdateDate_
        if (_lastUserActionDate_set) map_[parameters.getValue("lastUserActionDate")] = _lastUserActionDate_
        if (_namespace_set) map_[parameters.getValue("namespace")] = _namespace_
        if (_creationDate_set) map_[parameters.getValue("creationDate")] = _creationDate_
        primaryConstructor.callBy(map_)
      }
      return result_
    }
  }

  private companion object {
    private val primaryConstructor: KFunction<UserTimelineCol> by
        lazy(LazyThreadSafetyMode.PUBLICATION) { UserTimelineCol::class.primaryConstructor!! }

    private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
          mapOf(
            "_id" to primaryConstructor.findParameterByName("_id")!!,
            "playerId" to primaryConstructor.findParameterByName("playerId")!!,
            "userPreferences" to primaryConstructor.findParameterByName("userPreferences")!!,
            "userState" to primaryConstructor.findParameterByName("userState")!!,
            "temporaryIds" to primaryConstructor.findParameterByName("temporaryIds")!!,
            "applicationIds" to primaryConstructor.findParameterByName("applicationIds")!!,
            "lastActionText" to primaryConstructor.findParameterByName("lastActionText")!!,
            "lastUpdateDate" to primaryConstructor.findParameterByName("lastUpdateDate")!!,
            "lastUserActionDate" to primaryConstructor.findParameterByName("lastUserActionDate")!!,
            "namespace" to primaryConstructor.findParameterByName("namespace")!!,
            "creationDate" to primaryConstructor.findParameterByName("creationDate")!!,
          )
        }

    private val __id__reference: TypeReference<Id<UserTimelineCol>> =
        object : TypeReference<Id<UserTimelineCol>>() {}

    private val _temporaryIds__reference: TypeReference<MutableSet<String>> =
        object : TypeReference<MutableSet<String>>() {}

    private val _applicationIds__reference: TypeReference<MutableSet<String>> =
        object : TypeReference<MutableSet<String>>() {}
  }
}
