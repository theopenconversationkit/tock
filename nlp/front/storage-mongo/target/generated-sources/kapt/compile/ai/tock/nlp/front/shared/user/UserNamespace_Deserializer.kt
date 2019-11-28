package ai.tock.nlp.front.shared.user

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import kotlin.Boolean
import kotlin.String
import kotlin.collections.Map
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.findParameterByName
import kotlin.reflect.full.primaryConstructor
import org.litote.jackson.JacksonModuleServiceLoader

internal class UserNamespace_Deserializer : JsonDeserializer<UserNamespace>(),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addDeserializer(UserNamespace::class.java, this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): UserNamespace {
        with(p) {
            var _login_: String? = null
            var _login_set : Boolean = false
            var _namespace_: String? = null
            var _namespace_set : Boolean = false
            var _owner_: Boolean? = null
            var _owner_set : Boolean = false
            var _current_: Boolean? = null
            var _current_set : Boolean = false
            var _token_ : JsonToken? = currentToken
            while (_token_?.isStructEnd != true) { 
                if(_token_ != JsonToken.FIELD_NAME) {
                        _token_ = nextToken()
                        if (_token_?.isStructEnd == true) break
                        }

                val _fieldName_ = currentName
                _token_ = nextToken()
                when (_fieldName_) { 
                    "login" -> {
                            _login_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.text;
                            _login_set = true
                            }
                    "namespace" -> {
                            _namespace_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.text;
                            _namespace_set = true
                            }
                    "owner" -> {
                            _owner_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.booleanValue;
                            _owner_set = true
                            }
                    "current" -> {
                            _current_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.booleanValue;
                            _current_set = true
                            }
                    else -> {
                            if (_token_?.isStructStart == true)
                            p.skipChildren()
                            nextToken()
                            }
                    } 
                _token_ = currentToken
                        } 
            return if(_login_set && _namespace_set && _owner_set && _current_set)
                    UserNamespace(login = _login_!!, namespace = _namespace_!!, owner = _owner_!!,
                            current = _current_!!)
                    else {
                    val map = mutableMapOf<KParameter, Any?>()
                    if(_login_set)
                    map[parameters.getValue("login")] = _login_
                    if(_namespace_set)
                    map[parameters.getValue("namespace")] = _namespace_
                    if(_owner_set)
                    map[parameters.getValue("owner")] = _owner_
                    if(_current_set)
                    map[parameters.getValue("current")] = _current_ 
                    primaryConstructor.callBy(map) 
                    }
        } 
    }

    companion object {
        private val primaryConstructor: KFunction<UserNamespace> by
                lazy(LazyThreadSafetyMode.PUBLICATION) { UserNamespace::class.primaryConstructor!! }

        private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
                kotlin.collections.mapOf("login" to
                primaryConstructor.findParameterByName("login")!!, "namespace" to
                primaryConstructor.findParameterByName("namespace")!!, "owner" to
                primaryConstructor.findParameterByName("owner")!!, "current" to
                primaryConstructor.findParameterByName("current")!!) }
    }
}
