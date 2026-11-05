package ai.tock.nlp.front.shared.test

import ai.tock.nlp.front.shared.config.ApplicationDefinition
import ai.tock.nlp.front.shared.config.ClassifiedEntity
import ai.tock.nlp.front.shared.config.IntentDefinition
import java.time.Instant
import java.util.Locale
import kotlin.Any
import kotlin.Boolean
import kotlin.Double
import kotlin.Int
import kotlin.String
import kotlin.collections.List
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

internal class EntityTestError_Deserializer : ValueDeserializer<EntityTestError>(),
    JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addDeserializer(EntityTestError::class.java, this)

  override fun deserialize(p: JsonParser, ctxt: DeserializationContext): EntityTestError {
    with(p) {
      var _applicationId_: Id<ApplicationDefinition>? = null
      var _applicationId_set: Boolean = false
      var _language_: Locale? = null
      var _language_set: Boolean = false
      var _text_: String? = null
      var _text_set: Boolean = false
      var _intentId_: Id<IntentDefinition>? = null
      var _intentId_set: Boolean = false
      var _lastAnalyse_: List<ClassifiedEntity>? = null
      var _lastAnalyse_set: Boolean = false
      var _averageErrorProbability_: Double? = null
      var _averageErrorProbability_set: Boolean = false
      var _count_: Int? = null
      var _count_set: Boolean = false
      var _total_: Int? = null
      var _total_set: Boolean = false
      var _firstDetectionDate_: Instant? = null
      var _firstDetectionDate_set: Boolean = false
      var token_: JsonToken? = currentToken()
      if (token_ == JsonToken.START_OBJECT) token_ = nextToken()
      while (token_ == JsonToken.PROPERTY_NAME) {
        val fieldName_ = currentName()
        token_ = nextToken()
        when (fieldName_) {
          "applicationId" -> {
            _applicationId_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(_applicationId__reference)
            _applicationId_set = true
          }
          "language" -> {
            _language_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(Locale::class.javaObjectType)
            _language_set = true
          }
          "text" -> {
            _text_ = if (token_ == JsonToken.VALUE_NULL) null else p.string
            _text_set = true
          }
          "intentId" -> {
            _intentId_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(_intentId__reference)
            _intentId_set = true
          }
          "lastAnalyse" -> {
            _lastAnalyse_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(_lastAnalyse__reference)
            _lastAnalyse_set = true
          }
          "averageErrorProbability" -> {
            _averageErrorProbability_ = if (token_ == JsonToken.VALUE_NULL) null else p.doubleValue
            _averageErrorProbability_set = true
          }
          "count" -> {
            _count_ = if (token_ == JsonToken.VALUE_NULL) null else p.intValue
            _count_set = true
          }
          "total" -> {
            _total_ = if (token_ == JsonToken.VALUE_NULL) null else p.intValue
            _total_set = true
          }
          "firstDetectionDate" -> {
            _firstDetectionDate_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(Instant::class.javaObjectType)
            _firstDetectionDate_set = true
          }
          else -> {
            if (token_?.isStructStart == true) p.skipChildren()
          }
        }
        token_ = nextToken()
      }
      val result_ = if (_applicationId_set && _language_set && _text_set && _intentId_set && _lastAnalyse_set && _averageErrorProbability_set && _count_set && _total_set && _firstDetectionDate_set) {
        EntityTestError(applicationId = _applicationId_!!, language = _language_!!, text = _text_!!, intentId = _intentId_, lastAnalyse = _lastAnalyse_!!, averageErrorProbability = _averageErrorProbability_!!, count = _count_!!, total = _total_!!, firstDetectionDate = _firstDetectionDate_!!)
      } else {
        val map_ = mutableMapOf<KParameter, Any?>()
        if (_applicationId_set) map_[parameters.getValue("applicationId")] = _applicationId_
        if (_language_set) map_[parameters.getValue("language")] = _language_
        if (_text_set) map_[parameters.getValue("text")] = _text_
        if (_intentId_set) map_[parameters.getValue("intentId")] = _intentId_
        if (_lastAnalyse_set) map_[parameters.getValue("lastAnalyse")] = _lastAnalyse_
        if (_averageErrorProbability_set) map_[parameters.getValue("averageErrorProbability")] = _averageErrorProbability_
        if (_count_set) map_[parameters.getValue("count")] = _count_
        if (_total_set) map_[parameters.getValue("total")] = _total_
        if (_firstDetectionDate_set) map_[parameters.getValue("firstDetectionDate")] = _firstDetectionDate_
        primaryConstructor.callBy(map_)
      }
      return result_
    }
  }

  private companion object {
    private val primaryConstructor: KFunction<EntityTestError> by
        lazy(LazyThreadSafetyMode.PUBLICATION) { EntityTestError::class.primaryConstructor!! }

    private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
          mapOf(
            "applicationId" to primaryConstructor.findParameterByName("applicationId")!!,
            "language" to primaryConstructor.findParameterByName("language")!!,
            "text" to primaryConstructor.findParameterByName("text")!!,
            "intentId" to primaryConstructor.findParameterByName("intentId")!!,
            "lastAnalyse" to primaryConstructor.findParameterByName("lastAnalyse")!!,
            "averageErrorProbability" to primaryConstructor.findParameterByName("averageErrorProbability")!!,
            "count" to primaryConstructor.findParameterByName("count")!!,
            "total" to primaryConstructor.findParameterByName("total")!!,
            "firstDetectionDate" to primaryConstructor.findParameterByName("firstDetectionDate")!!,
          )
        }

    private val _applicationId__reference: TypeReference<Id<ApplicationDefinition>> =
        object : TypeReference<Id<ApplicationDefinition>>() {}

    private val _intentId__reference: TypeReference<Id<IntentDefinition>> =
        object : TypeReference<Id<IntentDefinition>>() {}

    private val _lastAnalyse__reference: TypeReference<List<ClassifiedEntity>> =
        object : TypeReference<List<ClassifiedEntity>>() {}
  }
}
