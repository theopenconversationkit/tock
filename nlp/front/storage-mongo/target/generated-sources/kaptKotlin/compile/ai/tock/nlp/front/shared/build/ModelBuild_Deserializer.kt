package ai.tock.nlp.front.shared.build

import ai.tock.nlp.front.shared.config.ApplicationDefinition
import ai.tock.nlp.front.shared.config.IntentDefinition
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

internal class ModelBuild_Deserializer : ValueDeserializer<ModelBuild>(),
    JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addDeserializer(ModelBuild::class.java, this)

  override fun deserialize(p: JsonParser, ctxt: DeserializationContext): ModelBuild {
    with(p) {
      var _applicationId_: Id<ApplicationDefinition>? = null
      var _applicationId_set: Boolean = false
      var _language_: Locale? = null
      var _language_set: Boolean = false
      var _type_: ModelBuildType? = null
      var _type_set: Boolean = false
      var _intentId_: Id<IntentDefinition>? = null
      var _intentId_set: Boolean = false
      var _entityTypeName_: String? = null
      var _entityTypeName_set: Boolean = false
      var _nbSentences_: Int? = null
      var _nbSentences_set: Boolean = false
      var _duration_: Duration? = null
      var _duration_set: Boolean = false
      var _error_: Boolean? = null
      var _error_set: Boolean = false
      var _errorMessage_: String? = null
      var _errorMessage_set: Boolean = false
      var _date_: Instant? = null
      var _date_set: Boolean = false
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
          "type" -> {
            _type_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(ModelBuildType::class.javaObjectType)
            _type_set = true
          }
          "intentId" -> {
            _intentId_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(_intentId__reference)
            _intentId_set = true
          }
          "entityTypeName" -> {
            _entityTypeName_ = if (token_ == JsonToken.VALUE_NULL) null else p.string
            _entityTypeName_set = true
          }
          "nbSentences" -> {
            _nbSentences_ = if (token_ == JsonToken.VALUE_NULL) null else p.intValue
            _nbSentences_set = true
          }
          "duration" -> {
            _duration_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(Duration::class.javaObjectType)
            _duration_set = true
          }
          "error" -> {
            _error_ = if (token_ == JsonToken.VALUE_NULL) null else p.booleanValue
            _error_set = true
          }
          "errorMessage" -> {
            _errorMessage_ = if (token_ == JsonToken.VALUE_NULL) null else p.string
            _errorMessage_set = true
          }
          "date" -> {
            _date_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(Instant::class.javaObjectType)
            _date_set = true
          }
          else -> {
            if (token_?.isStructStart == true) p.skipChildren()
          }
        }
        token_ = nextToken()
      }
      val result_ = if (_applicationId_set && _language_set && _type_set && _intentId_set && _entityTypeName_set && _nbSentences_set && _duration_set && _error_set && _errorMessage_set && _date_set) {
        ModelBuild(applicationId = _applicationId_!!, language = _language_!!, type = _type_!!, intentId = _intentId_, entityTypeName = _entityTypeName_, nbSentences = _nbSentences_!!, duration = _duration_!!, error = _error_!!, errorMessage = _errorMessage_, date = _date_!!)
      } else {
        val map_ = mutableMapOf<KParameter, Any?>()
        if (_applicationId_set) map_[parameters.getValue("applicationId")] = _applicationId_
        if (_language_set) map_[parameters.getValue("language")] = _language_
        if (_type_set) map_[parameters.getValue("type")] = _type_
        if (_intentId_set) map_[parameters.getValue("intentId")] = _intentId_
        if (_entityTypeName_set) map_[parameters.getValue("entityTypeName")] = _entityTypeName_
        if (_nbSentences_set) map_[parameters.getValue("nbSentences")] = _nbSentences_
        if (_duration_set) map_[parameters.getValue("duration")] = _duration_
        if (_error_set) map_[parameters.getValue("error")] = _error_
        if (_errorMessage_set) map_[parameters.getValue("errorMessage")] = _errorMessage_
        if (_date_set) map_[parameters.getValue("date")] = _date_
        primaryConstructor.callBy(map_)
      }
      return result_
    }
  }

  private companion object {
    private val primaryConstructor: KFunction<ModelBuild> by
        lazy(LazyThreadSafetyMode.PUBLICATION) { ModelBuild::class.primaryConstructor!! }

    private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
          mapOf(
            "applicationId" to primaryConstructor.findParameterByName("applicationId")!!,
            "language" to primaryConstructor.findParameterByName("language")!!,
            "type" to primaryConstructor.findParameterByName("type")!!,
            "intentId" to primaryConstructor.findParameterByName("intentId")!!,
            "entityTypeName" to primaryConstructor.findParameterByName("entityTypeName")!!,
            "nbSentences" to primaryConstructor.findParameterByName("nbSentences")!!,
            "duration" to primaryConstructor.findParameterByName("duration")!!,
            "error" to primaryConstructor.findParameterByName("error")!!,
            "errorMessage" to primaryConstructor.findParameterByName("errorMessage")!!,
            "date" to primaryConstructor.findParameterByName("date")!!,
          )
        }

    private val _applicationId__reference: TypeReference<Id<ApplicationDefinition>> =
        object : TypeReference<Id<ApplicationDefinition>>() {}

    private val _intentId__reference: TypeReference<Id<IntentDefinition>> =
        object : TypeReference<Id<IntentDefinition>>() {}
  }
}
