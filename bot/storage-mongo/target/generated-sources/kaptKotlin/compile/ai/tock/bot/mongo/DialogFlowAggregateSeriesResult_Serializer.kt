package ai.tock.bot.mongo

import kotlin.collections.List
import org.litote.jackson.JacksonModuleServiceLoader
import tools.jackson.core.JsonGenerator
import tools.jackson.databind.JacksonModule
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.module.SimpleModule
import tools.jackson.databind.ser.std.StdSerializer

internal class DialogFlowAggregateSeriesResult_Serializer : StdSerializer<DialogFlowAggregateSeriesResult>(DialogFlowAggregateSeriesResult::class.java),
    JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addSerializer(DialogFlowAggregateSeriesResult::class.java, this)

  override fun serialize(
    `value`: DialogFlowAggregateSeriesResult,
    gen: JsonGenerator,
    serializers: SerializationContext,
  ) {
    gen.writeStartObject()
    gen.writeName("values")
    val _values_ = value.values
    serializers.findTypedValueSerializer(serializers.typeFactory.constructCollectionType(List::class.java, serializers.typeFactory.constructType(DialogFlowAggregateResult::class.javaObjectType)), true).serialize(_values_, gen, serializers)
    gen.writeName("seriesKey")
    val _seriesKey_ = value.seriesKey
    gen.writeString(_seriesKey_)
    gen.writeEndObject()
  }
}
