package ai.tock.bot.mongo

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.litote.jackson.JacksonModuleServiceLoader

internal class DialogFlowAggregateResult_Serializer :
        StdSerializer<DialogFlowAggregateResult>(DialogFlowAggregateResult::class.java),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addSerializer(DialogFlowAggregateResult::class.java,
            this)

    override fun serialize(
        value: DialogFlowAggregateResult,
        gen: JsonGenerator,
        serializers: SerializerProvider
    ) {
        gen.writeStartObject()
        gen.writeFieldName("date")
        val _date_ = value.date
        gen.writeString(_date_)
        gen.writeFieldName("count")
        val _count_ = value.count
        gen.writeNumber(_count_)
        gen.writeFieldName("seriesKey")
        val _seriesKey_ = value.seriesKey
        gen.writeString(_seriesKey_)
        gen.writeEndObject()
    }
}
