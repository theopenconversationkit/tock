package fr.vsct.tock.nlp.core.configuration

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.litote.jackson.JacksonModuleServiceLoader

class NlpApplicationConfiguration_Serializer : StdSerializer<NlpApplicationConfiguration>(NlpApplicationConfiguration::class.java),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addSerializer(this)

    override fun serialize(
            value: NlpApplicationConfiguration,
            gen: JsonGenerator,
            serializers: SerializerProvider
    ) {
        gen.writeStartObject()
        gen.writeFieldName("tokenizerConfiguration")
        val _tokenizerConfiguration_ = value.tokenizerConfiguration
        serializers.defaultSerializeValue(_tokenizerConfiguration_, gen)
        gen.writeFieldName("intentConfiguration")
        val _intentConfiguration_ = value.intentConfiguration
        serializers.defaultSerializeValue(_intentConfiguration_, gen)
        gen.writeFieldName("entityConfiguration")
        val _entityConfiguration_ = value.entityConfiguration
        serializers.defaultSerializeValue(_entityConfiguration_, gen)
        gen.writeEndObject()
    }
}
