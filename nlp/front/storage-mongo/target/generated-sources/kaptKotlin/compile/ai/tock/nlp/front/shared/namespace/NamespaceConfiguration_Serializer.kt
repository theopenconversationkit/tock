package ai.tock.nlp.front.shared.namespace

import kotlin.String
import kotlin.collections.Map
import org.litote.jackson.JacksonModuleServiceLoader
import tools.jackson.core.JsonGenerator
import tools.jackson.databind.JacksonModule
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.module.SimpleModule
import tools.jackson.databind.ser.std.StdSerializer

internal class NamespaceConfiguration_Serializer : StdSerializer<NamespaceConfiguration>(NamespaceConfiguration::class.java),
    JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addSerializer(NamespaceConfiguration::class.java, this)

  override fun serialize(
    `value`: NamespaceConfiguration,
    gen: JsonGenerator,
    serializers: SerializationContext,
  ) {
    gen.writeStartObject()
    gen.writeName("namespace")
    val _namespace_ = value.namespace
    gen.writeString(_namespace_)
    gen.writeName("defaultSharingConfiguration")
    val _defaultSharingConfiguration_ = value.defaultSharingConfiguration
    serializers.writeValue(gen, _defaultSharingConfiguration_)
    gen.writeName("namespaceImportConfiguration")
    val _namespaceImportConfiguration_ = value.namespaceImportConfiguration
    serializers.findTypedValueSerializer(serializers.typeFactory.constructMapType(Map::class.java, serializers.typeFactory.constructType(String::class.javaObjectType), serializers.typeFactory.constructType(NamespaceSharingConfiguration::class.javaObjectType)), true).serialize(_namespaceImportConfiguration_, gen, serializers)
    gen.writeName("label")
    val _label_ = value.label
    if (_label_ == null) {
      gen.writeNull()
    } else {
      gen.writeString(_label_)
    }
    gen.writeEndObject()
  }
}
