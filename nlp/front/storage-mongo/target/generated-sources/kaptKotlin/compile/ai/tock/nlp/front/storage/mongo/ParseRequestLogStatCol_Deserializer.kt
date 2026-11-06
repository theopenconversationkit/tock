package ai.tock.nlp.front.storage.mongo

import ai.tock.nlp.front.shared.config.ApplicationDefinition
import java.time.Instant
import java.util.Locale
import kotlin.Any
import kotlin.Boolean
import kotlin.Double
import kotlin.Long
import kotlin.String
import kotlin.collections.Map
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.findParameterByName
import kotlin.reflect.full.primaryConstructor
import org.litote.jackson.JacksonModuleServiceLoader
import org.litote.kmongo.Id
import tools.jackson.core.JsonParser
import tools.jackson.core.JsonToken
import tools.jackson.core.type.TypeReference
import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.JacksonModule
import tools.jackson.databind.ValueDeserializer
import tools.jackson.databind.module.SimpleModule

internal class ParseRequestLogStatCol_Deserializer : ValueDeserializer<ParseRequestLogMongoDAO.ParseRequestLogStatCol>(),
    JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addDeserializer(ParseRequestLogMongoDAO.ParseRequestLogStatCol::class.java, this)

  override fun deserialize(p: JsonParser, ctxt: DeserializationContext): ParseRequestLogMongoDAO.ParseRequestLogStatCol {
    with(p) {
      var _text_: String? = null
      var _text_set: Boolean = false
      var _applicationId_: Id<ApplicationDefinition>? = null
      var _applicationId_set: Boolean = false
      var _language_: Locale? = null
      var _language_set: Boolean = false
      var _intent_: String? = null
      var _intent_set: Boolean = false
      var _intentProbability_: Double? = null
      var _intentProbability_set: Boolean = false
      var _entitiesProbability_: Double? = null
      var _entitiesProbability_set: Boolean = false
      var _lastUsage_: Instant? = null
      var _lastUsage_set: Boolean = false
      var _count_: Long? = null
      var _count_set: Boolean = false
      var _validated_: Boolean? = null
      var _validated_set: Boolean = false
      var token_: JsonToken? = currentToken()
      if (token_ == JsonToken.START_OBJECT) token_ = nextToken()
      while (token_ == JsonToken.PROPERTY_NAME) {
        val fieldName_ = currentName()
        token_ = nextToken()
        when (fieldName_) {
          "text" -> {
            _text_ = if (token_ == JsonToken.VALUE_NULL) null else p.string
            _text_set = true
          }
          "applicationId" -> {
            _applicationId_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(_applicationId__reference)
            _applicationId_set = true
          }
          "language" -> {
            _language_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(Locale::class.javaObjectType)
            _language_set = true
          }
          "intent" -> {
            _intent_ = if (token_ == JsonToken.VALUE_NULL) null else p.string
            _intent_set = true
          }
          "intentProbability" -> {
            _intentProbability_ = if (token_ == JsonToken.VALUE_NULL) null else p.doubleValue
            _intentProbability_set = true
          }
          "entitiesProbability" -> {
            _entitiesProbability_ = if (token_ == JsonToken.VALUE_NULL) null else p.doubleValue
            _entitiesProbability_set = true
          }
          "lastUsage" -> {
            _lastUsage_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(Instant::class.javaObjectType)
            _lastUsage_set = true
          }
          "count" -> {
            _count_ = if (token_ == JsonToken.VALUE_NULL) null else p.longValue
            _count_set = true
          }
          "validated" -> {
            _validated_ = if (token_ == JsonToken.VALUE_NULL) null else p.booleanValue
            _validated_set = true
          }
          else -> {
            if (token_?.isStructStart == true) p.skipChildren()
          }
        }
        token_ = nextToken()
      }
      val result_ = if (_text_set && _applicationId_set && _language_set && _intent_set && _intentProbability_set && _entitiesProbability_set && _lastUsage_set && _count_set && _validated_set) {
        ParseRequestLogMongoDAO.ParseRequestLogStatCol(text = _text_!!, applicationId = _applicationId_!!, language = _language_!!, intent = _intent_, intentProbability = _intentProbability_, entitiesProbability = _entitiesProbability_, lastUsage = _lastUsage_!!, count = _count_!!, validated = _validated_!!)
      } else {
        val map_ = mutableMapOf<KParameter, Any?>()
        if (_text_set) map_[parameters.getValue("text")] = _text_
        if (_applicationId_set) map_[parameters.getValue("applicationId")] = _applicationId_
        if (_language_set) map_[parameters.getValue("language")] = _language_
        if (_intent_set) map_[parameters.getValue("intent")] = _intent_
        if (_intentProbability_set) map_[parameters.getValue("intentProbability")] = _intentProbability_
        if (_entitiesProbability_set) map_[parameters.getValue("entitiesProbability")] = _entitiesProbability_
        if (_lastUsage_set) map_[parameters.getValue("lastUsage")] = _lastUsage_
        if (_count_set) map_[parameters.getValue("count")] = _count_
        if (_validated_set) map_[parameters.getValue("validated")] = _validated_
        primaryConstructor.callBy(map_)
      }
      return result_
    }
  }

  private companion object {
    private val primaryConstructor: KFunction<ParseRequestLogMongoDAO.ParseRequestLogStatCol> by
        lazy(LazyThreadSafetyMode.PUBLICATION) { ParseRequestLogMongoDAO.ParseRequestLogStatCol::class.primaryConstructor!! }

    private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
          mapOf(
            "text" to primaryConstructor.findParameterByName("text")!!,
            "applicationId" to primaryConstructor.findParameterByName("applicationId")!!,
            "language" to primaryConstructor.findParameterByName("language")!!,
            "intent" to primaryConstructor.findParameterByName("intent")!!,
            "intentProbability" to primaryConstructor.findParameterByName("intentProbability")!!,
            "entitiesProbability" to primaryConstructor.findParameterByName("entitiesProbability")!!,
            "lastUsage" to primaryConstructor.findParameterByName("lastUsage")!!,
            "count" to primaryConstructor.findParameterByName("count")!!,
            "validated" to primaryConstructor.findParameterByName("validated")!!,
          )
        }

    private val _applicationId__reference: TypeReference<Id<ApplicationDefinition>> =
        object : TypeReference<Id<ApplicationDefinition>>() {}
  }
}
