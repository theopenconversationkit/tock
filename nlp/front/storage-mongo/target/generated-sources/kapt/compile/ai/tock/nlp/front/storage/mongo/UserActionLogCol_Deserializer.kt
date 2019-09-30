package ai.tock.nlp.front.storage.mongo

import ai.tock.nlp.front.shared.config.ApplicationDefinition
import ai.tock.shared.jackson.AnyValueWrapper
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import java.time.Instant
import kotlin.Boolean
import kotlin.String
import kotlin.collections.Map
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.findParameterByName
import kotlin.reflect.full.primaryConstructor
import org.litote.jackson.JacksonModuleServiceLoader
import org.litote.kmongo.Id

internal class UserActionLogCol_Deserializer :
        JsonDeserializer<UserActionLogMongoDAO.UserActionLogCol>(), JacksonModuleServiceLoader {
    override fun module() =
            SimpleModule().addDeserializer(UserActionLogMongoDAO.UserActionLogCol::class.java, this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext):
            UserActionLogMongoDAO.UserActionLogCol {
        with(p) {
            var _namespace_: String? = null
            var _namespace_set : Boolean = false
            var _applicationId_: Id<ApplicationDefinition>? = null
            var _applicationId_set : Boolean = false
            var _login_: String? = null
            var _login_set : Boolean = false
            var _actionType_: String? = null
            var _actionType_set : Boolean = false
            var _newData_: AnyValueWrapper? = null
            var _newData_set : Boolean = false
            var _error_: Boolean? = null
            var _error_set : Boolean = false
            var _date_: Instant? = null
            var _date_set : Boolean = false
            var _token_ : JsonToken? = currentToken
            while (_token_?.isStructEnd != true) { 
                if(_token_ != JsonToken.FIELD_NAME) {
                        _token_ = nextToken()
                        if (_token_?.isStructEnd == true) break
                        }

                val _fieldName_ = currentName
                _token_ = nextToken()
                when (_fieldName_) { 
                    "namespace" -> {
                            _namespace_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.text;
                            _namespace_set = true
                            }
                    "applicationId" -> {
                            _applicationId_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(_applicationId__reference);
                            _applicationId_set = true
                            }
                    "login" -> {
                            _login_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.text;
                            _login_set = true
                            }
                    "actionType" -> {
                            _actionType_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.text;
                            _actionType_set = true
                            }
                    "newData" -> {
                            _newData_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(AnyValueWrapper::class.java);
                            _newData_set = true
                            }
                    "error" -> {
                            _error_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.booleanValue;
                            _error_set = true
                            }
                    "date" -> {
                            _date_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(Instant::class.java);
                            _date_set = true
                            }
                    else -> {
                            if (_token_?.isStructStart == true)
                            p.skipChildren()
                            nextToken()
                            }
                    } 
                _token_ = currentToken
                        } 
            return if(_namespace_set && _applicationId_set && _login_set && _actionType_set &&
                    _newData_set && _error_set && _date_set)
                    UserActionLogMongoDAO.UserActionLogCol(namespace = _namespace_!!, applicationId
                            = _applicationId_, login = _login_!!, actionType = _actionType_!!,
                            newData = _newData_, error = _error_!!, date = _date_!!)
                    else {
                    val map = mutableMapOf<KParameter, Any?>()
                    if(_namespace_set)
                    map[parameters.getValue("namespace")] = _namespace_
                    if(_applicationId_set)
                    map[parameters.getValue("applicationId")] = _applicationId_
                    if(_login_set)
                    map[parameters.getValue("login")] = _login_
                    if(_actionType_set)
                    map[parameters.getValue("actionType")] = _actionType_
                    if(_newData_set)
                    map[parameters.getValue("newData")] = _newData_
                    if(_error_set)
                    map[parameters.getValue("error")] = _error_
                    if(_date_set)
                    map[parameters.getValue("date")] = _date_ 
                    primaryConstructor.callBy(map) 
                    }
        } 
    }

    companion object {
        private val primaryConstructor: KFunction<UserActionLogMongoDAO.UserActionLogCol> by
                lazy(LazyThreadSafetyMode.PUBLICATION) {
                UserActionLogMongoDAO.UserActionLogCol::class.primaryConstructor!! }

        private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
                kotlin.collections.mapOf("namespace" to
                primaryConstructor.findParameterByName("namespace")!!, "applicationId" to
                primaryConstructor.findParameterByName("applicationId")!!, "login" to
                primaryConstructor.findParameterByName("login")!!, "actionType" to
                primaryConstructor.findParameterByName("actionType")!!, "newData" to
                primaryConstructor.findParameterByName("newData")!!, "error" to
                primaryConstructor.findParameterByName("error")!!, "date" to
                primaryConstructor.findParameterByName("date")!!) }

        private val _applicationId__reference: TypeReference<Id<ApplicationDefinition>> = object :
                TypeReference<Id<ApplicationDefinition>>() {}
    }
}
