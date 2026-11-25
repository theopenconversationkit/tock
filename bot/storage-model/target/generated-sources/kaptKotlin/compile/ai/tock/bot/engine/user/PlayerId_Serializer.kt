package ai.tock.bot.engine.user

import org.litote.jackson.JacksonModuleServiceLoader
import tools.jackson.core.JsonGenerator
import tools.jackson.databind.JacksonModule
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.module.SimpleModule
import tools.jackson.databind.ser.std.StdSerializer

internal class PlayerId_Serializer : StdSerializer<PlayerId>(PlayerId::class.java),
    JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addSerializer(PlayerId::class.java, this)

  override fun serialize(
    `value`: PlayerId,
    gen: JsonGenerator,
    serializers: SerializationContext,
  ) {
    gen.writeStartObject()
    gen.writeName("id")
    val _id_ = value.id
    gen.writeString(_id_)
    gen.writeName("type")
    val _type_ = value.type
    serializers.writeValue(gen, _type_)
    gen.writeName("clientId")
    val _clientId_ = value.clientId
    if (_clientId_ == null) {
      gen.writeNull()
    } else {
      gen.writeString(_clientId_)
    }
    gen.writeEndObject()
  }
}
