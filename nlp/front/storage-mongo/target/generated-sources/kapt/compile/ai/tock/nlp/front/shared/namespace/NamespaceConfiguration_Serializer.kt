package ai.tock.nlp.front.shared.namespace

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.litote.jackson.JacksonModuleServiceLoader

internal class NamespaceConfiguration_Serializer :
        StdSerializer<NamespaceConfiguration>(NamespaceConfiguration::class.java),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addSerializer(NamespaceConfiguration::class.java, this)

    override fun serialize(
        value: NamespaceConfiguration,
        gen: JsonGenerator,
        serializers: SerializerProvider
    ) {
        gen.writeStartObject()
        gen.writeFieldName("namespace")
        val _namespace_ = value.namespace
        gen.writeString(_namespace_)
        gen.writeFieldName("defaultSharingConfiguration")
        val _defaultSharingConfiguration_ = value.defaultSharingConfiguration
        serializers.defaultSerializeValue(_defaultSharingConfiguration_, gen)
        gen.writeFieldName("namespaceImportConfiguration")
        val _namespaceImportConfiguration_ = value.namespaceImportConfiguration
        serializers.findTypedValueSerializer(
                serializers.config.typeFactory.constructMapType(
                kotlin.collections.Map::class.java,
                serializers.config.typeFactory.constructType(kotlin.String::class.java),
                serializers.config.typeFactory.constructType(ai.tock.nlp.front.shared.namespace.NamespaceSharingConfiguration::class.java)
                ),
                true,
                null
                )
                .serialize(_namespaceImportConfiguration_, gen, serializers)
        gen.writeEndObject()
    }
}
