package fr.vsct.tock.nlp.core.configuration

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.litote.jackson.JacksonModuleServiceLoader

class NlpModelConfiguration_Serializer : StdSerializer<NlpModelConfiguration>(NlpModelConfiguration::class.java),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addSerializer(this)

    override fun serialize(
            value: NlpModelConfiguration,
            gen: JsonGenerator,
            serializers: SerializerProvider
    ) {
        gen.writeStartObject()
        gen.writeFieldName("properties")
        val _properties_ = value.properties
        serializers.defaultSerializeValue(_properties_, gen)
        gen.writeEndObject()
    }
}
