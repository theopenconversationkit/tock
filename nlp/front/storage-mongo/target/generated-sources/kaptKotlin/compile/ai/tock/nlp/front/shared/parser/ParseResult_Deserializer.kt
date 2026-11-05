package ai.tock.nlp.front.shared.parser

import java.util.Locale
import kotlin.Any
import kotlin.Boolean
import kotlin.Double
import kotlin.String
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.findParameterByName
import kotlin.reflect.full.primaryConstructor
import org.litote.jackson.JacksonModuleServiceLoader
import tools.jackson.core.JsonParser
import tools.jackson.core.JsonToken
import tools.jackson.core.type.TypeReference
import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.JacksonModule
import tools.jackson.databind.ValueDeserializer
import tools.jackson.databind.module.SimpleModule

internal class ParseResult_Deserializer : ValueDeserializer<ParseResult>(),
    JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addDeserializer(ParseResult::class.java, this)

  override fun deserialize(p: JsonParser, ctxt: DeserializationContext): ParseResult {
    with(p) {
      var _intent_: String? = null
      var _intent_set: Boolean = false
      var _intentNamespace_: String? = null
      var _intentNamespace_set: Boolean = false
      var _language_: Locale? = null
      var _language_set: Boolean = false
      var _entities_: List<ParsedEntityValue>? = null
      var _entities_set: Boolean = false
      var _notRetainedEntities_: List<ParsedEntityValue>? = null
      var _notRetainedEntities_set: Boolean = false
      var _intentProbability_: Double? = null
      var _intentProbability_set: Boolean = false
      var _entitiesProbability_: Double? = null
      var _entitiesProbability_set: Boolean = false
      var _retainedQuery_: String? = null
      var _retainedQuery_set: Boolean = false
      var _otherIntentsProbabilities_: Map<String, Double>? = null
      var _otherIntentsProbabilities_set: Boolean = false
      var _originalIntentsProbabilities_: Map<String, Double>? = null
      var _originalIntentsProbabilities_set: Boolean = false
      var token_: JsonToken? = currentToken()
      if (token_ == JsonToken.START_OBJECT) token_ = nextToken()
      while (token_ == JsonToken.PROPERTY_NAME) {
        val fieldName_ = currentName()
        token_ = nextToken()
        when (fieldName_) {
          "intent" -> {
            _intent_ = if (token_ == JsonToken.VALUE_NULL) null else p.string
            _intent_set = true
          }
          "intentNamespace" -> {
            _intentNamespace_ = if (token_ == JsonToken.VALUE_NULL) null else p.string
            _intentNamespace_set = true
          }
          "language" -> {
            _language_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(Locale::class.javaObjectType)
            _language_set = true
          }
          "entities" -> {
            _entities_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(_entities__reference)
            _entities_set = true
          }
          "notRetainedEntities" -> {
            _notRetainedEntities_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(_notRetainedEntities__reference)
            _notRetainedEntities_set = true
          }
          "intentProbability" -> {
            _intentProbability_ = if (token_ == JsonToken.VALUE_NULL) null else p.doubleValue
            _intentProbability_set = true
          }
          "entitiesProbability" -> {
            _entitiesProbability_ = if (token_ == JsonToken.VALUE_NULL) null else p.doubleValue
            _entitiesProbability_set = true
          }
          "retainedQuery" -> {
            _retainedQuery_ = if (token_ == JsonToken.VALUE_NULL) null else p.string
            _retainedQuery_set = true
          }
          "otherIntentsProbabilities" -> {
            _otherIntentsProbabilities_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(_otherIntentsProbabilities__reference)
            _otherIntentsProbabilities_set = true
          }
          "originalIntentsProbabilities" -> {
            _originalIntentsProbabilities_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(_originalIntentsProbabilities__reference)
            _originalIntentsProbabilities_set = true
          }
          else -> {
            if (token_?.isStructStart == true) p.skipChildren()
          }
        }
        token_ = nextToken()
      }
      val result_ = if (_intent_set && _intentNamespace_set && _language_set && _entities_set && _notRetainedEntities_set && _intentProbability_set && _entitiesProbability_set && _retainedQuery_set && _otherIntentsProbabilities_set && _originalIntentsProbabilities_set) {
        ParseResult(intent = _intent_!!, intentNamespace = _intentNamespace_!!, language = _language_!!, entities = _entities_!!, notRetainedEntities = _notRetainedEntities_!!, intentProbability = _intentProbability_!!, entitiesProbability = _entitiesProbability_!!, retainedQuery = _retainedQuery_!!, otherIntentsProbabilities = _otherIntentsProbabilities_!!, originalIntentsProbabilities = _originalIntentsProbabilities_!!)
      } else {
        val map_ = mutableMapOf<KParameter, Any?>()
        if (_intent_set) map_[parameters.getValue("intent")] = _intent_
        if (_intentNamespace_set) map_[parameters.getValue("intentNamespace")] = _intentNamespace_
        if (_language_set) map_[parameters.getValue("language")] = _language_
        if (_entities_set) map_[parameters.getValue("entities")] = _entities_
        if (_notRetainedEntities_set) map_[parameters.getValue("notRetainedEntities")] = _notRetainedEntities_
        if (_intentProbability_set) map_[parameters.getValue("intentProbability")] = _intentProbability_
        if (_entitiesProbability_set) map_[parameters.getValue("entitiesProbability")] = _entitiesProbability_
        if (_retainedQuery_set) map_[parameters.getValue("retainedQuery")] = _retainedQuery_
        if (_otherIntentsProbabilities_set) map_[parameters.getValue("otherIntentsProbabilities")] = _otherIntentsProbabilities_
        if (_originalIntentsProbabilities_set) map_[parameters.getValue("originalIntentsProbabilities")] = _originalIntentsProbabilities_
        primaryConstructor.callBy(map_)
      }
      return result_
    }
  }

  private companion object {
    private val primaryConstructor: KFunction<ParseResult> by
        lazy(LazyThreadSafetyMode.PUBLICATION) { ParseResult::class.primaryConstructor!! }

    private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
          mapOf(
            "intent" to primaryConstructor.findParameterByName("intent")!!,
            "intentNamespace" to primaryConstructor.findParameterByName("intentNamespace")!!,
            "language" to primaryConstructor.findParameterByName("language")!!,
            "entities" to primaryConstructor.findParameterByName("entities")!!,
            "notRetainedEntities" to primaryConstructor.findParameterByName("notRetainedEntities")!!,
            "intentProbability" to primaryConstructor.findParameterByName("intentProbability")!!,
            "entitiesProbability" to primaryConstructor.findParameterByName("entitiesProbability")!!,
            "retainedQuery" to primaryConstructor.findParameterByName("retainedQuery")!!,
            "otherIntentsProbabilities" to primaryConstructor.findParameterByName("otherIntentsProbabilities")!!,
            "originalIntentsProbabilities" to primaryConstructor.findParameterByName("originalIntentsProbabilities")!!,
          )
        }

    private val _entities__reference: TypeReference<List<ParsedEntityValue>> =
        object : TypeReference<List<ParsedEntityValue>>() {}

    private val _notRetainedEntities__reference: TypeReference<List<ParsedEntityValue>> =
        object : TypeReference<List<ParsedEntityValue>>() {}

    private val _otherIntentsProbabilities__reference: TypeReference<Map<String, Double>> =
        object : TypeReference<Map<String, Double>>() {}

    private val _originalIntentsProbabilities__reference: TypeReference<Map<String, Double>> =
        object : TypeReference<Map<String, Double>>() {}
  }
}
