package ai.tock.bot.mongo

import org.litote.jackson.JacksonModuleServiceLoader
import tools.jackson.core.JsonGenerator
import tools.jackson.databind.JacksonModule
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.module.SimpleModule
import tools.jackson.databind.ser.std.StdSerializer

internal class DialogFlowConfiguration_Serializer : StdSerializer<DialogFlowConfiguration>(DialogFlowConfiguration::class.java),
    JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addSerializer(DialogFlowConfiguration::class.java, this)

  override fun serialize(
    `value`: DialogFlowConfiguration,
    gen: JsonGenerator,
    serializers: SerializationContext,
  ) {
    gen.writeStartObject()
    gen.writeName("_id")
    val __id_ = value._id
    gen.writeString(__id_)
    gen.writeName("currentProcessedLevel")
    val _currentProcessedLevel_ = value.currentProcessedLevel
    gen.writeNumber(_currentProcessedLevel_)
    gen.writeEndObject()
  }
}
