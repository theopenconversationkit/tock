package ai.tock.nlp.front.shared.config

import kotlin.Any
import kotlin.Boolean
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

internal class EntityTypeDefinition_Deserializer : ValueDeserializer<EntityTypeDefinition>(),
    JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addDeserializer(EntityTypeDefinition::class.java, this)

  override fun deserialize(p: JsonParser, ctxt: DeserializationContext): EntityTypeDefinition {
    with(p) {
      var _name_: String? = null
      var _name_set: Boolean = false
      var _description_: String? = null
      var _description_set: Boolean = false
      var _subEntities_: List<EntityDefinition>? = null
      var _subEntities_set: Boolean = false
      var _dictionary_: Boolean? = null
      var _dictionary_set: Boolean = false
      var _obfuscated_: Boolean? = null
      var _obfuscated_set: Boolean = false
      var __id_: Id<EntityTypeDefinition>? = null
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
          "description" -> {
            _description_ = if (token_ == JsonToken.VALUE_NULL) null else p.string
            _description_set = true
          }
          "subEntities" -> {
            _subEntities_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(_subEntities__reference)
            _subEntities_set = true
          }
          "dictionary" -> {
            _dictionary_ = if (token_ == JsonToken.VALUE_NULL) null else p.booleanValue
            _dictionary_set = true
          }
          "obfuscated" -> {
            _obfuscated_ = if (token_ == JsonToken.VALUE_NULL) null else p.booleanValue
            _obfuscated_set = true
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
      val result_ = if (_name_set && _description_set && _subEntities_set && _dictionary_set && _obfuscated_set && __id_set) {
        EntityTypeDefinition(name = _name_!!, description = _description_!!, subEntities = _subEntities_!!, dictionary = _dictionary_!!, obfuscated = _obfuscated_!!, _id = __id_!!)
      } else {
        val map_ = mutableMapOf<KParameter, Any?>()
        if (_name_set) map_[parameters.getValue("name")] = _name_
        if (_description_set) map_[parameters.getValue("description")] = _description_
        if (_subEntities_set) map_[parameters.getValue("subEntities")] = _subEntities_
        if (_dictionary_set) map_[parameters.getValue("dictionary")] = _dictionary_
        if (_obfuscated_set) map_[parameters.getValue("obfuscated")] = _obfuscated_
        if (__id_set) map_[parameters.getValue("_id")] = __id_
        primaryConstructor.callBy(map_)
      }
      return result_
    }
  }

  private companion object {
    private val primaryConstructor: KFunction<EntityTypeDefinition> by
        lazy(LazyThreadSafetyMode.PUBLICATION) { EntityTypeDefinition::class.primaryConstructor!! }

    private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
          mapOf(
            "name" to primaryConstructor.findParameterByName("name")!!,
            "description" to primaryConstructor.findParameterByName("description")!!,
            "subEntities" to primaryConstructor.findParameterByName("subEntities")!!,
            "dictionary" to primaryConstructor.findParameterByName("dictionary")!!,
            "obfuscated" to primaryConstructor.findParameterByName("obfuscated")!!,
            "_id" to primaryConstructor.findParameterByName("_id")!!,
          )
        }

    private val _subEntities__reference: TypeReference<List<EntityDefinition>> =
        object : TypeReference<List<EntityDefinition>>() {}

    private val __id__reference: TypeReference<Id<EntityTypeDefinition>> =
        object : TypeReference<Id<EntityTypeDefinition>>() {}
  }
}
