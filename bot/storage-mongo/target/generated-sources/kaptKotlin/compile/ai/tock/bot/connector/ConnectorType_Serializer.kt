package ai.tock.bot.connector

import org.litote.jackson.JacksonModuleServiceLoader
import tools.jackson.core.JsonGenerator
import tools.jackson.databind.JacksonModule
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.module.SimpleModule
import tools.jackson.databind.ser.std.StdSerializer

internal class ConnectorType_Serializer : StdSerializer<ConnectorType>(ConnectorType::class.java),
    JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addSerializer(ConnectorType::class.java, this)

  override fun serialize(
    `value`: ConnectorType,
    gen: JsonGenerator,
    serializers: SerializationContext,
  ) {
    gen.writeStartObject()
    gen.writeName("id")
    val _id_ = value.id
    gen.writeString(_id_)
    gen.writeName("userInterfaceType")
    val _userInterfaceType_ = value.userInterfaceType
    serializers.writeValue(gen, _userInterfaceType_)
    gen.writeEndObject()
  }
}
