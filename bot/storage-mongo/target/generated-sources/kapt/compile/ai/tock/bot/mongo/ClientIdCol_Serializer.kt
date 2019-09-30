package ai.tock.bot.mongo

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.litote.jackson.JacksonModuleServiceLoader

internal class ClientIdCol_Serializer : StdSerializer<ClientIdCol>(ClientIdCol::class.java),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addSerializer(ClientIdCol::class.java, this)

    override fun serialize(
        value: ClientIdCol,
        gen: JsonGenerator,
        serializers: SerializerProvider
    ) {
        gen.writeStartObject()
        gen.writeFieldName("userIds")
        val _userIds_ = value.userIds
        serializers.findTypedValueSerializer(
                serializers.config.typeFactory.constructCollectionType(
                kotlin.collections.Set::class.java,
                serializers.config.typeFactory.constructType(kotlin.String::class.java)
                ),
                true,
                null
                )
                .serialize(_userIds_, gen, serializers)
        gen.writeFieldName("_id")
        val __id_ = value._id
        serializers.defaultSerializeValue(__id_, gen)
        gen.writeEndObject()
    }
}
