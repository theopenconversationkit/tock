package ai.tock.nlp.core.configuration

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.litote.jackson.JacksonModuleServiceLoader

internal class NlpApplicationConfiguration_Serializer :
        StdSerializer<NlpApplicationConfiguration>(NlpApplicationConfiguration::class.java),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addSerializer(NlpApplicationConfiguration::class.java,
            this)

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
        gen.writeFieldName("applicationConfiguration")
        val _applicationConfiguration_ = value.applicationConfiguration
        serializers.defaultSerializeValue(_applicationConfiguration_, gen)
        gen.writeFieldName("hasTokenizerConfiguration")
        val _hasTokenizerConfiguration_ = value.hasTokenizerConfiguration
        gen.writeBoolean(_hasTokenizerConfiguration_)
        gen.writeFieldName("hasIntentConfiguration")
        val _hasIntentConfiguration_ = value.hasIntentConfiguration
        gen.writeBoolean(_hasIntentConfiguration_)
        gen.writeFieldName("hasEntityConfiguration")
        val _hasEntityConfiguration_ = value.hasEntityConfiguration
        gen.writeBoolean(_hasEntityConfiguration_)
        gen.writeFieldName("hasApplicationConfiguration")
        val _hasApplicationConfiguration_ = value.hasApplicationConfiguration
        gen.writeBoolean(_hasApplicationConfiguration_)
        gen.writeEndObject()
    }
}
