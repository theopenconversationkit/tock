package ai.tock.bot.mongo

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.litote.jackson.JacksonModuleServiceLoader

internal class UserPreferencesWrapper_Serializer :
        StdSerializer<UserTimelineCol.UserPreferencesWrapper>(UserTimelineCol.UserPreferencesWrapper::class.java),
        JacksonModuleServiceLoader {
    override fun module() =
            SimpleModule().addSerializer(UserTimelineCol.UserPreferencesWrapper::class.java, this)

    override fun serialize(
        value: UserTimelineCol.UserPreferencesWrapper,
        gen: JsonGenerator,
        serializers: SerializerProvider
    ) {
        gen.writeStartObject()
        gen.writeFieldName("firstName")
        val _firstName_ = value.firstName
        if(_firstName_ == null) { gen.writeNull() } else {
                gen.writeString(_firstName_)
                }
        gen.writeFieldName("lastName")
        val _lastName_ = value.lastName
        if(_lastName_ == null) { gen.writeNull() } else {
                gen.writeString(_lastName_)
                }
        gen.writeFieldName("email")
        val _email_ = value.email
        if(_email_ == null) { gen.writeNull() } else {
                gen.writeString(_email_)
                }
        gen.writeFieldName("timezone")
        val _timezone_ = value.timezone
        serializers.defaultSerializeValue(_timezone_, gen)
        gen.writeFieldName("locale")
        val _locale_ = value.locale
        serializers.defaultSerializeValue(_locale_, gen)
        gen.writeFieldName("picture")
        val _picture_ = value.picture
        if(_picture_ == null) { gen.writeNull() } else {
                gen.writeString(_picture_)
                }
        gen.writeFieldName("gender")
        val _gender_ = value.gender
        if(_gender_ == null) { gen.writeNull() } else {
                gen.writeString(_gender_)
                }
        gen.writeFieldName("initialLocale")
        val _initialLocale_ = value.initialLocale
        serializers.defaultSerializeValue(_initialLocale_, gen)
        gen.writeFieldName("test")
        val _test_ = value.test
        gen.writeBoolean(_test_)
        gen.writeFieldName("encrypted")
        val _encrypted_ = value.encrypted
        gen.writeBoolean(_encrypted_)
        gen.writeEndObject()
    }
}
