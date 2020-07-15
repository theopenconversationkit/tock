package ai.tock.nlp.core.configuration

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.litote.jackson.JacksonModuleServiceLoader

internal class NlpModelConfiguration_Serializer :
        StdSerializer<NlpModelConfiguration>(NlpModelConfiguration::class.java),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addSerializer(NlpModelConfiguration::class.java, this)

    override fun serialize(
        value: NlpModelConfiguration,
        gen: JsonGenerator,
        serializers: SerializerProvider
    ) {
        gen.writeStartObject()
        gen.writeFieldName("properties")
        val _properties_ = value.properties
        serializers.defaultSerializeValue(_properties_, gen)
        gen.writeFieldName("markdown")
        val _markdown_ = value.markdown
        if(_markdown_ == null) { gen.writeNull() } else {
                gen.writeString(_markdown_)
                }
        gen.writeFieldName("hasProperties")
        val _hasProperties_ = value.hasProperties
        gen.writeBoolean(_hasProperties_)
        gen.writeFieldName("hasMarkdown")
        val _hasMarkdown_ = value.hasMarkdown
        gen.writeBoolean(_hasMarkdown_)
        gen.writeEndObject()
    }
}
