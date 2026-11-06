package ai.tock.bot.mongo

import org.litote.jackson.JacksonModuleServiceLoader
import tools.jackson.core.JsonGenerator
import tools.jackson.databind.JacksonModule
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.module.SimpleModule
import tools.jackson.databind.ser.std.StdSerializer

internal class ConfigurationLookup_Serializer : StdSerializer<ConfigurationLookup>(ConfigurationLookup::class.java),
    JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addSerializer(ConfigurationLookup::class.java, this)

  override fun serialize(
    `value`: ConfigurationLookup,
    gen: JsonGenerator,
    serializers: SerializationContext,
  ) {
    gen.writeStartObject()
    gen.writeName("configuration")
    val _configuration_ = value.configuration
    serializers.writeValue(gen, _configuration_)
    gen.writeEndObject()
  }
}
