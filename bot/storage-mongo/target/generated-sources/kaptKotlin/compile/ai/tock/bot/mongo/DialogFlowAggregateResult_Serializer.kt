package ai.tock.bot.mongo

import org.litote.jackson.JacksonModuleServiceLoader
import tools.jackson.core.JsonGenerator
import tools.jackson.databind.JacksonModule
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.module.SimpleModule
import tools.jackson.databind.ser.std.StdSerializer

internal class DialogFlowAggregateResult_Serializer : StdSerializer<DialogFlowAggregateResult>(DialogFlowAggregateResult::class.java),
    JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addSerializer(DialogFlowAggregateResult::class.java, this)

  override fun serialize(
    `value`: DialogFlowAggregateResult,
    gen: JsonGenerator,
    serializers: SerializationContext,
  ) {
    gen.writeStartObject()
    gen.writeName("date")
    val _date_ = value.date
    gen.writeString(_date_)
    gen.writeName("count")
    val _count_ = value.count
    gen.writeNumber(_count_)
    gen.writeName("seriesKey")
    val _seriesKey_ = value.seriesKey
    gen.writeString(_seriesKey_)
    gen.writeEndObject()
  }
}
