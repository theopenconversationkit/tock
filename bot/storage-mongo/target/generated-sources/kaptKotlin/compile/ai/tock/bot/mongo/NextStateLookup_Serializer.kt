package ai.tock.bot.mongo

import org.litote.jackson.JacksonModuleServiceLoader
import tools.jackson.core.JsonGenerator
import tools.jackson.databind.JacksonModule
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.module.SimpleModule
import tools.jackson.databind.ser.std.StdSerializer

internal class NextStateLookup_Serializer : StdSerializer<NextStateLookup>(NextStateLookup::class.java),
    JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addSerializer(NextStateLookup::class.java, this)

  override fun serialize(
    `value`: NextStateLookup,
    gen: JsonGenerator,
    serializers: SerializationContext,
  ) {
    gen.writeStartObject()
    gen.writeName("nextState")
    val _nextState_ = value.nextState
    serializers.writeValue(gen, _nextState_)
    gen.writeEndObject()
  }
}
