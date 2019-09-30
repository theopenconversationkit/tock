package ai.tock.bot.mongo

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import java.time.ZoneId
import java.util.Locale
import kotlin.Boolean
import kotlin.String
import kotlin.collections.Map
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.findParameterByName
import kotlin.reflect.full.primaryConstructor
import org.litote.jackson.JacksonModuleServiceLoader

internal class UserPreferencesWrapper_Deserializer :
        JsonDeserializer<UserTimelineCol.UserPreferencesWrapper>(), JacksonModuleServiceLoader {
    override fun module() =
            SimpleModule().addDeserializer(UserTimelineCol.UserPreferencesWrapper::class.java, this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext):
            UserTimelineCol.UserPreferencesWrapper {
        with(p) {
            var _firstName_: String? = null
            var _firstName_set : Boolean = false
            var _lastName_: String? = null
            var _lastName_set : Boolean = false
            var _email_: String? = null
            var _email_set : Boolean = false
            var _timezone_: ZoneId? = null
            var _timezone_set : Boolean = false
            var _locale_: Locale? = null
            var _locale_set : Boolean = false
            var _picture_: String? = null
            var _picture_set : Boolean = false
            var _gender_: String? = null
            var _gender_set : Boolean = false
            var _initialLocale_: Locale? = null
            var _initialLocale_set : Boolean = false
            var _test_: Boolean? = null
            var _test_set : Boolean = false
            var _encrypted_: Boolean? = null
            var _encrypted_set : Boolean = false
            var _token_ : JsonToken? = currentToken
            while (_token_?.isStructEnd != true) { 
                if(_token_ != JsonToken.FIELD_NAME) {
                        _token_ = nextToken()
                        if (_token_?.isStructEnd == true) break
                        }

                val _fieldName_ = currentName
                _token_ = nextToken()
                when (_fieldName_) { 
                    "firstName" -> {
                            _firstName_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.text;
                            _firstName_set = true
                            }
                    "lastName" -> {
                            _lastName_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.text;
                            _lastName_set = true
                            }
                    "email" -> {
                            _email_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.text;
                            _email_set = true
                            }
                    "timezone" -> {
                            _timezone_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(ZoneId::class.java);
                            _timezone_set = true
                            }
                    "locale" -> {
                            _locale_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(Locale::class.java);
                            _locale_set = true
                            }
                    "picture" -> {
                            _picture_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.text;
                            _picture_set = true
                            }
                    "gender" -> {
                            _gender_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.text;
                            _gender_set = true
                            }
                    "initialLocale" -> {
                            _initialLocale_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(Locale::class.java);
                            _initialLocale_set = true
                            }
                    "test" -> {
                            _test_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.booleanValue;
                            _test_set = true
                            }
                    "encrypted" -> {
                            _encrypted_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.booleanValue;
                            _encrypted_set = true
                            }
                    else -> {
                            if (_token_?.isStructStart == true)
                            p.skipChildren()
                            nextToken()
                            }
                    } 
                _token_ = currentToken
                        } 
            return if(_firstName_set && _lastName_set && _email_set && _timezone_set && _locale_set
                    && _picture_set && _gender_set && _initialLocale_set && _test_set &&
                    _encrypted_set)
                    UserTimelineCol.UserPreferencesWrapper(firstName = _firstName_, lastName =
                            _lastName_, email = _email_, timezone = _timezone_!!, locale =
                            _locale_!!, picture = _picture_, gender = _gender_, initialLocale =
                            _initialLocale_!!, test = _test_!!, encrypted = _encrypted_!!)
                    else {
                    val map = mutableMapOf<KParameter, Any?>()
                    if(_firstName_set)
                    map[parameters.getValue("firstName")] = _firstName_
                    if(_lastName_set)
                    map[parameters.getValue("lastName")] = _lastName_
                    if(_email_set)
                    map[parameters.getValue("email")] = _email_
                    if(_timezone_set)
                    map[parameters.getValue("timezone")] = _timezone_
                    if(_locale_set)
                    map[parameters.getValue("locale")] = _locale_
                    if(_picture_set)
                    map[parameters.getValue("picture")] = _picture_
                    if(_gender_set)
                    map[parameters.getValue("gender")] = _gender_
                    if(_initialLocale_set)
                    map[parameters.getValue("initialLocale")] = _initialLocale_
                    if(_test_set)
                    map[parameters.getValue("test")] = _test_
                    if(_encrypted_set)
                    map[parameters.getValue("encrypted")] = _encrypted_ 
                    primaryConstructor.callBy(map) 
                    }
        } 
    }

    companion object {
        private val primaryConstructor: KFunction<UserTimelineCol.UserPreferencesWrapper> by
                lazy(LazyThreadSafetyMode.PUBLICATION) {
                UserTimelineCol.UserPreferencesWrapper::class.primaryConstructor!! }

        private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
                kotlin.collections.mapOf("firstName" to
                primaryConstructor.findParameterByName("firstName")!!, "lastName" to
                primaryConstructor.findParameterByName("lastName")!!, "email" to
                primaryConstructor.findParameterByName("email")!!, "timezone" to
                primaryConstructor.findParameterByName("timezone")!!, "locale" to
                primaryConstructor.findParameterByName("locale")!!, "picture" to
                primaryConstructor.findParameterByName("picture")!!, "gender" to
                primaryConstructor.findParameterByName("gender")!!, "initialLocale" to
                primaryConstructor.findParameterByName("initialLocale")!!, "test" to
                primaryConstructor.findParameterByName("test")!!, "encrypted" to
                primaryConstructor.findParameterByName("encrypted")!!) }
    }
}
