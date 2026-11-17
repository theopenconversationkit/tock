package ai.tock.nlp.front.shared.config

import ai.tock.nlp.core.NlpEngineType
import java.util.Locale
import kotlin.Any
import kotlin.Boolean
import kotlin.Double
import kotlin.String
import kotlin.collections.Map
import kotlin.collections.Set
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

internal class ApplicationDefinition_Deserializer : ValueDeserializer<ApplicationDefinition>(),
    JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addDeserializer(ApplicationDefinition::class.java, this)

  override fun deserialize(p: JsonParser, ctxt: DeserializationContext): ApplicationDefinition {
    with(p) {
      var _name_: String? = null
      var _name_set: Boolean = false
      var _label_: String? = null
      var _label_set: Boolean = false
      var _namespace_: String? = null
      var _namespace_set: Boolean = false
      var _intents_: Set<Id<IntentDefinition>>? = null
      var _intents_set: Boolean = false
      var _supportedLocales_: Set<Locale>? = null
      var _supportedLocales_set: Boolean = false
      var _intentStatesMap_: Map<Id<IntentDefinition>, Set<String>>? = null
      var _intentStatesMap_set: Boolean = false
      var _nlpEngineType_: NlpEngineType? = null
      var _nlpEngineType_set: Boolean = false
      var _mergeEngineTypes_: Boolean? = null
      var _mergeEngineTypes_set: Boolean = false
      var _useEntityModels_: Boolean? = null
      var _useEntityModels_set: Boolean = false
      var _supportSubEntities_: Boolean? = null
      var _supportSubEntities_set: Boolean = false
      var _unknownIntentThreshold_: Double? = null
      var _unknownIntentThreshold_set: Boolean = false
      var _knownIntentThreshold_: Double? = null
      var _knownIntentThreshold_set: Boolean = false
      var _normalizeText_: Boolean? = null
      var _normalizeText_set: Boolean = false
      var __id_: Id<ApplicationDefinition>? = null
      var __id_set: Boolean = false
      var token_: JsonToken? = currentToken()
      if (token_ == JsonToken.START_OBJECT) token_ = nextToken()
      while (token_ == JsonToken.PROPERTY_NAME) {
        val fieldName_ = currentName()
        token_ = nextToken()
        when (fieldName_) {
          "name" -> {
            _name_ = if (token_ == JsonToken.VALUE_NULL) null else p.string
            _name_set = true
          }
          "label" -> {
            _label_ = if (token_ == JsonToken.VALUE_NULL) null else p.string
            _label_set = true
          }
          "namespace" -> {
            _namespace_ = if (token_ == JsonToken.VALUE_NULL) null else p.string
            _namespace_set = true
          }
          "intents" -> {
            _intents_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(_intents__reference)
            _intents_set = true
          }
          "supportedLocales" -> {
            _supportedLocales_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(_supportedLocales__reference)
            _supportedLocales_set = true
          }
          "intentStatesMap" -> {
            _intentStatesMap_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(_intentStatesMap__reference)
            _intentStatesMap_set = true
          }
          "nlpEngineType" -> {
            _nlpEngineType_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(NlpEngineType::class.javaObjectType)
            _nlpEngineType_set = true
          }
          "mergeEngineTypes" -> {
            _mergeEngineTypes_ = if (token_ == JsonToken.VALUE_NULL) null else p.booleanValue
            _mergeEngineTypes_set = true
          }
          "useEntityModels" -> {
            _useEntityModels_ = if (token_ == JsonToken.VALUE_NULL) null else p.booleanValue
            _useEntityModels_set = true
          }
          "supportSubEntities" -> {
            _supportSubEntities_ = if (token_ == JsonToken.VALUE_NULL) null else p.booleanValue
            _supportSubEntities_set = true
          }
          "unknownIntentThreshold" -> {
            _unknownIntentThreshold_ = if (token_ == JsonToken.VALUE_NULL) null else p.doubleValue
            _unknownIntentThreshold_set = true
          }
          "knownIntentThreshold" -> {
            _knownIntentThreshold_ = if (token_ == JsonToken.VALUE_NULL) null else p.doubleValue
            _knownIntentThreshold_set = true
          }
          "normalizeText" -> {
            _normalizeText_ = if (token_ == JsonToken.VALUE_NULL) null else p.booleanValue
            _normalizeText_set = true
          }
          "_id" -> {
            __id_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(__id__reference)
            __id_set = true
          }
          else -> {
            if (token_?.isStructStart == true) p.skipChildren()
          }
        }
        token_ = nextToken()
      }
      val result_ = if (_name_set && _label_set && _namespace_set && _intents_set && _supportedLocales_set && _intentStatesMap_set && _nlpEngineType_set && _mergeEngineTypes_set && _useEntityModels_set && _supportSubEntities_set && _unknownIntentThreshold_set && _knownIntentThreshold_set && _normalizeText_set && __id_set) {
        ApplicationDefinition(name = _name_!!, label = _label_!!, namespace = _namespace_!!, intents = _intents_!!, supportedLocales = _supportedLocales_!!, intentStatesMap = _intentStatesMap_!!, nlpEngineType = _nlpEngineType_!!, mergeEngineTypes = _mergeEngineTypes_!!, useEntityModels = _useEntityModels_!!, supportSubEntities = _supportSubEntities_!!, unknownIntentThreshold = _unknownIntentThreshold_!!, knownIntentThreshold = _knownIntentThreshold_!!, normalizeText = _normalizeText_!!, _id = __id_!!)
      } else {
        val map_ = mutableMapOf<KParameter, Any?>()
        if (_name_set) map_[parameters.getValue("name")] = _name_
        if (_label_set) map_[parameters.getValue("label")] = _label_
        if (_namespace_set) map_[parameters.getValue("namespace")] = _namespace_
        if (_intents_set) map_[parameters.getValue("intents")] = _intents_
        if (_supportedLocales_set) map_[parameters.getValue("supportedLocales")] = _supportedLocales_
        if (_intentStatesMap_set) map_[parameters.getValue("intentStatesMap")] = _intentStatesMap_
        if (_nlpEngineType_set) map_[parameters.getValue("nlpEngineType")] = _nlpEngineType_
        if (_mergeEngineTypes_set) map_[parameters.getValue("mergeEngineTypes")] = _mergeEngineTypes_
        if (_useEntityModels_set) map_[parameters.getValue("useEntityModels")] = _useEntityModels_
        if (_supportSubEntities_set) map_[parameters.getValue("supportSubEntities")] = _supportSubEntities_
        if (_unknownIntentThreshold_set) map_[parameters.getValue("unknownIntentThreshold")] = _unknownIntentThreshold_
        if (_knownIntentThreshold_set) map_[parameters.getValue("knownIntentThreshold")] = _knownIntentThreshold_
        if (_normalizeText_set) map_[parameters.getValue("normalizeText")] = _normalizeText_
        if (__id_set) map_[parameters.getValue("_id")] = __id_
        primaryConstructor.callBy(map_)
      }
      return result_
    }
  }

  private companion object {
    private val primaryConstructor: KFunction<ApplicationDefinition> by
        lazy(LazyThreadSafetyMode.PUBLICATION) { ApplicationDefinition::class.primaryConstructor!! }

    private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
          mapOf(
            "name" to primaryConstructor.findParameterByName("name")!!,
            "label" to primaryConstructor.findParameterByName("label")!!,
            "namespace" to primaryConstructor.findParameterByName("namespace")!!,
            "intents" to primaryConstructor.findParameterByName("intents")!!,
            "supportedLocales" to primaryConstructor.findParameterByName("supportedLocales")!!,
            "intentStatesMap" to primaryConstructor.findParameterByName("intentStatesMap")!!,
            "nlpEngineType" to primaryConstructor.findParameterByName("nlpEngineType")!!,
            "mergeEngineTypes" to primaryConstructor.findParameterByName("mergeEngineTypes")!!,
            "useEntityModels" to primaryConstructor.findParameterByName("useEntityModels")!!,
            "supportSubEntities" to primaryConstructor.findParameterByName("supportSubEntities")!!,
            "unknownIntentThreshold" to primaryConstructor.findParameterByName("unknownIntentThreshold")!!,
            "knownIntentThreshold" to primaryConstructor.findParameterByName("knownIntentThreshold")!!,
            "normalizeText" to primaryConstructor.findParameterByName("normalizeText")!!,
            "_id" to primaryConstructor.findParameterByName("_id")!!,
          )
        }

    private val _intents__reference: TypeReference<Set<Id<IntentDefinition>>> =
        object : TypeReference<Set<Id<IntentDefinition>>>() {}

    private val _supportedLocales__reference: TypeReference<Set<Locale>> =
        object : TypeReference<Set<Locale>>() {}

    private val _intentStatesMap__reference: TypeReference<Map<Id<IntentDefinition>, Set<String>>> =
        object : TypeReference<Map<Id<IntentDefinition>, Set<String>>>() {}

    private val __id__reference: TypeReference<Id<ApplicationDefinition>> =
        object : TypeReference<Id<ApplicationDefinition>>() {}
  }
}
