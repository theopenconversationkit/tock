package ai.tock.nlp.front.shared.config

import ai.tock.nlp.core.EntitiesRegexp
import java.util.LinkedHashSet
import java.util.Locale
import kotlin.Any
import kotlin.Boolean
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

internal class IntentDefinition_Deserializer : ValueDeserializer<IntentDefinition>(),
    JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addDeserializer(IntentDefinition::class.java, this)

  override fun deserialize(p: JsonParser, ctxt: DeserializationContext): IntentDefinition {
    with(p) {
      var _name_: String? = null
      var _name_set: Boolean = false
      var _namespace_: String? = null
      var _namespace_set: Boolean = false
      var _applications_: Set<Id<ApplicationDefinition>>? = null
      var _applications_set: Boolean = false
      var _entities_: Set<EntityDefinition>? = null
      var _entities_set: Boolean = false
      var _entitiesRegexp_: Map<Locale, LinkedHashSet<EntitiesRegexp>>? = null
      var _entitiesRegexp_set: Boolean = false
      var _mandatoryStates_: Set<String>? = null
      var _mandatoryStates_set: Boolean = false
      var _sharedIntents_: Set<Id<IntentDefinition>>? = null
      var _sharedIntents_set: Boolean = false
      var _label_: String? = null
      var _label_set: Boolean = false
      var _description_: String? = null
      var _description_set: Boolean = false
      var _category_: String? = null
      var _category_set: Boolean = false
      var __id_: Id<IntentDefinition>? = null
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
          "namespace" -> {
            _namespace_ = if (token_ == JsonToken.VALUE_NULL) null else p.string
            _namespace_set = true
          }
          "applications" -> {
            _applications_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(_applications__reference)
            _applications_set = true
          }
          "entities" -> {
            _entities_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(_entities__reference)
            _entities_set = true
          }
          "entitiesRegexp" -> {
            _entitiesRegexp_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(_entitiesRegexp__reference)
            _entitiesRegexp_set = true
          }
          "mandatoryStates" -> {
            _mandatoryStates_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(_mandatoryStates__reference)
            _mandatoryStates_set = true
          }
          "sharedIntents" -> {
            _sharedIntents_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(_sharedIntents__reference)
            _sharedIntents_set = true
          }
          "label" -> {
            _label_ = if (token_ == JsonToken.VALUE_NULL) null else p.string
            _label_set = true
          }
          "description" -> {
            _description_ = if (token_ == JsonToken.VALUE_NULL) null else p.string
            _description_set = true
          }
          "category" -> {
            _category_ = if (token_ == JsonToken.VALUE_NULL) null else p.string
            _category_set = true
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
      val result_ = if (_name_set && _namespace_set && _applications_set && _entities_set && _entitiesRegexp_set && _mandatoryStates_set && _sharedIntents_set && _label_set && _description_set && _category_set && __id_set) {
        IntentDefinition(name = _name_!!, namespace = _namespace_!!, applications = _applications_!!, entities = _entities_!!, entitiesRegexp = _entitiesRegexp_!!, mandatoryStates = _mandatoryStates_!!, sharedIntents = _sharedIntents_!!, label = _label_, description = _description_, category = _category_, _id = __id_!!)
      } else {
        val map_ = mutableMapOf<KParameter, Any?>()
        if (_name_set) map_[parameters.getValue("name")] = _name_
        if (_namespace_set) map_[parameters.getValue("namespace")] = _namespace_
        if (_applications_set) map_[parameters.getValue("applications")] = _applications_
        if (_entities_set) map_[parameters.getValue("entities")] = _entities_
        if (_entitiesRegexp_set) map_[parameters.getValue("entitiesRegexp")] = _entitiesRegexp_
        if (_mandatoryStates_set) map_[parameters.getValue("mandatoryStates")] = _mandatoryStates_
        if (_sharedIntents_set) map_[parameters.getValue("sharedIntents")] = _sharedIntents_
        if (_label_set) map_[parameters.getValue("label")] = _label_
        if (_description_set) map_[parameters.getValue("description")] = _description_
        if (_category_set) map_[parameters.getValue("category")] = _category_
        if (__id_set) map_[parameters.getValue("_id")] = __id_
        primaryConstructor.callBy(map_)
      }
      return result_
    }
  }

  private companion object {
    private val primaryConstructor: KFunction<IntentDefinition> by
        lazy(LazyThreadSafetyMode.PUBLICATION) { IntentDefinition::class.primaryConstructor!! }

    private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
          mapOf(
            "name" to primaryConstructor.findParameterByName("name")!!,
            "namespace" to primaryConstructor.findParameterByName("namespace")!!,
            "applications" to primaryConstructor.findParameterByName("applications")!!,
            "entities" to primaryConstructor.findParameterByName("entities")!!,
            "entitiesRegexp" to primaryConstructor.findParameterByName("entitiesRegexp")!!,
            "mandatoryStates" to primaryConstructor.findParameterByName("mandatoryStates")!!,
            "sharedIntents" to primaryConstructor.findParameterByName("sharedIntents")!!,
            "label" to primaryConstructor.findParameterByName("label")!!,
            "description" to primaryConstructor.findParameterByName("description")!!,
            "category" to primaryConstructor.findParameterByName("category")!!,
            "_id" to primaryConstructor.findParameterByName("_id")!!,
          )
        }

    private val _applications__reference: TypeReference<Set<Id<ApplicationDefinition>>> =
        object : TypeReference<Set<Id<ApplicationDefinition>>>() {}

    private val _entities__reference: TypeReference<Set<EntityDefinition>> =
        object : TypeReference<Set<EntityDefinition>>() {}

    private val _entitiesRegexp__reference:
        TypeReference<Map<Locale, LinkedHashSet<EntitiesRegexp>>> =
        object : TypeReference<Map<Locale, LinkedHashSet<EntitiesRegexp>>>() {}

    private val _mandatoryStates__reference: TypeReference<Set<String>> =
        object : TypeReference<Set<String>>() {}

    private val _sharedIntents__reference: TypeReference<Set<Id<IntentDefinition>>> =
        object : TypeReference<Set<Id<IntentDefinition>>>() {}

    private val __id__reference: TypeReference<Id<IntentDefinition>> =
        object : TypeReference<Id<IntentDefinition>>() {}
  }
}
