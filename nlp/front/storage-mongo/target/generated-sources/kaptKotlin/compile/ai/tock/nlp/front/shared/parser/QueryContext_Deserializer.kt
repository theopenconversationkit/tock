package ai.tock.nlp.front.shared.parser

import java.time.ZoneId
import java.time.ZonedDateTime
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

internal class QueryContext_Deserializer : ValueDeserializer<QueryContext>(),
    JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addDeserializer(QueryContext::class.java, this)

  override fun deserialize(p: JsonParser, ctxt: DeserializationContext): QueryContext {
    with(p) {
      var _language_: Locale? = null
      var _language_set: Boolean = false
      var _clientId_: String? = null
      var _clientId_set: Boolean = false
      var _clientDevice_: String? = null
      var _clientDevice_set: Boolean = false
      var _dialogId_: String? = null
      var _dialogId_set: Boolean = false
      var _referenceDate_: ZonedDateTime? = null
      var _referenceDate_set: Boolean = false
      var _referenceTimezone_: ZoneId? = null
      var _referenceTimezone_set: Boolean = false
      var _test_: Boolean? = null
      var _test_set: Boolean = false
      var _registerQuery_: Boolean? = null
      var _registerQuery_set: Boolean = false
      var _checkExistingQuery_: Boolean? = null
      var _checkExistingQuery_set: Boolean = false
      var _increaseQueryCounter_: Boolean? = null
      var _increaseQueryCounter_set: Boolean = false
      var token_: JsonToken? = currentToken()
      if (token_ == JsonToken.START_OBJECT) token_ = nextToken()
      while (token_ == JsonToken.PROPERTY_NAME) {
        val fieldName_ = currentName()
        token_ = nextToken()
        when (fieldName_) {
          "language" -> {
            _language_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(Locale::class.javaObjectType)
            _language_set = true
          }
          "clientId" -> {
            _clientId_ = if (token_ == JsonToken.VALUE_NULL) null else p.string
            _clientId_set = true
          }
          "clientDevice" -> {
            _clientDevice_ = if (token_ == JsonToken.VALUE_NULL) null else p.string
            _clientDevice_set = true
          }
          "dialogId" -> {
            _dialogId_ = if (token_ == JsonToken.VALUE_NULL) null else p.string
            _dialogId_set = true
          }
          "referenceDate" -> {
            _referenceDate_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(ZonedDateTime::class.javaObjectType)
            _referenceDate_set = true
          }
          "referenceTimezone" -> {
            _referenceTimezone_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(ZoneId::class.javaObjectType)
            _referenceTimezone_set = true
          }
          "test" -> {
            _test_ = if (token_ == JsonToken.VALUE_NULL) null else p.booleanValue
            _test_set = true
          }
          "registerQuery" -> {
            _registerQuery_ = if (token_ == JsonToken.VALUE_NULL) null else p.booleanValue
            _registerQuery_set = true
          }
          "checkExistingQuery" -> {
            _checkExistingQuery_ = if (token_ == JsonToken.VALUE_NULL) null else p.booleanValue
            _checkExistingQuery_set = true
          }
          "increaseQueryCounter" -> {
            _increaseQueryCounter_ = if (token_ == JsonToken.VALUE_NULL) null else p.booleanValue
            _increaseQueryCounter_set = true
          }
          else -> {
            if (token_?.isStructStart == true) p.skipChildren()
          }
        }
        token_ = nextToken()
      }
      val result_ = if (_language_set && _clientId_set && _clientDevice_set && _dialogId_set && _referenceDate_set && _referenceTimezone_set && _test_set && _registerQuery_set && _checkExistingQuery_set && _increaseQueryCounter_set) {
        QueryContext(language = _language_!!, clientId = _clientId_!!, clientDevice = _clientDevice_, dialogId = _dialogId_!!, referenceDate = _referenceDate_!!, referenceTimezone = _referenceTimezone_!!, test = _test_!!, registerQuery = _registerQuery_!!, checkExistingQuery = _checkExistingQuery_!!, increaseQueryCounter = _increaseQueryCounter_!!)
      } else {
        val map_ = mutableMapOf<KParameter, Any?>()
        if (_language_set) map_[parameters.getValue("language")] = _language_
        if (_clientId_set) map_[parameters.getValue("clientId")] = _clientId_
        if (_clientDevice_set) map_[parameters.getValue("clientDevice")] = _clientDevice_
        if (_dialogId_set) map_[parameters.getValue("dialogId")] = _dialogId_
        if (_referenceDate_set) map_[parameters.getValue("referenceDate")] = _referenceDate_
        if (_referenceTimezone_set) map_[parameters.getValue("referenceTimezone")] = _referenceTimezone_
        if (_test_set) map_[parameters.getValue("test")] = _test_
        if (_registerQuery_set) map_[parameters.getValue("registerQuery")] = _registerQuery_
        if (_checkExistingQuery_set) map_[parameters.getValue("checkExistingQuery")] = _checkExistingQuery_
        if (_increaseQueryCounter_set) map_[parameters.getValue("increaseQueryCounter")] = _increaseQueryCounter_
        primaryConstructor.callBy(map_)
      }
      return result_
    }
  }

  private companion object {
    private val primaryConstructor: KFunction<QueryContext> by
        lazy(LazyThreadSafetyMode.PUBLICATION) { QueryContext::class.primaryConstructor!! }

    private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
          mapOf(
            "language" to primaryConstructor.findParameterByName("language")!!,
            "clientId" to primaryConstructor.findParameterByName("clientId")!!,
            "clientDevice" to primaryConstructor.findParameterByName("clientDevice")!!,
            "dialogId" to primaryConstructor.findParameterByName("dialogId")!!,
            "referenceDate" to primaryConstructor.findParameterByName("referenceDate")!!,
            "referenceTimezone" to primaryConstructor.findParameterByName("referenceTimezone")!!,
            "test" to primaryConstructor.findParameterByName("test")!!,
            "registerQuery" to primaryConstructor.findParameterByName("registerQuery")!!,
            "checkExistingQuery" to primaryConstructor.findParameterByName("checkExistingQuery")!!,
            "increaseQueryCounter" to primaryConstructor.findParameterByName("increaseQueryCounter")!!,
          )
        }
  }
}
