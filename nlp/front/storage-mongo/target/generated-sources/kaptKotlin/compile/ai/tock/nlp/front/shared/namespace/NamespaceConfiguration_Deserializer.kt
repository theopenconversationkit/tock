package ai.tock.nlp.front.shared.namespace

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
import tools.jackson.core.type.TypeReference
import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.JacksonModule
import tools.jackson.databind.ValueDeserializer
import tools.jackson.databind.module.SimpleModule

internal class NamespaceConfiguration_Deserializer : ValueDeserializer<NamespaceConfiguration>(),
    JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addDeserializer(NamespaceConfiguration::class.java, this)

  override fun deserialize(p: JsonParser, ctxt: DeserializationContext): NamespaceConfiguration {
    with(p) {
      var _namespace_: String? = null
      var _namespace_set: Boolean = false
      var _defaultSharingConfiguration_: NamespaceSharingConfiguration? = null
      var _defaultSharingConfiguration_set: Boolean = false
      var _namespaceImportConfiguration_: Map<String, NamespaceSharingConfiguration>? = null
      var _namespaceImportConfiguration_set: Boolean = false
      var _label_: String? = null
      var _label_set: Boolean = false
      var token_: JsonToken? = currentToken()
      if (token_ == JsonToken.START_OBJECT) token_ = nextToken()
      while (token_ == JsonToken.PROPERTY_NAME) {
        val fieldName_ = currentName()
        token_ = nextToken()
        when (fieldName_) {
          "namespace" -> {
            _namespace_ = if (token_ == JsonToken.VALUE_NULL) null else p.string
            _namespace_set = true
          }
          "defaultSharingConfiguration" -> {
            _defaultSharingConfiguration_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(NamespaceSharingConfiguration::class.javaObjectType)
            _defaultSharingConfiguration_set = true
          }
          "namespaceImportConfiguration" -> {
            _namespaceImportConfiguration_ = if (token_ == JsonToken.VALUE_NULL) null else p.readValueAs(_namespaceImportConfiguration__reference)
            _namespaceImportConfiguration_set = true
          }
          "label" -> {
            _label_ = if (token_ == JsonToken.VALUE_NULL) null else p.string
            _label_set = true
          }
          else -> {
            if (token_?.isStructStart == true) p.skipChildren()
          }
        }
        token_ = nextToken()
      }
      val result_ = if (_namespace_set && _defaultSharingConfiguration_set && _namespaceImportConfiguration_set && _label_set) {
        NamespaceConfiguration(namespace = _namespace_!!, defaultSharingConfiguration = _defaultSharingConfiguration_!!, namespaceImportConfiguration = _namespaceImportConfiguration_!!, label = _label_)
      } else {
        val map_ = mutableMapOf<KParameter, Any?>()
        if (_namespace_set) map_[parameters.getValue("namespace")] = _namespace_
        if (_defaultSharingConfiguration_set) map_[parameters.getValue("defaultSharingConfiguration")] = _defaultSharingConfiguration_
        if (_namespaceImportConfiguration_set) map_[parameters.getValue("namespaceImportConfiguration")] = _namespaceImportConfiguration_
        if (_label_set) map_[parameters.getValue("label")] = _label_
        primaryConstructor.callBy(map_)
      }
      return result_
    }
  }

  private companion object {
    private val primaryConstructor: KFunction<NamespaceConfiguration> by
        lazy(LazyThreadSafetyMode.PUBLICATION) { NamespaceConfiguration::class.primaryConstructor!! }

    private val parameters: Map<String, KParameter> by lazy(LazyThreadSafetyMode.PUBLICATION) {
          mapOf(
            "namespace" to primaryConstructor.findParameterByName("namespace")!!,
            "defaultSharingConfiguration" to primaryConstructor.findParameterByName("defaultSharingConfiguration")!!,
            "namespaceImportConfiguration" to primaryConstructor.findParameterByName("namespaceImportConfiguration")!!,
            "label" to primaryConstructor.findParameterByName("label")!!,
          )
        }

    private val _namespaceImportConfiguration__reference:
        TypeReference<Map<String, NamespaceSharingConfiguration>> =
        object : TypeReference<Map<String, NamespaceSharingConfiguration>>() {}
  }
}
