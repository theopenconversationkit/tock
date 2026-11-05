package ai.tock.nlp.front.shared.user

import kotlin.Any
import kotlin.Boolean
import kotlin.String
import kotlin.collections.Map
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.findParameterByName
import kotlin.reflect.full.primaryConstructor
import org.litote.jackson.JacksonModuleServiceLoader
import tools.jackson.core.JsonParser
import tools.jackson.core.JsonToken
import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.JacksonModule
import tools.jackson.databind.ValueDeserializer
import tools.jackson.databind.module.SimpleModule

internal class UserNamespace_Deserializer : ValueDeserializer<UserNamespace>(),
    JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addDeserializer(UserNamespace::class.java, this)

  override fun deserialize(p: JsonParser, ctxt: DeserializationContext): UserNamespace {
    with(p) {
      var _login_: String? = null
      var _login_set: Boolean = false
      var _namespace_: String? = null
      var _namespace_set: Boolean = false
      var _owner_: Boolean? = null
      var _owner_set: Boolean = false
      var _current_: Boolean? = null
      var _current_set: Boolean = false
      var _label_: String? = null
      var _label_set: Boolean = false
      var token_: JsonToken? = currentToken()
      if (token_ == JsonToken.START_OBJECT) token_ = nextToken()
      while (token_ == JsonToken.PROPERTY_NAME) {
        val fieldName_ = currentName()
        token_ = nextToken()
        when (fieldName_) {
          "login" -> {
            _login_ = if (token_ == JsonToken.VALUE_NULL) null else p.string
            _login_set = true
          }
          "namespace" -> {
            _namespace_ = if (token_ == JsonToken.VALUE_NULL) null else p.string
            _namespace_set = true
          }
          "owner" -> {
            _owner_ = if (token_ == JsonToken.VALUE_NULL) null else p.booleanValue
            _owner_set = true
          }
          "current" -> {
            _current_ = if (token_ == JsonToken.VALUE_NULL) null else p.booleanValue
            _current_set = true
          }
          "label" -> {
            _label_ = if (token_ == JsonToken.VALUE_NULL) null else p.string
            _label_set = true
          }
          else -> {
            if (token_?.isStructStart == true) p.skipChildren()
          }
        }
        token_ = nextToken()
      }
      val result_ = if (_login_set && _namespace_set && _owner_set && _current_set && _label_set) {
        UserNamespace(login = _login_!!, namespace = _namespace_!!, owner = _owner_!!, current = _current_!!, label = _label_)
      } else {
        val map_ = mutableMapOf<KParameter, Any?>()
        if (_login_set) map_[parameters.getValue("login")] = _login_
        if (_namespace_set) map_[parameters.getValue("namespace")] = _namespace_
        if (_owner_set) map_[parameters.getValue("owner")] = _owner_
        if (_current_set) map_[parameters.getValue("current")] = _current_
        if (_label_set) map_[parameters.getValue("label")] = _label_
        primaryConstructor.callBy(map_)
      }
      return result_
    }
  }

  private companion object {
    private val primaryConstructor: KFunction<UserNamespace> by
        lazy(LazyThreadSafetyMode.PUBLICATION) { UserNamespace::class.primaryConstructor!! }

    private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
          mapOf(
            "login" to primaryConstructor.findParameterByName("login")!!,
            "namespace" to primaryConstructor.findParameterByName("namespace")!!,
            "owner" to primaryConstructor.findParameterByName("owner")!!,
            "current" to primaryConstructor.findParameterByName("current")!!,
            "label" to primaryConstructor.findParameterByName("label")!!,
          )
        }
  }
}
