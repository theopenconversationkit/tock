package ai.tock.bot.mongo

import org.litote.jackson.JacksonModuleServiceLoader
import tools.jackson.core.JsonGenerator
import tools.jackson.databind.JacksonModule
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.module.SimpleModule
import tools.jackson.databind.ser.std.StdSerializer

internal class NlpStatsColId_Serializer : StdSerializer<NlpStatsColId>(NlpStatsColId::class.java),
    JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addSerializer(NlpStatsColId::class.java, this)

  override fun serialize(
    `value`: NlpStatsColId,
    gen: JsonGenerator,
    serializers: SerializationContext,
  ) {
    gen.writeStartObject()
    gen.writeName("actionId")
    val _actionId_ = value.actionId
    serializers.writeValue(gen, _actionId_)
    gen.writeName("dialogId")
    val _dialogId_ = value.dialogId
    serializers.writeValue(gen, _dialogId_)
    gen.writeEndObject()
  }
}
