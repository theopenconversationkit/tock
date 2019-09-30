package ai.tock.bot.mongo

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.litote.jackson.JacksonModuleServiceLoader

internal class ConnectorMessageCol_Serializer :
        StdSerializer<ConnectorMessageCol>(ConnectorMessageCol::class.java),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addSerializer(ConnectorMessageCol::class.java, this)

    override fun serialize(
        value: ConnectorMessageCol,
        gen: JsonGenerator,
        serializers: SerializerProvider
    ) {
        gen.writeStartObject()
        gen.writeFieldName("_id")
        val __id_ = value._id
        serializers.defaultSerializeValue(__id_, gen)
        gen.writeFieldName("messages")
        val _messages_ = value.messages
        serializers.findTypedValueSerializer(
                serializers.config.typeFactory.constructCollectionType(
                kotlin.collections.List::class.java,
                serializers.config.typeFactory.constructType(ai.tock.shared.jackson.AnyValueWrapper::class.java)
                ),
                true,
                null
                )
                .serialize(_messages_, gen, serializers)
        gen.writeFieldName("date")
        val _date_ = value.date
        serializers.defaultSerializeValue(_date_, gen)
        gen.writeEndObject()
    }
}
