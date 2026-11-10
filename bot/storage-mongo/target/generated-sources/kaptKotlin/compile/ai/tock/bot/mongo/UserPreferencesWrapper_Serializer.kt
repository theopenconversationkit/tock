package ai.tock.bot.mongo

import org.litote.jackson.JacksonModuleServiceLoader
import tools.jackson.core.JsonGenerator
import tools.jackson.databind.JacksonModule
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.module.SimpleModule
import tools.jackson.databind.ser.std.StdSerializer

internal class UserPreferencesWrapper_Serializer : StdSerializer<UserTimelineCol.UserPreferencesWrapper>(UserTimelineCol.UserPreferencesWrapper::class.java),
    JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addSerializer(UserTimelineCol.UserPreferencesWrapper::class.java, this)

  override fun serialize(
    `value`: UserTimelineCol.UserPreferencesWrapper,
    gen: JsonGenerator,
    serializers: SerializationContext,
  ) {
    gen.writeStartObject()
    gen.writeName("firstName")
    val _firstName_ = value.firstName
    if (_firstName_ == null) {
      gen.writeNull()
    } else {
      gen.writeString(_firstName_)
    }
    gen.writeName("lastName")
    val _lastName_ = value.lastName
    if (_lastName_ == null) {
      gen.writeNull()
    } else {
      gen.writeString(_lastName_)
    }
    gen.writeName("email")
    val _email_ = value.email
    if (_email_ == null) {
      gen.writeNull()
    } else {
      gen.writeString(_email_)
    }
    gen.writeName("timezone")
    val _timezone_ = value.timezone
    serializers.writeValue(gen, _timezone_)
    gen.writeName("locale")
    val _locale_ = value.locale
    serializers.writeValue(gen, _locale_)
    gen.writeName("picture")
    val _picture_ = value.picture
    if (_picture_ == null) {
      gen.writeNull()
    } else {
      gen.writeString(_picture_)
    }
    gen.writeName("gender")
    val _gender_ = value.gender
    if (_gender_ == null) {
      gen.writeNull()
    } else {
      gen.writeString(_gender_)
    }
    gen.writeName("initialLocale")
    val _initialLocale_ = value.initialLocale
    serializers.writeValue(gen, _initialLocale_)
    gen.writeName("test")
    val _test_ = value.test
    gen.writeBoolean(_test_)
    gen.writeName("encrypted")
    val _encrypted_ = value.encrypted
    gen.writeBoolean(_encrypted_)
    gen.writeEndObject()
  }
}
