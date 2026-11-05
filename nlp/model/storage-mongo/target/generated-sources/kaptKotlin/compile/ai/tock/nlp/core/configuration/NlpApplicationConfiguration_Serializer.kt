package ai.tock.nlp.core.configuration

import org.litote.jackson.JacksonModuleServiceLoader
import tools.jackson.core.JsonGenerator
import tools.jackson.databind.JacksonModule
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.module.SimpleModule
import tools.jackson.databind.ser.std.StdSerializer

internal class NlpApplicationConfiguration_Serializer : StdSerializer<NlpApplicationConfiguration>(NlpApplicationConfiguration::class.java),
    JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addSerializer(NlpApplicationConfiguration::class.java, this)

  override fun serialize(
    `value`: NlpApplicationConfiguration,
    gen: JsonGenerator,
    serializers: SerializationContext,
  ) {
    gen.writeStartObject()
    gen.writeName("tokenizerConfiguration")
    val _tokenizerConfiguration_ = value.tokenizerConfiguration
    serializers.writeValue(gen, _tokenizerConfiguration_)
    gen.writeName("intentConfiguration")
    val _intentConfiguration_ = value.intentConfiguration
    serializers.writeValue(gen, _intentConfiguration_)
    gen.writeName("entityConfiguration")
    val _entityConfiguration_ = value.entityConfiguration
    serializers.writeValue(gen, _entityConfiguration_)
    gen.writeName("applicationConfiguration")
    val _applicationConfiguration_ = value.applicationConfiguration
    serializers.writeValue(gen, _applicationConfiguration_)
    gen.writeName("hasTokenizerConfiguration")
    val _hasTokenizerConfiguration_ = value.hasTokenizerConfiguration
    gen.writeBoolean(_hasTokenizerConfiguration_)
    gen.writeName("hasIntentConfiguration")
    val _hasIntentConfiguration_ = value.hasIntentConfiguration
    gen.writeBoolean(_hasIntentConfiguration_)
    gen.writeName("hasEntityConfiguration")
    val _hasEntityConfiguration_ = value.hasEntityConfiguration
    gen.writeBoolean(_hasEntityConfiguration_)
    gen.writeName("hasApplicationConfiguration")
    val _hasApplicationConfiguration_ = value.hasApplicationConfiguration
    gen.writeBoolean(_hasApplicationConfiguration_)
    gen.writeEndObject()
  }
}
