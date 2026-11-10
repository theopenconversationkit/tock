package ai.tock.nlp.core.configuration

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

internal class NlpApplicationConfiguration_Deserializer : ValueDeserializer<NlpApplicationConfiguration>(),
    JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addDeserializer(NlpApplicationConfiguration::class.java, this)

  override fun deserialize(p: JsonParser, ctxt: DeserializationContext): NlpApplicationConfiguration {
    with(p) {
      var _tokenizerConfiguration_: NlpModelConfiguration? = null
      var _tokenizerConfiguration_set: Boolean = false
      var _intentConfiguration_: NlpModelConfiguration? = null
      var _intentConfiguration_set: Boolean = false
      var _entityConfiguration_: NlpModelConfiguration? = null
      var _entityConfiguration_set: Boolean = false
      var _applicationConfiguration_: NlpModelConfiguration? = null
      var _applicationConfiguration_set: Boolean = false
      var _hasTokenizerConfiguration_: Boolean? = null
      var _hasTokenizerConfiguration_set: Boolean = false
      var _hasIntentConfiguration_: Boolean? = null
      var _hasIntentConfiguration_set: Boolean = false
      var _hasEntityConfiguration_: Boolean? = null
      var _hasEntityConfiguration_set: Boolean = false
      var _hasApplicationConfiguration_: Boolean? = null
      var _hasApplicationConfiguration_set: Boolean = false
      var token_: JsonToken? = currentToken()
      if (token_ == JsonToken.START_OBJECT) token_ = nextToken()
      while (token_ == JsonToken.PROPERTY_NAME) {
        val fieldName_ = currentName()
        token_ = nextToken()
        when (fieldName_) {
          "tokenizerConfiguration" -> {
            _tokenizerConfiguration_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(NlpModelConfiguration::class.javaObjectType)
            _tokenizerConfiguration_set = true
          }
          "intentConfiguration" -> {
            _intentConfiguration_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(NlpModelConfiguration::class.javaObjectType)
            _intentConfiguration_set = true
          }
          "entityConfiguration" -> {
            _entityConfiguration_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(NlpModelConfiguration::class.javaObjectType)
            _entityConfiguration_set = true
          }
          "applicationConfiguration" -> {
            _applicationConfiguration_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(NlpModelConfiguration::class.javaObjectType)
            _applicationConfiguration_set = true
          }
          "hasTokenizerConfiguration" -> {
            _hasTokenizerConfiguration_ = if (token_ == JsonToken.VALUE_NULL) null else p.booleanValue
            _hasTokenizerConfiguration_set = true
          }
          "hasIntentConfiguration" -> {
            _hasIntentConfiguration_ = if (token_ == JsonToken.VALUE_NULL) null else p.booleanValue
            _hasIntentConfiguration_set = true
          }
          "hasEntityConfiguration" -> {
            _hasEntityConfiguration_ = if (token_ == JsonToken.VALUE_NULL) null else p.booleanValue
            _hasEntityConfiguration_set = true
          }
          "hasApplicationConfiguration" -> {
            _hasApplicationConfiguration_ = if (token_ == JsonToken.VALUE_NULL) null else p.booleanValue
            _hasApplicationConfiguration_set = true
          }
          else -> {
            if (token_?.isStructStart == true) p.skipChildren()
          }
        }
        token_ = nextToken()
      }
      val result_ = if (_tokenizerConfiguration_set && _intentConfiguration_set && _entityConfiguration_set && _applicationConfiguration_set && _hasTokenizerConfiguration_set && _hasIntentConfiguration_set && _hasEntityConfiguration_set && _hasApplicationConfiguration_set) {
        NlpApplicationConfiguration(tokenizerConfiguration = _tokenizerConfiguration_!!, intentConfiguration = _intentConfiguration_!!, entityConfiguration = _entityConfiguration_!!, applicationConfiguration = _applicationConfiguration_!!, hasTokenizerConfiguration = _hasTokenizerConfiguration_!!, hasIntentConfiguration = _hasIntentConfiguration_!!, hasEntityConfiguration = _hasEntityConfiguration_!!, hasApplicationConfiguration = _hasApplicationConfiguration_!!)
      } else {
        val map_ = mutableMapOf<KParameter, Any?>()
        if (_tokenizerConfiguration_set) map_[parameters.getValue("tokenizerConfiguration")] = _tokenizerConfiguration_
        if (_intentConfiguration_set) map_[parameters.getValue("intentConfiguration")] = _intentConfiguration_
        if (_entityConfiguration_set) map_[parameters.getValue("entityConfiguration")] = _entityConfiguration_
        if (_applicationConfiguration_set) map_[parameters.getValue("applicationConfiguration")] = _applicationConfiguration_
        if (_hasTokenizerConfiguration_set) map_[parameters.getValue("hasTokenizerConfiguration")] = _hasTokenizerConfiguration_
        if (_hasIntentConfiguration_set) map_[parameters.getValue("hasIntentConfiguration")] = _hasIntentConfiguration_
        if (_hasEntityConfiguration_set) map_[parameters.getValue("hasEntityConfiguration")] = _hasEntityConfiguration_
        if (_hasApplicationConfiguration_set) map_[parameters.getValue("hasApplicationConfiguration")] = _hasApplicationConfiguration_
        primaryConstructor.callBy(map_)
      }
      return result_
    }
  }

  private companion object {
    private val primaryConstructor: KFunction<NlpApplicationConfiguration> by
        lazy(LazyThreadSafetyMode.PUBLICATION) { NlpApplicationConfiguration::class.primaryConstructor!! }

    private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
          mapOf(
            "tokenizerConfiguration" to primaryConstructor.findParameterByName("tokenizerConfiguration")!!,
            "intentConfiguration" to primaryConstructor.findParameterByName("intentConfiguration")!!,
            "entityConfiguration" to primaryConstructor.findParameterByName("entityConfiguration")!!,
            "applicationConfiguration" to primaryConstructor.findParameterByName("applicationConfiguration")!!,
            "hasTokenizerConfiguration" to primaryConstructor.findParameterByName("hasTokenizerConfiguration")!!,
            "hasIntentConfiguration" to primaryConstructor.findParameterByName("hasIntentConfiguration")!!,
            "hasEntityConfiguration" to primaryConstructor.findParameterByName("hasEntityConfiguration")!!,
            "hasApplicationConfiguration" to primaryConstructor.findParameterByName("hasApplicationConfiguration")!!,
          )
        }
  }
}
