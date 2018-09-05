package fr.vsct.tock.nlp.front.storage.mongo

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.litote.jackson.JacksonModuleServiceLoader

internal class DayAndYear_Serializer : StdSerializer<ParseRequestLogMongoDAO.DayAndYear>(ParseRequestLogMongoDAO.DayAndYear::class.java),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addSerializer(this)

    override fun serialize(
            value: ParseRequestLogMongoDAO.DayAndYear,
            gen: JsonGenerator,
            serializers: SerializerProvider
    ) {
        gen.writeStartObject()
        gen.writeFieldName("dayOfYear")
        val _dayOfYear_ = value.dayOfYear
        gen.writeNumber(_dayOfYear_)
        gen.writeFieldName("year")
        val _year_ = value.year
        gen.writeNumber(_year_)
        gen.writeEndObject()
    }
}
