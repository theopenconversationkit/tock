package ai.tock.bot.mongo

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.litote.jackson.JacksonModuleServiceLoader

internal class DialogFlowAggregateSeriesResult_Serializer :
        StdSerializer<DialogFlowAggregateSeriesResult>(DialogFlowAggregateSeriesResult::class.java),
        JacksonModuleServiceLoader {
    override fun module() =
            SimpleModule().addSerializer(DialogFlowAggregateSeriesResult::class.java, this)

    override fun serialize(
        value: DialogFlowAggregateSeriesResult,
        gen: JsonGenerator,
        serializers: SerializerProvider
    ) {
        gen.writeStartObject()
        gen.writeFieldName("values")
        val _values_ = value.values
        serializers.findTypedValueSerializer(
                serializers.config.typeFactory.constructCollectionType(
                kotlin.collections.List::class.java,
                serializers.config.typeFactory.constructType(ai.tock.bot.mongo.DialogFlowAggregateResult::class.java)
                ),
                true,
                null
                )
                .serialize(_values_, gen, serializers)
        gen.writeFieldName("seriesKey")
        val _seriesKey_ = value.seriesKey
        gen.writeString(_seriesKey_)
        gen.writeEndObject()
    }
}
