package ai.tock.bot.mongo

import ai.tock.bot.engine.dialog.Dialog
import ai.tock.bot.engine.user.PlayerId
import java.time.Instant
import kotlin.Any
import kotlin.Boolean
import kotlin.Int
import kotlin.String
import kotlin.collections.List
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

internal class DialogCol_Deserializer : ValueDeserializer<DialogCol>(), JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addDeserializer(DialogCol::class.java, this)

  override fun deserialize(p: JsonParser, ctxt: DeserializationContext): DialogCol {
    with(p) {
      var _playerIds_: Set<PlayerId>? = null
      var _playerIds_set: Boolean = false
      var __id_: Id<Dialog>? = null
      var __id_set: Boolean = false
      var _state_: DialogCol.DialogStateMongoWrapper? = null
      var _state_set: Boolean = false
      var _stories_: List<DialogCol.StoryMongoWrapper>? = null
      var _stories_set: Boolean = false
      var _applicationIds_: Set<String>? = null
      var _applicationIds_set: Boolean = false
      var _lastUpdateDate_: Instant? = null
      var _lastUpdateDate_set: Boolean = false
      var _groupId_: String? = null
      var _groupId_set: Boolean = false
      var _test_: Boolean? = null
      var _test_set: Boolean = false
      var _namespace_: String? = null
      var _namespace_set: Boolean = false
      var _rating_: Int? = null
      var _rating_set: Boolean = false
      var _review_: String? = null
      var _review_set: Boolean = false
      var token_: JsonToken? = currentToken()
      if (token_ == JsonToken.START_OBJECT) token_ = nextToken()
      while (token_ == JsonToken.PROPERTY_NAME) {
        val fieldName_ = currentName()
        token_ = nextToken()
        when (fieldName_) {
          "playerIds" -> {
            _playerIds_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(_playerIds__reference)
            _playerIds_set = true
          }
          "_id" -> {
            __id_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(__id__reference)
            __id_set = true
          }
          "state" -> {
            _state_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(DialogCol.DialogStateMongoWrapper::class.javaObjectType)
            _state_set = true
          }
          "stories" -> {
            _stories_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(_stories__reference)
            _stories_set = true
          }
          "applicationIds" -> {
            _applicationIds_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(_applicationIds__reference)
            _applicationIds_set = true
          }
          "lastUpdateDate" -> {
            _lastUpdateDate_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(Instant::class.javaObjectType)
            _lastUpdateDate_set = true
          }
          "groupId" -> {
            _groupId_ = if (token_ == JsonToken.VALUE_NULL) null else p.string
            _groupId_set = true
          }
          "test" -> {
            _test_ = if (token_ == JsonToken.VALUE_NULL) null else p.booleanValue
            _test_set = true
          }
          "namespace" -> {
            _namespace_ = if (token_ == JsonToken.VALUE_NULL) null else p.string
            _namespace_set = true
          }
          "rating" -> {
            _rating_ = if (token_ == JsonToken.VALUE_NULL) null else p.intValue
            _rating_set = true
          }
          "review" -> {
            _review_ = if (token_ == JsonToken.VALUE_NULL) null else p.string
            _review_set = true
          }
          else -> {
            if (token_?.isStructStart == true) p.skipChildren()
          }
        }
        token_ = nextToken()
      }
      val result_ = if (_playerIds_set && __id_set && _state_set && _stories_set && _applicationIds_set && _lastUpdateDate_set && _groupId_set && _test_set && _namespace_set && _rating_set && _review_set) {
        DialogCol(playerIds = _playerIds_!!, _id = __id_!!, state = _state_!!, stories = _stories_!!, applicationIds = _applicationIds_!!, lastUpdateDate = _lastUpdateDate_!!, groupId = _groupId_, test = _test_!!, namespace = _namespace_, rating = _rating_, review = _review_)
      } else {
        val map_ = mutableMapOf<KParameter, Any?>()
        if (_playerIds_set) map_[parameters.getValue("playerIds")] = _playerIds_
        if (__id_set) map_[parameters.getValue("_id")] = __id_
        if (_state_set) map_[parameters.getValue("state")] = _state_
        if (_stories_set) map_[parameters.getValue("stories")] = _stories_
        if (_applicationIds_set) map_[parameters.getValue("applicationIds")] = _applicationIds_
        if (_lastUpdateDate_set) map_[parameters.getValue("lastUpdateDate")] = _lastUpdateDate_
        if (_groupId_set) map_[parameters.getValue("groupId")] = _groupId_
        if (_test_set) map_[parameters.getValue("test")] = _test_
        if (_namespace_set) map_[parameters.getValue("namespace")] = _namespace_
        if (_rating_set) map_[parameters.getValue("rating")] = _rating_
        if (_review_set) map_[parameters.getValue("review")] = _review_
        primaryConstructor.callBy(map_)
      }
      return result_
    }
  }

  private companion object {
    private val primaryConstructor: KFunction<DialogCol> by
        lazy(LazyThreadSafetyMode.PUBLICATION) { DialogCol::class.primaryConstructor!! }

    private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
          mapOf(
            "playerIds" to primaryConstructor.findParameterByName("playerIds")!!,
            "_id" to primaryConstructor.findParameterByName("_id")!!,
            "state" to primaryConstructor.findParameterByName("state")!!,
            "stories" to primaryConstructor.findParameterByName("stories")!!,
            "applicationIds" to primaryConstructor.findParameterByName("applicationIds")!!,
            "lastUpdateDate" to primaryConstructor.findParameterByName("lastUpdateDate")!!,
            "groupId" to primaryConstructor.findParameterByName("groupId")!!,
            "test" to primaryConstructor.findParameterByName("test")!!,
            "namespace" to primaryConstructor.findParameterByName("namespace")!!,
            "rating" to primaryConstructor.findParameterByName("rating")!!,
            "review" to primaryConstructor.findParameterByName("review")!!,
          )
        }

    private val _playerIds__reference: TypeReference<Set<PlayerId>> =
        object : TypeReference<Set<PlayerId>>() {}

    private val __id__reference: TypeReference<Id<Dialog>> = object : TypeReference<Id<Dialog>>() {}

    private val _stories__reference: TypeReference<List<DialogCol.StoryMongoWrapper>> =
        object : TypeReference<List<DialogCol.StoryMongoWrapper>>() {}

    private val _applicationIds__reference: TypeReference<Set<String>> =
        object : TypeReference<Set<String>>() {}
  }
}
