package ai.tock.bot.mongo

import java.time.ZoneId
import java.util.Locale
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

internal class UserPreferencesWrapper_Deserializer : ValueDeserializer<UserTimelineCol.UserPreferencesWrapper>(),
    JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addDeserializer(UserTimelineCol.UserPreferencesWrapper::class.java, this)

  override fun deserialize(p: JsonParser, ctxt: DeserializationContext): UserTimelineCol.UserPreferencesWrapper {
    with(p) {
      var _firstName_: String? = null
      var _firstName_set: Boolean = false
      var _lastName_: String? = null
      var _lastName_set: Boolean = false
      var _email_: String? = null
      var _email_set: Boolean = false
      var _timezone_: ZoneId? = null
      var _timezone_set: Boolean = false
      var _locale_: Locale? = null
      var _locale_set: Boolean = false
      var _picture_: String? = null
      var _picture_set: Boolean = false
      var _gender_: String? = null
      var _gender_set: Boolean = false
      var _initialLocale_: Locale? = null
      var _initialLocale_set: Boolean = false
      var _test_: Boolean? = null
      var _test_set: Boolean = false
      var _encrypted_: Boolean? = null
      var _encrypted_set: Boolean = false
      var token_: JsonToken? = currentToken()
      if (token_ == JsonToken.START_OBJECT) token_ = nextToken()
      while (token_ == JsonToken.PROPERTY_NAME) {
        val fieldName_ = currentName()
        token_ = nextToken()
        when (fieldName_) {
          "firstName" -> {
            _firstName_ = if (token_ == JsonToken.VALUE_NULL) null else p.string
            _firstName_set = true
          }
          "lastName" -> {
            _lastName_ = if (token_ == JsonToken.VALUE_NULL) null else p.string
            _lastName_set = true
          }
          "email" -> {
            _email_ = if (token_ == JsonToken.VALUE_NULL) null else p.string
            _email_set = true
          }
          "timezone" -> {
            _timezone_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(ZoneId::class.javaObjectType)
            _timezone_set = true
          }
          "locale" -> {
            _locale_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(Locale::class.javaObjectType)
            _locale_set = true
          }
          "picture" -> {
            _picture_ = if (token_ == JsonToken.VALUE_NULL) null else p.string
            _picture_set = true
          }
          "gender" -> {
            _gender_ = if (token_ == JsonToken.VALUE_NULL) null else p.string
            _gender_set = true
          }
          "initialLocale" -> {
            _initialLocale_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(Locale::class.javaObjectType)
            _initialLocale_set = true
          }
          "test" -> {
            _test_ = if (token_ == JsonToken.VALUE_NULL) null else p.booleanValue
            _test_set = true
          }
          "encrypted" -> {
            _encrypted_ = if (token_ == JsonToken.VALUE_NULL) null else p.booleanValue
            _encrypted_set = true
          }
          else -> {
            if (token_?.isStructStart == true) p.skipChildren()
          }
        }
        token_ = nextToken()
      }
      val result_ = if (_firstName_set && _lastName_set && _email_set && _timezone_set && _locale_set && _picture_set && _gender_set && _initialLocale_set && _test_set && _encrypted_set) {
        UserTimelineCol.UserPreferencesWrapper(firstName = _firstName_, lastName = _lastName_, email = _email_, timezone = _timezone_!!, locale = _locale_!!, picture = _picture_, gender = _gender_, initialLocale = _initialLocale_!!, test = _test_!!, encrypted = _encrypted_!!)
      } else {
        val map_ = mutableMapOf<KParameter, Any?>()
        if (_firstName_set) map_[parameters.getValue("firstName")] = _firstName_
        if (_lastName_set) map_[parameters.getValue("lastName")] = _lastName_
        if (_email_set) map_[parameters.getValue("email")] = _email_
        if (_timezone_set) map_[parameters.getValue("timezone")] = _timezone_
        if (_locale_set) map_[parameters.getValue("locale")] = _locale_
        if (_picture_set) map_[parameters.getValue("picture")] = _picture_
        if (_gender_set) map_[parameters.getValue("gender")] = _gender_
        if (_initialLocale_set) map_[parameters.getValue("initialLocale")] = _initialLocale_
        if (_test_set) map_[parameters.getValue("test")] = _test_
        if (_encrypted_set) map_[parameters.getValue("encrypted")] = _encrypted_
        primaryConstructor.callBy(map_)
      }
      return result_
    }
  }

  private companion object {
    private val primaryConstructor: KFunction<UserTimelineCol.UserPreferencesWrapper> by
        lazy(LazyThreadSafetyMode.PUBLICATION) { UserTimelineCol.UserPreferencesWrapper::class.primaryConstructor!! }

    private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
          mapOf(
            "firstName" to primaryConstructor.findParameterByName("firstName")!!,
            "lastName" to primaryConstructor.findParameterByName("lastName")!!,
            "email" to primaryConstructor.findParameterByName("email")!!,
            "timezone" to primaryConstructor.findParameterByName("timezone")!!,
            "locale" to primaryConstructor.findParameterByName("locale")!!,
            "picture" to primaryConstructor.findParameterByName("picture")!!,
            "gender" to primaryConstructor.findParameterByName("gender")!!,
            "initialLocale" to primaryConstructor.findParameterByName("initialLocale")!!,
            "test" to primaryConstructor.findParameterByName("test")!!,
            "encrypted" to primaryConstructor.findParameterByName("encrypted")!!,
          )
        }
  }
}
