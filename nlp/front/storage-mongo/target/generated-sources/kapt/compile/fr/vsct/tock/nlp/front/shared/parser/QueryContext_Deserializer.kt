package fr.vsct.tock.nlp.front.shared.parser

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
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

internal class QueryContext_Deserializer : StdDeserializer<QueryContext>(QueryContext::class.java),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addDeserializer(QueryContext::class.java, this)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): QueryContext {
        with(p) {
            var _language_: Locale? = null
            var _language_set = false
            var _clientId_: String? = null
            var _clientId_set = false
            var _clientDevice_: String? = null
            var _clientDevice_set = false
            var _dialogId_: String? = null
            var _dialogId_set = false
            var _referenceDate_: ZonedDateTime? = null
            var _referenceDate_set = false
            var _referenceTimezone_: ZoneId? = null
            var _referenceTimezone_set = false
            var _test_: Boolean? = null
            var _test_set = false
            var _registerQuery_: Boolean? = null
            var _registerQuery_set = false
            var _checkExistingQuery_: Boolean? = null
            var _checkExistingQuery_set = false
            var _increaseQueryCounter_: Boolean? = null
            var _increaseQueryCounter_set = false
            while (currentToken != JsonToken.END_OBJECT && currentToken != JsonToken.END_ARRAY) { 
                if(currentToken != JsonToken.FIELD_NAME) { nextToken() }
                if (currentToken == JsonToken.END_OBJECT || currentToken == JsonToken.END_ARRAY) {
                        break } 
                val fieldName = currentName
                nextToken()
                when (fieldName) { 
                    "language" -> {
                            _language_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.readValueAs(Locale::class.java);
                            _language_set = true
                            }
                    "clientId" -> {
                            _clientId_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.text;
                            _clientId_set = true
                            }
                    "clientDevice" -> {
                            _clientDevice_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.text;
                            _clientDevice_set = true
                            }
                    "dialogId" -> {
                            _dialogId_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.text;
                            _dialogId_set = true
                            }
                    "referenceDate" -> {
                            _referenceDate_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.readValueAs(ZonedDateTime::class.java);
                            _referenceDate_set = true
                            }
                    "referenceTimezone" -> {
                            _referenceTimezone_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.readValueAs(ZoneId::class.java);
                            _referenceTimezone_set = true
                            }
                    "test" -> {
                            _test_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.readValueAs(Boolean::class.java);
                            _test_set = true
                            }
                    "registerQuery" -> {
                            _registerQuery_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.readValueAs(Boolean::class.java);
                            _registerQuery_set = true
                            }
                    "checkExistingQuery" -> {
                            _checkExistingQuery_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.readValueAs(Boolean::class.java);
                            _checkExistingQuery_set = true
                            }
                    "increaseQueryCounter" -> {
                            _increaseQueryCounter_ = if(currentToken == JsonToken.VALUE_NULL) null
                             else p.readValueAs(Boolean::class.java);
                            _increaseQueryCounter_set = true
                            }
                    else -> {
                            if (currentToken == JsonToken.START_OBJECT || currentToken ==
                                    JsonToken.START_ARRAY)
                            p.skipChildren()
                            nextToken()
                            }
                    } 
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
