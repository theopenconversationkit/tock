package ai.tock.bot.mongo

import org.litote.jackson.JacksonModuleServiceLoader
import tools.jackson.core.JsonGenerator
import tools.jackson.databind.JacksonModule
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.module.SimpleModule
import tools.jackson.databind.ser.std.StdSerializer

internal class ParseRequestSatisfactionStatCol_Serializer : StdSerializer<ParseRequestSatisfactionStatCol>(ParseRequestSatisfactionStatCol::class.java),
    JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addSerializer(ParseRequestSatisfactionStatCol::class.java, this)

  override fun serialize(
    `value`: ParseRequestSatisfactionStatCol,
    gen: JsonGenerator,
    serializers: SerializationContext,
  ) {
    gen.writeStartObject()
    gen.writeName("rating")
    val _rating_ = value.rating
    gen.writeNumber(_rating_)
    gen.writeName("count")
    val _count_ = value.count
    gen.writeNumber(_count_)
    gen.writeEndObject()
  }
}
