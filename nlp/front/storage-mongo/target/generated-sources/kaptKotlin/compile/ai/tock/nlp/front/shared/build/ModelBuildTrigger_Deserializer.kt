package ai.tock.nlp.front.shared.build

import ai.tock.nlp.front.shared.config.ApplicationDefinition
import kotlin.Any
import kotlin.Boolean
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

internal class ModelBuildTrigger_Deserializer : ValueDeserializer<ModelBuildTrigger>(),
    JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addDeserializer(ModelBuildTrigger::class.java, this)

  override fun deserialize(p: JsonParser, ctxt: DeserializationContext): ModelBuildTrigger {
    with(p) {
      var _applicationId_: Id<ApplicationDefinition>? = null
      var _applicationId_set: Boolean = false
      var _all_: Boolean? = null
      var _all_set: Boolean = false
      var _onlyIfModelNotExists_: Boolean? = null
      var _onlyIfModelNotExists_set: Boolean = false
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
          "all" -> {
            _all_ = if (token_ == JsonToken.VALUE_NULL) null else p.booleanValue
            _all_set = true
          }
          "onlyIfModelNotExists" -> {
            _onlyIfModelNotExists_ = if (token_ == JsonToken.VALUE_NULL) null else p.booleanValue
            _onlyIfModelNotExists_set = true
          }
          else -> {
            if (token_?.isStructStart == true) p.skipChildren()
          }
        }
        token_ = nextToken()
      }
      val result_ = if (_applicationId_set && _all_set && _onlyIfModelNotExists_set) {
        ModelBuildTrigger(applicationId = _applicationId_!!, all = _all_!!, onlyIfModelNotExists = _onlyIfModelNotExists_!!)
      } else {
        val map_ = mutableMapOf<KParameter, Any?>()
        if (_applicationId_set) map_[parameters.getValue("applicationId")] = _applicationId_
        if (_all_set) map_[parameters.getValue("all")] = _all_
        if (_onlyIfModelNotExists_set) map_[parameters.getValue("onlyIfModelNotExists")] = _onlyIfModelNotExists_
        primaryConstructor.callBy(map_)
      }
      return result_
    }
  }

  private companion object {
    private val primaryConstructor: KFunction<ModelBuildTrigger> by
        lazy(LazyThreadSafetyMode.PUBLICATION) { ModelBuildTrigger::class.primaryConstructor!! }

    private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
          mapOf(
            "applicationId" to primaryConstructor.findParameterByName("applicationId")!!,
            "all" to primaryConstructor.findParameterByName("all")!!,
            "onlyIfModelNotExists" to primaryConstructor.findParameterByName("onlyIfModelNotExists")!!,
          )
        }

    private val _applicationId__reference: TypeReference<Id<ApplicationDefinition>> =
        object : TypeReference<Id<ApplicationDefinition>>() {}
  }
}
