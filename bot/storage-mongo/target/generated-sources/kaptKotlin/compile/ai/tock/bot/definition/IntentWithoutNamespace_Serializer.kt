package ai.tock.bot.definition

import org.litote.jackson.JacksonModuleServiceLoader
import tools.jackson.core.JsonGenerator
import tools.jackson.databind.JacksonModule
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.module.SimpleModule
import tools.jackson.databind.ser.std.StdSerializer

internal class IntentWithoutNamespace_Serializer : StdSerializer<IntentWithoutNamespace>(IntentWithoutNamespace::class.java),
    JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addSerializer(IntentWithoutNamespace::class.java, this)

  override fun serialize(
    `value`: IntentWithoutNamespace,
    gen: JsonGenerator,
    serializers: SerializationContext,
  ) {
    gen.writeStartObject()
    gen.writeName("name")
    val _name_ = value.name
    gen.writeString(_name_)
    gen.writeEndObject()
  }
}
