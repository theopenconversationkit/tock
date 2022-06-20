package ai.tock.bot.mongo

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.litote.jackson.JacksonModuleServiceLoader

internal class GroupByIdContainer_Serializer :
        StdSerializer<GroupByIdContainer>(GroupByIdContainer::class.java),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addSerializer(GroupByIdContainer::class.java, this)

    override fun serialize(
        value: GroupByIdContainer,
        gen: JsonGenerator,
        serializers: SerializerProvider
    ) {
        gen.writeStartObject()
        gen.writeFieldName("_id")
        val __id_ = value._id
        serializers.defaultSerializeValue(__id_, gen)
        gen.writeEndObject()
    }
}
