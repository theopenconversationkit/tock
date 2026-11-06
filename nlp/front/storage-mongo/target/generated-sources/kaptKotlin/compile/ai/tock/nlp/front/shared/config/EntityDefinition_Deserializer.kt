package ai.tock.nlp.front.shared.config

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

internal class EntityDefinition_Deserializer : ValueDeserializer<EntityDefinition>(),
    JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addDeserializer(EntityDefinition::class.java, this)

  override fun deserialize(p: JsonParser, ctxt: DeserializationContext): EntityDefinition {
    with(p) {
      var _entityTypeName_: String? = null
      var _entityTypeName_set: Boolean = false
      var _role_: String? = null
      var _role_set: Boolean = false
      var _atStartOfDay_: Boolean? = null
      var _atStartOfDay_set: Boolean = false
      var token_: JsonToken? = currentToken()
      if (token_ == JsonToken.START_OBJECT) token_ = nextToken()
      while (token_ == JsonToken.PROPERTY_NAME) {
        val fieldName_ = currentName()
        token_ = nextToken()
        when (fieldName_) {
          "entityTypeName" -> {
            _entityTypeName_ = if (token_ == JsonToken.VALUE_NULL) null else p.string
            _entityTypeName_set = true
          }
          "role" -> {
            _role_ = if (token_ == JsonToken.VALUE_NULL) null else p.string
            _role_set = true
          }
          "atStartOfDay" -> {
            _atStartOfDay_ = if (token_ == JsonToken.VALUE_NULL) null else p.booleanValue
            _atStartOfDay_set = true
          }
          else -> {
            if (token_?.isStructStart == true) p.skipChildren()
          }
        }
        token_ = nextToken()
      }
      val result_ = if (_entityTypeName_set && _role_set && _atStartOfDay_set) {
        EntityDefinition(entityTypeName = _entityTypeName_!!, role = _role_!!, atStartOfDay = _atStartOfDay_)
      } else {
        val map_ = mutableMapOf<KParameter, Any?>()
        if (_entityTypeName_set) map_[parameters.getValue("entityTypeName")] = _entityTypeName_
        if (_role_set) map_[parameters.getValue("role")] = _role_
        if (_atStartOfDay_set) map_[parameters.getValue("atStartOfDay")] = _atStartOfDay_
        primaryConstructor.callBy(map_)
      }
      return result_
    }
  }

  private companion object {
    private val primaryConstructor: KFunction<EntityDefinition> by
        lazy(LazyThreadSafetyMode.PUBLICATION) { EntityDefinition::class.primaryConstructor!! }

    private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
          mapOf(
            "entityTypeName" to primaryConstructor.findParameterByName("entityTypeName")!!,
            "role" to primaryConstructor.findParameterByName("role")!!,
            "atStartOfDay" to primaryConstructor.findParameterByName("atStartOfDay")!!,
          )
        }
  }
}
