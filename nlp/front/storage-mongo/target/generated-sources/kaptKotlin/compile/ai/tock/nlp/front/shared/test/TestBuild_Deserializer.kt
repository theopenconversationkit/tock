package ai.tock.nlp.front.shared.test

import ai.tock.nlp.front.shared.config.ApplicationDefinition
import java.time.Duration
import java.time.Instant
import java.util.Locale
import kotlin.Any
import kotlin.Boolean
import kotlin.Int
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

internal class TestBuild_Deserializer : ValueDeserializer<TestBuild>(), JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addDeserializer(TestBuild::class.java, this)

  override fun deserialize(p: JsonParser, ctxt: DeserializationContext): TestBuild {
    with(p) {
      var _applicationId_: Id<ApplicationDefinition>? = null
      var _applicationId_set: Boolean = false
      var _language_: Locale? = null
      var _language_set: Boolean = false
      var _startDate_: Instant? = null
      var _startDate_set: Boolean = false
      var _buildModelDuration_: Duration? = null
      var _buildModelDuration_set: Boolean = false
      var _testSentencesDuration_: Duration? = null
      var _testSentencesDuration_set: Boolean = false
      var _nbSentencesInModel_: Int? = null
      var _nbSentencesInModel_set: Boolean = false
      var _nbSentencesTested_: Int? = null
      var _nbSentencesTested_set: Boolean = false
      var _nbErrors_: Int? = null
      var _nbErrors_set: Boolean = false
      var _intentErrors_: Int? = null
      var _intentErrors_set: Boolean = false
      var _entityErrors_: Int? = null
      var _entityErrors_set: Boolean = false
      var _nbSentencesTestedByIntent_: Map<String, Int>? = null
      var _nbSentencesTestedByIntent_set: Boolean = false
      var _intentErrorsByIntent_: Map<String, Int>? = null
      var _intentErrorsByIntent_set: Boolean = false
      var _entityErrorsByIntent_: Map<String, Int>? = null
      var _entityErrorsByIntent_set: Boolean = false
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
          "startDate" -> {
            _startDate_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(Instant::class.javaObjectType)
            _startDate_set = true
          }
          "buildModelDuration" -> {
            _buildModelDuration_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(Duration::class.javaObjectType)
            _buildModelDuration_set = true
          }
          "testSentencesDuration" -> {
            _testSentencesDuration_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(Duration::class.javaObjectType)
            _testSentencesDuration_set = true
          }
          "nbSentencesInModel" -> {
            _nbSentencesInModel_ = if (token_ == JsonToken.VALUE_NULL) null else p.intValue
            _nbSentencesInModel_set = true
          }
          "nbSentencesTested" -> {
            _nbSentencesTested_ = if (token_ == JsonToken.VALUE_NULL) null else p.intValue
            _nbSentencesTested_set = true
          }
          "nbErrors" -> {
            _nbErrors_ = if (token_ == JsonToken.VALUE_NULL) null else p.intValue
            _nbErrors_set = true
          }
          "intentErrors" -> {
            _intentErrors_ = if (token_ == JsonToken.VALUE_NULL) null else p.intValue
            _intentErrors_set = true
          }
          "entityErrors" -> {
            _entityErrors_ = if (token_ == JsonToken.VALUE_NULL) null else p.intValue
            _entityErrors_set = true
          }
          "nbSentencesTestedByIntent" -> {
            _nbSentencesTestedByIntent_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(_nbSentencesTestedByIntent__reference)
            _nbSentencesTestedByIntent_set = true
          }
          "intentErrorsByIntent" -> {
            _intentErrorsByIntent_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(_intentErrorsByIntent__reference)
            _intentErrorsByIntent_set = true
          }
          "entityErrorsByIntent" -> {
            _entityErrorsByIntent_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(_entityErrorsByIntent__reference)
            _entityErrorsByIntent_set = true
          }
          else -> {
            if (token_?.isStructStart == true) p.skipChildren()
          }
        }
        token_ = nextToken()
      }
      val result_ = if (_applicationId_set && _language_set && _startDate_set && _buildModelDuration_set && _testSentencesDuration_set && _nbSentencesInModel_set && _nbSentencesTested_set && _nbErrors_set && _intentErrors_set && _entityErrors_set && _nbSentencesTestedByIntent_set && _intentErrorsByIntent_set && _entityErrorsByIntent_set) {
        TestBuild(applicationId = _applicationId_!!, language = _language_!!, startDate = _startDate_!!, buildModelDuration = _buildModelDuration_!!, testSentencesDuration = _testSentencesDuration_!!, nbSentencesInModel = _nbSentencesInModel_!!, nbSentencesTested = _nbSentencesTested_!!, nbErrors = _nbErrors_!!, intentErrors = _intentErrors_!!, entityErrors = _entityErrors_!!, nbSentencesTestedByIntent = _nbSentencesTestedByIntent_!!, intentErrorsByIntent = _intentErrorsByIntent_!!, entityErrorsByIntent = _entityErrorsByIntent_!!)
      } else {
        val map_ = mutableMapOf<KParameter, Any?>()
        if (_applicationId_set) map_[parameters.getValue("applicationId")] = _applicationId_
        if (_language_set) map_[parameters.getValue("language")] = _language_
        if (_startDate_set) map_[parameters.getValue("startDate")] = _startDate_
        if (_buildModelDuration_set) map_[parameters.getValue("buildModelDuration")] = _buildModelDuration_
        if (_testSentencesDuration_set) map_[parameters.getValue("testSentencesDuration")] = _testSentencesDuration_
        if (_nbSentencesInModel_set) map_[parameters.getValue("nbSentencesInModel")] = _nbSentencesInModel_
        if (_nbSentencesTested_set) map_[parameters.getValue("nbSentencesTested")] = _nbSentencesTested_
        if (_nbErrors_set) map_[parameters.getValue("nbErrors")] = _nbErrors_
        if (_intentErrors_set) map_[parameters.getValue("intentErrors")] = _intentErrors_
        if (_entityErrors_set) map_[parameters.getValue("entityErrors")] = _entityErrors_
        if (_nbSentencesTestedByIntent_set) map_[parameters.getValue("nbSentencesTestedByIntent")] = _nbSentencesTestedByIntent_
        if (_intentErrorsByIntent_set) map_[parameters.getValue("intentErrorsByIntent")] = _intentErrorsByIntent_
        if (_entityErrorsByIntent_set) map_[parameters.getValue("entityErrorsByIntent")] = _entityErrorsByIntent_
        primaryConstructor.callBy(map_)
      }
      return result_
    }
  }

  private companion object {
    private val primaryConstructor: KFunction<TestBuild> by
        lazy(LazyThreadSafetyMode.PUBLICATION) { TestBuild::class.primaryConstructor!! }

    private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
          mapOf(
            "applicationId" to primaryConstructor.findParameterByName("applicationId")!!,
            "language" to primaryConstructor.findParameterByName("language")!!,
            "startDate" to primaryConstructor.findParameterByName("startDate")!!,
            "buildModelDuration" to primaryConstructor.findParameterByName("buildModelDuration")!!,
            "testSentencesDuration" to primaryConstructor.findParameterByName("testSentencesDuration")!!,
            "nbSentencesInModel" to primaryConstructor.findParameterByName("nbSentencesInModel")!!,
            "nbSentencesTested" to primaryConstructor.findParameterByName("nbSentencesTested")!!,
            "nbErrors" to primaryConstructor.findParameterByName("nbErrors")!!,
            "intentErrors" to primaryConstructor.findParameterByName("intentErrors")!!,
            "entityErrors" to primaryConstructor.findParameterByName("entityErrors")!!,
            "nbSentencesTestedByIntent" to primaryConstructor.findParameterByName("nbSentencesTestedByIntent")!!,
            "intentErrorsByIntent" to primaryConstructor.findParameterByName("intentErrorsByIntent")!!,
            "entityErrorsByIntent" to primaryConstructor.findParameterByName("entityErrorsByIntent")!!,
          )
        }

    private val _applicationId__reference: TypeReference<Id<ApplicationDefinition>> =
        object : TypeReference<Id<ApplicationDefinition>>() {}

    private val _nbSentencesTestedByIntent__reference: TypeReference<Map<String, Int>> =
        object : TypeReference<Map<String, Int>>() {}

    private val _intentErrorsByIntent__reference: TypeReference<Map<String, Int>> =
        object : TypeReference<Map<String, Int>>() {}

    private val _entityErrorsByIntent__reference: TypeReference<Map<String, Int>> =
        object : TypeReference<Map<String, Int>>() {}
  }
}
