package ai.tock.nlp.core.configuration

import java.util.Properties
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

internal class NlpModelConfiguration_Deserializer : ValueDeserializer<NlpModelConfiguration>(),
    JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addDeserializer(NlpModelConfiguration::class.java, this)

  override fun deserialize(p: JsonParser, ctxt: DeserializationContext): NlpModelConfiguration {
    with(p) {
      var _properties_: Properties? = null
      var _properties_set: Boolean = false
      var _markdown_: String? = null
      var _markdown_set: Boolean = false
      var _hasProperties_: Boolean? = null
      var _hasProperties_set: Boolean = false
      var _hasMarkdown_: Boolean? = null
      var _hasMarkdown_set: Boolean = false
      var token_: JsonToken? = currentToken()
      if (token_ == JsonToken.START_OBJECT) token_ = nextToken()
      while (token_ == JsonToken.PROPERTY_NAME) {
        val fieldName_ = currentName()
        token_ = nextToken()
        when (fieldName_) {
          "properties" -> {
            _properties_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(Properties::class.javaObjectType)
            _properties_set = true
          }
          "markdown" -> {
            _markdown_ = if (token_ == JsonToken.VALUE_NULL) null else p.string
            _markdown_set = true
          }
          "hasProperties" -> {
            _hasProperties_ = if (token_ == JsonToken.VALUE_NULL) null else p.booleanValue
            _hasProperties_set = true
          }
          "hasMarkdown" -> {
            _hasMarkdown_ = if (token_ == JsonToken.VALUE_NULL) null else p.booleanValue
            _hasMarkdown_set = true
          }
          else -> {
            if (token_?.isStructStart == true) p.skipChildren()
          }
        }
        token_ = nextToken()
      }
      val result_ = if (_properties_set && _markdown_set && _hasProperties_set && _hasMarkdown_set) {
        NlpModelConfiguration(properties = _properties_!!, markdown = _markdown_, hasProperties = _hasProperties_!!, hasMarkdown = _hasMarkdown_!!)
      } else {
        val map_ = mutableMapOf<KParameter, Any?>()
        if (_properties_set) map_[parameters.getValue("properties")] = _properties_
        if (_markdown_set) map_[parameters.getValue("markdown")] = _markdown_
        if (_hasProperties_set) map_[parameters.getValue("hasProperties")] = _hasProperties_
        if (_hasMarkdown_set) map_[parameters.getValue("hasMarkdown")] = _hasMarkdown_
        primaryConstructor.callBy(map_)
      }
      return result_
    }
  }

  private companion object {
    private val primaryConstructor: KFunction<NlpModelConfiguration> by
        lazy(LazyThreadSafetyMode.PUBLICATION) { NlpModelConfiguration::class.primaryConstructor!! }

    private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
          mapOf(
            "properties" to primaryConstructor.findParameterByName("properties")!!,
            "markdown" to primaryConstructor.findParameterByName("markdown")!!,
            "hasProperties" to primaryConstructor.findParameterByName("hasProperties")!!,
            "hasMarkdown" to primaryConstructor.findParameterByName("hasMarkdown")!!,
          )
        }
  }
}
