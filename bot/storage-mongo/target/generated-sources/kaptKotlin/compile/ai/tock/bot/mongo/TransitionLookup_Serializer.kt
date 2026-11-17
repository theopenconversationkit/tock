package ai.tock.bot.mongo

import org.litote.jackson.JacksonModuleServiceLoader
import tools.jackson.core.JsonGenerator
import tools.jackson.databind.JacksonModule
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.module.SimpleModule
import tools.jackson.databind.ser.std.StdSerializer

internal class TransitionLookup_Serializer : StdSerializer<TransitionLookup>(TransitionLookup::class.java),
    JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addSerializer(TransitionLookup::class.java, this)

  override fun serialize(
    `value`: TransitionLookup,
    gen: JsonGenerator,
    serializers: SerializationContext,
  ) {
    gen.writeStartObject()
    gen.writeName("transition")
    val _transition_ = value.transition
    serializers.writeValue(gen, _transition_)
    gen.writeEndObject()
  }
}
