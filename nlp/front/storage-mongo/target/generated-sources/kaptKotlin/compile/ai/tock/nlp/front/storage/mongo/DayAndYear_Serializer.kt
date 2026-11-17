package ai.tock.nlp.front.storage.mongo

import org.litote.jackson.JacksonModuleServiceLoader
import tools.jackson.core.JsonGenerator
import tools.jackson.databind.JacksonModule
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.module.SimpleModule
import tools.jackson.databind.ser.std.StdSerializer

internal class DayAndYear_Serializer : StdSerializer<ParseRequestLogMongoDAO.DayAndYear>(ParseRequestLogMongoDAO.DayAndYear::class.java),
    JacksonModuleServiceLoader {
  override fun module(): JacksonModule = SimpleModule().addSerializer(ParseRequestLogMongoDAO.DayAndYear::class.java, this)

  override fun serialize(
    `value`: ParseRequestLogMongoDAO.DayAndYear,
    gen: JsonGenerator,
    serializers: SerializationContext,
  ) {
    gen.writeStartObject()
    gen.writeName("dayOfYear")
    val _dayOfYear_ = value.dayOfYear
    gen.writeNumber(_dayOfYear_)
    gen.writeName("year")
    val _year_ = value.year
    gen.writeNumber(_year_)
    gen.writeEndObject()
  }
}
