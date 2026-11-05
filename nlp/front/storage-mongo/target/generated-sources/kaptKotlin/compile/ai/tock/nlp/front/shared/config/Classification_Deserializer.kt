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

internal class Classification_Deserializer : ValueDeserializer<Classification>(),
    JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addDeserializer(Classification::class.java, this)

  override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Classification {
    with(p) {
      var _intentId_: Id<IntentDefinition>? = null
      var _intentId_set: Boolean = false
      var _entities_: List<ClassifiedEntity>? = null
      var _entities_set: Boolean = false
      var token_: JsonToken? = currentToken()
      if (token_ == JsonToken.START_OBJECT) token_ = nextToken()
      while (token_ == JsonToken.PROPERTY_NAME) {
        val fieldName_ = currentName()
        token_ = nextToken()
        when (fieldName_) {
          "intentId" -> {
            _intentId_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(_intentId__reference)
            _intentId_set = true
          }
          "entities" -> {
            _entities_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(_entities__reference)
            _entities_set = true
          }
          else -> {
            if (token_?.isStructStart == true) p.skipChildren()
          }
        }
        token_ = nextToken()
      }
      val result_ = if (_intentId_set && _entities_set) {
        Classification(intentId = _intentId_!!, entities = _entities_!!)
      } else {
        val map_ = mutableMapOf<KParameter, Any?>()
        if (_intentId_set) map_[parameters.getValue("intentId")] = _intentId_
        if (_entities_set) map_[parameters.getValue("entities")] = _entities_
        primaryConstructor.callBy(map_)
      }
      return result_
    }
  }

  private companion object {
    private val primaryConstructor: KFunction<Classification> by
        lazy(LazyThreadSafetyMode.PUBLICATION) { Classification::class.primaryConstructor!! }

    private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
          mapOf(
            "intentId" to primaryConstructor.findParameterByName("intentId")!!,
            "entities" to primaryConstructor.findParameterByName("entities")!!,
          )
        }

    private val _intentId__reference: TypeReference<Id<IntentDefinition>> =
        object : TypeReference<Id<IntentDefinition>>() {}

    private val _entities__reference: TypeReference<List<ClassifiedEntity>> =
        object : TypeReference<List<ClassifiedEntity>>() {}
  }
}
