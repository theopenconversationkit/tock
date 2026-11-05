package ai.tock.nlp.front.shared.config

import kotlin.Any
import kotlin.Boolean
import kotlin.Int
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

internal class ClassifiedEntity_Deserializer : ValueDeserializer<ClassifiedEntity>(),
    JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addDeserializer(ClassifiedEntity::class.java, this)

  override fun deserialize(p: JsonParser, ctxt: DeserializationContext): ClassifiedEntity {
    with(p) {
      var _type_: String? = null
      var _type_set: Boolean = false
      var _role_: String? = null
      var _role_set: Boolean = false
      var _start_: Int? = null
      var _start_set: Boolean = false
      var _end_: Int? = null
      var _end_set: Boolean = false
      var _subEntities_: List<ClassifiedEntity>? = null
      var _subEntities_set: Boolean = false
      var token_: JsonToken? = currentToken()
      if (token_ == JsonToken.START_OBJECT) token_ = nextToken()
      while (token_ == JsonToken.PROPERTY_NAME) {
        val fieldName_ = currentName()
        token_ = nextToken()
        when (fieldName_) {
          "type" -> {
            _type_ = if (token_ == JsonToken.VALUE_NULL) null else p.string
            _type_set = true
          }
          "role" -> {
            _role_ = if (token_ == JsonToken.VALUE_NULL) null else p.string
            _role_set = true
          }
          "start" -> {
            _start_ = if (token_ == JsonToken.VALUE_NULL) null else p.intValue
            _start_set = true
          }
          "end" -> {
            _end_ = if (token_ == JsonToken.VALUE_NULL) null else p.intValue
            _end_set = true
          }
          "subEntities" -> {
            _subEntities_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(_subEntities__reference)
            _subEntities_set = true
          }
          else -> {
            if (token_?.isStructStart == true) p.skipChildren()
          }
        }
        token_ = nextToken()
      }
      val result_ = if (_type_set && _role_set && _start_set && _end_set && _subEntities_set) {
        ClassifiedEntity(type = _type_!!, role = _role_!!, start = _start_!!, end = _end_!!, subEntities = _subEntities_!!)
      } else {
        val map_ = mutableMapOf<KParameter, Any?>()
        if (_type_set) map_[parameters.getValue("type")] = _type_
        if (_role_set) map_[parameters.getValue("role")] = _role_
        if (_start_set) map_[parameters.getValue("start")] = _start_
        if (_end_set) map_[parameters.getValue("end")] = _end_
        if (_subEntities_set) map_[parameters.getValue("subEntities")] = _subEntities_
        primaryConstructor.callBy(map_)
      }
      return result_
    }
  }

  private companion object {
    private val primaryConstructor: KFunction<ClassifiedEntity> by
        lazy(LazyThreadSafetyMode.PUBLICATION) { ClassifiedEntity::class.primaryConstructor!! }

    private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
          mapOf(
            "type" to primaryConstructor.findParameterByName("type")!!,
            "role" to primaryConstructor.findParameterByName("role")!!,
            "start" to primaryConstructor.findParameterByName("start")!!,
            "end" to primaryConstructor.findParameterByName("end")!!,
            "subEntities" to primaryConstructor.findParameterByName("subEntities")!!,
          )
        }

    private val _subEntities__reference: TypeReference<List<ClassifiedEntity>> =
        object : TypeReference<List<ClassifiedEntity>>() {}
  }
}
