package fr.vsct.tock.nlp.front.shared.parser

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Locale
import kotlin.Boolean
import kotlin.String
import kotlin.collections.Map
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.findParameterByName
import kotlin.reflect.full.primaryConstructor
import org.litote.jackson.JacksonModuleServiceLoader

internal class QueryContext_Deserializer : JsonDeserializer<QueryContext>(),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addDeserializer(QueryContext::class.java, this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): QueryContext {
        with(p) {
            var _language_: Locale? = null
            var _language_set : Boolean = false
            var _clientId_: String? = null
            var _clientId_set : Boolean = false
            var _clientDevice_: String? = null
            var _clientDevice_set : Boolean = false
            var _dialogId_: String? = null
            var _dialogId_set : Boolean = false
            var _referenceDate_: ZonedDateTime? = null
            var _referenceDate_set : Boolean = false
            var _referenceTimezone_: ZoneId? = null
            var _referenceTimezone_set : Boolean = false
            var _test_: Boolean? = null
            var _test_set : Boolean = false
            var _registerQuery_: Boolean? = null
            var _registerQuery_set : Boolean = false
            var _checkExistingQuery_: Boolean? = null
            var _checkExistingQuery_set : Boolean = false
            var _increaseQueryCounter_: Boolean? = null
            var _increaseQueryCounter_set : Boolean = false
            var _token_ : JsonToken? = currentToken
            while (_token_?.isStructEnd != true) { 
                if(_token_ != JsonToken.FIELD_NAME) {
                        _token_ = nextToken()
                        if (_token_?.isStructEnd == true) break
                        }

                val _fieldName_ = currentName
                _token_ = nextToken()
                when (_fieldName_) { 
                    "language" -> {
                            _language_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(Locale::class.java);
                            _language_set = true
                            }
                    "clientId" -> {
                            _clientId_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.text;
                            _clientId_set = true
                            }
                    "clientDevice" -> {
                            _clientDevice_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.text;
                            _clientDevice_set = true
                            }
                    "dialogId" -> {
                            _dialogId_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.text;
                            _dialogId_set = true
                            }
                    "referenceDate" -> {
                            _referenceDate_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(ZonedDateTime::class.java);
                            _referenceDate_set = true
                            }
                    "referenceTimezone" -> {
                            _referenceTimezone_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.readValueAs(ZoneId::class.java);
                            _referenceTimezone_set = true
                            }
                    "test" -> {
                            _test_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.booleanValue;
                            _test_set = true
                            }
                    "registerQuery" -> {
                            _registerQuery_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.booleanValue;
                            _registerQuery_set = true
                            }
                    "checkExistingQuery" -> {
                            _checkExistingQuery_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.booleanValue;
                            _checkExistingQuery_set = true
                            }
                    "increaseQueryCounter" -> {
                            _increaseQueryCounter_ = if(_token_ == JsonToken.VALUE_NULL) null
                             else p.booleanValue;
                            _increaseQueryCounter_set = true
                            }
                    else -> {
                            if (_token_?.isStructStart == true)
                            p.skipChildren()
                            nextToken()
                            }
                    } 
                _token_ = currentToken
                        } 
            return if(_language_set && _clientId_set && _clientDevice_set && _dialogId_set &&
                    _referenceDate_set && _referenceTimezone_set && _test_set && _registerQuery_set
                    && _checkExistingQuery_set && _increaseQueryCounter_set)
                    QueryContext(language = _language_!!, clientId = _clientId_!!, clientDevice =
                            _clientDevice_, dialogId = _dialogId_!!, referenceDate =
                            _referenceDate_!!, referenceTimezone = _referenceTimezone_!!, test =
                            _test_!!, registerQuery = _registerQuery_!!, checkExistingQuery =
                            _checkExistingQuery_!!, increaseQueryCounter = _increaseQueryCounter_!!)
                    else {
                    val map = mutableMapOf<KParameter, Any?>()
                    if(_language_set)
                    map[parameters.getValue("language")] = _language_
                    if(_clientId_set)
                    map[parameters.getValue("clientId")] = _clientId_
                    if(_clientDevice_set)
                    map[parameters.getValue("clientDevice")] = _clientDevice_
                    if(_dialogId_set)
                    map[parameters.getValue("dialogId")] = _dialogId_
                    if(_referenceDate_set)
                    map[parameters.getValue("referenceDate")] = _referenceDate_
                    if(_referenceTimezone_set)
                    map[parameters.getValue("referenceTimezone")] = _referenceTimezone_
                    if(_test_set)
                    map[parameters.getValue("test")] = _test_
                    if(_registerQuery_set)
                    map[parameters.getValue("registerQuery")] = _registerQuery_
                    if(_checkExistingQuery_set)
                    map[parameters.getValue("checkExistingQuery")] = _checkExistingQuery_
                    if(_increaseQueryCounter_set)
                    map[parameters.getValue("increaseQueryCounter")] = _increaseQueryCounter_ 
                    primaryConstructor.callBy(map) 
                    }
        } 
    }

    companion object {
        private val primaryConstructor: KFunction<QueryContext> by
                lazy(LazyThreadSafetyMode.PUBLICATION) { QueryContext::class.primaryConstructor!! }

        private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
                kotlin.collections.mapOf("language" to
                primaryConstructor.findParameterByName("language")!!, "clientId" to
                primaryConstructor.findParameterByName("clientId")!!, "clientDevice" to
                primaryConstructor.findParameterByName("clientDevice")!!, "dialogId" to
                primaryConstructor.findParameterByName("dialogId")!!, "referenceDate" to
                primaryConstructor.findParameterByName("referenceDate")!!, "referenceTimezone" to
                primaryConstructor.findParameterByName("referenceTimezone")!!, "test" to
                primaryConstructor.findParameterByName("test")!!, "registerQuery" to
                primaryConstructor.findParameterByName("registerQuery")!!, "checkExistingQuery" to
                primaryConstructor.findParameterByName("checkExistingQuery")!!,
                "increaseQueryCounter" to
                primaryConstructor.findParameterByName("increaseQueryCounter")!!) }
    }
}
