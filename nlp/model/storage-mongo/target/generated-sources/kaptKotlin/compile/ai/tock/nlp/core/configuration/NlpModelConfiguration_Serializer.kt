package ai.tock.nlp.core.configuration

import java.util.Properties
import org.litote.jackson.JacksonModuleServiceLoader
import tools.jackson.core.JsonGenerator
import tools.jackson.databind.JacksonModule
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.module.SimpleModule
import tools.jackson.databind.ser.std.StdSerializer

internal class NlpModelConfiguration_Serializer : StdSerializer<NlpModelConfiguration>(NlpModelConfiguration::class.java),
    JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addSerializer(NlpModelConfiguration::class.java, this)

  override fun serialize(
    `value`: NlpModelConfiguration,
    gen: JsonGenerator,
    serializers: SerializationContext,
  ) {
    gen.writeStartObject()
    gen.writeName("properties")
    val _properties_ = value.properties
    serializers.findTypedValueSerializer(serializers.typeFactory.constructType(Properties::class.javaObjectType), true).serialize(_properties_, gen, serializers)
    gen.writeName("markdown")
    val _markdown_ = value.markdown
    if (_markdown_ == null) {
      gen.writeNull()
    } else {
      gen.writeString(_markdown_)
    }
    gen.writeName("hasProperties")
    val _hasProperties_ = value.hasProperties
    gen.writeBoolean(_hasProperties_)
    gen.writeName("hasMarkdown")
    val _hasMarkdown_ = value.hasMarkdown
    gen.writeBoolean(_hasMarkdown_)
    gen.writeEndObject()
  }
}
