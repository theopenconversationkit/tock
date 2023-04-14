package ai.tock.bot.mongo

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.litote.jackson.JacksonModuleServiceLoader

internal class ParseRequestSatisfactionStatCol_Serializer :
        StdSerializer<ParseRequestSatisfactionStatCol>(ParseRequestSatisfactionStatCol::class.java),
        JacksonModuleServiceLoader {
    override fun module() =
            SimpleModule().addSerializer(ParseRequestSatisfactionStatCol::class.java, this)

    override fun serialize(
        value: ParseRequestSatisfactionStatCol,
        gen: JsonGenerator,
        serializers: SerializerProvider
    ) {
        gen.writeStartObject()
        gen.writeFieldName("rating")
        val _rating_ = value.rating
        gen.writeNumber(_rating_)
        gen.writeFieldName("count")
        val _count_ = value.count
        gen.writeNumber(_count_)
        gen.writeEndObject()
    }
}
