package ai.tock.bot.connector

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.litote.jackson.JacksonModuleServiceLoader

internal class ConnectorType_Serializer : StdSerializer<ConnectorType>(ConnectorType::class.java),
        JacksonModuleServiceLoader {
    override fun module() = SimpleModule().addSerializer(ConnectorType::class.java, this)

    override fun serialize(
        value: ConnectorType,
        gen: JsonGenerator,
        serializers: SerializerProvider
    ) {
        gen.writeStartObject()
        gen.writeFieldName("id")
        val _id_ = value.id
        gen.writeString(_id_)
        gen.writeFieldName("userInterfaceType")
        val _userInterfaceType_ = value.userInterfaceType
        serializers.defaultSerializeValue(_userInterfaceType_, gen)
        gen.writeEndObject()
    }
}
